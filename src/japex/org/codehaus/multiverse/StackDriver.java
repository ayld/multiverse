package org.codehaus.multiverse;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

public class StackDriver extends JapexDriverBase {

    int i;

    public void prepare(TestCase testCase) {
        i = 0;
    }

    public void run(TestCase testCase) {
        for (int i = 0; i < 100; i++) {
            this.i = this.i + 1;
        }
    }
}
