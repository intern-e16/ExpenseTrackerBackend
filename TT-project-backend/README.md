# Expense Tracker: Backend

Spring Boot REST API with JPA persistence (H2 file database).
The web UI lives in the separate **TT-project-frontend** repository.

## Run
Requires JDK 17+. The Maven wrapper is included, so Maven is not required.

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
./mvnw test                   # run the tests
./mvnw clean package          # build target/expense-tracker-1.0.0.jar
java -jar target/expense-tracker-1.0.0.jar
```
The API runs on http://localhost:8080. Demo data is seeded on first run. Data is stored in `./data`.

## REST API
| Method | Endpoint                   | Description                              |
|--------|----------------------------|------------------------------------------|
| GET    | `/api/expenses`            | List (optional `category`, `from`, `to`) |
| GET    | `/api/expenses/summary`    | Total, count, per-category totals (same filters) |
| GET    | `/api/expenses/categories` | Available categories                     |
| GET    | `/api/expenses/{id}`       | Get one                                  |
| POST   | `/api/expenses`            | Create (201 + Location header)           |
| PUT    | `/api/expenses/{id}`       | Update                                   |
| DELETE | `/api/expenses/{id}`       | Delete (204)                             |

Dates use `yyyy-MM-dd`. Example:

```bash
curl -X POST localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Lunch","amount":12.50,"category":"FOOD","date":"2026-10-12","notes":"Cafe"}'

curl "localhost:8080/api/expenses?category=FOOD&from=2026-10-01"
```

## Configuration
| Setting | Env var | Default |
|---------|---------|---------|
| `app.cors.allowed-origins` | `APP_CORS_ALLOWED_ORIGINS` | `localhost` / `127.0.0.1` on ports 5500, 3000, 8000 |
| `spring.h2.console.enabled` | `SPRING_H2_CONSOLE_ENABLED` | `false` |
| `server.port` | `SERVER_PORT` | `8080` |

When you host the frontend somewhere else, add its origin to `APP_CORS_ALLOWED_ORIGINS`.

## Frontend
Clone `TT-project-frontend`, serve it (e.g. `python3 -m http.server 8000`) and open http://localhost:8000.

## Structure
```
src/main/java/com/example/expensetracker/
  model/        Expense (entity), Category (enum)
  dto/          ExpenseRequest (validated input), ExpenseSummary
  repository/   ExpenseRepository (Spring Data JPA + Specifications)
  service/      ExpenseService (business logic)
  controller/   ExpenseController (REST endpoints)
  exception/    ResourceNotFoundException, GlobalExceptionHandler
  config/       CorsConfig, DataSeeder (demo data, disabled in tests)
src/test/       ExpenseControllerTest
```

## Using another database
Swap the H2 dependency in `pom.xml` for MySQL or PostgreSQL and update the `spring.datasource.*` properties.
Keep passwords in environment variables, not in the repository.
