package org.multiverse.instrumentation.javaagent;

import java.lang.instrument.IllegalClassFormatException;

/**
 * The ClassFileTransformerContext is a support structure for {@link ClassFileTransformer}.
 * Instead of having a single instance that needs to transform all the classfiles, for every
 * classfile a new context can be created. This makes it possible to store 'global' state
 * inside the ClassFileTransformerContext without worrying about resetting.
 *
 * @author Peter Veentjer.
 */
public interface ClassFileTransformerContext {

    /**
     *
     * @return
     * @throws IllegalClassFormatException
     */
    byte[] transform() throws IllegalClassFormatException;
}
