# mysql-slow-query-log-analyzer

## Feature

- MySQL slow query log content parsing (In memory caching of analysis report)
- Filtering and Sorting (e.g. filter slower than 200ms and sort slowest first: `filterMillis=200&sort=queryTime:desc`)
- EXPLAIN result for each query
- Grouping by query pattern and provides statistics

```shell
# An example from tools-string-boot-demo
curl -XPOST http://localhost:8080/demo/analyze-mysql-slow-query-log-job \
-F 'logFile=@"/path/to/slow.log"' \
-F 'filterMillis="200"' \
-F 'filterQuery="SELECT"' \
-F 'sort="queryTime:desc"'

{
  "serverInfo": {
    "binary": "mysqld",
    "version": "5.7.31-log",
    "versionDescription": "MySQL Community Server (GPL)",
    "tcpPort": "0",
    "unixSocket": "/var/run/mysqld/mysqld.sock"
  },
  "summary": {
    "count": 3,
    "min": "PT0.225S",
    "average": "PT0.279S",
    "max": "PT0.384S"
  },
  "data": [
    {
      "key": "a2b338a897e73f6e577e9f100c8cc9ae",
      "time": "2023-04-25T09:43:53.212508+09:00",
      "user": "root",
      "host": "[172.19.0.1]",
      "id": "22",
      "queryTime": "PT0.384S",
      "lockTime": "PT0.013S",
      "rowsSent": 0,
      "rowsExamined": 1341,
      "sql": "SELECT a1.id FROM regions AS r1, addresses AS a1 WHERE r1.rtype <> 0 AND r1.d1 = '충청북도' AND r1.d2 = '청주시 상당구' AND r1.d3 = '용담.명암.산성동' AND (r1.rcode = a1.legal_dong_code OR r1.rcode = a1.admin_dong_code OR r1.rcode = a1.road_code) AND (a1.jibun_number LIKE '168-38%' OR a1.building_number LIKE '168-38%') AND a1.category_id in (1) ORDER BY r1.d1, r1.d2 limit 50;",
      "executionPlans": [
        {
          "id": "1",
          "selectType": "SIMPLE",
          "table": "r1",
          "partitions": "",
          "type": "ref",
          "possibleKeys": "IDX_REGION,IDX_RCODE_RTYPE",
          "key": "IDX_REGION",
          "keyLen": "488",
          "ref": "const,const,const",
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
    {},
    {}
    }
  ],
  "queryStats": [
    {
      "key": "a2b338a897e73f6e577e9f100c8cc9ae",
      "fingerprint": "select a1.id from regions as r1, addresses as a1 where r1.rtype <> ? and r1.d1 = ? and r1.d2 = ? and r1.d3 = ? and (r1.rcode = a1.legal_dong_code or r1.rcode = a1.admin_dong_code or r1.rcode = a1.road_code) and (a1.jibun_number like ? or a1.building_number like ?) and a1.category_id in (?) order by r1.d1, r1.d2 limit ?;",
      "calls": 1,
      "cumQueryTime": "PT0.384S",
      "cumLockTime": "PT0.013S",
      "cumRowsExamined": 1341,
      "cumRowsSent": 0,
      "minTime": "PT0.384S",
      "maxTime": "PT0.384S",
      "avgTime": "PT0.384S",
      "p50Time": "PT0.384S",
      "p95Time": "PT0.384S",
      "stddevTime": "PT0S"
    },
    {},
    {}
  ]
}
```

## Install

Add library as a project dependency.

```groovy
// build.gradle
implementation 'dev.appkr:tools-spring-boot-starter:0.0.1-RELEASE'
```

Integrate the feature to your service.

```java
public class YourService {
  final dev.appkr.tools.core.FilterableLogAnalyzer analyzer;
  
  void yourMethod() {
    java.nio.file.Path path = Paths.of("/path/to/your/slow.log");
    Integer filterMillis = 200;
    String filterQuery = "SELECT";
    org.springframework.data.domain.Sort sortSpec = Sort.by(Sort.Direction.DESC, "queryTime");
    
    dev.appkr.tools.core.model.AnalysisReport report = analyzer.analyze(path, filterMillis, filterQuery, sortSpec);
  }
}
```

## Demo project powered by [msa-starter](https://github.com/appkr/msa-starter)

You MAY NOT want to integrate the library your application; but just analyze the slow query log. Then the demo project fits your usecase.

Clone the project.

```shell
git clone git@github.com:appkr/mysql-slow-query-log-analyzer.git
# OR git clone https://github.com/appkr/mysql-slow-query-log-analyzer.git
```

Provide a jdbc connection info and run it. The demo project requires JDK 17 and the MySQL YOUR_DATABASE should be running and accessible at YOUR_HOST.

```shell
export SPRING_DATASOURCE_URL=jdbc:mysql://YOUR_HOST:3306/YOUR_DATABASE?useSSL=false;SPRING_DATASOURCE_USERNAME=root;SPRING_DATASOURCE_PASSWORD=root
./gradlew clean bootRun
```

Call the api.

```shell
curl -s 'http://localhost:8080/demo/analyze-mysql-slow-query-log-job' \
-F 'logFile=@"/path/to/your/slow.log"'
```

---

For developers

## Environment setup

Install (amazonaws corretto) jdk 17
```shell
$ brew install homebrew/cask-versions/corretto17 --cask
$ jenv add /Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
$ jenv versions
```

Run MySQL(3306, root / secret) using following command
```shell
~/mysql-slow-query-log-analyzer $ ./gradlew composeUp
# To stop and delete the cluster, ./gradlew composeDown
```

## Contribution

Issue and PRs are always welcomed.
