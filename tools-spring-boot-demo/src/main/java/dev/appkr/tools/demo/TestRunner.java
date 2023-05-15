package dev.appkr.tools.demo;

import dev.appkr.tools.core.FilterableLogAnalyzer;
import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.LogFilter;
import dev.appkr.tools.demo.support.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

//@Component
@RequiredArgsConstructor
public class TestRunner {

  final FilterableLogAnalyzer analyzer;

  @EventListener
  void onEvent(ApplicationReadyEvent e) throws IOException {
    Path path = Paths.get(String.join(File.separator, System.getProperty("user.home"), "mysql_log8/slow.log"));
    LogFilter filter = LogFilter.builder().slowerThanMillis(200).build();
    final AnalysisReport report = analyzer.analyze(path, filter);

    System.out.println(TestUtils.convertObjectToString(report));
  }
}
