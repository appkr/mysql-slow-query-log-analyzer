package dev.appkr.tools.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

import dev.appkr.tools.domain.AnalysisReport;
import dev.appkr.tools.domain.ExplainVisitor;
import dev.appkr.tools.domain.SlowQueryLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Slf4j
class SlowQueryLogAnalyzerTest {

  SlowQueryLogAnalyzer sut;

  @Test
  void analyze() {
    final String subject = """
# Time: 2023-04-16T12:02:32.382948Z
# User@Host: root[root] @  [172.18.0.1]  Id:  3464
# Query_time: 1.077059  Lock_time: 0.005489 Rows_sent: 8  Rows_examined: 54728
use local_address;
SET timestamp=1681646552;
SELECT a1.id-- line break
FROM addresses AS a1 USE INDEX(IDX_POI_QUERY)/* block comment */ 
WHERE 1 = 1 AND a1.si_do_name = '대구광역시' 
  AND/*block comment inside sql*/ a1.si_gun_gu_name = '동구' 
  AND (a1.building_name LIKE '근린생활시설1%' OR EXISTS(SELECT 1 FROM addresses a2 JOIN keywords k1 ON a2.id = k1.address_id WHERE a2.id = a1.id AND k1.name LIKE '근린생활시설1%')) 
LIMIT 50;
        """;
    final AnalysisReport result = sut.analyze(subject);

    assertThat(result).isNotNull();
    assertThat(result.getLogEntries()).isNotEmpty();
    final SlowQueryLog firstEntry = result.getLogEntries().iterator().next();
    assertThat(firstEntry).isNotNull();
    assertThat(firstEntry.getLogEntry().getSql()).contains(List.of("SELECT", "FROM", "WHERE"));
    log.info("result: {}", result);
  }

  @BeforeEach
  void setup() {
    final Object[] obj1 = new Object[]{"1", "PRIMARY", "a1", "", "ref", "IDX_POI_QUERY", "IDX_POI_QUERY", "244", "const,const", "123448", "100.00", "Using where; Using index"};
    final Object[] obj2 = new Object[]{"2", "DEPENDENT SUBQUERY", "k1", "", "range", "PRIMARY,IDX_KEYWORD_NAME", "IDX_KEYWORD_NAME", "local_address.a1.id", "362", "1", "100.00", "Using where; Using index"};
    final Object[] obj3 = new Object[]{"2", "DEPENDENT SUBQUERY", "a2", "", "eq_ref", "PRIMARY", "PRIMARY", "4", "local_address.a1.id", "1", "100.00", "Using where; Using index"};

    Query mockQuery = Mockito.mock(Query.class);
    Mockito.when(mockQuery.getResultList()).thenReturn(List.of(obj1, obj2, obj3));

    EntityManager mockEntityManager = Mockito.mock(EntityManager.class);
    Mockito.when(mockEntityManager.createNativeQuery(anyString()))
        .thenReturn(mockQuery);
    sut = new SlowQueryLogAnalyzer(new ExplainVisitor(mockEntityManager));
  }
}
