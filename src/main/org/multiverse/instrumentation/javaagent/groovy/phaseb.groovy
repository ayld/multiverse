import org.multiverse.api.Originator
import org.multiverse.multiversionedstm.DematerializedObject
import org.objectweb.asm.Opcodes

/**
 * Phase b is responsible for creating the DematerializedObjects.
 *
 */
CreateClass {

  withName 'DematerializedPeter'

  withPackage 'foo.bar'

  addInterface DematerializedObject.class

  //addModifiers Opcode.

  //for (FieldNode fieldNode: materializedClass.fields) {
  /**
   * If (fieldNode isDematerializable ){*   addField
   *}else{*    addField fieldNode.name fieldNode.class
   *
   *}*/
  //}

  addField name = 'originator', type = Originator.class, Opcodes.ACC_PUBLIC

  addConstructor {
    //addArgument materialized, materializedClass.class

    //for (FieldNode fieldNode: materializedClass.fields) {

    //}

    //assignment to the originator
  }

  addInterfaceMethod {//DematerializedObject.class.&rematerialize {
    'NEW' IllegalArgumentException.class
    'DUP'()
    'INVOKESPECIAL' IllegalArgumentException.class, '<init>', '()V'
    'ATHROW'()
  }

  addInterfaceMethod {//DematerializedObject.class.&getOriginator {
    'NEW' IllegalArgumentException.class
    'DUP'()
    'INVOKESPECIAL' IllegalArgumentException.class, '<init>', '()V'
    'ATHROW'()
  }
}