# Question Bank & Quiz Generator - Integration Layer

## Overview

The **Integration Layer** connects the Frontend, Backend Logic Layer and PostgreSQL Database into a single working application. It exposes REST APIs, routes HTTP requests to the appropriate backend services, processes JSON request and response data, and ensures seamless communication between all project modules.

---

## Objectives

The Integration Layer is responsible for:

- Integrating the Frontend with the Backend Logic Layer.
- Exposing REST APIs for application features.
- Routing incoming HTTP requests to backend services.
- Processing JSON request and response data.
- Coordinating communication with the PostgreSQL database.
- Returning standardized success and error responses.

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Programming Language | Java |
| API Architecture | REST APIs |
| Data Format | JSON |
| Database | PostgreSQL |
| Database Connectivity | JDBC |
| HTTP Server | Java HttpServer |
| Version Control | Git & GitHub |

---

## Integration Workflow

```text
Frontend
    │
    ▼
REST API Request
    │
    ▼
Integration Layer
(AppServer & API Handlers)
    │
    ▼
Business Logic Layer
    │
    ▼
DAO Layer
    │
    ▼
PostgreSQL Database
    │
    ▼
JSON Response
    │
    ▼
Frontend
```

---

## Responsibilities

The Integration Layer performs the following tasks:

- Connects the frontend with backend services.
- Exposes REST endpoints.
- Routes requests to service classes.
- Processes JSON requests and responses.
- Coordinates communication with the database.
- Handles API exceptions and error responses.
- Enables Cross-Origin Resource Sharing (CORS).

---

## REST API Documentation

The Integration Layer exposes RESTful APIs that enable communication between the Frontend and Backend Logic Layer. These APIs exchange data in JSON format and route client requests to the appropriate business logic services. The following REST APIs are available through the Integration Layer.

## Question Bank APIs

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/question-banks/submitted` | Retrieve submitted question banks |
| GET | `/api/question-banks/teacher/{teacherId}` | Retrieve question banks created by a teacher |
| GET | `/api/question-banks/{id}` | Retrieve a specific question bank |
| POST | `/api/question-banks` | Create a new question bank |
| PUT | `/api/question-banks/{id}/submit` | Submit a question bank for moderator review |
| PUT | `/api/question-banks/{id}/approve` | Approve a submitted question bank |
| PUT | `/api/question-banks/{id}/reject` | Reject a submitted question bank |
| PUT | `/api/question-banks/{id}/publish` | Publish an approved question bank |

---

## Question APIs

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/questions/bank/{bankId}` | Retrieve questions from a question bank |
| GET | `/api/questions/{id}/options` | Retrieve options for a question |
| POST | `/api/questions` | Create a new question |
| POST | `/api/questions/{id}/options` | Save MCQ options |
| DELETE | `/api/questions/{id}` | Delete a question |

---

## Quiz APIs

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/quizzes` | Create a new quiz |
| POST | `/api/quizzes/{id}/generate` | Generate a randomized quiz |
| GET | `/api/quizzes/{id}` | Retrieve quiz details |

---

## Quiz Attempt APIs

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/attempts/start` | Start a quiz attempt |
| GET | `/api/attempts/student/{studentId}` | Retrieve student quiz attempts |
| GET | `/api/attempts/quiz/{id}/results` | Retrieve quiz results and leaderboard |

---

## API Response Format

### Success Response

```json
{
  "status": "SUCCESS",
  "message": "Operation completed successfully",
  "data": {}
}
```

### Error Response

```json
{
  "status": "ERROR",
  "message": "Invalid request"
}
```

---

## Observations During Integration

During the integration process the following observations were made:

### Frontend

- Consumes backend services through REST APIs.
- Sends and receives data in JSON format.
- Requires consistent API responses and error handling.

### Backend Logic Layer

- Business logic is organized into modular service classes.
- Validation is performed before database operations.
- Service methods are accessed through REST endpoints.

### Database Layer

- PostgreSQL schema is normalized and uses primary and foreign key relationships.
- Stores users, subjects, questions, quizzes, quiz attempts and student answers.

---

## Integration Challenges

- Integrating independently developed project modules.
- Maintaining consistent API request and response formats.
- Synchronizing frontend validation with backend validation.
- Handling exceptions and standardized error responses.
- Ensuring reliable communication with the PostgreSQL database.

---

## Database Setup

1. Install PostgreSQL and pgAdmin.
2. Create a PostgreSQL database (e.g., question_bank)
3. Execute the `Database.sql` script.
4. Configure database connection details.
5. Start the backend server.

---

## Running the Integration Layer

Run the application server:

```bash
java AppServer.java
```

The server starts at:

```
http://localhost:8080
```

---

## Summary

The Integration Layer serves as a communication link between the Frontend, Backend Logic Layer, and PostgreSQL Database. Through REST APIs and proper request routing, it enables smooth, reliable and efficient communication among all components of the Question Bank & Quiz Generator system.
