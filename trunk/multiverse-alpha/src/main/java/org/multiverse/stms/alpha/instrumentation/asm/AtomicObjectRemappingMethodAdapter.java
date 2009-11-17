package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.AlphaStmUtils;
import org.multiverse.stms.alpha.AlphaTranlocal;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.isCategory2;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;

import static java.lang.String.format;

/**
 * A MethodAdapter that transforms all field access on atomic objects to the correct form.
 * So if a Tranlocal is needed, it will be pushed in between, so:
 * <p/>
 * person.firstname -> person.persontranlocal.firstname
 * <p/>
 * Where the persontranlocal is retrieved from the current transaction.
 */
public class AtomicObjectRemappingMethodAdapter extends MethodAdapter implements Opcodes {

    private final MetadataRepository metadataService;

    public AtomicObjectRemappingMethodAdapter(MethodVisitor mv) {
        super(mv);
        this.metadataService = MetadataRepository.INSTANCE;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String valueDesc) {
        String tranlocalName = metadataService.getTranlocalName(owner);

        if (metadataService.isManagedInstanceField(owner, name)) {
            switch (opcode) {
                case GETFIELD:
                    atomicObjectOnTopToTranlocal(owner);
                    mv.visitFieldInsn(GETFIELD, tranlocalName, name, valueDesc);
                    break;
                case PUTFIELD:
                    if (isCategory2(valueDesc)) {
                        //value(category2), owner(atomicobject),..

                        mv.visitInsn(DUP2_X1);
                        //[value(category2), owner(atomicobject), value(category2),...]

                        mv.visitInsn(POP2);
                        //[owner(atomicobject), value(category2), ...]
                    } else {
                        //[value(category1), owner(atomicobject),..
                        mv.visitInsn(SWAP);
                        //[owner(atomicobject), value(category1),..
                    }

                    atomicObjectOnTopToTranlocal(owner);

                    Label continueWithPut = new Label();
                    mv.visitInsn(DUP);
                    mv.visitFieldInsn(GETFIELD, tranlocalName, "___committed", "Z");
                    //if committed equals 0 then continueWithPut ( 0 is false, 1 is true)
                    mv.visitJumpInsn(IFEQ, continueWithPut);

                    mv.visitTypeInsn(NEW, Type.getInternalName(ReadonlyException.class));
                    mv.visitInsn(DUP);
                    String msg = format("Can't write on committed field %s.%s. The cause of this error is probably an update " +
                            "in a readonly transaction", owner, name);
                    //
                    mv.visitLdcInsn(msg);
                    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(ReadonlyException.class), "<init>", "(Ljava/lang/String;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitLabel(continueWithPut);

                    //[value(atomicobject), owner(tranlocal),..

                    if (isCategory2(valueDesc)) {
                        //[owner(tranlocal), value(category2),..

                        mv.visitInsn(DUP_X2);
                        //[owner(tranlocal), value(category2), owner(tranlocal)

                        mv.visitInsn(POP);
                        //[value(category2), owner(tranlocal),..
                    } else {
                        //[value(category1), owner(atomicobject),..
                        mv.visitInsn(SWAP);
                        //[owner(atomicobject), value(category1),..
                    }

                    mv.visitFieldInsn(PUTFIELD, tranlocalName, name, valueDesc);
                    //[..
                    break;
                case GETSTATIC:
                    throw new RuntimeException(format("GETSTATIC on instance field %s.%s not possible", owner, name));
                case PUTSTATIC:
                    throw new RuntimeException(format("PUTSTATIC on instance field %s.%s not possible", owner, name));
                default:
                    throw new RuntimeException();
            }
        } else {
            //fields of unmanaged objects can be used as is, no need for change.
            mv.visitFieldInsn(opcode, owner, name, valueDesc);
        }

        //System.out.println("end "+owner+"."+name+" opcode="+opcode);
    }

    private void atomicObjectOnTopToTranlocal(String atomicObjectName) {
        if (atomicObjectName.contains("__")) {
            throw new RuntimeException("No generated classes are allowed: " + atomicObjectName);
        }

        String tranlocalName = metadataService.getTranlocalName(atomicObjectName);
        //do the AlphaStmUtils.privatize call to place it in the tranlocal form
        String argDesc = getDescriptor(Object.class);
        String returnDesc = getDescriptor(AlphaTranlocal.class);
        String loadDesc = format("(%s)%s", argDesc, returnDesc);
        super.visitMethodInsn(
                INVOKESTATIC,
                getInternalName(AlphaStmUtils.class),
                "load",
                loadDesc);

        super.visitTypeInsn(CHECKCAST, tranlocalName);
    }
}
