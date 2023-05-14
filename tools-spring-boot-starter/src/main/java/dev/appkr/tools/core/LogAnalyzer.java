package dev.appkr.tools.core;

import dev.appkr.tools.core.model.AnalysisReport;
import java.io.IOException;
import java.nio.file.Path;

public interface LogAnalyzer {

  AnalysisReport analyze(Path path) throws IOException;
}
