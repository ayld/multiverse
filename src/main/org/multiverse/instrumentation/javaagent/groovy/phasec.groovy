import org.multiverse.multiversionedstm.MaterializedObject

/**
 * Phase c is responsible for making object that are annotated with @Dematerializable annotations,
 * are transformed so that the implement the  {@link org.multiverse.multiversionedstm.MaterializedObject}
 * interface.
 */
TransformClass {

  //requiresAnnotations(Dematerializable.class){
  //}

  addInterface MaterializedObject.class

  //for(){
  //  addField()
  //}

  //addField 'originator' Originator.class 

  addPublicField 'lastDematerialized', MaterializedObject.class

  addPublicField 'nextInChain', MaterializedObject.class

  //addInterfaceMethod MaterializedObject.class.&isDirty{
  //}

  //addInterfaceMethod 'getOriginator' {
  //}

  //addInterfaceMethod('dematerialize'){
  //}

  //addInterfaceMethod('getNextInChain'){
  //}

  //addInterfaceMethod 'setNextInChain' {
  //}
}