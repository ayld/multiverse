package org.multiverse.instrumentation.javaagent.groovy.dsl

import org.objectweb.asm.tree.InsnNode

/**
 * A Delegate for transforming code. This is done by listening to the instructions and seeing
 * if the opcode matches. If it does,
 */
class TransformCodeDelegate {

  void on_PUT_FIELD() {

  }

  void on_GET_FIELD() {

  }

  InsnNode createResult() {
    InsnNode result = new InsnNode();
    return result
  }
}