package org.multiverse.instrumentation.utils;

import org.objectweb.asm.tree.ClassNode;

/**
 * A utility class for classnames. A classname is the full blown name of the class, eg:
 * java.lang.String
 *
 * @author Peter Veentjer.
 */
public final class InternalFormClassnameUtil {

    public static String toInternalForm(Class clazz) {
        return clazz.getName().replace(".", "/");
    }

    public static String getPackagename(ClassNode classNode) {
        return getPackagename(classNode.name);
    }

    /**
     * Returns the package (in internal form) of the given class (also given in internal form).
     *
     * @param classname the classname (internal representation).
     * @return the name of the package. If the class has no package, an empty string is returned.
     * @throws NullPointerException if classname is null.
     */
    public static String getPackagename(String classname) {
        if (classname == null) {
            throw new NullPointerException();
        }
        int index = classname.lastIndexOf("/");
        return index == -1 ? "" : classname.substring(0, index);
    }

    /**
     * Returns the name of the class without the package.
     * <p/>
     * todo: it it is an internal class, it will return the name of theinternal class.
     *
     * @param classname the fully qualified classname
     * @return the name of the class without the package.
     * @throws NullPointerException if classname is null.
     */
    public static String getBaseClassname(String classname) {
        if (classname == null) {
            throw new NullPointerException();
        }
        int lastDolarSignIndex = classname.lastIndexOf("$");
        if (lastDolarSignIndex == -1) {
            int lastDashIndex = classname.lastIndexOf("/");
            return lastDashIndex == -1 ? classname : classname.substring(lastDashIndex + 1, classname.length());
        } else {
            return classname.substring(lastDolarSignIndex + 1, classname.length());
        }
    }

    //we don't want instances
    private InternalFormClassnameUtil() {
    }
}
