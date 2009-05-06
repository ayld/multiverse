package org.multiverse.instrumentation.javaagent;

import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InstrumentationUtils {

    public static Constructor getConstructor(Class clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Class clazz, String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getInternalNameOfDematerializedClass(Class materializedClass) {
        String packagename = materializedClass.getPackage().getName();
        String simpleName = "Dematerialized" + materializedClass.getSimpleName();
        return (packagename + "." + simpleName).replace('.', '/');
    }

    public static String getVoidMethodDescriptor(Class<?>... parameterTypes) {
        Type[] args = new Type[parameterTypes.length];
        for (int k = 0; k < parameterTypes.length; k++) {
            args[k] = Type.getType(parameterTypes[k]);
        }

        return Type.getMethodDescriptor(Type.getType(Void.TYPE), args);
    }

    public static String internalFormToDescriptor(String internalForm) {
        return java.lang.String.format("L%s;", internalForm);
    }
}
