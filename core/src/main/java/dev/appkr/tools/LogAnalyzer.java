package dev.appkr.tools;

import dev.appkr.tools.model.AnalysisReport;
import java.io.IOException;
import java.nio.file.Path;

public interface LogAnalyzer {

  AnalysisReport analyze(Path path) throws IOException;
}
