package dev.appkr.tools.application.port.in;

import dev.appkr.tools.domain.AnalysisReport;
import java.io.IOException;
import java.nio.file.Path;

public interface LogAnalyzer {

  AnalysisReport analyze(Path path) throws IOException;
}
