package org.multiverse.instrumentation.javaagent.utils;

import org.objectweb.asm.tree.FieldInsnNode;

public interface FieldInsnNodeVisitor<E> {

    E visit(FieldInsnNode node);
}
