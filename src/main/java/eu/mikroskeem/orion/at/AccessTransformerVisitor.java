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
        this.accessTransforms = accessTransforms;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClassRaw = name;
        String currentClass = name.replace('/', '.');

        /* Transform class access */
        AccessTransformEntry ate;
        int newAccess = (ate = findClassAT(currentClass)) != null ? getNewAccessModifier(access, ate) : access;

        /* Build method and field transformer maps */
        for (AccessTransformEntry accessTransform : accessTransforms) {
            if(!accessTransform.getClassName().equals(currentClass))
                continue;

            if(accessTransform.isMethod()) {
                methodTransforms.put(accessTransform.getDescriptor(), accessTransform);
            } else if(!accessTransform.isClassAt()) {
                fieldTransforms.put(accessTransform.getDescriptor(), accessTransform);
            }
        }

        super.visit(version, newAccess, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        /* Transform field access */
        AccessTransformEntry ate;
        int newAccess = (ate = findFieldAT(name)) != null ? getNewAccessModifier(access, ate) : access;
        return super.visitField(newAccess, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        /* Do not attempt to process static initializers */
        if("<clinit>".equals(name))
            return super.visitMethod(access, name, desc, signature, exceptions);

        /* Get new access level */
        AccessTransformEntry ate;
        int newAccess = (ate = findMethodAT(name, desc)) != null ? getNewAccessModifier(access, ate) : access;

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

    @Nullable
    private AccessTransformEntry findClassAT(@NonNull String className) {
        for(AccessTransformEntry ate: accessTransforms) {
            /* Skip non-class ATs */
            if(!ate.isClassAt())
                continue;

            /* Make sure we pick right AT */
            if(ate.getClassName().equals(className))
                return ate;
        }

        return null;
    }

    @Nullable
    private AccessTransformEntry findMethodAT(@NonNull String methodName, @NonNull String methodDesc) {
        return applyWild(methodTransforms.get(methodName + methodDesc), methodTransforms.get("*()"));
    }

    @Nullable
    private AccessTransformEntry findFieldAT(@NonNull String fieldName) {
        return applyWild(fieldTransforms.get(fieldName), fieldTransforms.get("*"));
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
            this.ownerClass = ownerClass;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
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
     * Helper method to get new access modifier
     */
    private static int getNewAccessModifier(int original, @NonNull AccessTransformEntry atEntry) {
        int newAccess = AccessLevel.overrideAccessLevel(original, atEntry.getAccessLevel());
        for (Modifier.ModifierEntry entry : atEntry.getModifiers()) {
            if(entry.isRemove()) {
                newAccess &= ~entry.getModifier().getOpcode();
            } else {
                newAccess |= entry.getModifier().getOpcode();
            }
        }

        return newAccess;
    }

    /**
     * Helper method to merge access modifier with wildcard one if present
     */
    @Nullable
    private static AccessTransformEntry applyWild(@Nullable AccessTransformEntry original, @Nullable AccessTransformEntry wild) {
        if(original == null) {
            original = wild;
        } else {
            original = wild != null ? wild.merge(original) : original;
        }
        return original;
    }
}
