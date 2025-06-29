# Spring Boot JWE Auth JPA Demo

## Overview

A secure demo application showcasing:

* **Spring Boot 3** backend with
  – JWT-secured REST endpoints
  – JWE (JSON Web Encryption) for payload confidentiality
  – Spring Data JPA (Hibernate + PostgreSQL/H2)
  – Spring Security OAuth2 Resource Server
  – HATEOAS support
* **Thymeleaf**-powered admin UI for managing users, authorities & messages

## Features

* **User registration & login** with JWE-encrypted tokens
* **Role-based access control** (admin vs. regular user)
* **HATEOAS** links in all GET/POST/PATCH/DELETE responses
* **Avatar upload** with ETag / caching support
* **MFA (TOTP)** enable/disable/verify flows
* **Rate limiting** via Bucket4j + Redis
* **I18n** support backed by database-driven messages

## Prerequisites

* Java 21
* Maven 3.x
* (Optional) GraalVM + `native-image` for native build

## Getting Started

### 1. Clone & Build

```bash
git clone https://github.com/susimsek/spring-boot-jwe-auth-jpa-demo.git
cd spring-boot-jwe-auth-jpa-demo
mvn clean install
```

### 2. Run Locally (H2)

```bash
mvn spring-boot:run
```

* **API** base: `http://localhost:8080/api/`
* **UI**: `http://localhost:8080`

### 3. H2 Console

* URL: `http://localhost:8080/h2-console`
* JDBC URL: `jdbc:h2:mem:demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`
* User: `admin` / Pass: `root`

## Native Image (GraalVM)

```bash
./mvnw package -Pnative,prod -DskipTests
./target/spring-boot-jwe-auth-jpa-demo
```

## API Documentation

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```
