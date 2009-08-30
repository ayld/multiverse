package org.multiverse.templates;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.manualinstrumentation.IntQueue;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntStack;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class OrElseTemplateTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void testRunWasSuccess() {
        final IntRef v = new IntRef(0);

        Transaction t = startUpdateTransaction();

        new OrElseTemplate() {
            @Override
            public Object run(Transaction t) {
                v.set(10);
                return null;
            }

            @Override
            public Object orelserun(Transaction t) {
                fail();
                return null;
            }
        }.execute();

        t.commit();
        setThreadLocalTransaction(null);
        assertEquals(10, v.get());
    }

    @Test
    public void testRunWasFailureTryOrElseRun() {
        final IntRef v = new IntRef(0);

        Transaction t = startUpdateTransaction();

        new OrElseTemplate() {
            @Override
            public Object run(Transaction t) {
                v.set(10);
                retry();
                return null;
            }

            @Override
            public Object orelserun(Transaction t) {
                v.set(20);
                return null;
            }
        }.execute();

        t.commit();
        setThreadLocalTransaction(null);
        assertEquals(20, v.get());
    }

    @Test
    public void testRunWasFailureTryOrElseRunWasAlsoFailure() {
        final IntRef v = new IntRef(0);

        Transaction t = startUpdateTransaction();

        try {
            new OrElseTemplate() {
                @Override
                public Object run(Transaction t) {
                    v.set(10);
                    retry();
                    return null;
                }

                @Override
                public Object orelserun(Transaction t) {
                    v.set(20);
                    retry();
                    return null;
                }
            }.execute();
            fail();
        } catch (RetryError e) {
        }

        t.abort();
        setThreadLocalTransaction(null);

        assertIsAborted(t);
        assertEquals(0, v.get());
    }

    @Test
    public void testTwoStackExampleWithElementOnTheFirst() {
        final IntStack stack1 = new IntStack();
        final IntStack stack2 = new IntStack();
        stack1.push(1);

        Transaction t = startUpdateTransaction();

        int result = new OrElseTemplate<Integer>() {
            @Override
            public Integer run(Transaction t) {
                return stack1.pop();
            }

            @Override
            public Integer orelserun(Transaction t) {
                return stack2.pop();
            }
        }.execute();

        t.commit();
        setThreadLocalTransaction(null);

        assertIsCommitted(t);
        assertEquals(1, result);
        assertTrue(stack1.isEmpty());
        assertTrue(stack2.isEmpty());
    }

    @Test
    public void testTwoStackExampleWithElementOnTheSecond() {
        final IntStack stack1 = new IntStack();
        final IntStack stack2 = new IntStack();
        stack2.push(5);

        Transaction t = startUpdateTransaction();

        int result = new OrElseTemplate<Integer>() {
            @Override
            public Integer run(Transaction t) {
                return stack1.pop();
            }

            @Override
            public Integer orelserun(Transaction t) {
                return stack2.pop();
            }
        }.execute();

        t.commit();
        setThreadLocalTransaction(null);

        assertIsCommitted(t);
        assertEquals(5, result);
        assertTrue(stack1.isEmpty());
        assertTrue(stack2.isEmpty());
    }

    @Test
    public void testTwoQueueExampleWithElementOnTheFirst() {
        final IntQueue queue1 = new IntQueue();
        final IntQueue queue2 = new IntQueue();
        queue1.push(1);

        Transaction t = startUpdateTransaction();

        int result = new OrElseTemplate<Integer>() {
            @Override
            public Integer run(Transaction t) {
                return queue1.pop();
            }

            @Override
            public Integer orelserun(Transaction t) {
                return queue2.pop();
            }
        }.execute();

        t.commit();
        setThreadLocalTransaction(null);

        assertIsCommitted(t);
        assertEquals(1, result);
        assertTrue(queue1.isEmpty());
        assertTrue(queue2.isEmpty());
    }

    @Test
    public void testTwoQueueExampleWithElementOnTheSecond() {
        final IntQueue queue1 = new IntQueue();
        final IntQueue queue2 = new IntQueue();
        queue2.push(1);

        Transaction t = startUpdateTransaction();

        int result = new OrElseTemplate<Integer>() {
            @Override
            public Integer run(Transaction t) {
                return queue1.pop();
            }

            @Override
            public Integer orelserun(Transaction t) {
                return queue2.pop();
            }
        }.execute();

        t.commit();
        setThreadLocalTransaction(null);

        assertIsCommitted(t);
        assertEquals(1, result);
        assertTrue(queue1.isEmpty());
        assertTrue(queue2.isEmpty());
    }


    @Test
    public void testNestedOrElseTemplate() {
        final IntStack stack1 = new IntStack();
        final IntStack stack2 = new IntStack();
        final IntStack stack3 = new IntStack();
        final IntStack stack4 = new IntStack();
        final IntRef value = new IntRef(0);
        stack3.push(1);

        Transaction t = startUpdateTransaction();

        int result = new OrElseTemplate<Integer>() {
            @Override
            public Integer run(Transaction t) {
                return new OrElseTemplate<Integer>() {
                    @Override
                    public Integer run(Transaction t) {
                        value.inc();
                        return stack1.pop();
                    }

                    @Override
                    public Integer orelserun(Transaction t) {
                        value.inc();
                        return stack2.pop();
                    }
                }.execute();
            }

            @Override
            public Integer orelserun(Transaction t) {
                return new OrElseTemplate<Integer>() {
                    @Override
                    public Integer run(Transaction t) {
                        value.inc();
                        return stack3.pop();
                    }

                    @Override
                    public Integer orelserun(Transaction t) {
                        value.inc();
                        return stack4.pop();
                    }
                }.execute();
            }
        }.execute();

        t.commit();

        assertIsCommitted(t);
        setThreadLocalTransaction(null);
        assertEquals(1, result);
        assertTrue(stack1.isEmpty());
        assertTrue(stack2.isEmpty());
        assertTrue(stack3.isEmpty());
        assertTrue(stack4.isEmpty());
        assertEquals(1, value.get());
    }

    @Test
    public void testRuntimeExceptionInRun() {
        final IntRef v = new IntRef(0);

        final RuntimeException ex = new RuntimeException();
        Transaction t = startUpdateTransaction();

        try {
            new OrElseTemplate() {
                @Override
                public Object run(Transaction t) {
                    v.set(10);
                    throw ex;
                }

                @Override
                public Object orelserun(Transaction t) {
                    v.set(20);
                    return null;
                }
            }.execute();
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertIsActive(t);
        assertEquals(10, v.get());
    }

    @Test
    public void testRuntimeExceptionInOrElseRun() {
        final IntRef v = new IntRef(0);

        final RuntimeException ex = new RuntimeException();
        Transaction t = startUpdateTransaction();

        try {
            new OrElseTemplate() {
                @Override
                public Object run(Transaction t) {
                    v.set(10);
                    retry();
                    return null;
                }

                @Override
                public Object orelserun(Transaction t) {
                    v.set(20);
                    throw ex;
                }
            }.execute();
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertIsActive(t);
        assertEquals(20, v.get());
    }
}
