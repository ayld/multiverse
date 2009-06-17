package org.benchy.executor;

import java.util.LinkedList;
import java.util.List;

/**
 * A {@link Benchmark} contains a List of testCases and the driver to use for those
 * testcases. So the driver is fixed, the arguments are flexibile.
 *
 * @author Peter Veentjer.
 */
public class Benchmark {

    public List<TestCase> testCaseList = new LinkedList<TestCase>();
    public String benchmarkName;
    public String driverClass;

    public Driver getDriver() {
        try {
            Class driverClass = Thread.currentThread().getContextClassLoader().loadClass(this.driverClass);
            return (Driver) driverClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize driver " + driverClass, e);
        }
    }

}
