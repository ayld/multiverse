package org.multiverse.datastructures.refs;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.datastructures.refs.IntRef;

public class IntRefTest {

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void test() {
        IntRef i = new IntRef(0);
        assertEquals(0, i.get());
    }

    @Test
    public void testSet() {
        IntRef i = new IntRef(0);
        i.setValue(10);
        assertEquals(10, i.get());
    }

    @Test
    public void testWithTransaction() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                IntRef i = new IntRef(0);
                return null;
            }
        }.execute();

        //((AtomicObject) i).acquireLock(null);
    }
}
