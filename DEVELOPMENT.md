# Development

This document covers everything needed to build and run Steam Widget locally.

---

## Tech Stack

- **Java 21**
- **Spring Boot** (Web, Data JPA, Thymeleaf, Actuator)
- **PostgreSQL**
- **Maven** (via the included `mvnw` wrapper)

---

## Prerequisites

- JDK 21
- A running PostgreSQL instance
- A [Steam Web API key](https://steamcommunity.com/dev/apikey)

---

## Configuration

The app reads its configuration from `src/main/resources/application.properties`. At minimum you need to set:

```properties
# JDBC URL for your local Postgres instance
spring.datasource.url=jdbc:postgresql://localhost:5432/steamwidget

# Your Steam Web API key
steam.api.key=YOUR_STEAM_API_KEY
```

Rather than editing `application.properties` directly, prefer overriding these via environment variables or `-D` system properties so local secrets never get committed:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/steamwidget
export STEAM_API_KEY=YOUR_STEAM_API_KEY
```

Other tunable settings (with defaults):

| Property | Default | Description |
|---|---|---|
| `hit.ip.retention.days` | `90` | Days after which IP addresses in hit records are nullified. |
| `hit.raw.retention.days` | `730` | Days after which entire raw hit records are deleted. Must be greater than `hit.ip.retention.days`. |

The database schema is managed by Hibernate/JPA against the entities in `src/main/java/codes/sharky/steamwidget/entity`; no separate migration step is required for local development.

---

## Running Locally

Build and run with the Maven wrapper — no local Maven install required:

```bash
./mvnw spring-boot:run
```

Or package and run the jar directly:

```bash
./mvnw clean package
java -jar target/steamwidget-*.jar
```

The app starts on `http://localhost:8080` by default. The static frontend pages (`index.html`, `tracking.html`, `playing-stats.html`, `profile-metrics.html`) are served from `src/main/resources/static`, and the JSON/image API described in [API.md](API.md) is available under `/widget`, `/api`, and `/metric`.

---

## Running Tests

```bash
./mvnw test
```

---

## Project Layout

```
src/main/java/codes/sharky/steamwidget/
├── controller/   # REST & web endpoints (widget, tracking, metrics, OpenID login)
├── entity/       # JPA entities and composite IDs
├── repository/   # Spring Data repositories
├── component/    # Steam Web API client, Steam OpenID integration
├── config/       # Spring configuration (async, Steam API client)
├── model/        # Enums/DTOs shared across the app
└── scheduled/    # Scheduled jobs (caching, retention cleanup)

src/main/resources/
├── static/       # Frontend pages served as-is
├── templates/    # Thymeleaf templates
└── application.properties
```
