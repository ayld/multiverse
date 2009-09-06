package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStmUtils;
import org.multiverse.stms.alpha.AlphaTranlocal;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.internalFormToDescriptor;
import org.multiverse.utils.TodoException;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.String.format;
import java.util.List;

/**
 * A MethodAdapter that transplants methods from the atomicobject, to the tranlocal.
 * The goal is that it always make sure that the stack contains objects in the
 * translocal form.
 * <p/>
 * This is done by listening to localvariables
 * This is done by listening to put/gets
 * This is done by listening to method calls.
 * <p/>
 * -
 * <p/>
 * <p/>
 * The big problem is that the references are still in the atomic form. So if
 * they are pushed on the stack, it should not be that object the call is executed
 * on, but it should be transformed to its tranlocal form.
 * <p/>
 * So if you see a aload,0.. everything is allright. But if you so an aload
 * of any other variable, it is in the atomicform. So the tranlocal that
 * belongs to the atomicobject should be retrieved, and the call
 * <p/>
 * Problem:
 * Arguments on the stack always are in the tranlocal form. But what if there
 * is a method that expects the arguments in the atomicobject form.
 * <p/>
 * Problem 2:
 * What if only runtime can be determined the type.
 *
 * @author Peter Veentjer
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "UnnecessaryLocalVariable"})
public class ManagedFieldRemappingMethodAdapter extends RemappingMethodAdapter implements Opcodes {
    private final MetadataService metadataService;
    private final ClassNode atomicObject;
    private final MethodNode method;

    public ManagedFieldRemappingMethodAdapter(MethodVisitor mv, ClassNode atomicObject, MethodNode method) {
        super(atomicObject.access, method.desc, mv, createRemapper(atomicObject));
        this.metadataService = MetadataService.INSTANCE;
        this.atomicObject = atomicObject;
        this.method = method;
    }

    public static SimpleRemapper createRemapper(ClassNode originalNode) {
        String newName = MetadataService.INSTANCE.getTranlocalName(originalNode);
        String oldName = originalNode.name;
        return new SimpleRemapper(oldName, newName);
    }


    // ============ stack transformation functions =====================
    // Transforms the upper element from translocal to atomicobject, or
    // from atomicobject to translocal.
    // ====================================================================

    /**
     * Converts the top element of the stack from atomicobject to translocal
     * form. The atomicObjectName neems to be provided to find the
     * tranlocal type to do the checkcast.
     *
     * @param atomicObjectName the internalname of the atomic object.
     */
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


    /**
     * Convert the top element of the stack from tranlocal to atomicobject form.
     * <p/>
     * The ownerName is needed to get the
     * Tranlocal.atomicObject
     * <p/>
     * It is needed to determine the tranlocal for the get and it
     */
    private void tranlocalOnTopToAtomicObject(String atomicObjectName) {
        if (atomicObjectName.contains("__")) {
            throw new RuntimeException("No generated classes are allowed: " + atomicObjectName);
        }

        String tranlocalName = metadataService.getTranlocalName(atomicObjectName);
        if (tranlocalName == null) {
            throw new RuntimeException("Missing translocalname for owner: " + atomicObjectName);
        }
        String s = internalFormToDescriptor(atomicObjectName);
        if (s.contains("__")) {
            throw new RuntimeException();
        }

        //do the stmutils.privatize call to place it in the tranlocal form
        String argDesc = getDescriptor(AlphaTranlocal.class);
        String returnDesc = getDescriptor(AlphaAtomicObject.class);
        String loadDesc = format("(%s)%s", argDesc, returnDesc);
        mv.visitMethodInsn(
                INVOKESTATIC,
                getInternalName(AlphaStmUtils.class),
                "getAtomicObject",
                loadDesc);

        mv.visitTypeInsn(CHECKCAST, atomicObjectName);
    }

    // ======================================================

    // ====== dealing with variables of the stackframe ======

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (var == 0) {
            //this already is in the tranlocal form, so nothing needs to be done.
            mv.visitVarInsn(opcode, var);
        } else if (opcode == ALOAD) {
            mv.visitVarInsn(opcode, var);
            if (hasAtomicObjectType(var)) {
                String topType = getVariableType(var).getInternalName();
                atomicObjectOnTopToTranlocal(topType);
            }//todo: de perhaps.
        } else if (opcode == ASTORE) {
            if (hasAtomicObjectType(var)) {
                String topType = getVariableType(var).getInternalName();
                tranlocalOnTopToAtomicObject(topType);
            }  //todo: de perhaps.
            mv.visitVarInsn(opcode, var);
        } else {
            //nothing needs to be done, a primative is loaded or stored.
            mv.visitVarInsn(opcode, var);
        }
    }

    private boolean hasAtomicObjectType(int var) {
        if (var == 0) {
            return true;
        }

        Type type = getVariableType(var);
        if (type.getSort() != Type.OBJECT) {
            return false;
        }

        return metadataService.isRealAtomicObject(type.getInternalName());
    }

    /**
     * gets the type for a specific variable. The Type is returned in
     * atomic form.
     * <p/>
     * When var == 0
     *
     * @param var the index of the variable.
     * @return the found type in atomicobject form.
     */
    private Type getVariableType(int var) {
        //System.out.println("method: " + method.desc);

        if (var == 0) {
            return Type.getObjectType(atomicObject.name);
        } else {
            //we need to check the descriptor first (it could be that there is a discriptor
            //and not a localVariables.
            int index = 1;
            Type[] argTypes = Type.getArgumentTypes(method.desc);
            for (Type argType : argTypes) {
                if (index == var) {
                    return argType;
                }

                index += argType.getSize();
            }

            //we didn't find it in the descriptor, so lets check the local variables.
            if (method.localVariables != null) {
                for (LocalVariableNode localVar : (List<LocalVariableNode>) method.localVariables) {
                    if (localVar.index == var) {
                        return Type.getType(localVar.desc);
                    }
                }
            }

            //we should have found it.
            throw new RuntimeException();
        }
    }

    // ======================put/get fields ===========================

    private boolean isAtomicObject(String desc) {
        Type type = Type.getType(desc);
        if (type.getSort() != Type.OBJECT) {
            return false;
        } else {
            String typeName = type.getInternalName();
            return metadataService.isRealAtomicObject(typeName);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String valueDesc) {
        String tranlocalName = metadataService.getTranlocalName(owner);

        if (metadataService.isManagedInstanceField(owner, name)) {
            switch (opcode) {
                case GETFIELD:
                    //It is a read done on a managed field of a managed object.
                    //The read needs to be done on the Tranlocal.

                    //[owner(translocal),..

                    mv.visitFieldInsn(GETFIELD, tranlocalName, name, valueDesc);
                    //[value(atomicobject),..

                    if (isAtomicObject(valueDesc)) {
                        String typeOnTop = Type.getType(valueDesc).getInternalName();
                        atomicObjectOnTopToTranlocal(typeOnTop);
                    }
                    //[value(tranlocal)
                    break;
                case PUTFIELD:
                    //It is a write done on a managed field of a managed object.
                    //The write needs to be done on the tranlocal.

                    //check for committed.
                    Label continueWithPut = new Label();

                    copyOwnerOfPutOnTop(valueDesc);
                    mv.visitFieldInsn(GETFIELD, tranlocalName, "committed", "Z");
                    //if committed equals 0 then continueWithPut ( 0 is false, 1 is true)
                    mv.visitJumpInsn(IFEQ, continueWithPut);

                    mv.visitTypeInsn(NEW, Type.getInternalName(ReadonlyException.class));
                    mv.visitInsn(DUP);
                    String msg = format("Can't write on committed field %s.%s. The cause of this error is probably an update" +
                            "in a readonly transaction", tranlocalName, name);

                    mv.visitLdcInsn(msg);
                    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(ReadonlyException.class), "<init>", "(Ljava/lang/String;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitLabel(continueWithPut);

                    //[value(tranlocal), owner(tranlocal),..
                    if (isAtomicObject(valueDesc)) {
                        String topType = Type.getType(valueDesc).getInternalName();
                        tranlocalOnTopToAtomicObject(topType);
                    }
                    //[value(atomicobject), owner(tranlocal),..

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
        } else if (metadataService.isRealAtomicObject(owner)) {
            //it is a unmanaged field of a managed object, field always
            //needs to be forwarded to the atomic object
            switch (opcode) {
                case GETFIELD:
                    //it is a read of an unmanaged field of a managed object.

                    //[owner(tranlocal),..
                    String typeOfTop = owner;
                    tranlocalOnTopToAtomicObject(typeOfTop);

                    //[owner(atomicobject),.
                    mv.visitFieldInsn(GETFIELD, owner, name, valueDesc);
                    //[value(atomicobject),..

                    if (isAtomicObject(valueDesc)) {
                        //we have to make sure that the item is the tranlocal form
                        String topType = Type.getType(valueDesc).getInternalName();
                        atomicObjectOnTopToTranlocal(topType);
                    }
                    //[value(tranlocal),...
                    break;
                case PUTFIELD:
                    //it is a write to an unmanaged field of a managed object.

                    //value(tranlocalform), owner(translocal)

                    if (isAtomicObject(valueDesc)) {
                        String topType = Type.getType(valueDesc).getInternalName();
                        tranlocalOnTopToAtomicObject(topType);
                    }

                    //[value(atomicobject), owner(tranlocal),..

                    mv.visitInsn(SWAP);
                    //[owner(tranlocal), value(atomicobject),..

                    String topType = owner;
                    tranlocalOnTopToAtomicObject(topType);
                    //owner(atomicobject), value(atomicobject)

                    mv.visitInsn(SWAP);
                    //[value(atomicobject), owner(atomicobject)

                    //[value(atomicobject), owner(atomicobject),..
                    mv.visitFieldInsn(PUTFIELD, owner, name, valueDesc);
                    break;
                case GETSTATIC:
                    //[..
                    mv.visitFieldInsn(GETSTATIC, owner, name, valueDesc);
                    //[value(atomicobject),..

                    if (isAtomicObject(valueDesc)) {
                        String typeOnTop = Type.getType(valueDesc).getInternalName();
                        atomicObjectOnTopToTranlocal(typeOnTop);
                    }
                    //[value(tranlocal),..
                    break;
                case PUTSTATIC:
                    //[value(tranlocal)

                    if (isAtomicObject(valueDesc)) {
                        String typeOnTop = Type.getType(valueDesc).getInternalName();
                        atomicObjectOnTopToTranlocal(typeOnTop);
                    }

                    mv.visitFieldInsn(PUTSTATIC, owner, name, valueDesc);
                    //[..
                    break;
                default:
                    throw new RuntimeException();
            }
        } else {
            //fields of unmanaged objects can be used as is, no need for change.
            mv.visitFieldInsn(opcode, owner, name, valueDesc);
        }
    }
    // =================== areturn =================
    // a return also needs to be fixed if the return type of the method
    // is of type AtomicObject.

    public void copyOwnerOfPutOnTop(String valueDesc) {
        if (isCategory2(valueDesc)) {
            //de stack ziet er als volgt uit [value64bits, target, ...]

            mv.visitInsn(DUP2_X1);
            //[value64bits, target, value64bits,...]

            mv.visitInsn(POP2);
            //[target, value64bits, ...]

            mv.visitInsn(DUP_X2);
            //[target, value64bits, target, ...]
        } else {
            mv.visitInsn(DUP2);
            mv.visitInsn(POP);
        }
    }

    private boolean isCategory2(String valueDesc) {
        return valueDesc.equals("J") || valueDesc.equals("D");
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN) {
            //if a return is done, the value should be transformed back to the atomic object form

            Type returnType = getReturnType(method.desc);
            if (isRealAtomicObject(returnType)) {
                String topType = returnType.getInternalName();
                tranlocalOnTopToAtomicObject(topType);
            }

            //[value(atomicobject),..
        }

        mv.visitInsn(opcode);
    }

    private boolean isRealAtomicObject(Type type) {
        if (type.getSort() != Type.OBJECT) {
            return false;
        } else {
            String returnTypeName = type.getInternalName();
            return metadataService.isRealAtomicObject(returnTypeName);
        }
    }
    // =================== method calls =================

    // method calls moeten ook aangepakt worden. Ipv de atomicobject, moet
    // de call nu plaats vinden op de tranlocal.

    // the big problem is that the arguments for a function call still are in the
    // tranlocal form on the stack, they should be in the atomicobject form.

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (oneOfTheArgumentsIsAtomicObject(desc)) {
            String message = format("The method contains an atomicobject and this is not allowed yet. Method %s.%s%s", owner, name, desc);
            throw new TodoException(message);
        }

        if (opcode == INVOKESTATIC) {
            mv.visitMethodInsn(opcode, owner, name, desc);
        } else if (opcode == INVOKESPECIAL && name.equals("<init>")) {
            mv.visitMethodInsn(opcode, owner, name, desc);
        } else if (metadataService.isRealAtomicObject(owner)) {
            //it is not a static method or constructor call, but some kind
            //of instance call on a real atomic object.

            //[args,owner(translocal)
            String translocal = metadataService.getTranlocalName(owner);
            mv.visitMethodInsn(opcode, translocal, name, desc);
            //[result
        } else {
            mv.visitMethodInsn(opcode, owner, name, desc);
        }

        //check to make sure that the result is in tranlocal form.
        Type returnType = Type.getReturnType(desc);
        if (isAtomicObject(returnType.getDescriptor())) {
            String topType = returnType.getInternalName();
            atomicObjectOnTopToTranlocal(topType);
        }
    }

    private boolean oneOfTheArgumentsIsAtomicObject(String desc) {
        //System.out.println("--------------------");
        //System.out.println(desc);
        Type[] argTypes = Type.getArgumentTypes(desc);
        for (Type argType : argTypes) {
            //System.out.println("argtype: " + argType);
            if (isRealAtomicObject(argType)) {
                return true;
            }
        }

        return false;
    }
}
