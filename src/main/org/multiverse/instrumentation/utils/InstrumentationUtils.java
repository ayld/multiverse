package org.multiverse.instrumentation.utils;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import static java.lang.String.format;
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

    public static String getInternalNameOfDematerializedClass(ClassNode materializedClass) {
        return materializedClass.name + "$Dematerialized" + InternalFormClassnameUtil.getBaseClassname(materializedClass.name);
    }

    public static String getInnerInternalNameOfDematerializedClass(ClassNode materializedClass) {
        return "Dematerialized" + InternalFormClassnameUtil.getBaseClassname(materializedClass.name);
    }

    public static String getVoidMethodDescriptor(ClassNode... parameterTypes) {
        Type[] args = new Type[parameterTypes.length];
        for (int k = 0; k < parameterTypes.length; k++) {
            args[k] = Type.getType(internalFormToDescriptor(parameterTypes[k].name));
        }

        return Type.getMethodDescriptor(Type.getType(Void.TYPE), args);
    }


    public static String internalFormToDescriptor(String internalForm) {
        return format("L%s;", internalForm);
    }

}
