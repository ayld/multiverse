package org.multiverse.benchmark;

import java.util.Map;
import java.util.HashMap;

public class TestResult {

    private TestCase testCase;
    private int x;
    private Map<String,String> properties = new HashMap<String,String>();

    public TestResult(TestCase testCase) {
        this.testCase = testCase;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public String get(String key){
        return properties.get(key);
    }
  
    public void put(String key, Object value){
        properties.put(key, value.toString());
    }
}
