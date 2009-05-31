package org.multiverse.tcollections;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.IntValue;

public class TLinkedListTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.getInstance();
    }

    @Test
    public void persistanceOfEmptyLinkedList() {
        //    MLinkedList<String> original = new MLinkedList<String>();

        //     Handle<MLinkedList<String>> handle = commit(original);

        //   Transaction t = stm.startTransaction();
        //   MLinkedList<String> found = t.read(handle);
        //   assertEquals(original, found);
    }

    // @Test
    public void persistanceOfNonEmptyLinkedList() {
        TLinkedList<String> original = new TLinkedList<String>();
        original.add("1");
        original.add("2");

        Handle<TLinkedList<String>> handle = commit(original);

        Transaction t = stm.startTransaction();
        TLinkedList<String> found = t.read(handle);
        assertEquals(original, found);
    }

    //@Test
    public void persistanceOfDepending() {
        IntValue value1 = new IntValue(1);
        IntValue value2 = new IntValue(2);
        TLinkedList<IntValue> original = new TLinkedList<IntValue>();
        original.add(value1);
        original.add(value2);

        Handle<TLinkedList<IntValue>> handle = commit(original);

        Transaction t = stm.startTransaction();
        TLinkedList<IntValue> found = t.read(handle);
        assertEquals(original, found);
    }
}
