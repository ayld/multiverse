package org.multiverse.multiversionedstm.examples;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"ClassExplicitlyExtendsThread"})
public class ExampleStackIntegrationTest {
    private Set<String> producedSet = new HashSet<String>();
    private Set<String> consumedSet = new HashSet<String>();

    private final static AtomicInteger consumeCountDown = new AtomicInteger();
    private final static AtomicInteger produceCountDown = new AtomicInteger();

    private MultiversionedStm stm;
    private Handle<ExampleStack<String>> stackHandle;

    @Before
    public void setUp() throws Exception {
        stm = new MultiversionedStm();
        stackHandle = commit(stm, new ExampleStack<String>());
        //    new PrintMultiversionedStmStatisticsThread(stm).start();
    }

    @After
    public void tearDown() throws Exception {
        assertEquals(producedSet, consumedSet);
        System.out.println(stm.getStatistics());
    }

    private void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                t.setDescription("Push transaction");
                ExampleStack<String> stack = t.read(stackHandle);
                stack.push(item);
                return null;
            }
        }.execute();
    }

    private String atomicPop() {
        return new TransactionTemplate<String>(stm) {
            protected String execute(Transaction t) throws Exception {
                t.setDescription("Pop transaction");
                ExampleStack<String> stack = t.read(stackHandle);
                return stack.pop();
            }
        }.execute();
    }

    private TestThread asynchronousPush(final String item) {
        TestThread testThread = new TestThread() {
            public void run() {
                atomicPush(item);
            }
        };

        testThread.start();
        return testThread;
    }

    private TestThread asynchronousPop() {
        TestThread t = new TestThread() {
            public void run() {
                try {
                    System.out.printf("popped %s\n", atomicPop());
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        };
        t.start();
        return t;
    }

    @Test
    //todo: test is flaky.
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
        TestThread t1 = asynchronousPop();
        TestThread t3 = asynchronousPush("Hello");

        joinAll(t1, t3);
        //todo: content needs to be checked
    }

    @Test
    public void testPopOnEmptyStackDoesntLoop() {
        //long active = multiversionedstm.getStatistics().getTransactionsStartedCount();

        asynchronousPop();
        sleep(500);

        //assertEquals(active + 1, multiversionedstm.getStatistics().getTransactionsStartedCount());
    }

    @Test
    public void testProduceConsumer_10000() {
        testProducerConsumer(100);
    }

    private void testProducerConsumer(int itemCount) {
        produceCountDown.set(itemCount);
        consumeCountDown.set(itemCount);

        //long startCommitSuccessCount = heap.getStatistics().commitSuccessCount.longValue();
        //long startCommitReadonlyCount = heap.getStatistics().commitReadonlyCount.longValue();

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();

        startAll(producerThread, consumerThread);
        joinAll(producerThread, consumerThread);

        assertEquals(producedSet, consumedSet);
        //assertEquals(startCommitSuccessCount + itemCount * 2, heap.getStatistics().commitSuccessCount.longValue());
        //assertEquals(startCommitReadonlyCount, heap.getStatistics().commitReadonlyCount.longValue());

        assertStackIsEmpty();
    }

    //todo: @Test
    public void testReferencesWithinStackArePersisted() {
        ExampleStack<ExampleIntegerValue> stack = new ExampleStack<ExampleIntegerValue>();
        stack.push(new ExampleIntegerValue(10));

        Handle<ExampleStack<ExampleIntegerValue>> handle = commit(stm, stack);

        Transaction t = stm.startTransaction();
        ExampleStack<ExampleIntegerValue> foundStack = t.read(handle);
        assertStackSize(foundStack, 1);
        ExampleIntegerValue foundValue = foundStack.peek();
        assertIntegerValue(10, foundValue);
        foundValue.inc();
        t.commit();

        t = stm.startTransaction();
        foundStack = t.read(handle);
        assertStackSize(foundStack, 1);
        foundValue = foundStack.peek();
        assertIntegerValue(11, foundValue);
        t.commit();
    }

    private void assertIntegerValue(int expected, ExampleIntegerValue value) {
        assertNotNull(value);
        assertEquals(expected, value.get());
    }

    private void assertStackSize(ExampleStack stack, int expected) {
        assertNotNull(stack);
        assertEquals(expected, stack.size());
    }

    private void assertStackIsEmpty() {
        Transaction t = stm.startTransaction();
        ExampleStack stack = t.read(stackHandle);
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
