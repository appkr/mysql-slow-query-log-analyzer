package dev.appkr.tools.core.model;

import dev.appkr.tools.core.SqlTokenizer;
import org.springframework.util.DigestUtils;

public class FingerprintVisitor {

  public TokenizedQuery.Tuple visit(SlowQueryLog visitable) {
    if (visitable.getSql() == null || visitable.getSql().isBlank()) {
      return null;
    }

    final String fingerprint = SqlTokenizer.tokenize(visitable.getSql());
    final String key = DigestUtils.md5DigestAsHex(fingerprint.getBytes());
    return new TokenizedQuery.Tuple(key, fingerprint, visitable.getQueryTime(),
        visitable.getLockTime(), visitable.getRowsExamined(), visitable.getRowsSent());
  }
}
