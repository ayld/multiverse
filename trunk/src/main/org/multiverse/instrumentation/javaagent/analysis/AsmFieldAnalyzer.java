package org.multiverse.instrumentation.javaagent.analysis;

import org.multiverse.instrumentation.utils.InternalFormClassnameUtil;
import static org.multiverse.instrumentation.utils.InternalFormFieldnameUtil.getClassname;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link FieldAnalyzer} implementation that uses ASM for classfile analysis.
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
public final class AsmFieldAnalyzer implements FieldAnalyzer {

    private final Map<String, FieldDescription> fieldMap = new HashMap<String, FieldDescription>();
    private final Set<String> ignoredFields = new HashSet<String>();
    private final Set<String> ignoredClasses = new HashSet<String>();
    private final ClassLoader classloader;

    /**
     * Creates a new AsmFieldAnalyzer
     *
     * @param loader the ClassLoader used to load the content of the classes for analysis.
     * @throws NullPointerException if loader is null
     */
    public AsmFieldAnalyzer(ClassLoader loader) {
        if (loader == null) throw new NullPointerException();
        this.classloader = loader;
    }

    /**
     * Returns a Map containing all fields that have been analyzed.
     * <p/>
     * Method should only be used for testing purposes.
     *
     * @return
     */
    Map<String, FieldDescription> getFieldMap() {
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

    public FieldDescription find(String fieldpath) {
        if (fieldpath == null) throw new NullPointerException();

        //check if the fieldDescription should be ignored.
        if (ignoredFields.contains(fieldpath))
            return null;

        //check if the fieldDescription already is retrieved
        FieldDescription fieldDescription = fieldMap.get(fieldpath);
        if (fieldDescription != null)
            return fieldDescription;

        //the fieldDescription was not yet analyzed, so analyze the class the fieldDescription belongs too.
        retrieveAllFieldsFromClass(getClassname(fieldpath));

        //if the fieldDescription was created, this value can be returned.
        fieldDescription = fieldMap.get(fieldpath);
        if (fieldDescription == null) {
            //no fieldDescription was created, so we should prevent analyzing again.
            ignoredFields.add(fieldpath);
        }

        return fieldDescription;
    }

    private void retrieveAllFieldsFromClass(String classname) {
        if (ignoredClasses.contains(classname))
            return;

        String superclassname = classname;
        do {
            superclassname = read(superclassname, classname);
        } while (!superclassname.equals("java/lang/Object"));
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
            return "java/lang/Object";
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
            FieldDescription fieldDescription = new FieldDescription(
                    access,
                    InternalFormClassnameUtil.getPackagename(targetClassname),
                    InternalFormClassnameUtil.getBaseClassname(targetClassname),
                    fieldname,
                    desc,
                    signature);
            //     System.out.println("  found fieldDescription: "+fieldDescription.toInternalForm());
            fieldMap.put(fieldDescription.toInternalForm(), fieldDescription);
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
