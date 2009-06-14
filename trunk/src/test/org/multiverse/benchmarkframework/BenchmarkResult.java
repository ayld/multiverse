package org.multiverse.benchmarkframework;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Peter Veentjer
 */
public class BenchmarkResult {

    private final List<TestCaseResult> testCaseResultList;

    public BenchmarkResult(){
        this(new LinkedList<TestCaseResult>());
    }

    public BenchmarkResult(List<TestCaseResult> testCaseResultList){
        if(testCaseResultList == null){
            throw new NullPointerException();
        }

        this.testCaseResultList = testCaseResultList;
    }

    public void add(TestCaseResult result) {
        if (result == null) {
            throw new NullPointerException();
        }
        testCaseResultList.add(result);
    }

    public List<TestCaseResult> getTestCaseResultList(){
        return Collections.unmodifiableList(testCaseResultList);
    }
}
