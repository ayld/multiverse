package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.joinAll;
import static org.codehaus.multiverse.TestUtils.startAll;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.StringDeflatable;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A performance test that tests the performance of reads on a snapshot.
 *
 * @author Peter Veentjer.
 */
public class ReadingFromSameSnapshotPerformanceTest {
    private int heapsize = 2000000;
    private int readerCount = 2;
    private long readCount = 10000000;

    private DefaultMultiversionedHeap heap;
    private HeapSnapshot snapshot;
    private AtomicLong readCountDown = new AtomicLong();
    private long[] handles;
    private long startMs, endMs;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        fillHeap();
        snapshot = heap.getActiveSnapshot();
    }

    @After
    public void tearDown() {
        long timeMs = (endMs - startMs) + 1;
        System.out.println(String.format("%s reads with %s threads and a heapsize of %s took %s ms",
                readCount, readerCount, heapsize, timeMs));
        System.out.println(String.format("%s reads/second", readCount / (timeMs / 1000.0)));
    }

    @Test
    public void test() {
        readCountDown.set(readCount);
        ReaderThread[] readerThreads = createReaderThreads();

        startMs = System.currentTimeMillis();
        startAll(readerThreads);
        joinAll(readerThreads);
        endMs = System.currentTimeMillis();
    }

    public void fillHeap() {
        System.out.println("Start filling heap");
        handles = new long[heapsize];
        for (int k = 0; k < handles.length; k++) {
            Deflatable deflatable = new StringDeflatable("" + k);
            handles[k] = deflatable.___getHandle();
            heap.commit(heap.getActiveSnapshot(), deflatable);
        }
        System.out.println("Finished filling heap");
    }

    public ReaderThread[] createReaderThreads() {
        ReaderThread[] threads = new ReaderThread[readerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ReaderThread(k);
        return threads;
    }

    private class ReaderThread extends TestThread {

        public ReaderThread(int id) {
            super("ReaderThread-" + id);

        }

        @Override
        public void run() {
            long x;
            long y = 0;
            while ((x = readCountDown.getAndDecrement()) > 0) {
                long handle = (y + x * 31) % handles.length;
                snapshot.read(handle);
                y++;
            }
        }
    }
}
