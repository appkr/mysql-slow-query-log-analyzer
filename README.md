# mysql-slow-query-log-analyzer

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
