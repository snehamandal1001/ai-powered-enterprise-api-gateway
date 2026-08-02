# AI-Powered Enterprise API Gateway

A microservices platform built with Spring Boot, demonstrating API Gateway
design, JWT authentication, Redis rate limiting, LLM-powered analytics, and
full observability — fully containerized and running as a single
`docker compose up` command.

**Stack:** Java 17 · Spring Boot · Spring Cloud Gateway · Spring Security ·
Redis · PostgreSQL · Docker · Prometheus · Grafana · Claude API

**Architecture:** 4 independent microservices (API Gateway, Catalog, Order,
Analytics), each owning its own database, communicating over REST behind a
single secured entry point.

[Architecture diagram] · [Quick start] · [API examples] · [Tests]
