package org.multiverse.tmutils;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.IntValue;

public class TmLinkedListTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.getInstance();
    }

    @Test
    public void persistanceOfEmptyLinkedList() {
        TmLinkedList<String> original = new TmLinkedList<String>();

        Handle<TmLinkedList<String>> handle = commit(original);

        Transaction t = stm.startTransaction();
        TmLinkedList<String> found = t.read(handle);
        assertEquals(original, found);
    }

    @Test
    public void persistanceOfNonEmptyLinkedList() {
        TmLinkedList<String> original = new TmLinkedList<String>();
        original.add("1");
        original.add("2");

        Handle<TmLinkedList<String>> handle = commit(original);

        Transaction t = stm.startTransaction();
        TmLinkedList<String> found = t.read(handle);
        assertEquals(original, found);
    }

    //@Test
    public void persistanceOfDepending() {
        IntValue value1 = new IntValue(1);
        IntValue value2 = new IntValue(2);
        TmLinkedList<IntValue> original = new TmLinkedList<IntValue>();
        original.add(value1);
        original.add(value2);

        Handle<TmLinkedList<IntValue>> handle = commit(original);

        Transaction t = stm.startTransaction();
        TmLinkedList<IntValue> found = t.read(handle);
        assertEquals(original, found);
    }
}
