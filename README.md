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

You can run this application either using **Docker Compose (Recommended)** or **Locally**.

### Method 1: Docker Compose (Recommended)
This approach launches the complete backend and frontend stack (Spring Boot application, PostgreSQL database, and React/Vite frontend) in isolated containers with zero manual configuration. 

#### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

#### One-Command Startup
Clone the repository, navigate to the root directory, and execute:
```bash
docker compose up --build
```
This single command will:
1. Create an isolated Docker bridge network.
2. Build the multi-stage Spring Boot backend Docker image.
3. Start the PostgreSQL database container and check its health.
4. Build the multi-stage React/Vite frontend Docker image.
5. Create the `talenttrade` database automatically.
6. Boot the Spring Boot application and auto-generate/update database tables using Hibernate DDL.
7. Launch the Nginx web server, exposing the user interface at **`http://localhost`** (port 80).
8. Automatically proxy REST and WebSocket traffic from the frontend to the backend container.

---

### Method 2: Manual Local Running
If you prefer running the application outside of Docker:
1. **PostgreSQL Setup**: Ensure PostgreSQL is running on port `5432` and create a database:
   ```sql
   CREATE DATABASE talenttrade;
   ```
2. **Settings**: Modify database credentials in `src/main/resources/application.properties` or set them as environment variables.
3. **Run Backend**:
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```
4. **Run Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 🐳 Docker Architecture & Design Decisions

### 1. Multi-Stage Dockerfiles
We utilize **multi-stage builds** for both the backend and frontend to optimize performance, file size, and security:
* **Spring Boot (Backend)**: 
  * **Build Stage**: Uses a full Maven JDK image (`maven:3.9.6-eclipse-temurin-21-alpine`) to package the application. Dependencies are cached using layer caching via `mvn dependency:go-offline`.
  * **Runtime Stage**: Uses a lightweight JRE Alpine image (`eclipse-temurin:21-jre-alpine`) executing under a custom non-root system user (`spring`) for security.
* **React/Vite (Frontend)**:
  * **Build Stage**: Uses a Node Alpine image (`node:20-alpine`) to install dependencies and run production compilation (`npm run build`).
  * **Runtime Stage**: Uses a lightweight Nginx web server (`nginx:1.25-alpine`) to serve the static assets.

### 2. Nginx Web Server & Reverse Proxying
In the frontend container, Nginx acts as:
* **Static File Server**: Delivers React assets and utilizes `try_files` to redirect non-file requests to `index.html`, allowing React Router client-side routing to work seamlessly.
* **Reverse Proxy**: Proxies `/api` traffic internally to the Java backend container (`http://app:8080/api`) and manages the `/ws` route to pass WebSocket connections with appropriate upgrade headers. This eliminates Cross-Origin Resource Sharing (CORS) issues in production!

### 3. Isolated Container Networking
All services reside within a custom Docker bridge network (`talenttrade-network`):
* The backend connects to the database via `jdbc:postgresql://db:5432/talenttrade` (using the container hostname `db`).
* The frontend Nginx container proxies API calls internally to `http://app:8080` (using the container hostname `app`).
* Port mapping exposes port `80` (HTTP) for the frontend container and `8080` (API/Swagger) for the backend container on the host machine.

### 4. Database Persistence
We define a named Docker volume (`postgres_data`) mapped to `/var/lib/postgresql/data` in the database container. This ensures that even if you tear down, rebuild, or restart the containers, your user accounts, matching tables, and chat histories persist safely on your host machine.

### 5. Container Startup Synchronization (Health Checks)
To avoid race conditions where Spring Boot starts up and attempts to connect before PostgreSQL has initialized, we configured a container health check:
* The database uses `pg_isready` to verify readiness.
* The Spring Boot app service is marked with `depends_on` containing the `condition: service_healthy` modifier.
* The application container startup is blocked until the PostgreSQL health check reports green.

### 6. Seamless Database and Schema Creation
* **Database Creation**: The official PostgreSQL image automatically spawns a database with the name specified in the `POSTGRES_DB` environment variable on startup.
* **Schema Generation**: Hibernate is configured via `spring.jpa.hibernate.ddl-auto=update` in `application.properties`. When Spring Boot boots up, Hibernate scans the `@Entity` classes (such as `User`, `ExchangeRequest`, `ChatMessage`, etc.) and automatically creates or updates the tables inside the database.

---

## ⚙️ Docker Lifecycle Command Catalog

Below are all the commands needed to manage the container lifecycles:

| Operation | Command |
| :--- | :--- |
| **Build & Start Containers** (Foreground) | `docker compose up --build` |
| **Start Containers** (Background / Detached) | `docker compose up -d` |
| **Stop Containers** (Gracefully) | `docker compose stop` |
| **Restart Containers** | `docker compose restart` |
| **View Combined Container Logs** | `docker compose logs -f` |
| **View Backend App Logs Only** | `docker compose logs -f app` |
| **View Frontend Container Logs Only** | `docker compose logs -f frontend` |
| **Access PostgreSQL Command Line (CLI)** | `docker exec -it talenttrade-db psql -U postgres -d talenttrade` |
| **Stop and Remove Containers/Networks** | `docker compose down` |
| **Remove Containers, Networks & Volumes** | `docker compose down -v` |

---

## 📖 Swagger API Documentation

Once containers are active, access the interactive API docs at:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** or via the Nginx proxy at **`http://localhost/swagger-ui.html`**.

### Testing Secured Endpoints
1. Call `POST /api/auth/login` (or create a user via `POST /api/auth/register` first).
2. Copy the returned `token` from the JSON response.
3. Click the green **Authorize** padlock button at the top right of the Swagger UI page.
4. Input `Bearer <your_copied_token>` and submit.
5. All authorized endpoints are now testable directly in the browser!

---

## 🛠️ Future Enhancements
- **Google OAuth**: Integrate SSO login for user onboarding.
- **Google Calendar & Meet**: Auto-schedule meetings and generate Google Meet invites.
- **AI Skill Recommendations**: Use LLMs to match users based on bio semantic analysis.
- **Redis Cache**: Speed up dashboards and matching engines with cache eviction.
- **CI/CD Integration**: Add GitHub Actions workflow to build, test, and push the Docker images to a container registry.


