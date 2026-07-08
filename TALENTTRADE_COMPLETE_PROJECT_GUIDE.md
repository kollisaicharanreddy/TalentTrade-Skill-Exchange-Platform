# 📘 TalentTrade - Complete Backend Developer Project Guide

Welcome to the **TalentTrade Platform Backend Developer Guide**! This document serves as the absolute blueprint for any developer joining the team. It outlines the overall architecture, maps every file and class to its corresponding business feature, details the exact purpose of every class and method, and explains how the system boots and processes requests.

---

## 🏗️ 1. High-Level Architecture & Stack

TalentTrade is a peer-to-peer skill exchange platform designed to match users who want to trade knowledge (e.g., Alice teaches Python to Bob, and Bob teaches French to Alice). 

### The Technology Stack
* **Java 21 & Spring Boot 3.x**: Core application framework.
* **Spring Security & OAuth2**: Manages JWT-based stateless authentication, Google Social Sign-In, and Role-Based Access Control (RBAC).
* **Spring Data JPA & Hibernate**: Object-Relational Mapping (ORM) connecting Java entities to our database.
* **PostgreSQL 16**: Relational SQL database for data persistence.
* **WebSockets & STOMP**: Full-duplex persistent connection framework for real-time messaging.
* **Docker & Docker Compose**: Containerization environment managing our app, React frontend, and PostgreSQL services.

---

## 📁 2. Workspace Directory Layout

For a junior developer, here is where everything is located:

```text
TalentTrade-Skill-Exchange-Platform/
├── migration.sql                         # SQL schema migration script for RBAC & OAuth2
├── pom.xml                               # Maven project dependencies file
├── Dockerfile                            # Backend multi-stage container build file
├── docker-compose.yml                    # Multi-container coordinator
├── src/main/java/com/talenttrade/
│   ├── TalentTradeApplication.java        # Main Spring Boot entry point & PasswordEncoder Bean
│   ├── config/                           # Configuration beans (WebSockets, Swagger, Data Seed)
│   ├── controller/                       # REST controllers (API endpoints reception)
│   ├── dto/                              # Data Transfer Objects (Request/Response schemas)
│   ├── entity/                           # JPA database model representations & Enums
│   ├── exception/                        # Custom exceptions & GlobalExceptionHandler
│   ├── repository/                       # Data layers (JPA Repositories)
│   ├── security/                         # JWT filters, UserDetailsService, OAuth2 classes
│   └── service/                          # Service layer containing core business logic
```

---

## 🛠️ 3. Feature-by-Feature Developer Blueprint

Here is a breakdown of the system, organized strictly by **business feature**. For each feature, we identify the files, classes, methods, annotations, and database tables involved, along with a detailed explanation.

---

### 🔑 Feature A: Authentication, Security & OAuth2
Provides secure register, login, email verification, and Google Social login workflows.

#### 🗄️ Database Tables:
* `users`: Stores emails, user details, BCrypt hashed passwords, verification statuses, roles, and authentication providers.
* `verification_tokens`: Temporary tokens generated for registering users to verify their emails.

#### 📂 File & Class Registry:
1. **Entity**: [User.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/User.java)
   * *What it contains*: Models the user details. Implements `UserDetails` for Spring Security integration.
   * *Key Methods*: `getAuthorities()` (returns user roles as Spring Security GrantedAuthorities), `isEnabled()` (checks if account is active), `isEmailVerified()` (checks if email verification completed).
2. **Entity**: [VerificationToken.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/VerificationToken.java)
   * *What it contains*: Models the email verification token mapping to a user.
3. **Repository**: [UserRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/UserRepository.java)
   * *Key Methods*: `findByEmail(email)`, `existsByEmail(email)`, `existsByUsername(username)`.
4. **Repository**: [VerificationTokenRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/VerificationTokenRepository.java)
   * *Key Methods*: `findByToken(token)`.
5. **DTOs**: [RegisterRequest.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/dto/RegisterRequest.java), [LoginRequest.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/dto/LoginRequest.java), [LoginResponse.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/dto/LoginResponse.java), [UserResponse.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/dto/UserResponse.java).
6. **Controller**: [AuthController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/AuthController.java)
   * *What it contains*: Exposes public authentication HTTP endpoints mapped to `/api/auth`.
   * *Key Methods*: 
     * `register(@Valid @RequestBody RegisterRequest)`: Creates a new user.
     * `login(@Valid @RequestBody LoginRequest)`: Authenticates user credentials and generates a JWT.
     * `verifyEmail(@RequestParam String token)`: Confirms token matches registry and marks user active.
     * `resendVerification(@RequestParam String email)`: Generates and triggers another email verification token.
7. **Service**: [AuthService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/AuthService.java)
   * *What it contains*: Orchestrates password hashing, token validation, and account state transitions.
   * *Key Methods*: `registerUser()`, `login()`, `verifyEmail()`, `resendVerificationEmail()`.
8. **Security Configurations**:
   * [SecurityConfig.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/SecurityConfig.java): Sets up CORS configuration, disables stateful sessions, authorizes specific URLs, and inserts the JWT filter.
   * [JwtService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/JwtService.java): Utility class that parses claims, extracts usernames, and builds JWT tokens signed with the application secret key.
   * [JwtAuthenticationFilter.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/JwtAuthenticationFilter.java): A servlet filter running `OncePerRequestFilter`. It reads headers for `Authorization: Bearer <token>`, validates the token, and configures the `SecurityContextHolder`.
   * [CustomUserDetailsService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/CustomUserDetailsService.java): Custom loader that reads the user from `UserRepository` by email to satisfy Spring Security's runtime authentication needs.
   * [CustomOAuth2UserService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/CustomOAuth2UserService.java): Handles incoming OAuth2 payloads, registers new users with a random hashed password, or logs in returning OAuth2 configurations.
   * [OAuth2AuthenticationSuccessHandler.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/OAuth2AuthenticationSuccessHandler.java): Invoked upon successful Google login. Generates a JWT and redirects the user back to the React frontend dashboard with the token.

---

### 🏷️ Feature B: Skill Profile Registry
Enables creating standard skills and allowing users to link skills they can teach or want to learn to their profiles.

#### 🗄️ Database Tables:
* `skills`: Universal list of standard skills (name, category, description).
* `user_skills`: Junction table matching a user to a skill with `type` (`TEACH` or `LEARN`) and `level` (`BEGINNER` to `EXPERT`).

#### 📂 File & Class Registry:
1. **Entity**: [Skill.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Skill.java)
   * *What it contains*: Global skill metadata.
2. **Entity**: [UserSkill.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/UserSkill.java)
   * *What it contains*: User-specific skill mapping linking `@ManyToOne` user and skill entities.
3. **Enums**: [SkillType.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/SkillType.java) (`TEACH`/`LEARN`), [SkillLevel.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/SkillLevel.java) (`BEGINNER` to `EXPERT`).
4. **Repository**: [SkillRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/SkillRepository.java)
   * *Key Methods*: `findByNameIgnoreCase(name)`, `existsByNameIgnoreCase(name)`.
5. **Repository**: [UserSkillRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/UserSkillRepository.java)
   * *Key Methods*: `findByUserEmail(email)`, `existsByUserAndSkillAndType(user, skill, type)`.
6. **Controllers**:
   * [SkillController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/SkillController.java): Exposes public GET skill endpoints.
   * [UserSkillController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/UserSkillController.java): REST endpoints under `/api/users/skills` allowing users to add/delete their teach/learn list.
7. **Services**:
   * [SkillService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/SkillService.java): Implements CRUD logic for skills, incorporating case-insensitive duplicate prevention.
   * [UserSkillService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/UserSkillService.java): Links skills to profiles, preventing duplicate mappings (e.g. adding "Python" twice for teach).

---

### 🧩 Feature C: Reciprocal Matching Engine
Matches users who teach what another wants to learn, and vice versa.

#### 🗄️ Database Tables:
* `matches`: Stores User A, User B, their computed match score, and calculation timestamps.

#### 📂 File & Class Registry:
1. **Entity**: [Match.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Match.java)
   * *What it contains*: Models the matched pair.
2. **Repository**: [MatchRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/MatchRepository.java)
   * *Key Methods*: `findByUser1EmailOrUser2Email(email1, email2)`. Contains custom JPQL to fetch scores.
3. **DTO**: [MatchResponseDTO.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/dto/MatchResponseDTO.java).
4. **Controller**: [MatchController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/MatchController.java)
   * *What it contains*: Exposes endpoints under `/api/matches`.
   * *Key Methods*: `getMatches()` (gets calculated recommendations for the logged-in user), `refreshMatches()` (forces recalculation).
5. **Service**: [MatchService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/MatchService.java)
   * *Key Methods*: `calculateMatchesForUser(String email)`:
     * Pulls the user profile skills.
     * Queries the database for other users who teach what this user wants to learn, and learn what this user teaches.
     * Computes compatible match scores based on overlapping skill numbers and expertise levels.
     * Saves computed scores in the `matches` table and returns them to the caller.

---

### 🤝 Feature D: Connection Handshake (Exchange Requests)
Enables matched users to request a skill-exchange partnership.

#### 🗄️ Database Tables:
* `exchange_requests`: Tracks sender, receiver, message notes, status (`PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`), and timestamp auditing.

#### 📂 File & Class Registry:
1. **Entity**: [ExchangeRequest.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/ExchangeRequest.java)
2. **Repository**: [ExchangeRequestRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/ExchangeRequestRepository.java)
   * *Key Methods*: `findBySenderIdOrReceiverId()`, `existsBySenderAndReceiverAndStatus()`.
3. **Controller**: [ExchangeRequestController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ExchangeRequestController.java)
   * *What it contains*: Exposes endpoints under `/api/requests`.
   * *Key Methods*: `createRequest(...)`, `acceptRequest(@PathVariable Long id)`, `rejectRequest(@PathVariable Long id)`.
4. **Service**: [ExchangeRequestService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/ExchangeRequestService.java)
   * *Key Methods*:
     * `sendRequest(...)`: Validates that a user isn't connecting with themselves or sending a duplicate request.
     * `acceptRequest(...)` / `rejectRequest(...)`: Updates connection status.
     * **Transactional Operations**: Annotating service methods with `@Transactional` ensures that saving the exchange request status and generating in-app notifications happen inside one database transaction; if the notification fail-saves, the request is rolled back safely.

---

### 📅 Feature E: Session Scheduler & Conflict Prevention
Coordinates scheduling dates, times, notes, and video conference links between accepted partners.

#### 🗄️ Database Tables:
* `sessions`: Stores scheduled slots linking back to the origin `exchange_request`. Contains status (`SCHEDULED`, `COMPLETED`, `CANCELLED`).

#### 📂 File & Class Registry:
1. **Entity**: [Session.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Session.java)
2. **Repository**: [SessionRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/SessionRepository.java)
   * *Key Methods*:
     * `hasTimeConflict(mentorId, learnerId, date, startTime, endTime)`: Queries database to check for session overlaps.
3. **Controller**: [SessionController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/SessionController.java)
   * *What it contains*: Exposes endpoints under `/api/sessions`.
   * *Key Methods*: `scheduleSession(...)`, `completeSession(...)`, `cancelSession(...)`.
4. **Service**: [SessionService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/SessionService.java)
   * *Key Methods*:
     * `scheduleSession(...)`: Performs a validation check:
       * Verifies the exchange request status is accepted.
       * Runs `hasTimeConflict` query. It evaluates overlap using:
         `!(existing.endTime <= new.startTime || existing.startTime >= new.endTime)`
       * If clean, scheduled state is saved and notification is generated.

---

### 💬 Feature F: WebSockets & Chat Control Guard
Full-duplex real-time messaging between matching members.

#### 🗄️ Database Tables:
* `chat_messages`: Persists historical chat messages (sender, receiver, text content, sent timestamp, read status).

#### 📂 File & Class Registry:
1. **Entity**: [ChatMessage.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/ChatMessage.java)
2. **Repository**: [ChatMessageRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/ChatMessageRepository.java)
3. **Config**: [WebSocketConfig.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/WebSocketConfig.java)
   * *What it contains*: Registers the WebSocket server route `/ws` and setups the message routing brokers (`/topic`, `/queue`, `/app`).
   * *Key Component*: `ChannelInterceptor`. Captures incoming connections (`CONNECT` frame), extracts the JWT bearer token from STOMP headers, validates it via `JwtService`, and binds the authenticated user context to the WebSocket connection.
4. **REST Controller**: [ChatRestController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ChatRestController.java)
   * *What it contains*: Exposes REST APIs under `/api/chat`.
   * *Key Methods*: `getChatHistory(...)` (queries history logs), `getConversations()` (lists active dialogue partners).
5. **WS Controller**: [ChatController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ChatController.java)
   * *What it contains*: WebSockets/STOMP handler class using `@MessageMapping`.
   * *Key Methods*: `sendMessage(@Payload ChatMessageRequestDTO)`: Intercepts clients routing messages to `/app/chat.send`, invokes `chatService.saveMessage()`, and broadcasts details via `SimpMessagingTemplate` to the path `/topic/chat/{conversationId}`.
6. **Service**: [ChatService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/ChatService.java)
   * *Key Methods*: `saveMessage(...)`:
     * Enforces the **Communication Guard Rule**: Checks if the sender and receiver have an accepted `ExchangeRequest` OR an active scheduled `Session`. If neither exists, it throws a security exception, blocking unauthorized spam.

---

### ⭐ Feature G: Ratings, Reviews & Trust
Allows users to review finished sessions and rate mentors.

#### 🗄️ Database Tables:
* `reviews`: Holds ratings (1-5), comments, session ID references, reviewer, and reviewee.

#### 📂 File & Class Registry:
1. **Entity**: [Review.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Review.java)
2. **Repository**: [ReviewRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/ReviewRepository.java)
   * *Key Methods*: `getAverageRatingForUser(userId)`: Calculates user average rating scores using PostgreSQL `AVG` aggregation.
3. **Controller**: [ReviewController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ReviewController.java)
   * *What it contains*: Exposes routes under `/api/reviews`.
   * *Key Methods*: `submitReview(...)`, `getUserReviews(...)`.
4. **Service**: [ReviewService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/ReviewService.java)
   * *Key Methods*: `submitReview(...)`:
     * Checks if the session status is indeed `COMPLETED`.
     * Validates that the reviewer is part of the session and is not leaving a self-review.
     * Prevents duplicate reviews (only one review per session participant).
     * Saves review and updates the reviewee's average profile rating score.

---

### 🔔 Feature H: Notifications System
Alerts users dynamically when requests are received, accepted, scheduled, or reviewed.

#### 🗄️ Database Tables:
* `notifications`: Backs notifications (title, message details, type, read status).

#### 📂 File & Class Registry:
1. **Entity**: [Notification.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Notification.java)
2. **Enum**: [NotificationType.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/NotificationType.java)
3. **Repository**: [NotificationRepository.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/repository/NotificationRepository.java)
4. **Controller**: [NotificationController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/NotificationController.java)
   * *What it contains*: Exposes endpoints under `/api/notifications`.
   * *Key Methods*: `getNotifications()`, `markAsRead(@PathVariable Long id)`, `markAllAsRead()`.
5. **Service**: [NotificationService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/NotificationService.java)
   * *Key Methods*: `createNotification(User, String title, String message, NotificationType)` (invoked by other services upon exchange/session updates).

---

### 📊 Feature I: User Dashboard Analytics
Compiles metrics to populate home UI layouts.

#### 📂 File & Class Registry:
1. **Controller**: [DashboardController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/DashboardController.java)
2. **DTO**: [DashboardResponseDTO.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/dto/DashboardResponseDTO.java)
3. **Service**: [DashboardService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/DashboardService.java)
   * *Key Methods*: `getUserDashboardStats(email)`:
     * Pulls the user.
     * Counts: matching profiles, pending requests, total skills configured, upcoming sessions, and formats average rating to 1 decimal place.

---

### 🛡️ Feature J: Admin Dashboards & Platform Control
Provides administrators with system statistics, user auditing, and catalog controls.

#### 📂 File & Class Registry:
1. **Controller**: [AdminController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/AdminController.java)
   * *Purpose*: Exposes administrative REST endpoints under `/api/admin`. (See previous reference for details).
2. **Service**: [AdminService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/AdminService.java)
   * *Purpose*: Contains platform statistics gathering and memory metrics mapping. (See previous reference for details).

---

## 🚀 4. Bootstrapping & Initialization Flow

When a developer runs the backend application, the execution proceeds as follows:

```text
[1. JVM Boot] ──► [2. Property Resolution] ──► [3. Hibernate Sync] ──► [4. Data Seed] ──► [5. App Ready]
```

1. **JVM Boot**: Spring Boot executes the main method inside `TalentTradeApplication.java`. It starts a Tomcat server on port `8080`.
2. **Property Resolution**: Reads `application.properties`, loading environment variables (e.g. JWT secret key, database username/password) with fallback default values.
3. **Hibernate Auto-Synchronization**: Hibernate scans files annotated with `@Entity` and compares them with the database. If tables do not exist, Hibernate constructs them automatically.
4. **Database Seeding**: [DataInitializer.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/DataInitializer.java) implements Spring's `CommandLineRunner`. Once the application context is loaded:
   * It scans the `skills` table.
   * If the table is empty, it seeds 18 popular skill configurations across categories (e.g., Software Engineering, Arts, Languages) case-insensitively, guaranteeing a working out-of-the-box catalog.
5. **App Ready**: The application transitions to a listening state, waiting to serve API and WebSocket requests.

---

## 🔗 5. Lifecycle of a Request (Tracing Execution)

When a client makes a secure HTTP request, the call travels through the layers in this sequence:

```text
[Client Call]
    │ (GET /api/sessions/upcoming with "Authorization: Bearer <JWT>")
    ▼
[JwtAuthenticationFilter]
    │ (Reads JWT, validates expiry/signature, configures SecurityContextHolder)
    ▼
[SecurityConfig (Authorization Check)]
    │ (Checks if authenticated user is allowed to access path)
    ▼
[SessionController]
    │ (Receives call, maps query page/size, delegates to SessionService)
    ▼
[SessionService]
    │ (Executes logic, queries database via Repository)
    ▼
[SessionRepository]
    │ (Translates call to SQL SELECT query, reads Postgres, returns entity)
    ▼
[SessionService (Mapping)]
    │ (Maps Session Entity records to SessionResponseDTO output wrapper)
    ▼
[SessionController]
    │ (Wraps DTO inside ApiResponse.success() wrapper)
    ▼
[Client Response] (JSON String payload returned over HTTP status 200 OK)
```
