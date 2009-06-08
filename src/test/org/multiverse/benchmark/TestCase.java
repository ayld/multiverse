package org.multiverse.benchmark;

import java.util.HashMap;
import java.util.Map;

public class TestCase {

    private String description;
    private final Map<String, String> properties = new HashMap<String, String>();
    private int warmupRunCount = 0;
    private int runCount = 1;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getProperties(){
        return properties.toString();
    }

    public String getLongDescription(){
        return description+properties;
    }

    public int getIntProperty(String name) {
        String value = properties.get(name);

        if (value == null) {
            throw new IllegalArgumentException("property with name " + name + " is not found");
        }

        return Integer.parseInt(value);
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

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public void setProperty(String name, int value) {
        properties.put(name, ""+value);
    }
}
