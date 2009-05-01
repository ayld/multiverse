package org.multiverse.instrumentation.javaagent.groovy.dsl

import org.multiverse.instrumentation.javaagent.groovy.dsl.CreateClassDelegate
import org.multiverse.instrumentation.javaagent.groovy.dsl.TransformClassDelegate

/**
 * Phase 1 is transforming all materializable objects so that the have the extra methods and fields.
 */
class TransformDsl {

  static void run(String txt) {
    Script dslScript = new GroovyShell().parse(txt)

    dslScript.metaClass = createEMC(dslScript.class, {
      ExpandoMetaClass emc ->

      emc.CreateClass = {
        Closure cl ->
        cl.delegate = new CreateClassDelegate()
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        cl.createResult()
      }

      emc.TransformClass = {
        Closure cl ->
        cl.delegate = new TransformClassDelegate()
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        cl.createResult()
      }
    })
    dslScript.run()
  }

  static ExpandoMetaClass createEMC(Class clazz, Closure cl) {
    ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)
    cl(emc)
    emc.initialize()
    return emc
  }

}