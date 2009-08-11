package org.multiverse.stms.alpha.instrumentation.asm;

import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * An Extractor responsible for collecting information about a ClassNode and
 * store it in the {@link MetadataService}. This is one of the first things
 * that should be run, so that the other transformers/factories have their
 * information in place.
 * <p/>
 * An instance should not be reused.
 *
 * @author Peter Veentjer
 */
public final class MetadataExtractor implements Opcodes {

    private boolean hasManagedFields = false;
    private boolean isAtomicObject = false;
    private boolean hasAtomicMethods = false;
    private ClassNode classNode;
    private MetadataService metadataService;

    public MetadataExtractor(ClassNode classNode) {
        this.classNode = classNode;
        this.metadataService = MetadataService.INSTANCE;
    }

    public void extract() {
        metadataService.signalLoaded(classNode);

        if (isAtomicObject()) {
            isAtomicObject = true;
        }

        extractFieldMetadata();
        extractMethodMetadata();

        metadataService.setIsAtomicObject(classNode, isAtomicObject);
        metadataService.setIsRealAtomicObject(classNode, hasManagedFields);
        metadataService.setHasAtomicMethods(classNode, hasAtomicMethods);

        if (isAtomicObject) {
            metadataService.setTranlocalName(classNode, classNode.name + "__Tranlocal");
            metadataService.setTranlocalSnapshotName(classNode, classNode.name + "__TranlocalSnapshot");
        }
    }

    private boolean isAtomicObject() {
        return hasAtomicObjectAnnotation(classNode) && !isInterface(classNode);
    }

    private void extractFieldMetadata() {
        for (FieldNode field : (List<FieldNode>) classNode.fields) {
            boolean isManagedField = false;

            if (isManagedField(field)) {
                hasManagedFields = true;
                isManagedField = true;
            }

            metadataService.setIsManagedInstanceField(classNode, field, isManagedField);
        }
    }

    private boolean isManagedField(FieldNode field) {
        return isAtomicObject &&
                hasDesiredFieldAccess(field.access) &&
                !isExcluded(field);
    }

    private boolean hasDesiredFieldAccess(int access) {
        return !(isFinal(access) || isSynthetic(access) || isStatic(access));
    }

    private void extractMethodMetadata() {
        for (MethodNode method : (List<MethodNode>) classNode.methods) {
            if (hasCorrectMethodAccess(method.access)) {
                boolean isAtomicMethod = isAtomicMethod(method);
                if (isAtomicMethod) {
                    hasAtomicMethods = true;
                }
                metadataService.setIsAtomicMethod(classNode, method, isAtomicMethod);
            }
        }
    }

    private boolean isAtomicMethod(MethodNode methodNode) {
        return isAtomicObject || hasAtomicMethodAnnotation(methodNode);
    }

    private boolean hasCorrectMethodAccess(int access) {
        return !(isSynthetic(access) || isAbstract(access) || isNative(access));
    }
}
