package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StackIntegrationTest {
    private long stackHandle;
    private Set<String> producedSet = new HashSet<String>();
    private Set<String> consumedSet = new HashSet<String>();

    private final static AtomicInteger consumeCountDown = new AtomicInteger();
    private final static AtomicInteger produceCountDown = new AtomicInteger();

    private DefaultMultiversionedHeap heap;
    private MultiversionedStm stm;

    @Before
    public void setUp() throws Exception {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        stackHandle = atomicInsert(stm, new Stack());

    }

    @After
    public void tearDown() throws Exception {
        System.out.println(heap.getStatistics());
    }

    private void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Stack<String> stack = (Stack) t.read(stackHandle);
                stack.push(item);
                return null;
            }
        }.execute();
    }

    private String atomicPop() {
        return new TransactionTemplate<String>(stm) {
            protected String execute(Transaction t) throws Exception {
                Stack stack = (Stack) t.read(stackHandle);
                return (String) stack.pop();
            }
        }.execute();
    }

    private void asynchronousPush(final String item) {
        new Thread() {
            public void run() {
                atomicPush(item);
            }
        }.start();
    }

    private void asynchronousPop() {
        new Thread() {
            public void run() {
                try {
                    atomicPop();
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    @Test
    public void testSequential() {
        atomicPush("foo");
        atomicPush("bar");

        String item1 = atomicPop();
        assertEquals("bar", item1);
        String item2 = atomicPop();
        assertEquals("foo", item2);
    }

    @Test
    public void testAsynchronous() {
        asynchronousPop();
        asynchronousPush("Hello");

        //todo: content needs to be checked
    }

    @Test
    public void testPopOnEmptyStackDoesntLoop() {
        long started = stm.getStatistics().getTransactionsStartedCount();

        asynchronousPop();
        sleep(500);

        assertEquals(started + 1, stm.getStatistics().getTransactionsStartedCount());
    }

    @Test
    public void testProduceConsumer_10000() {
        testProducerConsumer(10000);
    }

    private void testProducerConsumer(int itemCount) {
        produceCountDown.set(itemCount);
        consumeCountDown.set(itemCount);

        long startCommitSuccessCount = heap.getStatistics().commitSuccessCount.longValue();
        long startCommitReadonlyCount = heap.getStatistics().commitReadonlyCount.longValue();

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();

        startAll(consumerThread, producerThread);
        joinAll(consumerThread, producerThread);

        assertEquals(producedSet, consumedSet);
        assertEquals(startCommitSuccessCount + itemCount * 2, heap.getStatistics().commitSuccessCount.longValue());
        assertEquals(startCommitReadonlyCount, heap.getStatistics().commitReadonlyCount.longValue());

        assertStackIsEmpty();
    }

    private void assertStackIsEmpty() {
        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(stackHandle);
        assertTrue(stack.isEmpty());
        t.commit();
    }

    private class ProducerThread extends TestThread {

        public ProducerThread() {
            super("ProducerThread");
        }

        public void run() {
            while (produceCountDown.getAndDecrement() > 0) {
                String item = "" + System.nanoTime();
                producedSet.add(item);
                atomicPush(item);
            }
        }
    }

    private class ConsumerThread extends TestThread {

        public ConsumerThread() {
            super("ConsumerThread");
        }

        public void run() {
            while (consumeCountDown.getAndDecrement() > 0) {
                String item = atomicPop();
                consumedSet.add(item);
            }
        }
    }
}
