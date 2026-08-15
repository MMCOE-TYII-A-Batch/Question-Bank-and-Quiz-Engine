# Question Bank & Quiz Engine — Backend

The backend of the **Question Bank & Quiz Engine** provides the application services, business logic, REST APIs, database access, authentication, question-bank management, quiz generation, grading, audit logging, and syllabus management.

## Architecture

```text
Frontend
   │
   ▼
REST API / AppServer
   │
   ▼
Service Layer
   │
   ▼
DAO Layer
   │
   ▼
PostgreSQL
```

The backend follows a layered architecture:

* **Model** — application data structures
* **DAO** — database access and persistence
* **Service** — business rules and application logic
* **Validator** — input and domain validation
* **Util** — database and security utilities
* **Admin** — administrative user-management operations

## Features

### Authentication & User Management

* Role-based authentication
* User management for supported roles
* Password reset and forgot-password workflows
* Secure password handling
* Logout and authentication audit logging

### Question Bank Management

* Question-bank creation and lifecycle management
* Question and MCQ option management
* Moderator review and approval workflow
* Question classification by syllabus and assessment attributes

### Quiz Generation & Auto-Grading

* Randomized quiz generation
* Question selection based on configured criteria
* MCQ attempt management
* Automatic grading and score calculation
* Attempt completion and duplicate-attempt protection

### Subject & Syllabus Management

* Subject management
* Unit/Chapter and Topic hierarchy
* Question-to-topic mapping
* Syllabus version management
* Active/inactive syllabus versions
* Question quotas for units and topics

### Audit Logging

* Persistent logging of authentication and administrative actions
* User and timestamp tracking
* Logout event recording

### Bulk Question Import

* CSV-based descriptive-question import
* Subject, unit, topic, and faculty mapping
* Database relationship validation
* Import status and error reporting

## Project Structure

```text
backend/
├── src/
│   ├── admin/
│   ├── constants/
│   ├── dao/
│   ├── exception/
│   ├── model/
│   ├── service/
│   ├── util/
│   └── validator/
│
├── csvimport/
├── quiz-csv-import/
└── README.md
```

## API Modules

| Module         | Base Endpoint         |
| -------------- | --------------------- |
| Authentication | `/api/auth`           |
| Administration | `/api/admin`          |
| Questions      | `/api/questions`      |
| Question Banks | `/api/question-banks` |
| Quizzes        | `/api/quizzes`        |
| Quiz Attempts  | `/api/attempts`       |
| Audit Logs     | `/api/audit-logs`     |
| Syllabus       | `/api/syllabus`       |

## Database Configuration

Configure the PostgreSQL connection using environment variables:

```text
DB_URL
DB_USER
DB_PASSWORD
```

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/question_bank
DB_USER=postgres
DB_PASSWORD=your_password
```

Do not commit real credentials or secrets.

## Running the Backend

From the `backend` directory:

```bash
javac -cp "postgresql-42.7.3.jar" -d out <source-files>
```

Run the application:

```bash
java -cp "postgresql-42.7.3.jar;out" AppServer
```

The server starts on the configured application port.

## CSV Import

The CSV import utility is located in:

```text
csvimport/
```

It imports descriptive questions into PostgreSQL while resolving their syllabus and faculty relationships.

## Syllabus Hierarchy

```text
Subject
   │
   └── Unit / Chapter
          │
          └── Topic
                 │
                 └── Question
```

Syllabus versions allow multiple course structures to be maintained while preserving historical versions. Question quotas define the expected question allocation for units and topics.

## Development Guidelines

* Follow the existing DAO → Service → Validator architecture.
* Reuse existing models and database relationships.
* Keep authentication and database credentials outside source control.
* Do not commit compiled artifacts such as `.class` files or `out/`.
* Keep backend changes modular and focused.
