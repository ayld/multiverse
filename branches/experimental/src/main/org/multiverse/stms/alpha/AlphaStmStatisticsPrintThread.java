package org.multiverse.stms.alpha;

/**
 * A {@link Thread} that prints the statistics of a {@link AlphaStm} to the System.out.
 * It is useful for debugging purposes.
 * <p/>
 * It the Thread is interrupted, it stops.
 *
 * @author Peter Veentjer.
 */
public final class AlphaStmStatisticsPrintThread extends Thread {
    private final AlphaStm stm;
    private final long delayMs;

    /**
     * Creates a PrintStatisticsThread that reports every 2 seconds.
     *
     * @param stm the STM to show the statistics.
     * @throws NullPointerException if stm is null.
     */
    public AlphaStmStatisticsPrintThread(AlphaStm stm) {
        this(stm, 2000);
    }

    /**
     * Creates a PrintStatisticsThread.
     *
     * @param stm     the TL2Stm to show the statistics of.
     * @param delayMs the delay between prints.
     * @throws NullPointerException     if stm is null.
     * @throws IllegalArgumentException if delayMs is smaller than zero.
     */
    public AlphaStmStatisticsPrintThread(AlphaStm stm, long delayMs) {
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
