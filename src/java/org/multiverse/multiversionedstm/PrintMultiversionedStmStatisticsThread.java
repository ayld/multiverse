package org.multiverse.multiversionedstm;

public final class PrintMultiversionedStmStatisticsThread extends Thread {
    private final MultiversionedStm stm;
    private final long delayMs;

    public PrintMultiversionedStmStatisticsThread(MultiversionedStm stm) {
        this(stm, 2000);
    }

    public PrintMultiversionedStmStatisticsThread(MultiversionedStm stm, long delayMs) {
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
