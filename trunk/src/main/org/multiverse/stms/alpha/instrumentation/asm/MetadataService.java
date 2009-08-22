package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.stms.alpha.instrumentation.MultiverseJavaAgent;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.Class.forName;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A Service (singleton) that stores alle metadata needed for the instrumentation
 * process.
 *
 * @author Peter Veentjer
 */
public final class MetadataService {

    public final static MetadataService INSTANCE = new MetadataService();

    private final Map<String, Object> infoMap = new HashMap<String, Object>();

    public void ensureClassLoaded(String className) {
        if (isLoaded(className)) {
            return;
        }

        //System.out.println("Force load of class: "+className);
        try {
            //todo: doesn't work
            Class clazz = forName(className.replace('/', '.'), true, MultiverseJavaAgent.class.getClassLoader());
            //System.out.println(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAtomicMethod(ClassNode owner, MethodNode method) {
        return isAtomicMethod(owner.name, method.name, method.desc);
    }

    public boolean isAtomicMethod(String className, String name, String desc) {
        ensureClassLoaded(className);
        String key = "IsAtomicMethod#" + className + "#" + name + "#" + desc;
        return getPrepareInfoAsBoolean(key);
    }

    public void setIsAtomicMethod(ClassNode classNode, MethodNode method, boolean isAtomicMethod) {
        String key = "IsAtomicMethod#" + classNode.name + "#" + method.name + "#" + method.desc;
        putBoolean(isAtomicMethod, key);
    }

    private void putBoolean(boolean value, String key) {
        if (value) {
            infoMap.put(key, value);
        }
    }

    public void setTranlocalName(ClassNode atomicObject, String tranlocalName) {
        setTranlocalName(atomicObject.name, tranlocalName);
    }

    public void setTranlocalName(String atomicObjectName, String tranlocalName) {
        infoMap.put("TranlocalName#" + atomicObjectName, tranlocalName);
    }

    public String getTranlocalName(ClassNode atomicObject) {
        return getTranlocalName(atomicObject.name);
    }

    public String getTranlocalName(String atomicObjectName) {
        ensureClassLoaded(atomicObjectName);
        return (String) infoMap.get("TranlocalName#" + atomicObjectName);
    }

    public String getTranlocalSnapshotName(ClassNode atomicObject) {
        return getTranlocalSnapshotName(atomicObject.name);
    }

    public void setTranlocalSnapshotName(ClassNode atomicObject, String tranlocalSnapshotName) {
        setTranlocalSnapshotName(atomicObject.name, tranlocalSnapshotName);
    }

    public void setTranlocalSnapshotName(String atomicObject, String tranlocalSnapshotName) {
        ensureClassLoaded(atomicObject);
        infoMap.put("TranlocalSnapshotName#" + atomicObject, tranlocalSnapshotName);
    }

    public String getTranlocalSnapshotName(String atomicObjectName) {
        ensureClassLoaded(atomicObjectName);
        String key = "TranlocalSnapshotName#" + atomicObjectName;
        return (String) infoMap.get(key);
    }

    public boolean isManagedInstanceField(String atomicObjectname, String fieldName) {
        ensureClassLoaded(atomicObjectname);
        String key = "IsManagedInstanceField#" + atomicObjectname + "." + fieldName;
        return getPrepareInfoAsBoolean(key);
    }

    public void setIsManagedInstanceField(ClassNode atomicObject, FieldNode field, boolean managedField) {
        String key = "IsManagedInstanceField#" + atomicObject.name + "." + field.name;
        putBoolean(managedField, key);
    }

    public boolean hasManagedInstanceFields(ClassNode atomicObject) {
        return isRealAtomicObject(atomicObject.name);
    }

    public boolean isAtomicObject(ClassNode classNode, boolean isAtomicObject) {
        return isAtomicObject(classNode.name, isAtomicObject);
    }

    public boolean isAtomicObject(String className, boolean isAtomicObject) {
        ensureClassLoaded(className);
        String key = "IsAtomicObject#" + className;
        return getPrepareInfoAsBoolean(key);
    }

    public void setIsAtomicObject(ClassNode classNode, boolean atomicObject) {
        String key = "IsAtomicObject#" + classNode.name;
        putBoolean(atomicObject, key);
    }

    public boolean isRealAtomicObject(String className) {
        ensureClassLoaded(className);
        String key = "IsRealAtomicObject#" + className;
        return getPrepareInfoAsBoolean(key);
    }

    public void setIsRealAtomicObject(ClassNode classNode, boolean hasManagedFields) {
        String key = "IsRealAtomicObject#" + classNode.name;
        putBoolean(hasManagedFields, key);
    }

    private boolean getPrepareInfoAsBoolean(String key) {
        Object result = infoMap.get(key);
        return result == null ? false : (Boolean) result;
    }

    public List<FieldNode> getManagedInstanceFields(ClassNode classNode) {
        if (!isRealAtomicObject(classNode.name)) {
            return new LinkedList<FieldNode>();
        }

        List<FieldNode> fields = new LinkedList<FieldNode>();
        for (FieldNode fieldNode : (List<FieldNode>) classNode.fields) {
            if (isManagedInstanceField(classNode.name, fieldNode.name)) {
                fields.add(fieldNode);
            }
        }
        return fields;
    }

    public List<MethodNode> getAtomicMethods(ClassNode classNode) {
        List<MethodNode> result = new LinkedList<MethodNode>();
        for (MethodNode methodNode : (List<MethodNode>) classNode.methods) {
            if (isAtomicMethod(classNode.name, methodNode.name, methodNode.desc)) {
                result.add(methodNode);
            }
        }
        return result;
    }

    public void setHasAtomicMethods(ClassNode classNode, boolean hasAtomicMethods) {
        setHasAtomicMethods(classNode.name, hasAtomicMethods);
    }

    public void setHasAtomicMethods(String className, boolean hasAtomicMethods) {
        ensureClassLoaded(className);
        String key = "HasAtomicMethods#" + className;
        putBoolean(hasAtomicMethods, key);
    }

    public boolean hasAtomicMethods(ClassNode classNode) {
        return hasAtomicMethods(classNode.name);
    }

    public boolean hasAtomicMethods(String className) {
        ensureClassLoaded(className);
        String key = "HasAtomicMethods#" + className;
        return getPrepareInfoAsBoolean(key);
    }

    public void signalLoaded(ClassNode classNode) {
        String key = "Prepared#" + classNode.name;
        putBoolean(true, key);
    }

    public boolean isLoaded(ClassNode classNode) {
        return isLoaded(classNode.name);
    }

    public boolean isLoaded(String className) {
        String key = "Prepared#" + className;
        return getPrepareInfoAsBoolean(key);
    }
}
