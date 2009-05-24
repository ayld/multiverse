package org.multiverse.instrumentation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getType;
import org.objectweb.asm.tree.MethodNode;

public class OpcodeForType implements Opcodes {

    public static int LOAD(String s) {
        Type type = getType(s);
        switch (type.getSort()) {
            case Type.VOID:
                throw new RuntimeException("LOAD for type VOID is not possible");
            case Type.OBJECT:
                return ALOAD;
            default:
                throw new RuntimeException("Unhandled type for LOAD: " + type);
        }
    }

    public static int RETURN(MethodNode methodNode) {
        Type type = Type.getReturnType(methodNode.desc);
        return RETURN(type);
    }

    public static int RETURN(Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                return RETURN;
            case Type.INT:
                return IRETURN;
            case Type.DOUBLE:
                return DRETURN;
            case Type.OBJECT:
                return ARETURN;
            default:
                throw new RuntimeException("Unhandled type for RETURN: " + type);
        }
    }
}
