package org.multiverse.instrumentation.javaagent.groovy.dsl

import org.objectweb.asm.tree.InsnList

/**
 * A Delegate for creating code. The result will be a Insnlist.
 */
class CreateCodeDelegate {

  InsnList insnList = new InsnList()

  void NEW(Class classname) {
    //insnList.add(new TypeInsnNode(Opcodes.NEW, classname))
  }

  void INVOKESPECIAL(Class targetClass, String methodname, String desc) {
    //insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, classname, methodname, desc));
  }

  void DUP() {
    //insnList.add(new InsnNode(Opcodes.DUP));
  }

  void ATHROW() {
    //insnList.add(new InsnNode(Opcodes.ATHROW));
  }

  InsnList getResult() {
    return insnList
  }
}