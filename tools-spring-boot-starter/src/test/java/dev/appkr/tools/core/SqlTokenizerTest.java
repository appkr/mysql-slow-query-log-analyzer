package dev.appkr.tools.core;

import static dev.appkr.tools.core.SqlTokenizer.tokenize;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlTokenizerTest {

  @Test
  void removeCommentAndNewline() {
    String query = """
-- comment at the beginning of line
# sharp style comment
#### multiple sharp comment
#no space after sharp comment
/******
banner comment
******/
SELECT 1 -- inline comment
FROM /**/ DUAL; ## different style comment
        """;

    assertThat(tokenize(query)).isEqualTo("select 1 from dual");
  }

  @Test
  void removeRedundantWhiteSpace() {
    String query = "   SELECT     1     FROM   DUAL;    ";

    assertThat(tokenize(query)).isEqualTo("select 1 from dual");
  }

  @Test
  void replaceParametersForInsertQuery() {
    String query = """
insert into regions (d1, d2, d3, d4) values ('foo', 'bar', 'baz', 'qux'),
  ('', '', '', ''); -- empty parameters
""";

    assertThat(tokenize(query))
        .isEqualTo("insert into regions (d1, d2, d3, d4) values (?)");
  }

  @Test
  void replaceStringParameters() {
    String query = "select * from a_table where 1=1 and a='1' and b = '';";

    assertThat(tokenize(query))
        .isEqualTo("select * from a_table where 1 = 1 and a = ? and b = ?");
  }

  @Test
  void replaceNumberParameters() {
    String query = "select * from a_table where a=1 and b is null;";

    assertThat(tokenize(query))
        .isEqualTo("select * from a_table where a = ? and b is null");
  }

  @Test
  void replaceLikeParameters() {
    String query = "select * from a_table where a like 'foo%' and b not like '%bar%';";

    assertThat(tokenize(query))
        .isEqualTo("select * from a_table where a like ? and b not like ?");
  }

  @Test
  void replaceInParameters() {
    String query = "select * from a_table where a in (1,2) or b in (\\\"foo\\\", 'bar');";

    assertThat(tokenize(query))
        .isEqualTo("select * from a_table where a in (?) or b in (?)");
  }

  @Test
  void replaceBetweenParameters() {
    String query = "select * from a_table where a between 10 and 20;";

    assertThat(tokenize(query))
        .isEqualTo("select * from a_table where a between ? and ?");
  }

  @Test
  void replacePagingParameters() {
    String query = "select * from a_table limit 0,100;";

    assertThat(tokenize(query))
        .isEqualTo("select * from a_table limit ?");
  }

  @Test
  void updateCase() {
    String query = "update a_table set a=1 , b='foo' where a=1;";

    // Did not correct space before comma(,) intentionally
    assertThat(tokenize(query))
        .isEqualTo("update a_table set a = ? , b = ? where a = ?");
  }

  @Test
  void deleteCase() {
    String query = "delete form a_table where a is not null or a = ''";

    // Did not correct space before comma(,) intentionally
    assertThat(tokenize(query))
        .isEqualTo("delete form a_table where a is not null or a = ?");
  }

  @Test
  void subqueryCase() {
    String query = "select * from a_table where a in (select id from b_table where b between '2023-01-01' and '2023-01-02');";

    // Currently do not support subquery
    assertThat(tokenize(query))
        .isEqualTo("select * from a_table where a in (?)");
  }
}
