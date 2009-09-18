package org.multiverse.utils.profiling;

/**
 * A {@link Thread} that prints the profiled information to the System.out.
 * It is useful for profiling/debugging purposes.
 * <p/>
 * It the Thread is interrupted, it stops.
 *
 * @author Peter Veentjer.
 */
public final class PrintProfileInfoThread extends Thread {
    private final Profiler profiler;
    private final long delayMs;

    /**
     * Creates a PrintStatisticsThread that reports every 2 seconds.
     *
     * @param profiler the STM to show the statistics.
     * @throws NullPointerException if stm is null.
     */
    public PrintProfileInfoThread(Profiler profiler) {
        this(profiler, 2000);
    }

    /**
     * Creates a PrintStatisticsThread.
     *
     * @param profiler the Profiler to print the profiled information for.
     * @param delayMs  the delay between prints.
     * @throws NullPointerException     if profiler is null.
     * @throws IllegalArgumentException if delayMs is smaller than zero.
     */
    public PrintProfileInfoThread(Profiler profiler, long delayMs) {
        if (profiler == null) {
            throw new NullPointerException();
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException();
        }

        this.profiler = profiler;
        this.delayMs = delayMs;
        setDaemon(true);
        setName("PrintStmStatistics-Thread");
    }

    @Override
    public void run() {
        while (!interrupted()) {
            profiler.print();
            try {
                sleep(delayMs);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
