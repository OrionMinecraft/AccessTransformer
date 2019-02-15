package eu.mikroskeem.orion.at;

import eu.mikroskeem.orion.at.access.AccessLevel;
import eu.mikroskeem.orion.at.access.Modifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An access transforming class visitor
 *
 * @author Mark Vainomaa
 */
final class AccessTransformerVisitor extends ClassVisitor {
    private final List<AccessTransformEntry> accessTransforms;
    private Map<String, AccessTransformEntry> methodTransforms = new HashMap<>();
    private Map<String, AccessTransformEntry> fieldTransforms = new HashMap<>();
    private String currentClassRaw;

    AccessTransformerVisitor(@NonNull List<AccessTransformEntry> accessTransforms, @NonNull ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        this.accessTransforms = Objects.requireNonNull(accessTransforms, "accessTransforms must not be null");
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClassRaw = name;
        String currentClass = name.replace('/', '.');

        /* Build method and field transformer maps */
        for (AccessTransformEntry accessTransform : accessTransforms) {
            if(!accessTransform.getClassName().equals(currentClass))
                continue;

            if(accessTransform.isMethodAt()) {
                methodTransforms.put(accessTransform.getDescriptor(), accessTransform);
            } else if(accessTransform.isFieldAt()) {
                fieldTransforms.put(accessTransform.getDescriptor(), accessTransform);
            }
        }

        super.visit(version, replaceClassAccess(access, currentClass), name, signature, superName, interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, replaceClassAccess(access, name.replace('/', '.')));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(replaceFieldAccess(access, name), name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        /* Do not attempt to process static initializers */
        if("<clinit>".equals(name))
            return super.visitMethod(access, name, desc, signature, exceptions);

        /* Get new access level */
        int newAccess = replaceMethodAccess(access, name, desc);

        /* Apply opcode change, if given method was private and is normal method */
        if(!"<init>".equals(desc) && (access & Opcodes.ACC_PRIVATE) != 0 && (newAccess & Opcodes.ACC_PRIVATE) == 0) {
            return new AccessTransformingMethodAdapter(
                    super.visitMethod(newAccess, name, desc, signature, exceptions),
                    currentClassRaw,
                    name,
                    desc
            );
        }

        return super.visitMethod(newAccess, name, desc, signature, exceptions);
    }

    private int replaceClassAccess(int access, @NonNull String className) {
        AccessTransformEntry entry = accessTransforms
                .stream()
                .filter(AccessTransformEntry::isClassAt)
                .filter(ate -> ate.getClassName().equals(className))
                .findFirst()
                .orElse(null);

        return overrideAccessModifier(access, entry);
    }

    private int replaceMethodAccess(int access, @NonNull String methodName, @NonNull String methodDesc) {
        return overrideAccessModifier(access, applyWild(methodTransforms.get(methodName + methodDesc), methodTransforms.get("*()")));
    }

    private int replaceFieldAccess(int access, @NonNull String fieldName) {
        return overrideAccessModifier(access, applyWild(fieldTransforms.get(fieldName), fieldTransforms.get("*")));
    }

    /**
     * Access transforming method adapter
     */
    private static class AccessTransformingMethodAdapter extends MethodVisitor {
        private final String ownerClass; // Note: raw class name, a'la 'foo/bar/Baz'
        private final String methodName;
        private final String methodDesc;

        public AccessTransformingMethodAdapter(MethodVisitor mv, String ownerClass, String methodName,
                                               String methodDesc) {
            super(Opcodes.ASM5, mv);
            this.ownerClass = Objects.requireNonNull(ownerClass, "ownerClass must not be null");
            this.methodName = Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodDesc = Objects.requireNonNull(methodDesc, "methodDesc must not be null");
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            /* Replace INVOKESPECIAL with INVOKEVIRTUAL */
            if(!itf && opcode == Opcodes.INVOKESPECIAL
                    && ownerClass.equals(owner)
                    && methodName.equals(name)
                    && methodDesc.equals(desc)) {
                opcode = Opcodes.INVOKEVIRTUAL;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Helper method to override access modifier
     */
    private static int overrideAccessModifier(int original, @Nullable AccessTransformEntry atEntry) {
        if(atEntry == null)
            return original;

        int newAccess = AccessLevel.overrideAccessLevel(original, atEntry.getAccessLevel());
        for (Modifier.ModifierEntry entry : atEntry.getModifiers()) {
            int opcode = entry.getModifier().getOpcode();
            newAccess = entry.isRemove() ? newAccess & ~opcode : newAccess | opcode;
        }

        return newAccess;
    }

    /**
     * Helper method to merge access modifier with wildcard one if present
     */
    @Nullable
    private static AccessTransformEntry applyWild(@Nullable AccessTransformEntry original, @Nullable AccessTransformEntry wild) {
        return original != null ? (wild != null ? wild.merge(original) : original) : wild;
    }
}
