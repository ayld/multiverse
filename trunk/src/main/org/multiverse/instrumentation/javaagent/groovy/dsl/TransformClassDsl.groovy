package org.multiverse.instrumentation.javaagent.groovy.dsl

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

/**
 * The TransformClassDelegate is responsible for transforming classes.
 */
class TransformClassDelegate {

  ClassNode nodeToTransform

  private List<FieldNode> addedFields = new LinkedList<FieldNode>()
  private List<MethodNode> addedMethods = new LinkedList<MethodNode>()
  private List<String> addedInterfaces = new LinkedList<String>()

  TransformClassDelegate(Class system) {

  }

  void requiresAnnotations(List<Class> annotations) {

  }

  void addInterface(Class theInterface) {
    addedInterfaces.add Type.getDescriptor(theInterface)
  }

  void addMethod(Object method, Closure closure) {
    MethodNode methodNode = new MethodNode()
    //methodNode.name = method.name
    methodNode.instructions = createCode(closure)
    addedMethods.add methodNode
  }

  List createCode(Closure closure) {

  }

  void addPublicField(String fieldName, Class fieldType) {
    addedFields.add new FieldNode(
            Opcodes.ACC_PUBLIC,
            fieldName,
            Type.getDescriptor(fieldType),//desc
            null,//signature
            null)//value
  }

  ClassNode createResult() {
    nodeToTransform.methods.addAll addedMethods
    nodeToTransform.interfaces.addAll addedInterfaces
    nodeToTransform.fields.addAll addedFields
    nodeToTransform
  }
}