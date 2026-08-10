# Question Bank & Quiz Generator - Backend Logic Layer

Welcome! This repository contains the **Core Business Logic Layer**, **Data Access Objects (DAO)**, **Quiz Paper Randomizer Engine**, and **Instant Auto-Grading Engine** for the **Question Bank & Quiz Generator** platform.

This guide explains everything built in this module and provides a step-by-step integration walkthrough written for beginners.

---

## What is Inside This Repository (What Built)

All source code is organized into standard, modular Java packages under `src/`:

```
src/
├── admin/                  # Admin logic for student entry,teacher entry and updates.
├── model/                  # Data structures (QuestionBank, Question, QuestionOption, Quiz, QuizAttempt, etc.)
├── service/                # Business logic rules, validations, quiz paper generator, & auto-grading
├── dao/                    # Database queries (SQL PreparedStatements for PostgreSQL)
├── validator/              # Validation checks (10-char stem limit, 4 MCQ options, 1 correct answer)
├── util/                   # Connection helpers (DBConnection.java) & JSON API wrappers (ApiResponse.java)
├── exception/              # ValidationException handling
├── constant/               # Status codes & PostgreSQL Database Schema (Database.sql)
```

---

## How the Business Workflows Work

The system supports 3 primary roles: **Teacher**, **Moderator**, and **Student**.

```
[ Teacher ] ──> Create Bank ──> Add Questions ──> Submit for Review
                                                       │
[ Moderator ] ─────────────────────────────────────────┴──> Inspect & Edit Questions ──> Approve / Reject
                                                                                               │
┌──────────────────────────────────────────────────────────────────────────────────────────────┘
│
├──> If DESCRIPTIVE Bank ──> Teacher clicks "Make Live" ──> [ Student ] Views Bank for Study
│
└──> If MCQ Bank ──────────> Teacher clicks "Generate Quiz" ──> [ Student ] Takes Quiz ──> Auto-Grading Score
```

### 1. Teacher Workflow
- Creates a Question Bank (`MCQ` or `DESCRIPTIVE`) bound to a Subject and Academic Year.
- Adds questions with **Bloom's Taxonomy Levels (BTL)** and marks. For MCQ questions, adds **4 choices** and marks **1 choice as correct**.
- Submits the Question Bank for moderation review (`DRAFT` ➔ `SUBMITTED`).
- If approved (`APPROVED`):
  - **Descriptive Bank**: Clicks **Make Live** (`LIVE`) to make it readable for students.
  - **MCQ Bank**: Clicks **Generate Quiz** to create a randomized exam paper.
- Views the **Student Performance & Scores Leaderboard** for assigned quizzes.

### 2. Moderator Workflow
- Views all Question Banks pending review (`SUBMITTED` status).
- Inspects questions and option choices. Can **edit question text, BTL levels, marks, or correct option selection** directly during review.
- Gives **Approval** (`SUBMITTED` ➔ `APPROVED`) or **Rejection** (`SUBMITTED` ➔ `REJECTED`).

### 3. Student Workflow
- Views published **Live Descriptive Question Banks** for practice/study.
- Takes scheduled **MCQ Quizzes**.
- Submits answers and receives **instant auto-graded score** (`scoreObtained / totalMarks`).
- Once submitted, the attempt is locked (`COMPLETED`) to prevent re-taking the quiz.

---

## 🛠️ What the Integration Team Needs to Do (Step-by-Step Guide)

If you are on the **Integration Team** connecting Frontend UI, Login Authentication, or Web Controllers, follow these 3 steps:

### Step 1: Set Up the PostgreSQL Database
1. Create a PostgreSQL database named `question_bank` (or set environment variable `DB_URL`).
2. Run the SQL script located at `constant/Database.sql` in your database. This creates all necessary tables (`question_bank`, `question`, `question_option`, `quiz`, `quiz_question`, `quiz_attempt`, `student_answer`).

---

### Step 2: Connect Login / User Authentication
The Login module written by your teammate will authenticate users and return `userId`, `role`, and `subjectId`.

Simply pass those IDs directly into the service methods:

```java
// Example: After Teacher Login (userId = 101, subjectId = 1)
QuestionBankService bankService = new QuestionBankServiceImpl();

// 1. Fetch teacher's question banks
List<QuestionBank> teacherBanks = bankService.getQuestionBanksByTeacher(101);

// Example: After Student Login (studentId = 201)
QuizAttemptService attemptService = new QuizAttemptServiceImpl();

// 2. Start quiz attempt for student #201
int attemptId = attemptService.startQuizAttempt(quizId, 201);
```

---

### Step 3: Connect Frontend to Backend APIs

You have **two options** to connect your web frontend (React, HTML/JS, Angular, Vue):

#### Option A: Use the Included `AppServer.java` (Zero Setup)
Simply run [AppServer.java](AppServer.java). It launches a REST server on `http://localhost:8080` with CORS enabled:

```bash
java AppServer.java
```

---

## 🔌 Service Method & REST Endpoint Cheatsheet

| Task | Java Service Method | REST Endpoint |
| :--- | :--- | :--- |
| **Create Bank** | `bankService.createQuestionBank(bank)` | `POST /api/question-banks` |
| **Get Teacher Banks** | `bankService.getQuestionBanksByTeacher(teacherId)` | `GET /api/question-banks/teacher/{teacherId}` |
| **Submit Bank** | `bankService.submitQuestionBank(bankId)` | `PUT /api/question-banks/{id}/submit` |
| **Approve Bank** | `bankService.approveQuestionBank(bankId)` | `PUT /api/question-banks/{id}/approve` |
| **Reject Bank** | `bankService.rejectQuestionBank(bankId)` | `PUT /api/question-banks/{id}/reject` |
| **Make Live** | `bankService.publishQuestionBank(bankId)` | `PUT /api/question-banks/{id}/publish` |
| **Add Question** | `questionService.createQuestion(question)` | `POST /api/questions` |
| **Save MCQ Options** | `optionService.saveOptions(optionsList)` | `POST /api/questions/{id}/options` |
| **Generate Quiz** | `quizService.generateRandomQuiz(quizId, criteria)` | `POST /api/quizzes/{id}/generate` |
| **Start Quiz Attempt** | `attemptService.startQuizAttempt(quizId, studentId)` | `POST /api/attempts/start` |
| **Submit & Auto-Grade** | `attemptService.submitAndAutoGradeQuiz(attemptId, answersMap)` | `POST /api/attempts/{id}/submit` |
| **Quiz Score Leaderboard** | `attemptService.getQuizResultsForTeacher(quizId)` | `GET /api/attempts/quiz/{id}/results` |

---

## ⚙️ Environment Variables (Optional)

Configure custom database credentials without changing code:

- `DB_URL` (Default: `jdbc:postgresql://localhost:5432/question_bank`)
- `DB_USER` (Default: `postgres`)
- `DB_PASSWORD` (Default: `postgres` or your database password)

---

### ✨ Summary
This backend logic layer is fully verified, self-contained, and ready to be integrated into any web framework or frontend interface.
