package dev.appkr.demo.application.port.in;

import dev.appkr.demo.domain.AnalysisReport;
import java.io.IOException;
import java.nio.file.Path;

public interface LogAnalyzer {

  AnalysisReport analyze(Path path) throws IOException;
}
