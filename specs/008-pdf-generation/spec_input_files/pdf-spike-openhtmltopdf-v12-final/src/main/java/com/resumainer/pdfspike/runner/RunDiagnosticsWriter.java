package com.resumainer.pdfspike.runner;

import com.resumainer.pdfspike.model.FitAttempt;
import com.resumainer.pdfspike.model.FitResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class RunDiagnosticsWriter {
    private final Path logPath;

    public RunDiagnosticsWriter(Path out) {
        this.logPath = out.resolve("logs").resolve("pdf-spike.log");
    }

    public Path logPath() { return logPath; }

    public void init() {
        try {
            Files.createDirectories(logPath.getParent());
            Files.writeString(logPath, "PDF Spike diagnostics log\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize diagnostics log", e);
        }
    }

    public void scenarioStart(String fileBase, String reason, int targetPages) {
        append("\nSCENARIO " + fileBase + " targetPages=" + targetPages + " rule=\"" + reason + "\"\n");
    }

    public void scenarioResult(String fileBase, FitResult result) {
        append("RESULT " + fileBase + " selected=" + (result.selectedAttempt() == null ? "none" : result.selectedAttempt().pdfPath()) + " attempts=" + result.attempts().size() + "\n");
        for (FitAttempt a : result.attempts()) {
            append("  attempt=" + a.attemptNumber()
                    + " valid=" + a.valid()
                    + " reason=" + a.reason()
                    + " pages=" + a.metrics().actualPageCount()
                    + " fill=" + a.metrics().fillRatios()
                    + " state=" + a.state().label()
                    + " html=" + a.htmlPath()
                    + " pdf=" + a.pdfPath()
                    + "\n");
        }
    }

    public void note(String message) {
        append("NOTE " + message + "\n");
    }

    private void append(String line) {
        try {
            Files.writeString(logPath, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append diagnostics log", e);
        }
    }
}
