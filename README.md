# TalentTrade - Skill Exchange Platform Backend

TalentTrade is a peer-to-peer skill exchange platform that enables users to teach skills they possess and learn skills they want from other users through mutual skill exchanges rather than monetary transactions.

This repository hosts the production-ready Spring Boot backend for the platform.

---

## 🛠️ Technology Stack

- **Core Framework**: Java 21 & Spring Boot 3.3.2
- **Database**: PostgreSQL (Relational Database)
- **Object-Relational Mapping**: Spring Data JPA & Hibernate
- **Security**: Spring Security & JWT (JSON Web Tokens)
- **JSON Parser/Signing**: JJWT (`0.11.5`)
- **Documentation**: OpenAPI Spec / Swagger UI
- **Build Tool**: Maven

---

## 📂 Project Structure

```text
com.talenttrade
├── config          # Global configurations (e.g., Swagger)
├── security        # JWT Services, security configurations, and filters
├── controller      # REST Controllers (Auth, Users)
├── service         # Business logic layer
├── repository      # Database persistence interfaces
├── entity          # JPA Entities (User)
├── dto             # Request and Response Data Transfer Objects
├── exception       # Custom exceptions and Global Exception Handler
└── TalentTradeApplication.java # Spring Boot Main Class
```

---

## ⚙️ Prerequisites

- **Java Development Kit (JDK)**: Version 21 (Temurin JDK 21+ recommended)
- **Apache Maven**: Version 3.9+
- **PostgreSQL**: Running instance on port `5432`

---

## 🚀 Getting Started

### 1. Database Setup
Ensure PostgreSQL is running, then log in and create the database:
```sql
CREATE DATABASE talenttrade;
```

### 2. Configure Settings
By default, the application is configured in `src/main/resources/application.properties` to connect to PostgreSQL on port `5432` with username `postgres` and password `kolli`. Modify these as needed:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/talenttrade
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build the Project
Compile the files and build the target outputs:
```bash
mvn clean compile
```

### 4. Run the Application
Run the Spring Boot development server:
```bash
mvn spring-boot:run
```
The server will boot up and bind to port `8080`.

---

## 🔑 Authentication & Day 1 REST APIs

All application endpoints are documented using Swagger OpenAPI and are fully interactive.

### Public Routes
- **Register**: `POST /api/auth/register` — Creates a new user profile with verified email and username uniqueness.
- **Login**: `POST /api/auth/login` — Authenticates credentials and returns a secure JWT bearer token.

### Protected Routes (Requires Bearer Token)
- **Get Profile**: `GET /api/users/me` — Fetches current user profile.
- **Update Profile**: `PUT /api/users/me` — Modifies user bio, location, fullName, and username.

---

## 📖 API Documentation & Testing

### Swagger UI
After starting the server, open your browser and navigate to:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

To test authenticated endpoints in Swagger:
1. Make a request to the `POST /api/auth/login` endpoint.
2. Copy the token string from the JSON response body.
3. Click the **Authorize** lock icon at the top of the Swagger page.
4. Input `Bearer <your_copied_token>` or just paste the raw token depending on the prompt, and select Authorize.
5. You can now execute protected requests successfully!

### Global Exception Responses
Errors are standardized across all controllers:
```json
{
  "success": false,
  "message": "Validation failed: Username is required, Email already exists",
  "timestamp": "2026-06-23T22:31:52.292"
}
```
