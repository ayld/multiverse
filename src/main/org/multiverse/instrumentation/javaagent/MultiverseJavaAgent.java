package org.multiverse.instrumentation.javaagent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;


/**
 * @author Peter Veentjer.
 */
public final class MultiverseJavaAgent {
    private static final String PROPERTY_FILENAME = "concurrencydetector.properties";

    //method that a javaagent must implement.
    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        System.out.println("Starting the concurrencydetecting javaagent");


        System.out.println("The concurrencydetector javaagent started successfully");
    }
}
