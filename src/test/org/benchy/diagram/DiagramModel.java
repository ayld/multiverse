package org.benchy.diagram;

import org.benchy.TestCaseResult;

import java.util.*;

public class DiagramModel {

    private Map<String, List<TestCaseResult>> map = new HashMap<String, List<TestCaseResult>>();

    public void add(String lineId, TestCaseResult testCaseResult) {
        List<TestCaseResult> list = map.get(lineId);
        if (list == null) {
            list = new LinkedList<TestCaseResult>();
            map.put(lineId, list);
        }

        list.add(testCaseResult);
    }

    public Set<String> getLineIds() {
        return map.keySet();
    }

    public List<TestCaseResult> get(String lineId) {
        return map.get(lineId);
    }
}
