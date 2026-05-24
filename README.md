# Personal Finance Manager API

A production-ready REST API for personal finance management built with **Java 17**, **Spring Boot 3**, **Spring Security** (session-based), **Spring Data JPA**, and **MySQL**.

## Features

- 🔐 Session-based authentication with HttpOnly cookies
- 💸 Transaction management with category-based filtering
- 📂 System & custom categories
- 🎯 Savings goals with live progress tracking
- 📊 Monthly & yearly financial reports
- ✅ Bean validation on all inputs
- 🛡️ Complete user data isolation
- 📖 Swagger UI documentation

---

## Tech Stack

| Component       | Technology                |
|-----------------|---------------------------|
| Language        | Java 17                   |
| Framework       | Spring Boot 3.2.5         |
| Security        | Spring Security 6         |
| Database        | MySQL 8                   |
| ORM             | Spring Data JPA/Hibernate |
| Build Tool      | Maven                     |
| Documentation   | SpringDoc OpenAPI 2.3     |
| Testing         | JUnit 5 + Mockito         |
| Coverage        | JaCoCo (≥80%)             |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

---

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/finance-manager.git
cd finance-manager
```

### 2. Create the MySQL database

```sql
CREATE DATABASE finance_manager;
```

### 3. Configure `application.properties`

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance_manager?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 4. Build and run

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

The API will be available at: **http://localhost:8080**

### 5. Swagger UI

Open **http://localhost:8080/swagger-ui.html** in your browser.

---

## Running Tests

```bash
# Run all tests
mvn test

# Run tests + generate JaCoCo coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## API Documentation

### Base URL
`http://localhost:8080/api`

### Authentication Flow

1. **Register** → `POST /api/auth/register`
2. **Login** → `POST /api/auth/login` — receives `JSESSIONID` cookie
3. **Use cookie** → include cookie in all subsequent requests
4. **Logout** → `POST /api/auth/logout` — invalidates session

---

### 1. Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```
**Response 201:**
```json
{ "message": "User registered successfully", "userId": 1 }
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "user@example.com", "password": "password123" }
```
**Response 200:**
```json
{ "message": "Login successful" }
```
Sets cookie: `JSESSIONID=<session-id>; HttpOnly`

#### Logout
```http
POST /api/auth/logout
Cookie: JSESSIONID=<session-id>
```
**Response 200:**
```json
{ "message": "Logout successful" }
```

---

### 2. Categories

#### Get All Categories
```http
GET /api/categories
Cookie: JSESSIONID=<session-id>
```
**Response 200:**
```json
{
  "categories": [
    { "name": "Salary", "type": "INCOME", "isCustom": false },
    { "name": "Food", "type": "EXPENSE", "isCustom": false },
    { "name": "MyBusiness", "type": "INCOME", "isCustom": true }
  ]
}
```

#### Create Custom Category
```http
POST /api/categories
Cookie: JSESSIONID=<session-id>
Content-Type: application/json

{ "name": "SideBusinessIncome", "type": "INCOME" }
```
**Response 201:**
```json
{ "name": "SideBusinessIncome", "type": "INCOME", "isCustom": true }
```

#### Delete Custom Category
```http
DELETE /api/categories/{name}
Cookie: JSESSIONID=<session-id>
```
**Response 200:**
```json
{ "message": "Category deleted successfully" }
```

---

### 3. Transactions

#### Create Transaction
```http
POST /api/transactions
Cookie: JSESSIONID=<session-id>
Content-Type: application/json

{
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}
```
**Response 201:**
```json
{
  "id": 1,
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary",
  "type": "INCOME"
}
```

#### Get Transactions (with filters)
```http
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryId=1
Cookie: JSESSIONID=<session-id>
```

#### Update Transaction
```http
PUT /api/transactions/{id}
Cookie: JSESSIONID=<session-id>
Content-Type: application/json

{ "amount": 60000.00, "description": "Updated Salary" }
```
> ⚠️ Date field cannot be updated — it is ignored even if sent.

#### Delete Transaction
```http
DELETE /api/transactions/{id}
Cookie: JSESSIONID=<session-id>
```

---

### 4. Savings Goals

#### Create Goal
```http
POST /api/goals
Cookie: JSESSIONID=<session-id>
Content-Type: application/json

{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-01-01",
  "startDate": "2025-01-01"
}
```
**Response 201:**
```json
{
  "id": 1,
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-01-01",
  "startDate": "2025-01-01",
  "currentProgress": 1000.00,
  "progressPercentage": 20.0,
  "remainingAmount": 4000.00
}
```

#### Get All Goals
```http
GET /api/goals
Cookie: JSESSIONID=<session-id>
```

#### Get Goal by ID
```http
GET /api/goals/{id}
Cookie: JSESSIONID=<session-id>
```

#### Update Goal
```http
PUT /api/goals/{id}
Cookie: JSESSIONID=<session-id>
Content-Type: application/json

{ "targetAmount": 6000.00, "targetDate": "2026-06-01" }
```

#### Delete Goal
```http
DELETE /api/goals/{id}
Cookie: JSESSIONID=<session-id>
```

---

### 5. Reports

#### Monthly Report
```http
GET /api/reports/monthly/2024/1
Cookie: JSESSIONID=<session-id>
```
**Response 200:**
```json
{
  "month": 1,
  "year": 2024,
  "totalIncome": { "Salary": 3000.00 },
  "totalExpenses": { "Food": 400.00, "Rent": 1200.00 },
  "netSavings": 1400.00
}
```

#### Yearly Report
```http
GET /api/reports/yearly/2024
Cookie: JSESSIONID=<session-id>
```

---

## Error Responses

All errors return JSON:
```json
{ "message": "Error description" }
```
Validation errors:
```json
{
  "message": "Validation failed",
  "errors": { "fieldName": "error message" }
}
```

| Status | Meaning |
|--------|---------|
| 400 | Validation failed / Bad request |
| 401 | Not authenticated or bad credentials |
| 403 | Cannot access other user's resource |
| 404 | Resource not found |
| 409 | Conflict (duplicate email, category name) |

---

## Deployment on Render

### 1. Create a MySQL database on Railway or PlanetScale (free tier)

Get your database URL in format:
```
jdbc:mysql://<host>:<port>/<dbname>?useSSL=true&serverTimezone=UTC
```

### 2. Push code to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/<username>/finance-manager.git
git push -u origin main
```

### 3. Create a Web Service on Render

1. Go to [render.com](https://render.com) and create a new **Web Service**
2. Connect your GitHub repo
3. Set:
   - **Runtime**: Java
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/finance-manager-1.0.0.jar`

### 4. Add Environment Variables on Render

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `render` |
| `DATABASE_URL` | `jdbc:mysql://...` |
| `DATABASE_USERNAME` | your DB username |
| `DATABASE_PASSWORD` | your DB password |

### 5. Run the test script

```bash
bash financial_manager_tests.sh https://your-app.onrender.com/api
```

---

## Default Categories

These system categories are pre-seeded and cannot be deleted:

| Name | Type |
|------|------|
| Salary | INCOME |
| Food | EXPENSE |
| Rent | EXPENSE |
| Transportation | EXPENSE |
| Entertainment | EXPENSE |
| Healthcare | EXPENSE |
| Utilities | EXPENSE |

---

## Design Decisions

1. **Session-based auth** — used per spec requirement. JSESSIONID cookie is HttpOnly and SameSite=Strict.
2. **Soft delete for transactions** — sets `is_deleted=true` so goals/reports automatically exclude deleted data without losing history.
3. **Category by name in transactions** — the spec uses `"category": "Salary"` (name string), not ID. Service resolves name → entity.
4. **Live progress for goals** — progress is computed on every read from current transaction data (not cached), ensuring accuracy.
5. **Constructor injection** — all services use `@RequiredArgsConstructor` (Lombok) for constructor injection, following SOLID principles.

---

## Project Structure

```
src/main/java/com/syfe/financemanager/
├── config/          SecurityConfig, OpenApiConfig, DataInitializer
├── controller/      AuthController, TransactionController, CategoryController,
│                    SavingsGoalController, ReportController
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, TransactionRequest,
│   │                UpdateTransactionRequest, CategoryRequest,
│   │                SavingsGoalRequest, UpdateGoalRequest
│   └── response/    RegisterResponse, MessageResponse, TransactionResponse,
│                    TransactionListResponse, CategoryResponse, CategoryListResponse,
│                    SavingsGoalResponse, GoalListResponse,
│                    MonthlyReportResponse, YearlyReportResponse
├── entity/          User, Category, Transaction, SavingsGoal
├── enums/           TransactionType
├── exception/       GlobalExceptionHandler, ResourceNotFoundException,
│                    ForbiddenException, ConflictException, BadRequestException
├── repository/      UserRepository, CategoryRepository,
│                    TransactionRepository, SavingsGoalRepository
├── security/        CustomUserDetailsService
├── service/         AuthService, CategoryService, TransactionService,
│                    SavingsGoalService, ReportService
└── util/            SecurityUtils
```
