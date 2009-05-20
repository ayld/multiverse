package org.multiverse.multiversionedstm;

/**
 * A {@link Thread} that prints the statistics of a {@link MultiversionedStm} to the System.out.
 * It is useful for debugging purposes.
 * <p/>
 * It the Thread is interrupted, it stops.
 *
 * @author Peter Veentjer.
 */
public final class PrintMultiversionedStmStatisticsThread extends Thread {
    private final MultiversionedStm stm;
    private final long delayMs;

    /**
     * Creates a PrintMultiversionedStmStatisticsThread that reports every 2 seconds.
     *
     * @param stm the MultiversionedStm to show the statistics of.
     * @throws NullPointerException if stm is null.
     */
    public PrintMultiversionedStmStatisticsThread(MultiversionedStm stm) {
        this(stm, 2000);
    }

    /**
     * Creates a PrintMultiversionedStmStatisticsThread.
     *
     * @param stm     the MultiversionedStm to show the statistics of.
     * @param delayMs the delay between prints.
     * @throws NullPointerException     if stm is null.
     * @throws IllegalArgumentException if delayMs is smaller than zero.
     */
    public PrintMultiversionedStmStatisticsThread(MultiversionedStm stm, long delayMs) {
        if (stm == null) {
            throw new NullPointerException();
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException();
        }

        this.stm = stm;
        this.delayMs = delayMs;
        setDaemon(true);
        setName("PrintStmStatistics-Thread");
    }

    @Override
    public void run() {
        while (!interrupted()) {
            System.out.println(stm.getStatistics());
            try {
                sleep(delayMs);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
