package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.annotations.AtomicMethod;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * An Extractor responsible for collecting information about a ClassNode and
 * store it in the {@link MetadataRepository}. This is one of the first things
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
    private MetadataRepository metadataRepository;

    public MetadataExtractor(ClassNode classNode) {
        this.classNode = classNode;
        this.metadataRepository = MetadataRepository.INSTANCE;
    }

    public void extract() {
        metadataRepository.signalLoaded(classNode);

        if (isAtomicObject()) {
            isAtomicObject = true;
        }

        extractFieldMetadata();
        extractMethodMetadata();

        metadataRepository.setIsAtomicObject(classNode, isAtomicObject);
        metadataRepository.setIsRealAtomicObject(classNode, hasManagedFields);
        metadataRepository.setHasAtomicMethods(classNode, hasAtomicMethods);

        if (isAtomicObject) {
            metadataRepository.setTranlocalName(classNode, classNode.name + "__Tranlocal");
            metadataRepository.setTranlocalSnapshotName(classNode, classNode.name + "__TranlocalSnapshot");
        }
    }

    private boolean isAtomicObject() {
        return hasAtomicObjectAnnotation(classNode) && !isInterface(classNode);
    }

    private void extractFieldMetadata() {
        for (FieldNode field : (List<FieldNode>) classNode.fields) {
            extractFieldMetadata(field);
        }
    }

    private void extractFieldMetadata(FieldNode field) {
        boolean isManagedField = false;

        if (isManagedField(field)) {
            hasManagedFields = true;
            isManagedField = true;
        }

        metadataRepository.setIsManagedInstanceField(classNode, field, isManagedField);
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
            extractMethodMetadata(method);
        }
    }

    private void extractMethodMetadata(MethodNode method) {
        boolean isAtomicMethod = false;
        AtomicMethodParams params = null;

        ensureNoAtomicMethodAccessModifierViolation(method);

        if (isAtomicObject) {
            if (hasAtomicMethodAnnotation(method)) {
                params = getParams(method);
                isAtomicMethod = true;
            } else if (hasCorrectMethodAccessForAtomicMethod(method.access)) {
                isAtomicMethod = true;
                params = createDefaultParams(method);
            }
        } else if (hasAtomicMethodAnnotation(method)) {
            isAtomicMethod = true;
            params = getParams(method);
        }

        if (isAtomicMethod) {
            hasAtomicMethods = true;
        }

        metadataRepository.setIsAtomicMethod(classNode, method, isAtomicMethod);

        if (isAtomicMethod) {
            metadataRepository.setAtomicMethodParams(classNode, method, params);
        }
    }

    private void ensureNoAtomicMethodAccessModifierViolation(MethodNode method) {
        boolean hasAtomicMethodAnnotation = hasAtomicMethodAnnotation(method);
        boolean hasInvalidAccessModifier = hasCorrectMethodAccessForAtomicMethod(method.access);

        if (hasAtomicMethodAnnotation && !hasInvalidAccessModifier) {
            String msg = "Invalid access modifier for method:" + classNode.name + "." + method.name + method.desc +
                    " (native, abstract and synthetic not allowed) ";
            throw new RuntimeException(msg);
        }
    }

    private AtomicMethodParams createDefaultParams(MethodNode method) {
        AtomicMethodParams params = new AtomicMethodParams();
        params.isReadonly = false;
        params.familyName = createDefaultFamilyName(method);
        params.retryCount = Integer.MAX_VALUE;
        return params;
    }

    private AtomicMethodParams getParams(MethodNode method) {
        AnnotationNode atomicMethodAnnotation = AsmUtils.getVisibleAnnotation(method, AtomicMethod.class);

        AtomicMethodParams params = new AtomicMethodParams();
        params.isReadonly = (Boolean) getValue(atomicMethodAnnotation, "readonly", false);
        params.familyName = (String) getValue(atomicMethodAnnotation, "familyName", createDefaultFamilyName(method));
        params.retryCount = (Integer) getValue(atomicMethodAnnotation, "retryCount", Integer.MAX_VALUE);
        return params;
    }

    private String createDefaultFamilyName(MethodNode method) {
        return classNode.name + "." + method.desc;
    }

    private Object getValue(AnnotationNode node, String name, Object defaultValue) {
        if (node.values == null) {
            return defaultValue;
        }

        for (int k = 0; k < node.values.size(); k += 2) {
            String found = (String) node.values.get(k);
            if (name.equals(found)) {
                return node.values.get(k + 1);
            }
        }

        return defaultValue;
    }

    private boolean hasCorrectMethodAccessForAtomicMethod(int access) {
        return !(isSynthetic(access) || isAbstract(access) || isNative(access));
    }
}
