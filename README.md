# Personal Finance Manager API

[![Build Status](https://img.shields.io/badge/Build-Success-success.svg)](#)
[![Tests Coverage](https://img.shields.io/badge/Coverage-%E2%89%A580%25-green.svg)](#)
[![Deployment](https://img.shields.io/badge/Deployed-Render-brightgreen.svg)](https://financemanager-n4i0.onrender.com/swagger-ui.html)
[![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20PostgreSQL-blue.svg)](#)

A production-quality REST API backend for personal finance management. Engineered with **Java 21**, **Spring Boot 3.2.5**, **Spring Security** (session-based data isolation), **Spring Data JPA**, and support for both **MySQL** and **PostgreSQL** databases.

---

## Live Production Deployment

The backend service is fully deployed and configured on Render, backed by a production-grade PostgreSQL database on Supabase:

*   **Production API URL**: `https://financemanager-n4i0.onrender.com/api`
*   **Interactive Swagger Documentation**: `https://financemanager-n4i0.onrender.com/swagger-ui.html`

You can validate the live service at any time by running:
```bash
./financial_manager_tests.sh https://financemanager-n4i0.onrender.com/api
```

---

## Features

- **Session-Based Authentication**: Secure authentication using HttpOnly, SameSite cookies.
- **Strict Data Isolation**: Strict user-level resource security prevents cross-tenant access.
- **Transaction Management**: Streamlined income/expense entry with category filtering.
- **Flexible Category System**: Includes pre-seeded system categories and custom user-defined categories.
- **Savings Goals tracking**: Live progress tracking and dynamic remaining-amount computation.
- **Financial Reporting**: Complete monthly and yearly report generation (net savings, category breakdowns).
- **Robust Validation**: Extensive Java Bean Validation constraints on all endpoints.

---

## Tech Stack

| Component         | Technology                  | Details                                         |
| :---------------- | :-------------------------- | :---------------------------------------------- |
| **Language**      | Java 21                     | Modern JDK features and runtime                 |
| **Framework**     | Spring Boot 3.2.5           | Autoconfigured web ecosystem                    |
| **Security**      | Spring Security 6           | Session fixation protection, cookie management |
| **Database**      | MySQL & PostgreSQL          | Dev: MySQL / Production: Supabase PostgreSQL   |
| **ORM / JPA**     | Spring Data JPA / Hibernate | Agnostic driver and dialect resolution          |
| **Documentation** | SpringDoc OpenAPI 2.3       | Automated Swagger UI and API Docs               |
| **Testing**       | JUnit 5 + Mockito + E2E     | Unit tests, integration tests, E2E bash suite   |

---

## Local Setup & Development

### 1. Clone the repository
```bash
git clone https://github.com/dixitshubham93/FinanceManager.git
cd FinanceManager
```

### 2. Configure Environment Variables
Create or edit your `.env` file in the root directory:
```properties
# MySQL Local Development Database
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=finance_manager

# Supabase Production Database (PostgreSQL)
DATABASE_URL=jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres
DATABASE_USERNAME=postgres.foarzorcvvosqpjkzgqa
DATABASE_PASSWORD=shubham@123

PORT=8081
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### 3. Run Locally (MySQL)
By default, running the application locally will connect to MySQL:
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```
The local API will start on **http://localhost:8080** (Swagger: http://localhost:8080/swagger-ui.html).

### 4. Run Locally with Supabase PostgreSQL
To run locally but connected to your Supabase cloud database:
```powershell
.\run_with_supabase.ps1
```
This loads your `.env` variables and activates the `render` profile which switches Hibernate to PostgreSQL mode.

### 5. Run via Docker Compose
To build and start both the app container and the local database:
```bash
docker compose up --build -d
```

---

## Testing

### 1. Unit & Integration Tests (JUnit 5)
Run the test suite with coverage report:
```bash
mvn test jacoco:report
```
*Coverage reports will be generated under `target/site/jacoco/index.html`.*

### 2. End-to-End API Test Suite
We provide a comprehensive E2E validation script (`financial_manager_tests.sh`) that tests all 86 API requirements.

- **Test Local (MySQL)**:
  ```bash
  ./financial_manager_tests.sh http://localhost:8080/api
  ```
- **Test Local Docker (Supabase)**:
  ```bash
  ./financial_manager_tests.sh http://localhost:8081/api
  ```
- **Test Live Production**:
  ```bash
  ./financial_manager_tests.sh https://financemanager-n4i0.onrender.com/api
  ```

---

## API Reference

### Authentication

#### **Register**
- **Method/Route**: `POST /api/auth/register`
- **Request Body**:
  ```json
  {
    "username": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  { "message": "User registered successfully", "userId": 1 }
  ```

#### **Login**
- **Method/Route**: `POST /api/auth/login`
- **Request Body**:
  ```json
  { "username": "user@example.com", "password": "password123" }
  ```
- **Success Response (200 OK)**:
  ```json
  { "message": "Login successful" }
  ```
  *Sets HTTP cookie: `JSESSIONID=<session-id>; HttpOnly`*

#### **Logout**
- **Method/Route**: `POST /api/auth/logout`
- **Success Response (200 OK)**:
  ```json
  { "message": "Logout successful" }
  ```

---

### Categories

#### **Get All Categories**
- **Method/Route**: `GET /api/categories`
- **Success Response (200 OK)**:
  ```json
  {
    "categories": [
      { "name": "Salary", "type": "INCOME", "isCustom": false },
      { "name": "Food", "type": "EXPENSE", "isCustom": false },
      { "name": "Rent", "type": "EXPENSE", "isCustom": false },
      { "name": "Freelance", "type": "INCOME", "isCustom": true }
    ]
  }
  ```

#### **Create Custom Category**
- **Method/Route**: `POST /api/categories`
- **Request Body**:
  ```json
  { "name": "Investments", "type": "INCOME" }
  ```
- **Success Response (201 Created)**:
  ```json
  { "name": "Investments", "type": "INCOME", "isCustom": true }
  ```

#### **Delete Custom Category**
- **Method/Route**: `DELETE /api/categories/{name}`
- **Success Response (200 OK)**:
  ```json
  { "message": "Category deleted successfully" }
  ```

---

### Transactions

#### **Create Transaction**
- **Method/Route**: `POST /api/transactions`
- **Request Body**:
  ```json
  {
    "amount": 5500.00,
    "date": "2024-01-15",
    "category": "Salary",
    "description": "January Main Salary"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "id": 1,
    "amount": 5500.00,
    "date": "2024-01-15",
    "category": "Salary",
    "description": "January Main Salary",
    "type": "INCOME"
  }
  ```

#### **Get Transactions (with filters)**
- **Method/Route**: `GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&category=Salary`
- **Success Response (200 OK)**:
  ```json
  {
    "transactions": [
      {
        "id": 1,
        "amount": 5500.00,
        "date": "2024-01-15",
        "category": "Salary",
        "description": "January Main Salary",
        "type": "INCOME"
      }
    ]
  }
  ```

#### **Update Transaction**
- **Method/Route**: `PUT /api/transactions/{id}`
- **Request Body**:
  ```json
  { "amount": 6000.00, "description": "Updated Salary amount" }
  ```
  > *Note: The transaction date is fixed on creation and cannot be edited. Date properties sent to PUT are silently ignored.*

#### **Delete Transaction**
- **Method/Route**: `DELETE /api/transactions/{id}`
- **Success Response (200 OK)**:
  ```json
  { "message": "Transaction deleted successfully" }
  ```

---

### Savings Goals

#### **Create Goal**
- **Method/Route**: `POST /api/goals`
- **Request Body**:
  ```json
  {
    "goalName": "Emergency Fund",
    "targetAmount": 15000.00,
    "targetDate": "2028-01-01",
    "startDate": "2024-01-01"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "id": 1,
    "goalName": "Emergency Fund",
    "targetAmount": 15000.00,
    "targetDate": "2028-01-01",
    "startDate": "2024-01-01",
    "currentProgress": 5500.00,
    "progressPercentage": 36.7,
    "remainingAmount": 9500.00
  }
  ```

#### **Get Goal Progress**
- **Method/Route**: `GET /api/goals/{id}`
- **Success Response (200 OK)**:
  *Returns current progress calculated in real-time from active income transactions.*

---

### Reports

#### **Monthly Report**
- **Method/Route**: `GET /api/reports/monthly/{year}/{month}` (e.g. `GET /api/reports/monthly/2024/1`)
- **Success Response (200 OK)**:
  ```json
  {
    "month": 1,
    "year": 2024,
    "totalIncome": { "Salary": 5500.00 },
    "totalExpenses": { "Food": 450.00 },
    "netSavings": 5050.00
  }
  ```

#### **Yearly Report**
- **Method/Route**: `GET /api/reports/yearly/{year}` (e.g. `GET /api/reports/yearly/2024`)

---

## Design Decisions

1. **Clean Architecture / SQL Agnostic Engine**: The persistence engine is decoupled from database-specific dialects. Hibernate dynamically detects whether MySQL or PostgreSQL is used based on JDBC connection metadata.
2. **Wildcard CORS Policy**: Setup with pattern matching support for clean cross-origin frontend integrations.
3. **Session Fixation Defense**: Generates a new session ID upon successful authentication, preventing session hijacking attacks.
4. **Validation-Driven Error Handling**: Out-of-bounds inputs return exact, detailed field validation error maps to the client.
