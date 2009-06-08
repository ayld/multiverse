package org.multiverse.benchmark;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ResultRepository {
    private File benchmarkNrDir;

    public ResultRepository() {
        File benchmarksDirectory = new File(getTmpDir(), "benchmarks");

        File dateDir = getDateDir(benchmarksDirectory);

        benchmarkNrDir = nextDir(dateDir);
    }

    public void save(TestResult testResult) {
        long durationNs = Long.parseLong(testResult.get("durationNs"));

        System.out.printf("TestCase %s took %s ms\n",
                testResult.getTestCase().getLongDescription(),
                TimeUnit.NANOSECONDS.toMillis(durationNs));


        File file = createOutputFile(testResult);
        writeOutputToFile(testResult, file);
    }

    private void writeOutputToFile(TestResult testResult, File file) {
        try {
            Writer output = new BufferedWriter(new FileWriter(file));
            try {
               output.write(testResult.getProperties().toString());
            }
            finally {
                output.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private File createOutputFile(TestResult testResult) {
        File dir = getDestinationDir(testResult);
        return new File(dir, testResult.getX() + ".txt");
    }

    private File getDestinationDir(TestResult testResult) {
        File x = new File(benchmarkNrDir, testResult.getTestCase().getDescription());

        File dir = new File(x, testResult.getTestCase().getProperties());

        dir.mkdirs();
        return dir;
    }

    private File getDateDir(File benchmarksDirectory) {
        SimpleDateFormat format = new SimpleDateFormat("y-M-d");
        String date = format.format(new Date());

        File dateDir = new File(benchmarksDirectory, date);
        dateDir.mkdirs();
        return dateDir;
    }

    public File nextDir(File dir) {
        int max = -1;
        for (String s : dir.list()) {
            try {
                int i = Integer.parseInt(s);
                if (i > max) {
                    max = i;
                }
            } catch (NumberFormatException ex) {
            }
        }
        return new File(dir, "" + (max + 1));
    }

    public File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
