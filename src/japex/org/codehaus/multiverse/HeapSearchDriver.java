package org.codehaus.multiverse;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.DummyDehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.growingheap.DefaultHeapNode;
import org.codehaus.multiverse.multiversionedstm.growingheap.HeapNode;

import static java.lang.Math.round;
import java.util.HashSet;

public class HeapSearchDriver extends JapexDriverBase {
    private HeapNode root;
    private int heapsize;
    private int readcount;
    private boolean balanced;
    private HashSet<Long> handles;

    public void prepare(TestCase testCase) {
        readParams(testCase);
        createHeap();
    }

    private void readParams(TestCase testCase) {
        heapsize = testCase.getIntParam("heapsize");
        readcount = testCase.getIntParam("readcount");
        balanced = testCase.getBooleanParam("balanced");
    }

    private void createHeap() {
        root = null;
        handles = new HashSet<Long>();
        for (int k = 0; k < heapsize; k++) {
            long handle = randomHandle();
            if (!handles.add(handle)) {
                DehydratedStmObject change = new DummyDehydratedStmObject(handle);
                addChange(change);
            }
        }
    }

    private void addChange(DehydratedStmObject change) {
        if (root == null) {
            root = new DefaultHeapNode(change, null, null);
        } else {
            root = root.createNew(change);
        }
    }

    private long randomHandle() {
        return round(Math.random() * 100000) + 1;
    }

    public void run(TestCase testCase) {

    }
}
