package org.multiverse.benchmarkframework.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A TestCase is one of the cases that needs to be executed to create a benchmark.
 * It contains all the specific parameters and the {@link Driver}.
 * <p/>
 * The same testcase can execute multiple times.
 *
 * @author Peter Veentjer.
 */
public class TestCase {

    private final String benchmarkname;
    private final Properties properties = new Properties();
    private int warmupRunCount = 0;
    private int runCount = 1;
    private Driver driver;

    public TestCase(Benchmark benchmark, Driver driver) {
        if (driver == null) {
            throw new NullPointerException();
        }
        this.benchmarkname = benchmark.getBenchmarkName();
        this.driver = driver;
    }

    public Driver getDriver() {
        return driver;
    }

    public String getBenchmarkName() {
        return benchmarkname;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public void setProperty(String name, int value) {
        properties.put(name, "" + value);
    }

    public String getPropertiesDescription() {
        Map<String, String> map = new HashMap<String, String>();

        for (String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }

        return map.toString();
    }

    public int getIntProperty(String name) {
        String value = properties.getProperty(name);

        if (value == null) {
            throw new IllegalArgumentException("property with name " + name + " is not found");
        }

        return Integer.parseInt(value);
    }

    public long getLongProperty(String name) {
        String value = properties.getProperty(name);

        if (value == null) {
            throw new IllegalArgumentException("property with name " + name + " is not found");
        }

        return Long.parseLong(value);
    }

    public boolean getBooleanProperty(String name) {
        String value = properties.getProperty(name);

        if (value == null) {
            throw new IllegalArgumentException("property with name " + name + " is not found");
        }

        return Boolean.parseBoolean(value);
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public int getWarmupRunCount() {
        return warmupRunCount;
    }

    public void setWarmupRunCount(int warmupRunCount) {
        this.warmupRunCount = warmupRunCount;
    }
}
