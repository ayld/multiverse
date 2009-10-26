package org.multiverse.integrationtests;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.datastructures.collections.StrictLinkedBlockingDeque;
import org.multiverse.utils.TodoException;

import java.util.concurrent.BlockingDeque;

/**
 */
public class ConnectionPoolLongTest {

    private int threadCount = 100;
    private ConnectionPool pool;

    @Before
    public void setUp() {
        pool = new ConnectionPool(100);
    }

    @Test
    public void test() {
        //ConnectionUserThread[] threads = createThreads();
        //startAll(threads);
        //joinAll(threads);
    }


    @AtomicObject
    static class ConnectionPool {

        final BlockingDeque<Connection> deque = new StrictLinkedBlockingDeque<Connection>();

        ConnectionPool(int maxPoolsize) {

        }

        Connection takeConnection() {
            ///Sta/return deque.takeFirst();
            throw new TodoException();
        }

        void returnConnection(Connection c) {

        }
    }

    static class Connection {

    }

    private ConnectionUserThread[] createThreads() {
        ConnectionUserThread[] threads = new ConnectionUserThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new ConnectionUserThread(k);
        }
        return threads;
    }

    class ConnectionUserThread extends TestThread {

        public ConnectionUserThread(int id) {
            super("ConnectionUser-" + id);
        }

        @Override
        public void doRun() throws Exception {
            throw new UnsupportedOperationException("Method not implemented");
        }
    }
}
