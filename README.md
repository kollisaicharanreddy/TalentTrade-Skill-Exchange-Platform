# TalentTrade - Skill Exchange Platform Backend

TalentTrade is a production-ready, peer-to-peer skill exchange platform backend built on Spring Boot. It empowers users to trade skills they possess (TEACH) for skills they want to acquire (LEARN) through a non-monetary matching engine, exchange requests, virtual scheduling, session feedback, in-app notifications, and secure real-time messaging using WebSockets.

---

## 🏗️ High-Level Architecture

![TalentTrade Backend Architecture](./TalentTrade%20Backend%20Architecture.png)

---

## 🛠️ Technology Stack

- **Core Framework**: Java 21 & Spring Boot 3.3.2
- **Database**: PostgreSQL (Relational Database)
- **Object-Relational Mapping**: Spring Data JPA & Hibernate
- **Security**: Spring Security & JWT (JSON Web Tokens)
- **JSON Parser/Signing**: JJWT (`0.11.5`)
- **Real-Time Communication**: Spring WebSocket, STOMP Broker, SockJS
- **Documentation**: OpenAPI Spec / Swagger UI (`springdoc-openapi`)
- **Build Tool**: Maven
- **Lombok**: Metadata generation & clean boilerplate reduction

---

## 📂 Project Structure

```text
com.talenttrade
├── config          # Global configurations (Swagger, WebSockets)
├── controller      # REST Controllers & WebSocket Message Handlers
├── service         # Business logic layer
├── repository      # Database persistence interfaces
├── entity          # JPA Entities (User, Skill, UserSkill, Match, ExchangeRequest, Session, Review, Notification, ChatMessage)
├── dto             # Request and Response Data Transfer Objects (including standardized ApiResponse wrapper)
├── security        # JWT Services, security configurations, and filters
└── exception       # Custom exceptions and Global Exception Handler
```

---

## 🗄️ Database Schema

The backend uses PostgreSQL with automatic Hibernate DDL update. The logical schema is structured as follows:

```sql
-- 1. users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    bio VARCHAR(1000),
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 2. skills Table
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255) NOT NULL,
    description VARCHAR(1000)
);

-- 3. user_skills Table
CREATE TABLE user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- 'TEACH', 'LEARN'
    level VARCHAR(50) NOT NULL, -- 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'
    CONSTRAINT uq_user_skill_type UNIQUE (user_id, skill_id, type)
);

-- 4. matches Table
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    match_score INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_match_pair UNIQUE (user1_id, user2_id)
);

-- 5. exchange_requests Table
CREATE TABLE exchange_requests (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message VARCHAR(1000),
    status VARCHAR(50) NOT NULL, -- 'PENDING', 'ACCEPTED', 'REJECTED'
    created_at TIMESTAMP NOT NULL
);

-- 6. sessions Table
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    exchange_request_id BIGINT NOT NULL UNIQUE REFERENCES exchange_requests(id) ON DELETE CASCADE,
    mentor_id BIGINT NOT NULL REFERENCES users(id),
    learner_id BIGINT NOT NULL REFERENCES users(id),
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    meeting_link VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'SCHEDULED', 'COMPLETED', 'CANCELLED'
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 7. reviews Table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    reviewee_id BIGINT NOT NULL REFERENCES users(id),
    rating INT NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_session_reviewer UNIQUE (session_id, reviewer_id)
);

-- 8. notifications Table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'REQUEST_RECEIVED', 'REQUEST_ACCEPTED', etc.
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

-- 9. chat_messages Table
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message VARCHAR(2000) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE
);
```

---

## ✨ Features

### 1. Security & Authentication
- **Secure Auth**: Custom user registration and login endpoints utilizing **Spring Security** and stateless **JWT Tokens**.
- **Role Control**: Endpoints secured under `ROLE_USER` hierarchy using filter interceptors.

### 2. Skill Management
- **Universal Skill Registry**: Directory containing categorized skills (e.g., Programming, Language, Design).
- **User Profile Association**: Users attach skills they wish to teach or learn, along with levels (`BEGINNER` to `EXPERT`).

### 3. Mutual Matching Engine
- **Reciprocal Matches**: Finds users who teach what you want to learn, and learn what you teach.
- **Score Calculation**: Automatically calculates matching scores and creates match profiles.

### 4. Exchange Requests
- **Structured Handshakes**: Users dispatch requests to match candidates. Duplicates are strictly prevented.
- **State Flow**: Requests transition from `PENDING` to `ACCEPTED` or `REJECTED`.

### 5. Session Scheduler
- **Meeting Organization**: Accepted requests can generate unique training sessions.
- **Overlap Conflict Checks**: Interceptor logic checks user calendar constraints to prevent conflicting session overlaps.

### 6. Ratings & Feedback
- **Quality Control**: Completed sessions can be reviewed and rated (1-5 stars).
- **Security Check**: Self-reviews and duplicate reviews are strictly barred.

### 7. Real-Time Chat (WebSocket)
- **STOMP Broker**: Instant message routing via WebSocket channel subscriptions.
- **Subscription Route**: `/topic/chat/{conversationId}` (where `conversationId` is `minUserId_maxUserId`).
- **Connection Handshake**: `/ws` authenticated securely using JWT tokens in STOMP headers.
- **Communication Rules**: Users can only exchange messages if they have an `ACCEPTED` exchange request OR an active scheduled session.

### 8. API Standardization, Pagination & Sorting
- **Unified Schema**: Every controller returns the standardized `ApiResponse<T>` structure:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-06-26T10:39:47"
}
```
- **Sorting Support**: Listing resources (Users, Skills, Matches, Requests, Sessions, Reviews, Notifications, Chat History) support parameters: `page`, `size`, `sortBy`, and `direction`.

---

## 🚀 How to Run

### 1. PostgreSQL Setup
Configure a running PostgreSQL service on port `5432` and run:
```sql
CREATE DATABASE talenttrade;
```

### 2. Configure Settings
Modify credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/talenttrade
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build & Compile
Execute maven package lifecycle verification:
```bash
mvn clean compile
```

### 4. Run the Dev Server
```bash
mvn spring-boot:run
```
The application will boot up at `http://localhost:8080`.

---

## 📖 Swagger API Documentation
Open Swagger UI to interact with all API endpoints:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

To test secured endpoints:
1. Call `POST /api/auth/login`.
2. Copy the returned token.
3. Click the green **Authorize** padlock in Swagger, enter `Bearer <your_token>`, and confirm.

---

## 🛠️ Future Enhancements
- **Google OAuth**: Integrate SSO login for user onboarding.
- **Google Calendar & Meet**: Auto-schedule meetings and generate Google Meet invites.
- **AI Skill Recommendations**: Use LLMs to match users based on bio semantic analysis.
- **Redis Cache**: Speed up dashboards and matching engines with cache eviction.
- **Docker & CI/CD**: Provide build images and configure pipeline automation.
