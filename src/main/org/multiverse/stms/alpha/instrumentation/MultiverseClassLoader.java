package org.multiverse.stms.alpha.instrumentation;

import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.toBytecode;
import org.objectweb.asm.tree.ClassNode;

public class MultiverseClassLoader extends TestLoader {

    public static final MultiverseClassLoader INSTANCE = new MultiverseClassLoader();

    public MultiverseClassLoader() {
        super(MultiverseClassLoader.class.getClassLoader());
    }

    @Override
    public Class defineClass(String className, byte[] bytecode) {
        return super.defineClass(className.replace("/", "."), bytecode);
    }

    public Class defineClass(ClassNode classNode) {
        return this.defineClass(classNode.name, toBytecode(classNode));
    }
}

/*    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("find class: " + name);
        if (name.endsWith("__Tranlocal")) {
            String baseName = getString(name, "__Tranlocal");
            return defineTranlocal(baseName, name);
        } else if (name.endsWith("__TranlocalSnapshot")) {
            String baseName = getString(name, "__TranlocalSnapshot");
            return defineTranlocalSnapshot(baseName, name);
        } else if (name.endsWith("__AtomicTemplate")) {
            return defineAtomicTemplate(name);
        } else {
            return super.findClass(name);
            //throw new ClassNotFoundException(name);
        }
    }

    private String getString(String name, String end) {
        int indexOf = name.lastIndexOf(end);
        return name.substring(0, indexOf);
    }


    public Class<?> defineTranlocal(String originalName, String tranlocalName) {
        System.out.printf("defineTranlocal originalname=%s tranlocalname=%s \n",originalName,tranlocalName);

        ClassNode original = loadAsClassNode(MultiverseJavaAgent.class.getClassLoader(), originalName);
        TranlocalFactory factory = new TranlocalFactory(original);
        ClassNode translocal = factory.create();
        byte[] bytecode = saveToBytecode(translocal);
        AsmUtils.writeToFileInTmpDirectory(translocal.name + ".class", bytecode);
        return defineClass(toBinaryName(tranlocalName), bytecode);
    }

    private Class<?> defineTranlocalSnapshot(String originalName, String snapshotName) {
        System.out.printf("defineTranlocalSnapshot originalname=%s tranlocalname=%s \n",originalName,snapshotName);
        
        ClassNode original = loadAsClassNode(MultiverseJavaAgent.class.getClassLoader(), originalName);
        TranlocalSnapshotFactory factory = new TranlocalSnapshotFactory(original);
        ClassNode translocalSnapshot = factory.create();
        byte[] resultBytecode = saveToBytecode(translocalSnapshot);
        AsmUtils.writeToFileInTmpDirectory(translocalSnapshot.name + ".class", resultBytecode);
        return defineClass(toBinaryName(snapshotName), resultBytecode);
    }

    private String toBinaryName(String name){
        return name.replace('/','.');
    }

    private Class<?> defineAtomicTemplate(String name) {
        //System.out.println("defineAtomicTemplate: "+name);
        ClassNode original = loadAsClassNode(MultiverseJavaAgent.class.getClassLoader(), name);
        AtomicTemplateFactory factory = new AtomicTemplateFactory(original);
        ClassNode template = factory.create();
        byte[] resultBytecode = saveToBytecode(template);
        AsmUtils.writeToFileInTmpDirectory(template.name + ".class", resultBytecode);
        return defineClass(toBinaryName(name), resultBytecode);
    }
}     */