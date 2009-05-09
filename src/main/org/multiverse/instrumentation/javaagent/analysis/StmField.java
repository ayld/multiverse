package org.multiverse.instrumentation.javaagent.analysis;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;

/**
 * A Field describes a field declaration in an Object. The java.lang.reflect.Field can't be used because
 * the usage could load other classes and that could give problems while transforming (like the
 * ClassCircularityError for example).
 *
 * @author Peter Veentjer.
 */
public final class StmField {
    private final int modifiers;
    private final String classname;
    private final String packagename;
    private final String fieldname;
    private final String typeDesc;
    private final String signature;

    public StmField(int modifiers, String packagename, String classname, String fieldname, String typeDesc, String signature) {
        if (classname == null || fieldname == null || typeDesc == null)
            throw new NullPointerException();
        this.modifiers = modifiers;
        this.packagename = packagename;
        this.classname = classname;
        this.fieldname = fieldname;
        this.typeDesc = typeDesc;
        this.signature = signature;
    }

    public boolean needsInstrumentation() {
        //todo
        return true;
    }

    public String getFullyQualifiedPackagename() {
        return packagename.replace("/", ".");
    }

    public String getPackageName() {
        return packagename;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getClassname() {
        return classname;
    }

    public String getFullyQualifiedClassname() {
        return getFullyQualifiedPackagename() + "." + classname;
    }

    public String getFullyQualifiedFieldname() {
        return getFullyQualifiedClassname() + "." + fieldname;
    }

    public String getFieldName() {
        return fieldname;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public String getSignature() {
        return signature;
    }

    public String toInternalForm() {
        return packagename + "/" + classname + "." + fieldname;
    }

    //todo: equals and hash?

    /**
     * Checks if the field is of a category 2 type (long or double). For more information
     * see the following link:
     * http://java.sun.com/docs/books/jvms/second_edition/html/Overview.doc.html#7565
     *
     * @return true if the field is of a category 2, false otherwise.
     */
    public boolean hasSecondCategoryType() {
        //J = long, D = Double
        return typeDesc.equals("J") || typeDesc.equals("D");
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public boolean isSynthetic() {
        return (modifiers & Opcodes.ACC_SYNTHETIC) != 0;
    }
}
