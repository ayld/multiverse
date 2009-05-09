package org.multiverse.instrumentation.javaagent.analysis;

import org.multiverse.instrumentation.utils.InternalFormClassnameUtil;
import static org.multiverse.instrumentation.utils.InternalFormFieldnameUtil.getClassname;
import org.objectweb.asm.*;
import static org.objectweb.asm.Type.getInternalName;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link StmAnalyzer} implementation that uses ASM for classfile analysis.
 * <p/>
 * If a field can't be found on a class, it won't lead to re-analyzing the class again
 * when the AsmFieldAnalyzer is asked again.
 * <p/>
 * If a class causes problems while analyzing, it won't lead to re-analyzing the class
 * again when the AsmFieldAnalyzer is asked again.
 * <p/>
 * This class is not threadsafe.
 *
 * @author Peter Veentjer.
 */
public final class AsmStmAnalyzer implements StmAnalyzer {

    private static final String OBJECT_INTERNAL_NAME = getInternalName(Object.class);

    private final Map<String, StmField> fieldMap = new HashMap<String, StmField>();
    private final Set<String> ignoredFields = new HashSet<String>();
    private final Set<String> ignoredClasses = new HashSet<String>();
    private final ClassLoader classloader;


    /**
     * Creates a new AsmFieldAnalyzer
     *
     * @param loader the ClassLoader used to load the content of the classes for analysis.
     * @throws NullPointerException if loader is null
     */
    public AsmStmAnalyzer(ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException();
        }
        this.classloader = loader;
    }

    /**
     * Returns a Map containing all fields that have been analyzed.
     * <p/>
     * Method should only be used for testing purposes.
     *
     * @return
     */
    Map<String, StmField> getFieldMap() {
        return fieldMap;
    }

    /**
     * Returns a Set containing all fields that are ignored.
     * <p/>
     * Method should only be used for testing purposes.
     *
     * @return
     */
    Set<String> getIgnoredFields() {
        return ignoredFields;
    }

    /**
     * Returns a Set containing all classes that are ignored.
     * <p/>
     * Method should only be used for testing purposes.
     *
     * @return
     */
    Set<String> getIgnoredClasses() {
        return ignoredClasses;
    }

    /**
     * Returns the {@link ClassLoader} that is used for retrieving field information.
     *
     * @return
     */
    public ClassLoader getClassloader() {
        return classloader;
    }

    public StmField findField(String fieldpath) {
        if (fieldpath == null) throw new NullPointerException();

        //check if the stmField should be ignored.
        if (ignoredFields.contains(fieldpath))
            return null;

        //check if the stmField already is retrieved
        StmField stmField = fieldMap.get(fieldpath);
        if (stmField != null)
            return stmField;

        //the stmField was not yet analyzed, so analyze the class the stmField belongs too.
        retrieveAllFieldsFromClass(getClassname(fieldpath));

        //if the stmField was created, this value can be returned.
        stmField = fieldMap.get(fieldpath);
        if (stmField == null) {
            //no stmField was created, so we should prevent analyzing again.
            ignoredFields.add(fieldpath);
        }

        return stmField;
    }

    private void retrieveAllFieldsFromClass(String classname) {
        if (ignoredClasses.contains(classname))
            return;

        String superclassname = classname;
        do {
            superclassname = read(superclassname, classname);
        } while (!superclassname.equals(OBJECT_INTERNAL_NAME));
    }

    private String read(String actualClassname, String targetClassname) {
        String resource = actualClassname + ".class";

        try {


            ClassReader cr = new ClassReader(classloader.getResourceAsStream(resource));


            FieldAnalyzingClassVisitor classVisitor = new FieldAnalyzingClassVisitor(targetClassname);
            cr.accept(classVisitor, 0);
            return classVisitor.superName;
        } catch (IOException e) {
            ignoredClasses.add(targetClassname);
            System.out.printf("WARNING: could not load class '%s'\n", resource);
            return OBJECT_INTERNAL_NAME;
        }
    }

    private class FieldAnalyzingClassVisitor implements ClassVisitor {

        private String superName;
        private String targetClassname;

        public FieldAnalyzingClassVisitor(String targetClassname) {
            this.targetClassname = targetClassname;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.superName = superName;
        }

        public void visitSource(String source, String debug) {
            //don't care
        }

        public void visitOuterClass(String owner, String name, String desc) {
            //don't care
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            //don't care
            return null;
        }

        public void visitAttribute(Attribute attr) {
            //don't care
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            //don't care
        }

        public FieldVisitor visitField(int access, String fieldname, String desc, String signature, Object value) {
            StmField stmField = new StmField(
                    access,
                    InternalFormClassnameUtil.getPackagename(targetClassname),
                    InternalFormClassnameUtil.getBaseClassname(targetClassname),
                    fieldname,
                    desc,
                    signature);
            fieldMap.put(stmField.toInternalForm(), stmField);
            return null;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            //don't care
            return null;
        }

        public void visitEnd() {
            //don't care
        }
    }
}
