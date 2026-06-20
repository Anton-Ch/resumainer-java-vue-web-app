package com.resumainer.pdfspike.runner;

import com.resumainer.pdfspike.budget.BudgetResolver;
import com.resumainer.pdfspike.dao.*;
import com.resumainer.pdfspike.db.*;
import com.resumainer.pdfspike.model.*;
import com.resumainer.pdfspike.pdf.*;
import com.resumainer.pdfspike.plan.PagePlanBuilder;
import com.resumainer.pdfspike.render.XhtmlTemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class SpikeRunner {
    private static final Logger log = LoggerFactory.getLogger(SpikeRunner.class);
    private final String mode;
    private final Path out;
    private final Path db;
    private final boolean debugAttempts;

    private SpikeRunner(String mode, Path out, Path db, boolean debugAttempts) {
        this.mode = mode;
        this.out = out;
        this.db = db;
        this.debugAttempts = debugAttempts;
    }

    public static SpikeRunner fromArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                int idx = arg.indexOf('=');
                map.put(arg.substring(2, idx), arg.substring(idx + 1));
            }
        }
        return new SpikeRunner(
                map.getOrDefault("mode", "edge"),
                Path.of(map.getOrDefault("out", "output-edge")),
                Path.of(map.getOrDefault("db", "work/pdf-spike-edge.sqlite")),
                Boolean.parseBoolean(map.getOrDefault("debug-attempts", "false")));
    }

    public void run() throws Exception {
        if (!"edge".equalsIgnoreCase(mode) && !"all".equalsIgnoreCase(mode)) {
            throw new IllegalArgumentException("V12 supports --mode=edge or --mode=all. Requested: " + mode);
        }
        Files.createDirectories(db.getParent());
        try (SimpleConnectionPool pool = new SimpleConnectionPool(new ConnectionPoolConfig("jdbc:sqlite:" + db.toAbsolutePath(), 4))) {
            new SchemaInitializer(pool).initialize();
            ScenarioDao scenarioDao = new ScenarioDao(pool);
            SpikeConfigDao configDao = new SpikeConfigDao(pool);
            FitLimits limits = configDao.loadFitLimits();
            RunDiagnosticsWriter diagnostics = new RunDiagnosticsWriter(out);
            diagnostics.init();
            diagnostics.note("mode=" + mode + " debugAttempts=" + debugAttempts + " db=" + db.toAbsolutePath());
            BudgetResolver resolver = new BudgetResolver(scenarioDao);
            PagePlanBuilder planBuilder = new PagePlanBuilder();
            ResumeDataFactory dataFactory = new ResumeDataFactory();
            Path resourcesDir = Path.of("src/main/resources").toAbsolutePath();
            FeedbackFitEngine fitEngine = new FeedbackFitEngine(new XhtmlTemplateRenderer(), new OpenHtmlPdfRenderer(resourcesDir), new PdfAnalyzer(), new PdfValidationService(), limits);

            List<RunRow> rows = new ArrayList<>();
            for (Scenario s : scenarioDao.loadEdgeScenarios()) {
                EdgeCaseRule rule = scenarioDao.loadRule(s.ecNumber());
                MockCandidate candidate = scenarioDao.loadCandidate(s.ecNumber());
                ResumeData data = dataFactory.create(candidate, s.language());
                EdgeCaseRule resolved = resolver.resolve(rule.ecNumber(), candidate.workCount(), candidate.projectCount(), candidate.courseCount());
                PagePlan plan = planBuilder.build(data, resolved, resolved.expectedPages());
                Map<Integer, FillTarget> targets = configDao.loadFillTargets(plan.targetPageCount());

                String name = fileBase(rule, s.language(), plan.targetPageCount());
                Path html = out.resolve("html").resolve(name + ".html");
                Path pdf = out.resolve("pdf").resolve(name + ".pdf");
                Path debug = out.resolve("debug-attempts").resolve(name);
                log.info("Rendering {} rule={} targetPages={} debugAttempts={}", name, rule.reason(), plan.targetPageCount(), debugAttempts);
                diagnostics.scenarioStart(name, rule.reason(), plan.targetPageCount());
                FitResult result = fitEngine.fit(data, plan, targets, html, pdf, debug, debugAttempts);
                diagnostics.scenarioResult(name, result);
                rows.add(RunRow.of(s, rule, name, result));
            }
            writeReports(rows);
            diagnostics.note("reportMd=" + out.resolve("report").resolve("pdf-spike-report.md"));
        }
    }

    private String fileBase(EdgeCaseRule r, Language lang, int pages) {
        return "ec%02d_%s_%dpages_%dwe_%dprojects".formatted(r.ecNumber(), lang.name().toLowerCase(Locale.ROOT), pages, r.maxWork(), r.projectCount());
    }

    private void writeReports(List<RunRow> rows) throws Exception {
        Path reportDir = out.resolve("report");
        Files.createDirectories(reportDir);
        StringBuilder md = new StringBuilder("# PDF Spike V12 Housekeeping Parity Report\n\n");
        md.append("| Scenario | Selected | Valid | Reason | Attempts | Fill |\n|---|---|---:|---|---:|---|\n");
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < rows.size(); i++) {
            RunRow r = rows.get(i);
            md.append("| ").append(r.fileBase).append(" | ").append(r.selectedFile).append(" | ").append(r.valid).append(" | ").append(r.reason).append(" | ").append(r.attempts).append(" | ").append(r.fill).append(" |\n");
            if (i > 0) json.append(",\n");
            json.append(r.toJson());
        }
        json.append("\n]\n");
        Files.writeString(reportDir.resolve("pdf-spike-report.md"), md);
        Files.writeString(reportDir.resolve("pdf-spike-report.json"), json);
    }

    private record RunRow(String scenarioKey, String fileBase, String selectedFile, boolean valid, String reason, int attempts, String fill) {
        static RunRow of(Scenario s, EdgeCaseRule rule, String fileBase, FitResult result) {
            FitAttempt selected = result.selectedAttempt();
            String selectedFile = selected == null ? "" : selected.pdfPath().toString();
            boolean valid = selected != null && selected.valid();
            String reason = selected == null ? "NO_ATTEMPT" : selected.reason();
            String fill = selected == null ? "{}" : selected.metrics().fillRatios().toString();
            return new RunRow(s.key(), fileBase, selectedFile, valid, reason, result.attempts().size(), fill);
        }
        String toJson() {
            return "  {\n" +
                    "    \"scenarioKey\": \"" + esc(scenarioKey) + "\",\n" +
                    "    \"fileBase\": \"" + esc(fileBase) + "\",\n" +
                    "    \"selectedFile\": \"" + esc(selectedFile) + "\",\n" +
                    "    \"valid\": " + valid + ",\n" +
                    "    \"reason\": \"" + esc(reason) + "\",\n" +
                    "    \"attempts\": " + attempts + ",\n" +
                    "    \"fill\": \"" + esc(fill) + "\"\n" +
                    "  }";
        }
        private static String esc(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }
    }
}
