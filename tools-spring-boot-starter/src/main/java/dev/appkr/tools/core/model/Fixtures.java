package dev.appkr.tools.core.model;

import java.time.Duration;
import java.util.List;
import org.springframework.util.DigestUtils;

public class Fixtures {

  public static long MIN_QUERY_TIME = 1_000L; // milli sec
  public static long MAX_QUERY_TIME = 4_000L; // milli sec

  public static String aLogString1() {
    return """
/usr/sbin/mysqld, Version: 8.0.33 (MySQL Community Server - GPL). started with:
Tcp port: 3306  Unix socket: /var/run/mysqld/mysqld.sock
Time                 Id Command    Argument
# Time: 2023-01-01T00:00:00.000000Z
# User@Host: root[root] @  [172.18.0.1]  Id:  1
# Query_time: 2.000000  Lock_time: 0.100000 Rows_sent: 5  Rows_examined: 10
use local_demo;
select a1.col1 from a_table a1 where col1 = 'foo';
        """;
  }

  public static String aLogString2() {
    return """
/usr/sbin/mysqld, Version: 8.0.33 (MySQL Community Server - GPL). started with:
Tcp port: 3306  Unix socket: /var/run/mysqld/mysqld.sock
Time                 Id Command    Argument
# Time: 2023-01-01T00:00:00.000001Z
# User@Host: root[root] @  [172.18.0.1]  Id:  2
# Query_time: 1.000000  Lock_time: 0.200000 Rows_sent: 10  Rows_examined: 20
use local_demo;
select a1.col1 from a_table a1 where col1 = 'bar';
        """;
  }

  public static String aLogString3() {
    return """
/usr/sbin/mysqld, Version: 8.0.33 (MySQL Community Server - GPL). started with:
Tcp port: 3306  Unix socket: /var/run/mysqld/mysqld.sock
Time                 Id Command    Argument
# Time: 2023-01-01T00:00:00.000000Z
# User@Host: root[root] @  [172.18.0.1]  Id:  100
# Query_time: 4.000000  Lock_time: 0.005000 Rows_sent: 8  Rows_examined: 50000
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

  public static TokenizedQuery.Tuple aTuple1() {
    final String fingerprint = "select a1.col1 from a_table a1 where col1 = ?";
    final String key = DigestUtils.md5DigestAsHex(fingerprint.getBytes());
    return new TokenizedQuery.Tuple(key, fingerprint, Duration.ofMillis(2_000L), Duration.ofMillis(100L), 10, 5);
  }

  public static TokenizedQuery.Tuple aTuple2() {
    final String fingerprint = "select a1.col1 from a_table a1 where col1 = ?";
    final String key = DigestUtils.md5DigestAsHex(fingerprint.getBytes());
    return new TokenizedQuery.Tuple(key, fingerprint, Duration.ofMillis(MIN_QUERY_TIME), Duration.ofMillis(200L), 20, 10);
  }

  public static TokenizedQuery.Tuple aTuple3() {
    final String fingerprint = "select a1.col1 from a_table a1 where col1 = ?";
    final String key = DigestUtils.md5DigestAsHex(fingerprint.getBytes());
    return new TokenizedQuery.Tuple(key, fingerprint, Duration.ofMillis(MAX_QUERY_TIME), Duration.ofMillis(5), 50_000, 8);
  }
}
