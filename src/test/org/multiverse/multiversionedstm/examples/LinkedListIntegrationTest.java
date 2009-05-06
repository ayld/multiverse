package org.multiverse.multiversionedstm.examples;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class LinkedListIntegrationTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void rematerializeEmptyList() {
        LinkedList<String> list = new LinkedList<String>();
        Handle<LinkedList<String>> handle = commit(stm, list);

        Transaction t = stm.startTransaction();
        LinkedList<String> found = t.read(handle);
        assertTrue(found.isEmpty());
    }

    @Test
    public void rematerializeNonEmptyList() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("1");
        list.add("2");
        list.add("3");

        Handle<LinkedList<String>> handle = commit(stm, list);
        Transaction t = stm.startTransaction();
        LinkedList<String> found = t.read(handle);
        assertEquals(list.size(), found.size());
        assertEquals("1", found.get(0));
        assertEquals("2", found.get(1));
        assertEquals("3", found.get(2));
    }
}
