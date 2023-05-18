package dev.appkr.tools.core;

import static java.util.regex.Pattern.quote;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SqlTokenizer {

  // /* foo */
  static final Pattern PATTERN_BLOCK_COMMENT = Pattern.compile("\\/[*]+[^*]*[*]+\\/");

  // -- foo; # foo; ## foo; #foo
  static final Pattern PATTERN_INLINE_COMMENT = Pattern.compile("(--|#).*");

  // insert into a_table(col1, col2) values('a', 'b'), ('c', 'd')
  static final Pattern PATTERN_INSERT_QUERY = Pattern.compile("insert.*values\\s*(?<v>[^;]*)");

  // foo="bar"; foo=1; foo= 1; foo =1; foo='bar'
  static final Pattern PATTERN_EXPRESSION_TYPE1 = Pattern
      .compile("(?<k>[^!><=\\s]+)\\s?(?<o>[!><=]{1,2})\\s?(?<v>[^!><=\\s]+)");

  // foo in('bar')
  static final Pattern PATTERN_EXPRESSION_TYPE2 = Pattern.compile("(?:\\s+)(?<o>in)\\((?<v>[^\\s]+)");

  public static String tokenize(String query) {
    // Remove inline comment
    // Join lines
    // Removing semicolon
    // Remove backtick
    // Replace all ascii char to lower case
    // Remove leading and trailing white spaces
    query = PATTERN_INLINE_COMMENT.matcher(query).replaceAll("")
        .replaceAll("\\n", " ")
        .replaceAll(quote(";"), "")
        .replaceAll(quote("`"), "")
        .toLowerCase()
        .trim();

    // Remove block comment
    final Matcher m1 = PATTERN_BLOCK_COMMENT.matcher(query);
    while (m1.find()) {
      query = query.replaceAll(quote(m1.group()), " ");
    }

    // Replace bound parameters with question marks for INSERT query and return early
    final Matcher insertQueryMatcher = PATTERN_INSERT_QUERY.matcher(query);
    if (insertQueryMatcher.matches()) {
      return query.replaceAll(quote(insertQueryMatcher.group("v")), "(?)");
    }

    // Normalize white spaces: insert space if required
    // foo='bar' -> foo = 'bar'; foo in('bar','baz') -> foo in ('bar','baz')
    final Matcher m2 = PATTERN_EXPRESSION_TYPE1.matcher(query);
    while (m2.find()) {
      query = query.replaceAll(quote(m2.group()),
          String.join(" ", m2.group("k"), m2.group("o"), m2.group("v")));
    }

    // foo in('bar') -> foo in ('bar')
    final Matcher m3 = PATTERN_EXPRESSION_TYPE2.matcher(query);
    while (m3.find()) {
      query = query.replaceAll(quote(m3.group()),
          String.join(" ", "", m3.group("o"), m3.group("v")));
    }

    // Split by white space
    final List<String> tokens = List.of(query.split("\\s+"));

    // Replace bound parameters with question mark
    final List<String> normalizedTokens = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++) {
      final String prev = (i - 1 >= 0) ? tokens.get(i - 1) : "";
      final String current = tokens.get(i).trim();
      String next = (i + 1 < tokens.size()) ? tokens.get(i + 1) : "";

      if (current.isBlank()) {
        continue;
      } else if (current.matches("[!><=]{1,2}")) {
        if (prev.equals(next)) {
          // 1 = 1; 1 <> 1
          normalizedTokens.add(current);
          normalizedTokens.add(next);
        } else {
          normalizedTokens.add(current);
          normalizedTokens.add("?");
        }
        i++;
      } else if (current.equals("like")) {
        normalizedTokens.add(current);
        normalizedTokens.add("?");
        i++;
      } else if (current.equals("in")) {
        normalizedTokens.add(current);
        normalizedTokens.add("(?)");
        i++;
        while(next.matches(".*[^)]$")) {
          next = (i + 1 < tokens.size()) ? tokens.get(i + 1) : "";
          i++;
        }
      } else if (current.equals("between")) {
        normalizedTokens.add(current);
        normalizedTokens.add("? and ?");
        i += 3;
      } else if (current.matches("offset|limit")) {
        normalizedTokens.add(current);
        normalizedTokens.add("?");
        i = tokens.size();
      } else {
        normalizedTokens.add(current);
      }
    }

    return normalizedTokens.stream().collect(Collectors.joining(" "));
  }
}
