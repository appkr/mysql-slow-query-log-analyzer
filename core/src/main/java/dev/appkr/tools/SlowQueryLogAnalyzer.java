package dev.appkr.tools;

import dev.appkr.tools.model.AnalysisReport;
import dev.appkr.tools.model.ExplainVisitor;
import dev.appkr.tools.model.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SlowQueryLogAnalyzer implements LogAnalyzer {

  // # Time: 2023-04-16T12:02:32.382948Z
  // # User@Host: root[root] @  [172.18.0.1]  Id:  3464
  // # Query_time: 1.077059  Lock_time: 0.005489 Rows_sent: 8  Rows_examined: 54728
  // use local_address;
  // SET timestamp=1681646552;
  // SELECT a1.id FROM addresses AS a1 ...]
  Pattern regex = Pattern.compile("^((#\\s*Time:\\s*.*)\\n"
      + "(#\\s*User@Host:\\s*.*)\\n"
      + "(#\\s*Query_time:\\s*.*)\\n"
      + "(?:use.*\\n)?(SET.*)\\n"
      + "(SELECT|UPDATE|DELETE|INSERT|REPLACE)\\s*[\\p{L}\\p{Nd}`~!@$%^&*()_\\-=+\\|{}:;'\\\"<>?,./\\s\\n]+)$",
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  final ExplainVisitor visitor;

  public SlowQueryLogAnalyzer(ExplainVisitor visitor) {
    this.visitor = visitor;
  }

  public AnalysisReport analyze(Path path) throws IOException {
    final String logContent = Files.readString(path);
    return analyze(logContent);
  }

  public AnalysisReport analyze(String logContent) {
    final List<SlowQueryLog> logEntries = new ArrayList<>();

    final Matcher entryMatcher = regex.matcher(logContent);
    while (entryMatcher.find()) {
      final SlowQueryLog logEntry = new SlowQueryLog(entryMatcher.group(1));
      logEntry.accept(visitor);
      logEntries.add(logEntry);
    }

    return new AnalysisReport(logEntries);
  }
}
