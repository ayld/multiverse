package org.multiverse.benchmarks;

import org.multiverse.benchmarkframework.BenchmarkResult;
import org.multiverse.benchmarkframework.BenchmarkResultRepository;
import org.multiverse.benchmarkframework.FileBasedBenchmarkResultRepository;
import org.multiverse.benchmarkframework.TestCaseResult;
import org.multiverse.benchmarkframework.diagram.DiagramModel;
import org.multiverse.benchmarkframework.diagram.DiagramWriter;
import org.multiverse.benchmarkframework.diagram.GnuPlotDiagramWriter;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class DiagramMain {

    private BenchmarkResultRepository resultRepository;
    private DiagramModel model = new DiagramModel();

    public DiagramMain(File benchmarkDir) {
        resultRepository = new FileBasedBenchmarkResultRepository(benchmarkDir);
    }

    public void doIt(File outputFile, List<String> names, String x, String y) {
        Date now = new Date();

        for (String name : names) {
            addToModel(now, name);
        }

        DiagramWriter writer = new GnuPlotDiagramWriter(outputFile, x, y);
        writer.write(model);
    }

    public void addToModel(Date date, String benchmarkName) {
        BenchmarkResult benchmarkResult = resultRepository.load(date, benchmarkName);

        for (TestCaseResult caseResult : benchmarkResult.getTestCaseResultList()) {
            model.add(benchmarkName, caseResult);
        }
    }

    public static void main(String[] args) {
        File benchmarkDir = new File(args[0]);
        File outputFile = new File(args[1]);
        List<String> names = getBenchmarks(args[2]);
        String x = args[3];
        String y = args[4];

        DiagramMain diagramMain = new DiagramMain(benchmarkDir);
        diagramMain.doIt(outputFile, names, x, y);
    }

    private static List<String> getBenchmarks(String s) {
        List<String> benchmarks = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(s, ";");
        while (tokenizer.hasMoreTokens()) {
            benchmarks.add(tokenizer.nextToken());
        }
        return benchmarks;
    }
}
