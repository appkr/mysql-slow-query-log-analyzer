# mysql-slow-query-log-analyzer

## 기능

- MySQL slow query log 파일 업로드
- 각 쿼리별 EXPLAIN 결과 제공
- 필터 및 정렬 (e.g. 200밀리초 이상만 필터링하고 가장 느린 순으로 정렬: filterMillis=200&sort=queryTime:desc)

```
curl -XPOST http://localhost:8080/tools/analyze-mysql-slow-query-log-job \
-F 'logFile=@"/path/to/slow.log"' \
-F 'filterMillis="200"' \
-F 'filterQuery="SELECT"' \
-F 'sort="queryTime:desc"'

{
  "summary": {
    "count": 3,
    "min": "PT0.225S",
    "average": "PT0.279S",
    "max": "PT0.384S"
  },
  "data": [
    {
      "time": "2023-04-25T09:43:53.224572+09:00",
      "user": "root",
      "host": "[172.19.0.1]",
      "id": "20",
      "queryTime": "PT0.384S",
      "lockTime": "PT0.002S",
      "rowsSent": 0,
      "rowsExamined": 494,
      "sql": "SELECT a1.id FROM regions AS r1, addresses AS a1 WHERE r1.rtype <> 0 AND r1.d1 = '강원도' AND r1.d2 = '철원군' AND r1.d3 = '갈말읍' AND r1.d4 = '상사리' AND (r1.rcode = a1.legal_dong_code OR r1.rcode = a1.admin_dong_code OR r1.rcode = a1.road_code) AND (a1.jibun_number LIKE '532-2%' OR a1.building_number LIKE '532-2%') AND a1.expose = 0 ORDER BY r1.d1, r1.d2 limit 50;",
      "executionPlans": [
        {
          "id": "1",
          "selectType": "SIMPLE",
          "table": "r1",
          "partitions": "",
          "type": "ref",
          "possibleKeys": "IDX_REGION,IDX_RCODE_RTYPE",
          "key": "IDX_REGION",
          "keyLen": "651",
          "ref": "const,const,const,const",
          "rows": "1",
          "filtered": "90.0",
          "extra": "Using where"
        },
        {
          "id": "1",
          "selectType": "SIMPLE",
          "table": "a1",
          "partitions": "",
          "type": "ALL",
          "possibleKeys": "IDX_LEGAL_DONG_CODE,IDX_ADMIN_DONG_CODE,IDX_ROAD_CODE",
          "key": "",
          "keyLen": "",
          "ref": "",
          "rows": "12088875",
          "filtered": "0.57",
          "extra": "Range checked for each record (index map: 0xE)"
        }
      ]
    },
    {...},
    {...}
  ]
}
```

## 개발 환경

- amazonaws corretto jdk 17을 사용합니다
```shell
$ brew install homebrew/cask-versions/corretto17 --cask
$ jenv add /Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
$ jenv versions
```

- 아래 명령으로 MySQL(3306)을 구동합니다
```shell
~/mysql-slow-query-log-analyzer $ ./gradlew composeUp
# To stop and delete the cluster, ./gradlew composeDown
```

- 애플리케이션을 구동합니다
```shell
~/mysql-slow-query-log-analyzer $ export SPRING_PROFILES_ACTIVE=local; export USER_TIMEZONE="Asia/Seoul"; ./gradlew clean bootRun
$ curl -s http://localhost:8080/management/health
```

### 계정

docker service|username|password
---|---|---
mysql|root|secret
