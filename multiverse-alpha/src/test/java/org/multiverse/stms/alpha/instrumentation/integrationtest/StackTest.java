package org.multiverse.stms.alpha.instrumentation.integrationtest;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.stms.alpha.AlphaTransaction;
import static org.multiverse.stms.alpha.instrumentation.AlphaReflectionUtils.*;
import org.multiverse.stms.alpha.instrumentation.asm.MetadataRepository;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.utils.TransactionThreadLocal;

/**
 * @author Peter Veentjer
 */
public class StackTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @After
    public void tearDown() {
        // assertNoInstrumentationProblems();
    }

    @Test
    public void testStructuralContent() {
        assertFalse(existsField(Stack.class, "size"));
        assertFalse(existsField(Stack.class, "head"));
        assertTrue(existsTranlocalClass(Stack.class));
        assertTrue(existsTranlocalField(Stack.class, "size"));
        assertTrue(existsTranlocalField(Stack.class, "head"));
    }

    @Test
    public void testIsTransformed() {
        Stack stack = new Stack();
        assertTrue(((Object) stack) instanceof AlphaAtomicObject);
    }

    @Test
    public void readUncommitted() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                Stack stack = new Stack();
                AlphaTransaction alphaTransaction = (AlphaTransaction) t;
                AlphaTranlocal tranlocal = alphaTransaction.load((AlphaAtomicObject) ((Object) stack));
                assertFalse(tranlocal.committed);
                assertEquals((long) Long.MIN_VALUE, tranlocal.version);
                assertSame(stack, tranlocal.getAtomicObject());
                return null;
            }
        }.execute();
    }

    @Test
    public void readCommitted() {
        final Stack stack = new Stack();

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                AlphaTransaction alphaTransaction = (AlphaTransaction) t;
                AlphaTranlocal tranlocal = alphaTransaction.load((AlphaAtomicObject) ((Object) stack));
                assertEquals(stm.getClockVersion(), tranlocal.version);
                assertFalse(tranlocal.committed);
                assertSame(stack, tranlocal.getAtomicObject());
                return null;
            }
        }.execute();
    }

    @Test
    public void readReadonly() {
        final Stack stack = new Stack();

        AlphaAtomicObject atomicObject = ((AlphaAtomicObject) ((Object) stack));
        final AlphaTranlocal storedTranlocal = atomicObject.load();

        new AtomicTemplate(true) {
            @Override
            public Object execute(Transaction t) throws Exception {
                AlphaTransaction alphaTransaction = (AlphaTransaction) t;
                AlphaTranlocal tranlocal = alphaTransaction.load((AlphaAtomicObject) ((Object) stack));
                assertEquals(stm.getClockVersion(), tranlocal.version);
                assertSame(storedTranlocal, tranlocal);
                assertTrue(tranlocal.committed);
                assertSame(stack, tranlocal.getAtomicObject());
                return null;
            }
        }.execute();
    }

    @Test
    public void test() {
        MetadataRepository s = MetadataRepository.INSTANCE;

        Stack stack = new Stack();
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    public void testPush() {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());
    }

    @Test
    public void popFromNonEmptyStack() {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(10);
        int result = (Integer) stack.pop();
        assertEquals(result, 10);
        assertEquals(0, stack.size());
    }

    @Test
    public void clear() {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(10);

        stack.clear();
        assertEquals(0, stack.size());
    }

    @Test
    public void testRollback() {
        Stack<String> stack = new Stack<String>();

        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("testRollback");
        TransactionThreadLocal.setThreadLocalTransaction(t);

        stack.push("foo");
        stack.push("bar");

        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertTrue(stack.isEmpty());
    }
}
