# AI-Powered Enterprise API Gateway

A microservices platform demonstrating Spring Boot, Spring Security (JWT), Redis,
Docker, PostgreSQL, REST APIs, gRPC-free service-to-service REST calls, an
LLM-powered analytics query endpoint, and Prometheus/Grafana monitoring.

## Architecture

```
                      +----------------+
   Client  ---------> |  api-gateway   |  (JWT auth, Redis rate limiting)
                      +--------+-------+
                               |
              +----------------+----------------+
              |                                  |
      +-------v-------+                  +-------v-------+
      | catalog-service|<-----------------| order-service |
      |  (Postgres)    |   REST call      |  (Postgres)   |
      +----------------+                  +---------------+
              |
              v
      +----------------+
      | analytics-service| --(logs every request)
      |  (Postgres+Redis)| --(LLM query endpoint)
      +----------------+
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Single entry point, auth, rate limiting, routing |
| catalog-service | 8081 | Product CRUD |
| order-service | 8082 | Order CRUD, calls catalog-service |
| analytics-service | 8083 | Request logging, AI query endpoint, anomaly detection |

## Status

- [x] Step 1: Repo skeleton
- [x] Step 2: catalog-service
- [x] Step 3: order-service
- [ ] Step 4: api-gateway routing
- [ ] Step 5: Spring Security + JWT
- [ ] Step 6: Redis rate limiting
- [ ] Step 7: analytics-service logging pipeline
- [ ] Step 8: AI query endpoint + anomaly detection
- [ ] Step 9: Monitoring (Actuator, Prometheus, Grafana)
- [ ] Step 10: Full Docker Compose + polish

## Prerequisites (install before Step 2)

- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Docker Desktop (`docker -version`)
- An IDE (IntelliJ IDEA Community Edition recommended, free)

## Running catalog-service locally (Step 2)

```bash
cd catalog-service
docker compose up -d postgres-catalog   # starts just the DB (see docker-compose.yml)
mvn spring-boot:run
```

Then test it:
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","description":"Ergonomic mouse","price":19.99,"stockQuantity":100}'

curl http://localhost:8081/api/products
```
