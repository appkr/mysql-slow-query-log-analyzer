package dev.appkr.tools.core;

import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.ExecutionPlanVisitor;
import dev.appkr.tools.core.model.ServerInfo;
import dev.appkr.tools.core.model.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public class SlowQueryLogAnalyzer implements LogAnalyzer {

  // /usr/sbin/mysqld, Version: 8.0.33 (MySQL Community Server - GPL). started with:
  // Tcp port: 3306  Unix socket: /var/run/mysqld/mysqld.sock
  // Time                 Id Command    Argument
  final Pattern PATTERN_SERVER_INFO = Pattern.compile("^(?<binary>[a-z\\/]+),\\s+"
      + "Version:\\s+(?<version>[^\\s]+)\\s+"
      + "\\((?<versionDescription>.+)\\).\\sstarted with:\\n"
      + "Tcp\\s+port:\\s+(?<tcpPort>[0-9]+)\\s+"
      + "Unix\\s+socket:\\s+(?<unixSocket>[a-z\\/.]+)\\n"
      + "Time\\s+Id\\s+Command\\s+Argument$");

  // # Time: 2023-04-16T12:02:32.382948Z
  // # User@Host: root[root] @  [172.18.0.1]  Id:  3464
  // # Query_time: 1.077059  Lock_time: 0.005489 Rows_sent: 8  Rows_examined: 54728
  // use local_address;
  // SET timestamp=1681646552;
  // SELECT a1.id FROM addresses AS a1 ...]
  final Pattern PATTERN_LOG_ENTRY = Pattern.compile("^((#\\s*Time:\\s*.*)\\n"
      + "(#\\s*User@Host:\\s*.*)\\n"
      + "(#\\s*Query_time:\\s*.*)\\n"
      + "(?:use.*\\n)?"
      + "(?:SET.*\\n)?"
      + "(SELECT|UPDATE|DELETE|INSERT|REPLACE)\\s*[\\p{L}\\p{Nd}`~!@$%^&*()_\\-=+\\|{}:;'\\\"<>?,./\\s\\n]+)$",
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  final ExecutionPlanVisitor visitor;

  public SlowQueryLogAnalyzer(ExecutionPlanVisitor visitor) {
    this.visitor = visitor;
  }

  public AnalysisReport analyze(Path path) throws IOException {
    final String logContent = Files.readString(path);
    return analyze(logContent);
  }

  AnalysisReport analyze(String logContent) {
    ServerInfo serverInfo = null;
    final String firstThreeLine = Stream
        .of(logContent.split(System.lineSeparator()))
        .limit(3)
        .collect(Collectors.joining(System.lineSeparator()));
    final Matcher serverInfoMatcher = PATTERN_SERVER_INFO.matcher(firstThreeLine);
    if (serverInfoMatcher.matches()) {
      serverInfo = new ServerInfo(serverInfoMatcher.group("binary"),
          serverInfoMatcher.group("version"),
          serverInfoMatcher.group("versionDescription"),
          serverInfoMatcher.group("tcpPort"),
          serverInfoMatcher.group("unixSocket"));
    }

    final List<SlowQueryLog> logEntries = new ArrayList<>();
    final Matcher entryMatcher = PATTERN_LOG_ENTRY.matcher(logContent);
    while (entryMatcher.find()) {
      final SlowQueryLog logEntry = new SlowQueryLog(entryMatcher.group(1));
      logEntry.accept(visitor);
      logEntries.add(logEntry);
    }

    return new AnalysisReport(serverInfo, logEntries);
  }
}
