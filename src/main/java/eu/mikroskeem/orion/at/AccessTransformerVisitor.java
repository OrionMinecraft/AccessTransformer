package eu.mikroskeem.orion.at;

import eu.mikroskeem.orion.at.access.AccessLevel;
import eu.mikroskeem.orion.at.access.Modifier;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An access transforming visitor
 *
 * @author Mark Vainomaa
 */
final class AccessTransformerVisitor extends ClassVisitor {
    private final List<AccessTransformEntry> accessTransforms;
    private String currentClass;
    private List<AccessTransformEntry> currentClassAccessTransforms;

    AccessTransformerVisitor(List<AccessTransformEntry> accessTransforms, ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        this.accessTransforms = accessTransforms;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        int newAccess = access;
        currentClass = name.replace('/', '.');
        currentClassAccessTransforms = accessTransforms.stream()
                .filter(ate -> ate.getClassName().equals(currentClass))
                .collect(Collectors.toList());

        /* Transform class access */
        AccessTransformEntry ate = findClassAT(currentClass);
        if(ate != null) {
            newAccess = AccessLevel.overrideAccessLevel(access, ate.getAccessLevel());
            for (Modifier.ModifierEntry entry : ate.getModifiers()) {
                if(entry.isRemove()) {
                    newAccess &= ~entry.getModifier().getOpcode();
                } else {
                    newAccess |= entry.getModifier().getOpcode();
                }
            }
        }
        super.visit(version, newAccess, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        int newAccess = access;

        /* Transform field access */
        AccessTransformEntry ate = findFieldAT(currentClass, name);
        if(ate != null) {
            newAccess = AccessLevel.overrideAccessLevel(access, ate.getAccessLevel());
            for (Modifier.ModifierEntry entry : ate.getModifiers()) {
                if (entry.isRemove())
                    newAccess &= ~entry.getModifier().getOpcode();
                else
                    newAccess |= entry.getModifier().getOpcode();
            }
        }
        return super.visitField(newAccess, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        int newAccess = access;
        AccessTransformEntry ate;
        if(!name.equals("<clinit>") && (ate = findMethodAT(currentClass, name, desc)) != null) {
            newAccess = AccessLevel.overrideAccessLevel(access, ate.getAccessLevel());
            for (Modifier.ModifierEntry entry : ate.getModifiers()) {
                if (entry.isRemove())
                    newAccess &= ~entry.getModifier().getOpcode();
                else
                    newAccess |= entry.getModifier().getOpcode();
            }

            /* Apply opcode change, if given method was private and is normal method */
            if(!desc.equals("<init>")
                    && (access & Opcodes.ACC_PRIVATE) != 0
                    && (newAccess & Opcodes.ACC_PRIVATE) == 0) {
                return new AccessTransformingMethodAdapter(
                        super.visitMethod(newAccess, name, desc, signature, exceptions),
                        currentClass,
                        name,
                        desc
                );
            }
        }
        return super.visitMethod(newAccess, name, desc, signature, exceptions);
    }

    @Nullable
    private AccessTransformEntry findClassAT(String className) {
        for(AccessTransformEntry ate: accessTransforms) {
            /* Skip non-class ATs */
            if(!ate.isClassAt()) continue;

            /* Make sure we pick right AT */
            if(ate.getClassName().equals(className))
                return ate;
        }

        return null;
    }

    @Nullable
    private AccessTransformEntry findMethodAT(String ownerClass, String methodName, String methodDesc) {
        for(AccessTransformEntry ate: accessTransforms) {
            /* Skip non-method ATs */
            if(ate.isClassAt() || !ate.isMethod()) continue;

            /* Make sure owner class is equal */
            if(!ownerClass.equals(ate.getClassName())) continue;

            /* If AT entry is wildcard, return given AT entry */
            if(ate.getDescriptor().equals("*"))
                return ate;

            /* Return given AT entry if method name & desc equal */
            if((methodName + methodDesc).equals(ate.getDescriptor()))
                return ate;
        }

        return null;
    }

    @Nullable
    private AccessTransformEntry findFieldAT(String ownerClass, String fieldName) {
        for(AccessTransformEntry ate: accessTransforms) {
            /* Skip class & method ATs */
            if(ate.isClassAt() || ate.isMethod()) continue;

            /* Make sure owner class is equal */
            if(!ownerClass.equals(ate.getClassName())) continue;

            /* If AT entry is wildcard, return given AT entry */
            if(ate.getDescriptor().equals("*"))
                return ate;

            /* Return given AT entry if field name equal */
            if(fieldName.equals(ate.getDescriptor()))
                return ate;
        }

        return null;
    }

    /**
     * Access transforming method adapter
     */
    private static class AccessTransformingMethodAdapter extends MethodVisitor {
        private final String ownerClass;
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
                    && owner.equals(ownerClass.replace('.', '/'))
                    && name.equals(methodName)
                    && desc.equals(methodDesc)) {
                opcode = Opcodes.INVOKEVIRTUAL;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
