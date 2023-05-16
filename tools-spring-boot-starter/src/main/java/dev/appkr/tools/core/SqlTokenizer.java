package dev.appkr.tools.core;

import static java.util.regex.Pattern.quote;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTokenizer {

  // ... /* foo */ ...
  static final Pattern PATTERN_COMMENT_BLOCK = Pattern.compile("/\\*+[^\\\\*]*\\*+/");

  // ... -- foo; ... # foo
  static final Pattern PATTERN_COMMENT_INLINE = Pattern.compile("(--|#).*");

  // insert into a_table(col1, col2) values('a', 'b'), ('c', 'd')
  static final Pattern PATTERN_INSERT_QUERY = Pattern
      .compile("insert.*values\\s*(?<v>[^;]*)", Pattern.CASE_INSENSITIVE);

  // a = 'b'
  static final Pattern PATTERN_EXPRESSION = Pattern.compile("(?<k>[^\\s]+)\\s*(?<o>[!><=]{1,2})\\s*'(?<v>[^']+)'");

  // a = 1; a = 1.0
  static final Pattern PATTERN_NUM_EXPRESSION = Pattern.compile("(?<k>[^\\s]+)\\s*(?<o>[!><=]{1,2})\\s*(?<v>[0-9.]+)");

  // a like 'b'; a not like 'b';
  static final Pattern PATTERN_LIKE_EXPRESSION = Pattern
      .compile("(?<k>[^\\s]+)\\s*(?<o>(not)?\\s+like)\\s+'(?<v>[^']+)'", Pattern.CASE_INSENSITIVE);

  // a in (1, 2); a in ('foo', 'bar'); a between (1 and 2); a between ('foo' and 'bar')
  static final Pattern PATTERN_IN_EXPRESSION = Pattern
      .compile("(?<k>[^\\s]+)\\s*in\\s*\\((?<v>[^\\)]+)\\)", Pattern.CASE_INSENSITIVE);

  // a in (1, 2); a in ('foo', 'bar'); a between (1 and 2); a between ('foo' and 'bar')
  static final Pattern PATTERN_BETWEEN_EXPRESSION = Pattern
      .compile("(?<k>[^\\s]+)\\s*between\\s+(?<v>[^\\s]+\\s*and\\s*[^\\s;]+)", Pattern.CASE_INSENSITIVE);

  // offset 0; offset 1000; limit 5; limit 100, 100
  static final Pattern PATTERN_PAGING_EXPRESSION = Pattern
      .compile("(?<o>offset|limit)\\s+(?<v>[0-9]+(\\s*,\\s*[0-9]+)?)", Pattern.CASE_INSENSITIVE);

  public static String tokenize(String query) {
    // Remove inline comment first; then Remove new line; then remove block comment
    query = PATTERN_COMMENT_INLINE.matcher(query)
        .replaceAll("")
        .replaceAll("\\n", " ");
    final Matcher cm = PATTERN_COMMENT_BLOCK.matcher(query);
    while (cm.find()) {
      query = query.replaceAll(quote(cm.group()), "");
    }

    // Remove redundant white space
    // Replace empty parameter value into question mark
    query = query
        .replaceAll("[\\s]{2,}", " ")
        .replaceAll("'\\s*'", "'?'")
        .replaceAll("\\\"\\s*\\\"", "'?'")
        .trim();

    // Replace bound parameters with question marks for INSERT query
    final Matcher iqm = PATTERN_INSERT_QUERY.matcher(query);
    if (iqm.find()) {
      query = query.replaceAll(quote(iqm.group("v")), "(?)");
    }

    // Replace bound parameters with question marks for other queries
    final Matcher em = PATTERN_EXPRESSION.matcher(query);
    while (em.find()) {
      query = query.replace(em.group(),
          String.join(" ", em.group("k"), em.group("o"), "?"));
    }

    final Matcher nm = PATTERN_NUM_EXPRESSION.matcher(query);
    while (nm.find()) {
      // Skip 1 = 1
      if (!nm.group("k").equals(nm.group("v"))) {
        query = query.replace(nm.group(),
            String.join(" ", nm.group("k"), nm.group("o"), "?"));
      }
    }

    final Matcher lm = PATTERN_LIKE_EXPRESSION.matcher(query);
    while (lm.find()) {
      query = query.replace(lm.group(),
          String.join(" ", lm.group("k"), lm.group("o"), "?"));
    }

    final Matcher im = PATTERN_IN_EXPRESSION.matcher(query);
    while (im.find()) {
      query = query.replace(im.group(),
          String.join(" ", im.group("k"), "in (?)"));
    }

    final Matcher bm = PATTERN_BETWEEN_EXPRESSION.matcher(query);
    while (bm.find()) {
      query = query.replace(bm.group(),
          String.join(" ", bm.group("k"), "between ? and ?"));
    }

    final Matcher pm = PATTERN_PAGING_EXPRESSION.matcher(query);
    while (pm.find()) {
      query = query.replace(pm.group(), String.join(" ", pm.group("o"), "?"));
    }

    // Remove single and double quotes; Re-replace redundant white space; Replace keyword to all lower case
    return query
        .replaceAll("'|\\\"", "")
        .replaceAll("[\\s]{2,}", " ")
        .toLowerCase();
  }
}
