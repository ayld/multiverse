package org.multiverse.instrumentation.javaagent.groovy.dsl

import org.multiverse.instrumentation.javaagent.groovy.dsl.CreateCodeDelegate
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodNode

class CreateClassDelegate {

  String theName
  String thePackage
  int theAccessModifiers = Opcodes.ACC_PUBLIC
  List<String> interfaces = new LinkedList<String>()
  List<MethodNode> methods = new LinkedList<MethodNode>()
  List<FieldNode> fields = new LinkedList<FieldNode>()

  void withName(String theName) {
    this.theName = theName
  }

  void withPackage(String thePackage) {
    this.thePackage = thePackage
  }

  void withAccess(int theAccessModifiers) {
    this.theAccessModifiers = theAccessModifiers
  }

  void addInterface(Class theInterface) {
    interfaces.add Type.getInternalName(theInterface)
  }

  void addField(String fieldName, Class fieldClass, int accessModifiers) {
    fields.add new FieldNode(
            accessModifiers,
            fieldName,
            Type.getDescriptor(fieldClass),//desc
            null,//signature
            null)//value 
  }

  void addConstructor(Closure codeClosure) {
    createCode(codeClosure)
  }

  void addInterfaceMethod(Closure codeClosure) {
    createCode(codeClosure)
  }

  void addMethod(String methodName, int accessModifiers, Closure codeClosure) {
    //MethodNode method = new MethodNode()
    //method.access = accessModifiers
    //method.name = methodName
    //method.instructions.addAll createCode(codeClosure)
    //methods.add method
  }

  List<InsnNode> createCode(Closure cl) {
    cl.delegate = new CreateCodeDelegate()
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  ClassNode createResult() {
    ClassNode classNode = new ClassNode();
    classNode.name = thePackage.replace('.', '/') + '/' + theName
    classNode.access = theAccessModifiers
    classNode.interfaces.addAll interfaces
    classNode.methods.addAll methods
    classNode.fields.addAll fields
    return classNode;
  }
}