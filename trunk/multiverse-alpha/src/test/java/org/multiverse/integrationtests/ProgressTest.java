package org.multiverse.integrationtests;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.datastructures.refs.IntRef;

/**
 * @author Peter Veentjer.
 */
public class ProgressTest {

    private IntRef ref;
    private int incCount = 1000 * 1000;

    @Before
    public void setup() {
        ref = new IntRef();
    }

    @Test
    public void test() {
        for (int k = 0; k < incCount; k++) {
            ref.inc();

            if (k % (100 * 1000) == 0) {
                System.out.println("at inc " + k);
            }
        }

        TestCase.assertEquals(incCount, ref.get());
    }
}
