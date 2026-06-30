# Spring Boot Advanced Topics Study Guide

Welcome to the **TalentTrade Advanced Spring Boot Developer Guide**. This document is designed for Java developers who want to master advanced topics such as **Spring Security (JWT)**, **WebSockets (STOMP)**, **Spring Data JPA (Relationships & Performance)**, **Spring MVC API architecture**, and **Spring Cache (Redis)**.

---

## 🏗️ High-Level Architecture

![TalentTrade Backend Architecture](./TalentTrade%20Backend%20Architecture.png)

---

For each topic, you will find:
1. **General Concepts**: Plain-English explanations and high-level architectural walkthroughs.
2. **Annotations & Classes**: Detailed breakdowns of built-in and user-defined elements.
3. **Project Application**: A tour of how these concepts are implemented in the `TalentTrade` codebase.
4. **Deep Dives**: Practical discussions on complex issues (e.g., the Security Filter Chain, STOMP Interceptors, JPA N+1 problem).

---

## Table of Contents
- [1. Spring Security & JWT Authentication](#1-spring-security--jwt-authentication)
- [2. WebSockets & Real-Time STOMP Messaging](#2-websockets--real-time-stomp-messaging)
- [3. Spring Data JPA & Database Relationships](#3-spring-data-jpa--database-relationships)
- [4. Spring MVC, DTO Validation & Exception Handling](#4-spring-mvc-dto-validation--exception-handling)
- [5. Caching & Performance Optimization (Planned Enhancement)](#5-caching--performance-optimization-planned-enhancement)
- [6. Boilerplate Reduction & Documentation (Lombok & Swagger)](#6-boilerplate-reduction--documentation-lombok--swagger)
- [7. Complete Controller & API Reference (All 12 Controllers)](#7-complete-controller--api-reference-all-12-controllers)
- [8. Containerization & Deployment Architecture (Docker & Docker Compose)](#8-containerization--deployment-architecture-docker--docker-compose)

---

## 1. Spring Security & JWT Authentication

### A. General Concepts
In traditional web applications, sessions are kept on the server memory (stateful). However, for APIs, we prefer **stateless** authentication using **JWT (JSON Web Tokens)**. 
- **Authentication**: Confirming *who* you are (login with username/password).
- **Authorization**: Confirming *what* you are allowed to do (checking roles/privileges).

#### How JWT Works:
1. Client logs in with a password.
2. Server verifies the password and returns a cryptographically signed **JWT token** containing the username and expiration.
3. For every subsequent request, the client includes the JWT in the `Authorization` header as `Bearer <token>`.
4. The server validates the signature. If valid, the request is processed without querying the database or checking server session memory.

```text
  [ Client ] -- 1. POST /login (Credentials) --> [ Server ]
  [ Client ] <-- 2. Returns Signed JWT Token --- [ Server ]
  
  [ Client ] -- 3. GET /profile (Bearer JWT) --> [ Security Filter Chain ]
                                                          |
                                               Validates Signature & Expiry
                                                          |
                                                          v
  [ Client ] <-- 4. Returns 200 OK Profile ----- [ Controller ]
```

---

### B. Core Annotations & Classes
* `@Configuration`: Tells Spring that this class contains one or more `@Bean` definitions and will be processed by the Spring container to generate bean definitions and service requests.
* `@EnableWebSecurity`: Enables Spring Security's web security support and integrates it with Spring MVC.
* `@Component`: Tells Spring to register this class as a managed bean so it can be auto-injected elsewhere.
* `@Value`: Used to inject properties from `application.properties` (e.g., secrets, timeouts).
* `SecurityFilterChain`: The backbone of web security; a chain of filters that intercept, analyze, and approve or reject incoming HTTP requests.
* `OncePerRequestFilter`: A base class that guarantees a single execution per request dispatch, which is critical for parsing authentication headers.
* `UsernamePasswordAuthenticationToken`: Represents an authenticated principal (user) containing details, credentials, and authorities.
* `SecurityContextHolder`: The central store where Spring Security stores details of who is authenticated.

---

### C. Deep Dive: Security Filter Chain Execution Flow
When an HTTP request enters the application, it goes through a pipeline of servlet filters. The filter chain execution proceeds as follows:

```text
HTTP Request ---> [CorsFilter] ---> [CsrfFilter] ---> [JwtAuthenticationFilter] ---> [UsernamePasswordAuthenticationFilter] ---> Controller
```

In [SecurityConfig.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/SecurityConfig.java):
1. **CSRF & CORS Disabling**: Disallowed stateful protection, since JWT is immune to standard CSRF attacks when state is stateless.
2. **Authorize Http Requests**: Configures paths that do not require authentication (`/api/auth/register`, `/api/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`, `/ws/**`) using `.permitAll()`. Everything else requires `.authenticated()`.
3. **Session Management**: Configured as `SessionCreationPolicy.STATELESS` so Spring Security will never create an `HttpSession` nor obtain a SecurityContext from it.
4. **addFilterBefore**: Places the `JwtAuthenticationFilter` right before `UsernamePasswordAuthenticationFilter`.

---

### D. Project Application

#### 1. [JwtAuthenticationFilter.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/JwtAuthenticationFilter.java)
An interceptor that runs on every request:
- Extracts the HTTP `Authorization` header.
- Checks if it starts with `"Bearer "`.
- Extracts the JWT and gets the user email from it via `JwtService.extractUsername(jwt)`.
- If the user is not yet authenticated in `SecurityContextHolder`, it loads the user profile via `CustomUserDetailsService`.
- Verifies the token. If valid, it creates a `UsernamePasswordAuthenticationToken` and sets it in `SecurityContextHolder`:
  ```java
  SecurityContextHolder.getContext().setAuthentication(authToken);
  ```

#### 2. [JwtService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/JwtService.java)
Responsible for utility operations on JWTs using the `io.jsonwebtoken` library:
- **Hashing/Signing**: Generates a sign-in key using base64 decoding of the `jwt.secret` property. Uses `HS256` signature algorithm.
- **Parsing**: Reads claims (subject, expiration date) from incoming tokens using `Jwts.parserBuilder()`.
- **Validation**: Checks that the token's username matches the database profile and has not expired.

#### 3. [CustomUserDetailsService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/CustomUserDetailsService.java)
Implements Spring Security's `UserDetailsService`. It overrides `loadUserByUsername(String username)` to fetch the user profile from the database (`UserRepository`) and wrap it in a Spring Security-compatible `UserDetails` object.

---

## 2. WebSockets & Real-Time STOMP Messaging

### A. General Concepts
- **HTTP**: Unidirectional. The client sends a request, the server responds. The connection closes.
- **WebSockets**: Bidirectional, full-duplex, persistent connection over a single TCP socket. Once the handshake is successful, the client and server can send data to each other at any time.
- **STOMP (Simple Text Oriented Messaging Protocol)**: WebSockets are just transport pipes; they do not dictate routing. STOMP is a sub-protocol built on top of WebSockets that defines a frame format (like `CONNECT`, `SUBSCRIBE`, `SEND`, `MESSAGE`) allowing clients to subscribe to specific topics and publish messages.

```text
  [ Client ] --- WebSocket Handshake HTTP GET (Upgrade) ---> [ Server ]
  [ Client ] <--- 101 Switching Protocols Response -------- [ Server ]
  
  -- Connection established --
  
  [ Client ] --- SUBSCRIBE /topic/chat/1_2 -----------------> [ Server STOMP Broker ]
  [ Client ] --- SEND to /app/chat.send (Payload) ----------> [ ChatController ]
                                                                     |
                                                               Saves in DB
                                                                     |
  [ Client ] <--- MESSAGE /topic/chat/1_2 (Broadcast) -------- [ Server Broker ]
```

---

### B. Core Annotations & Classes
* `@EnableWebSocketMessageBroker`: Enables WebSocket message handling, backed by a message broker.
* `@MessageMapping`: Maps incoming WebSocket/STOMP messages carrying a matching destination header to a controller handler method (similar to `@RequestMapping` in REST).
* `@Payload`: Indicates that the method argument should be bound to the payload (body) of the incoming message.
* `SimpMessagingTemplate`: A Spring component that allows you to send messages to specific broker destinations programmatically from any class.

---

### C. Deep Dive: Handshake & STOMP JWT Authentication
Because WebSockets establish connections via a standard HTTP handshake, standard HTTP Authorization headers are not easily forwarded in WebSockets. Instead, in modern applications, we send the JWT token as a custom header during the initial STOMP **`CONNECT`** frame.

In [WebSocketConfig.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/WebSocketConfig.java):
1. We intercept the channel inbound stream:
   ```java
   public void configureClientInboundChannel(ChannelRegistration registration) {
       registration.interceptors(new ChannelInterceptor() { ... });
   }
   ```
2. We filter for `StompCommand.CONNECT`.
3. We extract the `"Authorization"` header from the native STOMP headers.
4. We extract the JWT, validate it via `JwtService`, load details via `CustomUserDetailsService`, and set the websocket user principal:
   ```java
   accessor.setUser(authentication);
   ```

---

### D. Project Application

#### 1. [WebSocketConfig.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/WebSocketConfig.java)
- **STOMP Endpoint Registry**: Configures `/ws` as the endpoint that clients connect to. Uses `.withSockJS()` to allow fallback transports if raw WebSockets are blocked by proxies.
- **Message Broker Configuration**:
  - `registry.enableSimpleBroker("/topic", "/queue")`: Enables a simple in-memory broker that routes messages starting with `/topic` (pub-sub broadcasts) or `/queue` (point-to-point) back to the clients.
  - `registry.setApplicationDestinationPrefixes("/app")`: Defines that messages sent by clients with destinations starting with `/app` are routed directly to controller `@MessageMapping` methods.

#### 2. [ChatController.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ChatController.java)
- Listens to incoming chat actions on path `/chat.send` (full client destination `/app/chat.send`).
- Identifies the sender using the authenticated `Principal`.
- Saves the message to the database via `ChatService`.
- Automatically broadcasts the message to the dynamic conversation topic path:
  ```java
  messagingTemplate.convertAndSend("/topic/chat/" + conversationId, response);
  ```
  *(Where `conversationId` is formed alphabetically by sorting user IDs, e.g. `minUserId_maxUserId`, forcing both chatters into the same subscription channel).*

---

## 3. Spring Data JPA & Database Relationships

### A. General Concepts
**JPA (Jakarta Persistence API)** is a specification that defines how Java objects (Entities) map to Relational Database tables. **Hibernate** is the default runtime provider (implementation) of JPA.
- `@Entity`: Identifies that a Java class represents a database table.
- **Relationships**: Database tables use foreign keys. Java uses object references. We declare relationships to bridge this:
  - `@ManyToOne`: Multiple instances of the current entity link to one instance of another (e.g. Multiple `UserSkill` belong to one `User`).
  - `@OneToMany`: One instance links to multiple instances (e.g. One `User` has many `UserSkill` records).
  - `@ManyToMany`: Many records link to many records. In practice, we map this with an intermediate entity (like `UserSkill` joining `User` and `Skill` with attributes like level).

---

### B. Deep Dive: FetchType (LAZY vs EAGER) & the N+1 Query Problem
When fetching data from a database with mapped associations, performance is heavily impacted by the fetch strategy:
1. **EAGER**: Automatically loads the associated entity along with the parent.
2. **LAZY**: Loads the parent first and replaces the child with a "proxy". The child is only queried from the database when its getter is called (e.g. `user.getSkills()`).

#### The N+1 Select Problem:
Suppose you fetch 100 users, and each user has a LAZY relationship to their skills.
- Query 1: `SELECT * FROM users` (returns 100 users).
- Query 2 to 101: When looping over each user and calling `.getSkills()`, Hibernate triggers an individual query for each user: `SELECT * FROM user_skills WHERE user_id = ?`.
Result: **101 database roundtrips** (1 query to load parent, N queries to load children). This crushes performance.

#### The Solution:
Use custom queries with **`JOIN FETCH`** in Repository interfaces. This forces Hibernate to perform an SQL `INNER JOIN` or `LEFT JOIN` and pull both parent and child details in a single query database call:
```java
@Query("SELECT u FROM User u LEFT JOIN FETCH u.skills WHERE u.email = :email")
Optional<User> findByEmailWithSkills(@Param("email") String email);
```

---

### C. Project Application
The `TalentTrade` project maps complex entities:
- [User.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/User.java): Maps to `users` table. Contains fields like full name, email, encrypted password.
- [UserSkill.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/UserSkill.java): Serves as the junction entity between `User` and `Skill`. Annotated with:
  ```java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  ```
- [Match.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Match.java): Connects two users who are matches (`user1` and `user2`) with a calculated `matchScore`.
- [Session.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/Session.java): Links a mentor (`User`), learner (`User`), and the originating `ExchangeRequest`. Has enum validations (`SessionStatus`).
- [ExchangeRequest.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/ExchangeRequest.java): Relates a sender and receiver.

---

## 4. Spring MVC, DTO Validation & Exception Handling

### A. General Concepts
- **Spring MVC**: Maps incoming HTTP requests to Java controller methods using `@RestController` and handles returning JSON payloads.
- **DTO (Data Transfer Object)**: Separate classes from entities. Instead of exposing database columns directly, DTOs carry only the necessary fields for specific API requests or responses.
- **Bean Validation**: Validates user inputs (e.g. preventing blank passwords or negative ratings) before processing them in the service layer.
- **Global Exception Handling**: Instead of returning raw Java stack traces on error, we intercept exceptions globally and return clean, standard JSON error envelopes.

---

### B. Core Annotations
* `@RestController`: Combines `@Controller` and `@ResponseBody`. Ensures that values returned from methods are serialized directly into the JSON response body.
* `@RequestMapping`: Defines the base URI path prefix for all endpoints in a controller.
* `@RequestBody`: Deserializes the incoming JSON body directly into a Java object (DTO).
* `@PathVariable`: Binds a URI template variable (e.g., `/api/users/{id}`) to a method parameter.
* `@RequestParam`: Extracts query parameters (e.g. `/api/users?page=0&size=10`) from the request URL.
* `@Valid`: Informs Spring to execute validation rules on the incoming object.
* `@NotBlank`: Jakarta validation constraint. The field must not be null and must contain at least one non-whitespace character.
* `@NotNull`: The field must not be null.
* `@Email`: The field must form a valid email format.
* `@Size(min=X, max=Y)`: Restricts length constraints.
* `@Min` / `@Max`: Restricts number values.

---

### C. Deep Dive: Global Interception & DTO Validation Flow
1. Client submits a POST request to `/api/auth/register` with an invalid email.
2. The controller intercepts the call:
   ```java
   public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request)
   ```
3. Because of `@Valid`, Spring's validation engine verifies the constraints inside `RegisterRequest`.
4. If validations fail, it throws `MethodArgumentNotValidException`.
5. [GlobalExceptionHandler.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/exception/GlobalExceptionHandler.java) detects this exception:
   ```java
   @RestControllerAdvice
   public class GlobalExceptionHandler {
       @ExceptionHandler(MethodArgumentNotValidException.class)
       public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(...)
   }
   ```
6. The handler parses the validation errors, packs them into a clean, uniform format (`ApiResponse`), and returns a HTTP `400 Bad Request` code.

---

## 5. Caching & Performance Optimization (Planned Enhancement)

### A. General Concepts
**Caching** is the process of storing copies of data in a high-speed memory layer (such as RAM or an in-memory database like Redis) so that future requests for that data can be served faster.
- **Cache Hit**: Requested data is found in the cache. Retries instantly.
- **Cache Miss**: Requested data is not found in the cache. The application queries the database, saves the result in the cache for next time, and returns the result to the user.

```text
  GET /api/matches/me ---> [ Cache Check ] -- Hit --> Return Cached JSON
                                 |
                                Miss
                                 |
                                 v
                          [ Database Query ] ---> Save in Cache ---> Return JSON
```

---

### B. Core Annotations & Configuration
Spring provides an abstraction layer that allows you to easily plug in cache engines (Redis, Ehcache, Caffeine) using simple annotations:

* `@EnableCaching`: Added to the main application configuration class to activate Spring’s annotation-driven caching capabilities.
* `@Cacheable`: Applied to methods to cache their results. Before execution, Spring checks if the input parameter exists as a key in the cache. If it exists, Spring returns the cached value without running the method.
* `@CachePut`: Always executes the method and updates the cache. Useful for create/update operations.
* `@CacheEvict`: Removes data from the cache. Essential to prevent stale data when entries are deleted or modified.
* `@CacheConfig`: Class-level configuration specifying common cache names.

---

### C. Implementation Blueprint: Integrating Redis Caching
To add Redis cache to the `TalentTrade` matching engine:

#### 1. Add Maven Dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### 2. Enable Caching in the Application Class:
```java
@SpringBootApplication
@EnableCaching  // Enables Spring Cache
public class TalentTradeApplication { ... }
```

#### 3. Configure Redis properties in `application.properties`:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
# Set default cache expiration (TTL) to 10 minutes
spring.cache.redis.time-to-live=600000
```

#### 4. Apply Cache in the Service Layer:
In [MatchService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/MatchService.java):
```java
// Caches matches by user email. Evicts automatically after TTL or when explicitly evicted.
@Cacheable(value = "matches", key = "#userEmail")
public List<MatchResponseDTO> findMatchesForUser(String userEmail) {
    // Heavy DB query computing match scores goes here...
    return matchRepository.calculateScores(userEmail);
}

// When a user updates their profile skills, we must evict the stale cache
@CacheEvict(value = "matches", key = "#userEmail")
public void evictMatchCache(String userEmail) {
    // Cache is cleared for this key so the next call performs calculations fresh
}
```

---

## 6. Boilerplate Reduction & Documentation (Lombok & Swagger)

### A. Lombok Annotations
Lombok is a compiler-level tool that generates boilerplate Java code at build time:
* `@Data`: Bundle annotation that generates `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, and `@RequiredArgsConstructor`.
* `@NoArgsConstructor`: Generates a constructor with no parameters (required by Hibernate/JPA).
* `@AllArgsConstructor`: Generates a constructor with parameters for all fields.
* `@Builder`: Implements the Builder pattern, allowing fluent creation of object instances:
  ```java
  User user = User.builder().email("test@email.com").fullName("Name").build();
  ```
* `@RequiredArgsConstructor`: Generates a constructor for all `final` fields, enabling clean constructor-based **Dependency Injection** without writing `@Autowired`:
  ```java
  @Service
  @RequiredArgsConstructor
  public class UserService {
      private final UserRepository userRepository; // Auto-injected via constructor
  }
  ```
* `@Slf4j`: Automatically injects a Simple Logging Facade for Java logger instance named `log` into the class.

---

### B. Swagger / OpenAPI Documentation
Automates interactive REST API endpoints validation.
- Configured in [SwaggerConfig.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/SwaggerConfig.java).
- Exposes Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Uses `@Tag` to categorize endpoints, `@Operation` to describe endpoints, and `@ApiResponse` to document outcome expectations.

---

## 7. Complete Controller & API Reference (All 12 Controllers)

Every REST controller in `TalentTrade` returns a standardized `ApiResponse<T>` JSON envelope. Below is the comprehensive API dictionary.

---

### 1. AuthController
- **Path Prefix**: `/api/auth`
- **Purpose**: Manages public authentication and registration.
- **Endpoints**:
  * **`POST /register`**
    * *Input*: `@Valid @RequestBody RegisterRequest` (fullName, username, email, password)
    * *Output*: `ApiResponse<UserResponse>`
    * *Description*: Registers a new user. The password is encrypted using `BCryptPasswordEncoder` prior to storage.
  * **`POST /login`**
    * *Input*: `@Valid @RequestBody LoginRequest` (email, password)
    * *Output*: `ApiResponse<LoginResponse>` (token, user details, expiration time)
    * *Description*: Authenticates credentials and returns a valid JWT bearer token.

---

### 2. UserController
- **Path Prefix**: `/api/users`
- **Purpose**: Handles user profiles and user search.
- **Endpoints**:
  * **`GET /me`**
    * *Input*: Authenticated `Principal` context.
    * *Output*: `ApiResponse<UserResponse>`
    * *Description*: Retrieves profile info for the currently logged-in user.
  * **`PUT /me`**
    * *Input*: `@Valid @RequestBody UpdateProfileRequest` (fullName, bio, location)
    * *Output*: `ApiResponse<UserResponse>`
    * *Description*: Updates profile details of the current logged-in user.
  * **`GET`**
    * *Input*: Query Params: `page`, `size`, `sortBy`, `direction`
    * *Output*: `ApiResponse<List<UserResponse>>` (Paginated)
    * *Description*: Retrieves all registered users.
  * **`GET /search`**
    * *Input*: Query Params: `query` (name/location), `page`, `size`
    * *Output*: `ApiResponse<List<UserResponse>>`
    * *Description*: Performs text-based searches on user names, bios, or locations.

---

### 3. SkillController
- **Path Prefix**: `/api/skills`
- **Purpose**: Manages the global registry of skills.
- **Endpoints**:
  * **`POST`**
    * *Input*: `@Valid @RequestBody SkillDTO` (name, category, description)
    * *Output*: `ApiResponse<SkillResponseDTO>`
    * *Description*: Registers a new global skill (e.g. Python, Public Speaking) to the exchange pool.
  * **`GET`**
    * *Input*: Query Params: `category` (optional filter), `page`, `size`, `sortBy`
    * *Output*: `ApiResponse<List<SkillResponseDTO>>`
    * *Description*: Fetches all global skills, optionally filtered by categories.
  * **`GET /{id}`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<SkillResponseDTO>`
    * *Description*: Fetches a single skill registry by ID.
  * **`DELETE /{id}`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<Void>`
    * *Description*: Deletes a skill from the global catalog.

---

### 4. UserSkillController
- **Path Prefix**: `/api/users/skills`
- **Purpose**: Links users to the skills they want to teach or learn.
- **Endpoints**:
  * **`POST`**
    * *Input*: `@Valid @RequestBody UserSkillRequestDTO` (skillId, type [TEACH/LEARN], level [BEGINNER to EXPERT])
    * *Output*: `ApiResponse<UserSkillResponseDTO>`
    * *Description*: Associates a skill to the logged-in user.
  * **`GET`**
    * *Input*: Authenticated `Principal` context
    * *Output*: `ApiResponse<List<UserSkillResponseDTO>>`
    * *Description*: Lists all skills associated with the current user.
  * **`DELETE /{id}`**
    * *Input*: `@PathVariable Long id` (UserSkill association ID)
    * *Output*: `ApiResponse<Void>`
    * *Description*: Removes a skill association from the user profile.

---

### 5. MatchController
- **Path Prefix**: `/api/matches`
- **Purpose**: Power matching calculations between reciprocal skills.
- **Endpoints**:
  * **`GET`**
    * *Input*: Authenticated `Principal`, Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<MatchResponseDTO>>`
    * *Description*: Finds other users whose teaching skills align with the current user's learning needs and vice versa.
  * **`GET /{id}`**
    * *Input*: `@PathVariable Long id` (Match ID)
    * *Output*: `ApiResponse<MatchResponseDTO>`
    * *Description*: Retrieves details of a specific match profile.
  * **`POST /refresh`**
    * *Input*: Authenticated `Principal`
    * *Output*: `ApiResponse<List<MatchResponseDTO>>`
    * *Description*: Forces the scoring engine to recalculate reciprocal matches for the user.

---

### 6. ExchangeRequestController
- **Path Prefix**: `/api/requests`
- **Purpose**: Manages the handshake/connection pipeline.
- **Endpoints**:
  * **`POST`**
    * *Input*: `@Valid @RequestBody ExchangeRequestDTO` (receiverId, message)
    * *Output*: `ApiResponse<ExchangeRequestResponseDTO>`
    * *Description*: Dispatches a new connection request to another user.
  * **`GET`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ExchangeRequestResponseDTO>>`
    * *Description*: Returns all requests where the logged-in user is either sender or receiver.
  * **`GET /sent`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ExchangeRequestResponseDTO>>`
    * *Description*: Lists requests sent by the current user.
  * **`GET /received`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ExchangeRequestResponseDTO>>`
    * *Description*: Lists requests received by the current user.
  * **`PUT /{id}/accept`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<ExchangeRequestResponseDTO>`
    * *Description*: Accepts a pending request, transitioning status to `ACCEPTED`.
  * **`PUT /{id}/reject`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<ExchangeRequestResponseDTO>`
    * *Description*: Rejects a request, transitioning status to `REJECTED`.

---

### 7. SessionController
- **Path Prefix**: `/api/sessions`
- **Purpose**: Manages learning sessions scheduled between matched users.
- **Endpoints**:
  * **`POST`**
    * *Input*: `@Valid @RequestBody SessionRequestDTO` (exchangeRequestId, scheduledDate, startTime, endTime, meetingLink, notes)
    * *Output*: `ApiResponse<SessionResponseDTO>`
    * *Description*: Schedules a learning session. Includes conflict checks to prevent user session scheduling overlaps.
  * **`GET`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<SessionResponseDTO>>`
    * *Description*: Lists all sessions where the user is either mentor or learner.
  * **`GET /{id}`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<SessionResponseDTO>`
    * *Description*: Retrieves detailed properties of a session.
  * **`PUT /{id}`**
    * *Input*: `@PathVariable Long id`, `@Valid @RequestBody SessionRequestDTO`
    * *Output*: `ApiResponse<SessionResponseDTO>`
    * *Description*: Reschedules details of a scheduled session.
  * **`PUT /{id}/complete`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<SessionResponseDTO>`
    * *Description*: Completes a session.
  * **`PUT /{id}/cancel`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<SessionResponseDTO>`
    * *Description*: Cancels a session.
  * **`DELETE /{id}`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<Void>`
    * *Description*: Cancels and deletes a session record.
  * **`GET /upcoming`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<SessionResponseDTO>>`
    * *Description*: Fetches scheduled future sessions.
  * **`GET /completed`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<SessionResponseDTO>>`
    * *Description*: Fetches sessions marked completed.
  * **`GET /history`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<SessionResponseDTO>>`
    * *Description*: Retrieves historical timeline of all sessions.

---

### 8. ReviewController
- **Path Prefix**: `/api/reviews`
- **Purpose**: Provides ratings and comments on completed sessions.
- **Endpoints**:
  * **`POST`**
    * *Input*: `@Valid @RequestBody ReviewRequestDTO` (sessionId, rating [1-5], comment)
    * *Output*: `ApiResponse<ReviewResponseDTO>`
    * *Description*: Submits a review. Restricts self-reviews or duplicate session reviews.
  * **`GET /user/{userId}`**
    * *Input*: `@PathVariable Long userId`, Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ReviewResponseDTO>>`
    * *Description*: Fetches reviews received by a specific user.
  * **`GET /me`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ReviewResponseDTO>>`
    * *Description*: Fetches reviews written by the currently logged-in user.
  * **`DELETE /{id}`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<Void>`
    * *Description*: Deletes a submitted review.

---

### 9. NotificationController
- **Path Prefix**: `/api/notifications`
- **Purpose**: Keeps users informed about requests and sessions.
- **Endpoints**:
  * **`GET`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<NotificationResponseDTO>>`
    * *Description*: Lists all notifications (read and unread) for the current user.
  * **`PUT /{id}/read`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<NotificationResponseDTO>`
    * *Description*: Marks a specific notification as read.
  * **`PUT /read-all`**
    * *Input*: Authenticated context
    * *Output*: `ApiResponse<Void>`
    * *Description*: Marks all notifications for the current user as read.
  * **`DELETE /{id}`**
    * *Input*: `@PathVariable Long id`
    * *Output*: `ApiResponse<Void>`
    * *Description*: Deletes a specific notification record.

---

### 10. ChatRestController
- **Path Prefix**: `/api/chat`
- **Purpose**: REST controller handling chat history retrieval.
- **Endpoints**:
  * **`GET /history/{userId}`**
    * *Input*: `@PathVariable Long userId`, Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ChatMessageResponseDTO>>`
    * *Description*: Fetches historical chat records between the current user and the specified recipient.
  * **`GET /conversations`**
    * *Input*: Query Params: `page`, `size`
    * *Output*: `ApiResponse<List<ConversationResponseDTO>>`
    * *Description*: Retrieves all distinct conversation chat threads (recent chats).
  * **`PUT /read/{messageId}`**
    * *Input*: `@PathVariable Long messageId`
    * *Output*: `ApiResponse<Void>`
    * *Description*: Marks a received chat message as read.
  * **`DELETE /{messageId}`**
    * *Input*: `@PathVariable Long messageId`
    * *Output*: `ApiResponse<Void>`
    * *Description*: Deletes a chat message.

---

### 11. ChatController
- **Routing Protocol**: WebSocket / STOMP
- **Destination mapping**: `/chat.send` (configured client target: `/app/chat.send`)
- **Purpose**: Handles real-time messaging pipeline.
- **Flow**:
  * Receives `ChatMessageRequestDTO` (receiverId, message) over the WebSocket socket pipe.
  * Asserts user identity from the socket authenticated context (`Principal`).
  * Persists message history and broadcasts payload to target `/topic/chat/{conversationId}`.

---

### 12. DashboardController
- **Path Prefix**: `/api/dashboard`
- **Purpose**: Summarizes user activity metrics.
- **Endpoints**:
  * **`GET`**
    * *Input*: Authenticated context
    * *Output*: `ApiResponse<DashboardResponseDTO>` (counts of skills, matches, pending requests, upcoming sessions, average rating)
    * *Description*: Returns full user statistics for dashboard layouts.

---

## 8. Complete Project Flow & Execution Lifecycle

When describing the architecture and operation of `TalentTrade` (e.g., in a technical interview or code walkthrough), it is best explained as a sequential lifecycle. Here is the step-by-step execution path of the project, including how components call one another, their dependencies, and the core concepts involved at each stage.

```text
[Step 1: System Boot] 
      │
      ▼
[Step 2: Register] ──► Password Encryption (BCrypt) ──► Persist in PostgreSQL
      │
      ▼
[Step 3: Login] ──► Credentials Check (DAO Provider) ──► Generate signed JWT token
      │
      ▼
[Step 4: Profile] ──► Add TEACH & LEARN skills (Many-to-Many Junctions)
      │
      ▼
[Step 5: Match Engine] ──► Compute Reciprocal Intersection Scores ──► Query Matches
      │
      ▼
[Step 6: Handshake] ──► POST Exchange Request (PENDING) ──► Notify User B (Transaction)
      │
      ▼
[Step 7: Schedule] ──► ACCEPT Request ──► Create Session (Time Conflict Checks)
      │
      ▼
[Step 8: WS Connect] ──► Upgrade to WSS ──► Intercept CONNECT frame ──► Authenticate STOMP via JWT
      │
      ▼
[Step 9: Chat] ──► Check Permission (Access Check) ──► Save Message ──► Broadcast to Subscribed Topic
      │
      ▼
[Step 10: Review] ──► Complete Session ──► Write Review (Self-check) ──► Update User Ratings
      │
      ▼
[Step 11: Dashboard] ──► Aggregate counts & compute metrics for Home UI display
```

---

### Step 1: System Boot & Initialization
1. **Action**: The Spring Boot engine bootstraps from the [TalentTradeApplication](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/TalentTradeApplication.java) main class.
2. **Database Verification**: Under the hood, Spring reads `application.properties`. Hibernate detects the property `spring.jpa.hibernate.ddl-auto=update` and scans classes annotated with `@Entity`. It auto-synchronizes the tables (`users`, `skills`, `user_skills`, etc.) with PostgreSQL.
3. **Security Injection**: Spring Security constructs the `SecurityFilterChain` bean defined in [SecurityConfig](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/SecurityConfig.java), defining public vs secured endpoints, and setups WebSocket connection routes.

---

### Step 2: User Onboarding (Registration)
1. **Trigger**: An unauthenticated user submits a registration request.
2. **Execution Path**:
   - The browser hits `POST /api/auth/register` carrying a `RegisterRequest` DTO payload.
   - [AuthController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/AuthController.java) validates parameters (e.g. `@Email`, `@NotBlank`) and routes to [AuthService](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/AuthService.java).
   - **Key Concept: Password Hashing**: To protect user security, the raw password is encrypted using a strong one-way hash algorithm (`BCryptPasswordEncoder`) before storing.
   - The user profile entity is saved in the database via the `UserRepository` interface.

---

### Step 3: Authentication (Login)
1. **Trigger**: User inputs email and password to log in.
2. **Execution Path**:
   - Client hits `POST /api/auth/login` carrying `LoginRequest`.
   - `AuthController` invokes `AuthService.login()`.
   - **Key Concept: DaoAuthenticationProvider**: The service passes credentials to Spring Security's `AuthenticationManager`. It delegates to `DaoAuthenticationProvider`, which requests the user data via [CustomUserDetailsService](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/security/CustomUserDetailsService.java) and compares the stored BCrypt hash with the typed password.
   - If authentication is successful, the service uses `JwtService.generateToken()` to build a stateless **JWT (JSON Web Token)**. The token encodes the username and expiration, cryptographically signed with the base64-encoded secret key.
   - The token is sent back inside a `LoginResponse` DTO. The client stores it locally.

---

### Step 4: Profile Enrichment (Adding Skills)
1. **Trigger**: The logged-in user configures what they can teach and what they want to learn.
2. **Execution Path**:
   - The client invokes `POST /api/users/skills` with a `UserSkillRequestDTO` containing the global `skillId`, `type` (TEACH or LEARN), and skill `level`.
   - [UserSkillController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/UserSkillController.java) delegates to `UserSkillService`.
   - **Key Concept: Many-to-Many Junction mapping**: Since a User can have multiple Skills, and a Skill can belong to multiple Users, the database maps this using an intermediate table `user_skills`. This is represented by the [UserSkill](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/entity/UserSkill.java) entity, which links to `User` and `Skill` with `@ManyToOne` relationships.
   - The skill relationship is saved via `UserSkillRepository`.

---

### Step 5: Reciprocal Matching Engine Execution
1. **Trigger**: User opens the dashboard to see match recommendations.
2. **Execution Path**:
   - Client hits `GET /api/matches` or triggers a match recalculation via `POST /api/matches/refresh`.
   - [MatchController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/MatchController.java) triggers `MatchService`.
   - **Key Concept: Reciprocal Scoring Logic**: The match engine executes custom queries to retrieve other users where:
     * User A teaches a skill that User B wants to learn.
     * User B teaches a skill that User A wants to learn.
   - It computes a compatibility score based on how many skills match and their respective levels. Matches are saved to the `matches` table and returned.

---

### Step 6: Connection Handshake (Exchange Requests)
1. **Trigger**: User A sends a connection request to User B from their match suggestions list.
2. **Execution Path**:
   - Client hits `POST /api/requests` with an `ExchangeRequestDTO` containing the target user's ID.
   - [ExchangeRequestController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ExchangeRequestController.java) invokes `ExchangeRequestService`.
   - **Key Concept: Transaction Boundaries (`@Transactional`)**: The request must be saved in the database with status `PENDING`, and simultaneously, an alert must be created in the `notifications` table for User B. Marking the service method as `@Transactional` ensures that both operations succeed together; if one fails, the entire database transaction rolls back.
   - User B later views the request and hits `PUT /api/requests/{id}/accept`. The status transitions to `ACCEPTED` in the database.

---

### Step 7: Session Scheduling & Overlap Collision Check
1. **Trigger**: Matched users schedule a learning session.
2. **Execution Path**:
   - User A hits `POST /api/sessions` with date, time window, notes, and a meeting link.
   - [SessionController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/SessionController.java) delegates to `SessionService`.
   - **Key Concept: Interval Overlap Logic**: To prevent calendar booking collisions, before saving a session, the system queries the database for any existing sessions booked for either User A or User B during that date. It evaluates this mathematical overlap:
     ```
     !(existingSession.endTime <= newSession.startTime || existingSession.startTime >= newSession.endTime)
     ```
     If an overlap exists, it aborts the process by throwing `InvalidSessionStateException` which gets translated to an HTTP 400. Otherwise, the session is scheduled.

---

### Step 8: WebSockets Handshake & STOMP Authentication
1. **Trigger**: Client opens the application and establishes a real-time message connection.
2. **Execution Path**:
   - Client requests a WebSocket connection by hitting `/ws`.
   - **Key Concept: STOMP Connection Interception**: Standard WebSocket requests cannot easily send custom HTTP authorization headers. Thus, the client sends a STOMP `CONNECT` frame carrying their JWT token inside a custom native STOMP header.
   - [WebSocketConfig](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/WebSocketConfig.java)'s `ChannelInterceptor` captures the inbound connection, extracts the JWT, verifies it via `JwtService`, loads details via `CustomUserDetailsService`, and sets the socket `Principal` context.

---

### Step 9: Messaging Permission Check & Chat Broadcast
1. **Trigger**: User A sends a real-time chat message to User B.
2. **Execution Path**:
   - Client sends a STOMP frame with payload `ChatMessageRequestDTO` to endpoint `/app/chat.send`.
   - [ChatController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/ChatController.java) intercepts this frame.
   - **Key Concept: Communication Guard Rules**: To prevent spamming, the `ChatService` checks the database for two conditions:
     1. Is there an `ACCEPTED` exchange request between the two users?
     2. OR, do they have an active scheduled learning session?
     If neither condition is met, the message is blocked.
   - If authorized, the message is persisted to the database and broadcasted to `/topic/chat/{conversationId}` using `SimpMessagingTemplate` so both users receive the text instantly on their UI screen.

---

### Step 10: Feedback & Rating Aggregation
1. **Trigger**: The scheduled session takes place, after which it is marked completed and reviewed.
2. **Execution Path**:
   - A user completes the session via `PUT /api/sessions/{id}/complete`.
   - A user leaves feedback via `POST /api/reviews` containing the session ID, rating (1-5), and comments.
   - **Key Concept: Self-Review & Duplicate Guard Rails**: The service checks that the review writer is indeed a participant in the session, that they are not reviewing themselves, and that they haven't already left a review for this specific session.
   - Upon saving the review, the system triggers a recalculation of the reviewee's average rating (which is stored on their profile page).

---

### Step 11: Real-time UI Summary (Dashboard)
1. **Trigger**: The user views their home dashboard.
2. **Execution Path**:
   - Client hits `GET /api/dashboard`.
   - [DashboardController](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/controller/DashboardController.java) calls `DashboardService`.
   - The service aggregates database metrics: total skills configured, total reciprocal matches found, counts of pending connection requests, count of upcoming sessions, and the user's average rating.
   - It returns the counts inside a `DashboardResponseDTO` to populate the frontend home widgets.

---

## 8. Containerization & Deployment Architecture (Docker & Docker Compose)

### A. General Concepts
In modern microservices architecture, we package application code, dependencies, runtime environments, and configuration into immutable units called **Containers**.
* **Multi-Stage Build**: In a production-grade Dockerfile, we separate the build pipeline from the runtime execution. This ensures our final deployable image contains only the runtime binaries and JRE, leaving behind large build tools (like Maven, Node compiler libraries), which dramatically reduces the image size and vulnerability surface.
* **Isolated Networking**: We construct a private bridge network. Containers address each other by their declared container/service names. Docker's internal DNS automatically translates these names to dynamically allocated private container IPs.
* **Database Seeding**: Initial database values (such as default lists of universal registry items) are seeded automatically on application bootstrap if the tables are clean. This provides an out-of-the-box working application for developers and automated environments.
* **Case-Insensitive Deduplication**: Ensuring database fields that must be unique (such as user-submitted names or tags) are checked case-insensitively before insertion. If a duplicate is submitted, the system adapts to retrieve the existing one rather than erroring out.

---

### B. Annotations & Core Elements
* `CommandLineRunner`: A functional Spring Boot interface used to indicate that a bean should run its execution block when it is fully initialized by the Spring Application context.
* `@Component`: Automatically detects and registers custom class beans during classpath scanning.
* `${PROPERTY_NAME:DEFAULT_VALUE}`: Spring property value syntax supporting environment overrides with fallback default values.
* `findByNameIgnoreCase()`: Spring Data JPA query method keyword that compiles an SQL statement with `LOWER(column) = LOWER(?)` for case-insensitive records searches.

---

### C. Project Application

#### 1. Backend [Dockerfile](file:///c:/TRAINING/TalentTrade/Dockerfile)
A multi-stage build:
* **Stage 1 (Build)**: Leverages `maven:3.9.6-eclipse-temurin-21-alpine` to load configurations, run dependency go-offline resolving (`mvn dependency:go-offline` cached layer), and compiles the runnable JAR.
* **Stage 2 (Runtime)**: Uses JRE alpine `eclipse-temurin:21-jre-alpine`, configures a custom non-root system user `spring`, and runs the backend using container-aware RAM settings (`-XX:MaxRAMPercentage=75.0`).

#### 2. Frontend [frontend/Dockerfile](file:///c:/TRAINING/TalentTrade/frontend/Dockerfile) and [frontend/nginx.conf](file:///c:/TRAINING/TalentTrade/frontend/nginx.conf)
* **Node Build**: Compiles React and Vite templates into static SPA distribution folders (`dist/`).
* **Nginx Alpine**: Acts as the production web server serving React assets and proxying `/api` and `/ws` WebSocket requests to the backend `app:8080` internally.

#### 3. [docker-compose.yml](file:///c:/TRAINING/TalentTrade/docker-compose.yml)
Coordinates three services:
1. `db`: Run in `postgres:16-alpine` on private port 5432, storing persistence records on named volume `postgres_data`, and exposing a `pg_isready` healthcheck.
2. `app`: Built from root context, starting only when `db` is reported healthy, resolving credentials from `.env`, and running on port 8080.
3. `frontend`: Built from `./frontend` context, running Nginx on port 80 to proxy traffic to the app backend.

#### 4. [DataInitializer.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/config/DataInitializer.java)
Seeds 18 default popular skills on startup:
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final SkillRepository skillRepository;

    @Override
    public void run(String... args) {
        // Seeds core popular skills across categories case-insensitively
        seedSkills();
    }
}
```

#### 5. Graceful Deduplication in [SkillService.java](file:///c:/TRAINING/TalentTrade/src/main/java/com/talenttrade/service/SkillService.java)
```java
    @Transactional
    public SkillResponseDTO createSkill(SkillDTO skillDTO) {
        String nameTrimmed = skillDTO.getName().trim();
        Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(nameTrimmed);
        if (existingSkill.isPresent()) {
            return mapToResponseDTO(existingSkill.get()); // Adapt and reuse existing
        }
        // Save new skill if not present
        Skill savedSkill = skillRepository.save(newSkill);
        return mapToResponseDTO(savedSkill);
    }
```


