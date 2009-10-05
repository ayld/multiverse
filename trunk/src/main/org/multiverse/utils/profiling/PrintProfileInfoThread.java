package org.multiverse.utils.profiling;

import org.multiverse.api.Stm;
import org.multiverse.utils.GlobalStmInstance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A daemon {@link Thread} that prints the profiled information to the System.out.
 * It is useful for profiling/debugging purposes.
 * <p/>
 * It the Thread is interrupted, it stops.
 *
 * @author Peter Veentjer.
 */
public final class PrintProfileInfoThread extends Thread {

    private final static Logger logger = Logger.getLogger(PrintProfileInfoThread.class.getName());

    private final long delayMs;
    private final File file;

    static {
        boolean enabled = parseBoolean(System.getProperty("multiverse.profiler.printthread.enabled", "false"));
        System.out.println("enabled: " + enabled);
        if (enabled) {
            logger.info("Starting profilethread");
            long delayMs = parseLong(System.getProperty("multiverse.profiler.printthread.delayms", "2000"));
            String file = System.getProperty("multiverse.profiler.printthread.location", "/tmp/multiverseprofiler.html");
            new PrintProfileInfoThread(delayMs, new File(file)).start();
        }
    }

    /**
     * Creates a PrintStatisticsThread.
     *
     * @param delayMs the delay between prints.
     * @throws NullPointerException     if profiler is null.
     * @throws IllegalArgumentException if delayMs is smaller than zero.
     */
    public PrintProfileInfoThread(long delayMs, File file) {
        if (delayMs < 0) {
            throw new IllegalArgumentException();
        }

        if (!file.mkdirs()) {
            throw new IllegalArgumentException("Could not create profile output file: " + file);
        }

        this.delayMs = delayMs;
        this.file = file;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        setName("PrintProfileInfoThread" + System.identityHashCode(this));
    }

    @Override
    public void run() {
        while (!interrupted()) {
            Stm stm = GlobalStmInstance.get();
            if (stm instanceof ProfilerAware) {
                ProfileDataRepository profiler = ((ProfilerAware) stm).getProfiler();
                if (profiler != null) {
                    HumanReasableOutputGenerator writer = new HumanReasableOutputGenerator();
                    String s = writer.toString(profiler);
                    writeText(s);
                }
            }

            try {
                sleep(delayMs);
            } catch (InterruptedException ignore) {
            }
        }
    }

    private void writeText(String s) {
        if (logger.isLoggable(Level.FINE)) {
            String msg = format("Start write profiling information to file %s", file.getAbsolutePath());
            logger.log(Level.FINE, msg);
        }

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            out.write(s);
        } catch (Exception ex) {
            String msg = format("Failed to write profiling information to file %s", file.getAbsolutePath());
            logger.log(Level.WARNING, msg, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    String msg = format("Failed to close bufferedwriter on file %s", file.getAbsolutePath());
                    logger.log(Level.WARNING, msg, ex);
                }
            }
        }
    }
}
