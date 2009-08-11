package org.multiverse.stms.alpha.instrumentation.asm;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Remapper;

/**
 * @author Peter Veentjer
 */
public class RemappingInnerClassAdapter extends ClassAdapter {
    private final Remapper remapper;

    public RemappingInnerClassAdapter(ClassVisitor cv, Remapper remapper) {
        super(cv);
        this.remapper =remapper;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return super.visitMethod(access, name, desc, signature, exceptions);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
