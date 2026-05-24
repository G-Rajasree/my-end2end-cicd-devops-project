# Galaxy DevOps — GitHub Trending Repositories App

A Java 21 Spring Boot REST API that returns trending GitHub repositories by language and time period. Built as part of a production-grade AWS DevOps CI/CD pipeline.

## Endpoints

| Endpoint | Description |
|---|---|
| `GET /` | App name, version, build number, status |
| `GET /trending` | Top 10 trending GitHub repos (weekly) |
| `GET /trending?language=java&count=5&period=daily` | Filtered by language and period |
| `GET /info` | Build info — version, build number, deploy date |
| `GET /actuator/health` | Kubernetes liveness/readiness probe |
| `GET /actuator/prometheus` | Prometheus metrics scrape endpoint |

## Run Locally

```bash
cd github-trending
mvn clean package
java -jar target/demo-workshop-2.1.2.jar
# App starts on http://localhost:8000
```

## Run Tests

```bash
mvn test
```

## Tech
- Java 21
- Spring Boot 3.2.5
- Maven 3.9.16
- JaCoCo (code coverage)
- SonarQube (code quality)
