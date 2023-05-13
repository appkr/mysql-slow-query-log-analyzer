package dev.appkr.tools.core.model;

import java.util.List;

public class Fixtures {

  public static String aLogString1() {
    return """
# Time: 2023-01-01T00:00:00.000000Z
# User@Host: root[root] @  [172.18.0.1]  Id:  1
# Query_time: 2.000000  Lock_time: 0.100000 Rows_sent: 5  Rows_examined: 10
use local_demo;
SELECT 1 FROM DUAL;
        """;
  }

  public static String aLogString2() {
    return """
# Time: 2023-01-01T00:00:00.000001Z
# User@Host: root[root] @  [172.18.0.1]  Id:  2
# Query_time: 1.000000  Lock_time: 0.200000 Rows_sent: 10  Rows_examined: 20
use local_demo;
SELECT 1 FROM DUAL;
        """;
  }

  public static String aLogString3() {
    return """
# Time: 2023-01-01T00:00:00.000000Z
# User@Host: root[root] @  [172.18.0.1]  Id:  100
# Query_time: 1.077059  Lock_time: 0.005489 Rows_sent: 8  Rows_examined: 54728
use local_address;
SET timestamp=1672531200;
SELECT a1.id-- line break
FROM addresses AS a1 USE INDEX(IDX_POI_QUERY)/* block comment */ 
WHERE 1 = 1 AND a1.si_do_name = '대구광역시' 
  AND/*block comment inside sql*/ a1.si_gun_gu_name = '동구' 
  AND (a1.building_name LIKE '근린생활시설1%' OR EXISTS(SELECT 1 FROM addresses a2 JOIN keywords k1 ON a2.id = k1.address_id WHERE a2.id = a1.id AND k1.name LIKE '근린생활시설1%')) 
LIMIT 50;
        """;
  }

  public static List<Object[]> anExecutionPlan() {
    final Object[] obj1 = new Object[]{"1", "PRIMARY", "a1", "", "ref", "IDX_POI_QUERY", "IDX_POI_QUERY", "244", "const,const", "123448", "100.00", "Using where; Using index"};
    final Object[] obj2 = new Object[]{"2", "DEPENDENT SUBQUERY", "k1", "", "range", "PRIMARY,IDX_KEYWORD_NAME", "IDX_KEYWORD_NAME", "local_address.a1.id", "362", "1", "100.00", "Using where; Using index"};
    final Object[] obj3 = new Object[]{"2", "DEPENDENT SUBQUERY", "a2", "", "eq_ref", "PRIMARY", "PRIMARY", "4", "local_address.a1.id", "1", "100.00", "Using where; Using index"};
    return List.of(obj1, obj2, obj3);
  }
}
