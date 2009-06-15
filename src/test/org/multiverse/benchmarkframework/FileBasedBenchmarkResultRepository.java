package org.multiverse.benchmarkframework;

import java.io.*;
import static java.lang.String.format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * A {@link BenchmarkResultRepository} that persist on file system.
 *
 * @author Peter Veentjer.
 */
public class FileBasedBenchmarkResultRepository implements BenchmarkResultRepository {

    private File rootDir;

    public FileBasedBenchmarkResultRepository(File rootDir) {
        if (rootDir == null) {
            throw new NullPointerException();
        }

        if (!rootDir.isDirectory()) {
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            } else {
                throw new IllegalArgumentException();
            }
        }

        this.rootDir = rootDir;
    }

    @Override
    public BenchmarkResult loadLast(Date date, String benchmarkName) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public BenchmarkResult loadLast(String benchmark) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public BenchmarkResult load(Date date, String benchmarkName) {
        if (date == null || benchmarkName == null) {
            throw new NullPointerException();
        }

        File dir = getDateDir(date);
        File benchmarkDir = new File(dir, benchmarkName);

        List<TestCaseResult> caseResults = new LinkedList<TestCaseResult>();
        for (File file : benchmarkDir.listFiles(new ResultFileFilter())) {

            Properties properties = new Properties();
            try {
                properties.load(new FileReader(file));
                TestCaseResult caseResult = new TestCaseResult(properties);
                caseResults.add(caseResult);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new BenchmarkResult(caseResults);
    }

    public void store(BenchmarkResult benchmarkResult) {
        if (benchmarkResult == null) {
            throw new NullPointerException();
        }

        for (TestCaseResult result : benchmarkResult.getTestCaseResultList()) {
            File file = createOutputFile(result);
            writeOutputToFile(result, file);
        }
    }

    private void writeOutputToFile(TestCaseResult testCaseResult, File file) {
        try {
            Writer output = new BufferedWriter(new FileWriter(file));
            try {
                testCaseResult.getProperties().store(output, "");
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private File createOutputFile(TestCaseResult testCaseResult) {
        File dir = getDestinationDir(testCaseResult);
        return new File(dir, System.currentTimeMillis() + ".txt");
    }

    private File getDestinationDir(TestCaseResult testCaseResult) {
        File destinationDir = new File(getDateDir(new Date()), testCaseResult.getBenchmarkName());
        if (destinationDir.isDirectory()) {
            return destinationDir;
        } else if (destinationDir.exists()) {
            String msg = format("DestinationDir %s is not a directory", destinationDir.getAbsolutePath());
            throw new IllegalStateException(msg);
        }

        destinationDir.mkdirs();
        return destinationDir;
    }

    private File getDateDir(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("y-M-d");
        File dateDir = new File(rootDir, format.format(date));
        dateDir.mkdirs();
        return dateDir;
    }

    private static class ResultFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".txt");
        }
    }
}
