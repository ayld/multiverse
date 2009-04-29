package org.multiverse.instrumentation.utils;

import static java.lang.String.format;

/**
 * A utility class for fieldpaths. A fully qualified fieldpath is a String containing the complete
 * package & classname & fieldname where the packages are seperated by /
 * java/lang/String.size
 *       /
 * @author Peter Veentjer.
 */
public final class InternalFormFieldnameUtil {

    public static String toInternalForm(Class clazz, String fieldname){
        if(fieldname == null)throw new NullPointerException();
        return toInternalForm(InternalFormClassnameUtil.toInternalForm(clazz),fieldname);
    }

    public static String toInternalForm(String classname, String fieldname){
        if(classname == null || fieldname == null)throw new NullPointerException();        
        return classname+"."+fieldname;
    }

    /**
     * Retrieves the name (internal form) of the class the field belongs to.
     *
     * @param fieldname the name of the field (internal form).
     * @return the class name (in internal form).
     * @throws NullPointerException if fieldpath is null
     */
    public static String getClassname(String fieldname) {
        if (fieldname == null) throw new NullPointerException();

        int lastDotIndex = fieldname.lastIndexOf(".");
        if (lastDotIndex == -1) {
            String msg = format("fieldname %s is not a valid fieldpath, it doesn't contain a dot", fieldname);
            throw new IllegalArgumentException(msg);
        }
        return fieldname.substring(0, lastDotIndex);
    }

    //we don't want instances
    private InternalFormFieldnameUtil() {
    }
}
