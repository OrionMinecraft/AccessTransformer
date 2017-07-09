package eu.mikroskeem.orion.at;

import eu.mikroskeem.orion.at.access.AccessLevel;
import eu.mikroskeem.orion.at.access.Modifier;
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
        currentClass = name.replace('/', '.');
        currentClassAccessTransforms = accessTransforms.stream()
                .filter(ate -> ate.getClassName().equals(currentClass))
                .collect(Collectors.toList());

        /* Transform class access */
        for (AccessTransformEntry ate : currentClassAccessTransforms) {
            if(ate.isClassAt()) {
                access = AccessLevel.overrideAccessLevel(access, ate.getAccessLevel());
                for (Modifier.ModifierEntry entry : ate.getModifiers()) {
                    if(entry.isRemove()) {
                        access &= ~entry.getModifier().getOpcode();
                    } else {
                        access |= entry.getModifier().getOpcode();
                    }
                }
                break;
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        /* Iterate through all AT entries */
        for(AccessTransformEntry ate: currentClassAccessTransforms) {
            /* Check if AT entry is for given field or if it is wildcard instead */
            if(!ate.isMethod() && (ate.getDescriptor().equals("*") || name.equals(ate.getDescriptor()))) {
                access = AccessLevel.overrideAccessLevel(access, ate.getAccessLevel());
                for (Modifier.ModifierEntry entry : ate.getModifiers()) {
                    if(entry.isRemove())
                        access &= ~entry.getModifier().getOpcode();
                    else
                        access |= entry.getModifier().getOpcode();
                }
                break;
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        /* Iterate through all AT entries */
        for(AccessTransformEntry ate: currentClassAccessTransforms) {
            /* Skip static init */
            if(name.equals("<clinit>")) break;

            /* Check if AT entry is for given method or if it is wildcard instead */
            if(ate.isMethod() && (ate.getDescriptor().equals("*") || (name + desc).equals(ate.getDescriptor()))) {
                access = AccessLevel.overrideAccessLevel(access, ate.getAccessLevel());
                if(name.equals("<init>")) break;
                for (Modifier.ModifierEntry entry : ate.getModifiers()) {
                    if(entry.isRemove())
                        access &= ~entry.getModifier().getOpcode();
                    else
                        access |= entry.getModifier().getOpcode();
                }
                break;
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
