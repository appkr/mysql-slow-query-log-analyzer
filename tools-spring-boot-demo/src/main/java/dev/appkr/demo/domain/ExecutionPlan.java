package dev.appkr.demo.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * 참고: https://dev.mysql.com/doc/refman/8.0/en/explain-output.html
 */
@Data
public class ExecutionPlan implements Serializable {

  private static final long serialVersionUID = 1L;

  String id;

  /**
   * SELECT 쿼리의 타입: DEPENDENT, 임시테이블, DERIVED는 제거 대상
   *
   * - SIMPLE: No UNION, No Sub-query
   * - PRIMARY: UNION, Sub-Query 시나리오에서 가장 바깥쪽의 쿼리; 반드시 하나만 존재함
   * - UNION: UNION에 사용된 첫번째 SELECT 쿼리는 DERIVED로 표시됨
   * - DEPENDENT UNION: 내부 쿼리가 외부 쿼리를 참조해서 처리될 때
   * - UNION RESULT: UNION 결과를 담은 임시 테이블 사용
   * - SUBQUERY: FROM 절 이외에 사용된 Sub-Query (FROM 절에 사용된 Sub-Query는 DERIVED로 표시됨)
   * - DEPENDENT SUBQUERY: 내부 쿼리가 외부 쿼리를 참조해서 처리될 때
   * - DERIVED: FROM 절에 사용된 Sub-Query (SELECT 결과를 메모리나 디스크에 임시 테이블로 만들어 사용함)
   * - DEPENDENT DERIVED: Lateral-Join 사용시
   * - UNCACHEABLE SUBQUERY: 특정 조건(함수 사용) 등으로 Sub-Query 결과를 캐슁할 수 없는 경우
   * - MATERIALIZED: FROM 또는 IN 절에 사용된 Sub-Query 결과를 임시 테이블로 구체화한 후 바깥족 테이블과 조인하는 형태로 최적화했을 때
   */
  String selectType;

  /**
   * 실행 계획 Row가 어떤 테이블의 정보인지를 표시함; <>로 싸여진 경우 임시 테이블임
   */
  String table;

  String partitions;

  /**
   * 실행 계획이 인덱스를 참조했는 지 확인할 수 있는 값; ALL을 빼고 모두 인덱스를 참조함
   *
   * (성능 좋음 -> 나쁨 순)
   * - system: 레코드가 0~1 건인 경우
   * - const: PRIMARY, UNIQUE 컬럼을 참조하는 WHERE 조건절이 있어서 결과가 1건을 반환하는 쿼리
   * - eq_ref: 조인 케이스; 왼쪽의 컬럼과 조인되는 오른쪽 컬럼이 PRIMARY/NOT NULL UNIQUE 일 때
   * - ref: 조인 케이스; 동등 조건으로 왼쪽과 오른쪽 컬럼을 조인할 때
   * - fulltext: MATCH...AGAINST를 사용하는 전문 검색 인덱스일 때
   * - ref_or_null: ref와 같은 데 NULL이 포함된 경우
   * - unique_subquery: IN 절의 Sub-Query 반환값이 유니크 값을 반환할 때
   * - index_subquery: IN 절의 Sub-Query 반환값이 유니크가 아니어서 중복을 제거하고 조인해야 할 때
   * - range: 인덱스를 범위로 검색해서 조인해야 하는 경우; <, >, IS NULL, BETWEEN, IN, LIKE 등의 연산자로 인덱스를 탐색하는 경우
   * - index_merge: 2개 이상의 인덱스를 이용해 각각의 검색 결과를 구한후 결과를 합쳐서 조인하는 경우
   * - index: 인덱스를 처음부터 끝가지 읽어어 필터링, 정렬해야 하는 경우; 데이터 파일(테이블)을 읽지는 않지만 비효율적임
   * - ALL: 풀 테이블 스캔
   */
  String type;

  /**
   * 옵티마이저의 선택 후보 인덱스 목록
   */
  String possibleKeys;

  /**
   * 옵티마이저가 선택하여 사용한 인덱스
   */
  String key;

  /**
   * 사용한 인덱스의 크기
   *
   * e.g. (INT, VARCHAR(10)) 복합 인덱스에서 keyLen값이 4이면 4바이트만 사용했다는 의미,
   * 즉 VARCHAR(10)을 사용하지 않고 쿼리를 수행했음을 의미함
   * e.g. VARCHAR 컬럼의 인덱스는 Charset에 따라 길이가 다를 수 있음; 4바이트 문자라면 VARCHAR(10)은 40 바이트
   */
  String keyLen;

  /**
   * type=ref 일때, 조인에 사용한 컬럼 표시
   */
  String ref;

  /**
   * 옵티마이저가 비용을 산정하기 위해 검토해야하는 레코드의 수(예측 값)
   */
  String rows;

  /**
   * 쿼리 해석 및 수행에 대한 부가 정보
   * 주의: type=index(완전 비효율), Extra=Using index(최고 효율)을 혼동하지 말것
   *
   * - const row not found: const 방식이지만 결과 레코드가 0개
   * - distinct: 중복이 제거된 값으로 조인했음
   * - Full scan on NULL key: WHERE 조건에 Nullable 컬럼이 있어서, NULL을 만나면 풀 스캔을 하겠다는 경고
   * - Impossible HAVING: HAVING을 만족하는 레코드가 없음
   * - Impossible WHERE: WHERE 조건이 항상 False인 경우
   * - Impossible WHERE noticed after reading const table: 실행해보니 WHERE 조건이 False인 경우
   * - No matching min/max row: 결과 레코드가 없어서 min(), max()를 구할 수 없는 경우
   * - No table used: FROM 절이 없는 경우 또는 FROM DUAL
   * - Not exists: 오른쪽 테이블에 존재하지 않는 왼쪽 테이블의 레코드를 조회할 때(차집합 조회), 옵티마티저가 NOT EXISTS 스타일로 최적화했음
   * - Range checked for each record(index map: N): WHERE 조건에 두 개의 테이블을 사용하여 WHERE 조건에 따라 인덱스를 탈지, 풀 스캔할 지 결정하는 하는 경우
   * - Select tables optimized away: SELECT 절에 min(), max()만 조회할 때, 옵티마이저가 인덱스를 사용하여 내림차순, 오름차순 정렬하고 1건만 조회하는 식으로 최적화했음
   * - unique row not found: PRIMARY 또는 UNIQUE 컬럼으로 OUTER JOIN을 수행할 때, 아우터 테이블에 일치하는 컬럼이 없을 때
   * - Using filesort: ORDER BY를 처리하기 위해 인덱스를 사용하지 못한 경우
   * - Using index: 데이터 파일(테이블)을 읽지 않고 인덱스만으로 쿼리를 처리한 경우(커버링 인덱스)
   * - Using index for group by: GROUP BY를 인덱스로 처리한 경우(=INDEX LOOSE SCAN)
   * - Using join buffer: 조인하려는 오른쪽 테이블의 컬럼이 인덱스가 걸려있지 않아서, 임시로 Join Buffer를 이용해서 조인을 수행한 경우
   * - Using sort_union, Using union, Using intersect: type=index_merge일 때, 두 인덱스를 어떻게 합쳤는지 정보를 제공함
   *   - Using intersect: 인덱스를 사용하는 조건이 AND로 연결된 경우(교집합)
   *   - Using union: 인덱스를 사용하는 조건이 OR로 연결된 경우(합집합)
   *   - Using sort_union: 인덱스를 사용하는 조건이 OR로 연결되었는데, OR로 연결된 인덱스를 range로 탐색하는 경우
   * - Using temporary: 쿼리 수행을 위해 임시 테이블 사용
   *   - FROM 절에 Sub-Query를 사용한 경우
   *   - COUNT(DISTINCT 컬럼)이 인덱스를 사용할 수 없는 경우
   *   - UNION, UNION ALL을 사용한 경우
   *   - 인덱스를 사용할 수 없는 정렬
   * - Using where: WHERE 조건으로 필터해서 쿼리를 수행한 경우
   */
  String extra;

  /**
   * Extra=Using where 일 때, 얼마나 많은 레코드가 필터링 되었는 지 알려줌
   * 필터링 후 몇 %가 남았는지를 표현하므로, 값이 높을 수록 WHERE 조건이 잘 설계되었음을 의미함
   */
  String filtered;
}
