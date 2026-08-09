Question Bank & Quiz Engine — Frontend

A frontend prototype for a role-based Question Bank & Quiz Engine. The application provides separate interfaces for administrators, question authors, moderators, and students, with a shared maroon and gold design system.

Overview

The frontend is designed to support the complete user-facing flow of the platform:

User login and role-based navigation
Administrator management interface
Question authoring and question-bank creation
MCQ and Descriptive question support
CSV-based question import
Moderator review interface
Student-facing quiz interface
Consistent UI and interactions across roles
Tech Stack
HTML5 — page structure
CSS3 — styling, layout, components, and responsive design
Vanilla JavaScript — interactions, validation, DOM manipulation, and frontend logic
LocalStorage — temporary browser-side state for the prototype
JSON — mock data representation
Google Fonts — typography

No frontend framework or library is currently used.

Pages
File	Description
index.html	Main entry point
01-login-register.html	Login / registration interface
login.html	Login page
admin.html	Administrator dashboard
author.html	Question Author dashboard
create-question-bank.html	Question-bank creation and submission
moderator.html	Moderator / Reviewer dashboard
student.html	Student / Quiz Taker interface
Main Frontend Features
Login & Role-Based UI

The frontend provides login and role-based navigation for:

Administrator
Question Author
Moderator / Reviewer
Student / Quiz Taker

Different roles are presented with their respective dashboards and available actions.

Administrator Dashboard

The administrator interface includes:

User listing
Create and edit user interfaces
Role management
Account activation/deactivation
User filtering
User activity information
Audit log interface
Question Authoring

Authors can create question banks through a staged workflow.

Supported question types:

MCQ
Descriptive

Common question fields include:

Subject
Unit
Topic
Question
Author
Marks
BTL

MCQs additionally support:

Option A
Option B
Option C
Option D
Correct answer
CSV Import

The question-bank interface supports importing questions from CSV files.

Descriptive format:

subject_name,Question,unit,topic_name,author,marks,BTL

MCQ format:

subject_name,Question,unit,topic_name,author,marks,BTL,option_a,option_b,option_c,option_d,correct_answer

The frontend provides:

CSV template generation
File upload
Row-by-row validation
Missing-field detection
MCQ correct-answer validation
Error messages for invalid rows
Preview of imported questions
Question Batch Management

Manually entered and CSV-imported questions are displayed together in a staged question-bank table.

Authors can review the questions before submitting the complete batch for moderation.

Moderator Interface

The moderator page provides the frontend interface for reviewing submitted question banks.

The interface is designed around actions such as:

Reviewing submitted batches
Viewing question details
Checking question information
Approving question banks
Returning question banks for edits
Adding moderator feedback
Student Interface

The student page provides the frontend interface for the quiz-taker experience, including the foundation for:

Viewing available quizzes
Attempting quizzes
Answering questions
Viewing results
Frontend Workflow
              Login
                │
                ▼
        Role-based Dashboard
                │
     ┌──────────┼──────────┐
     │          │          │
     ▼          ▼          ▼
   Admin      Author    Moderator
     │          │          │
     │          ▼          │
     │    Create Question  │
     │       Bank          │
     │          │          │
     │          ▼          │
     │       Submit ───────┘
     │
     ▼
 User Management

                │
                ▼
             Student
                │
                ▼
          Quiz Interface
UI & Design

The frontend follows a shared visual design across the pages.

Design characteristics
Maroon and gold primary theme
Clean card-based layouts
Consistent buttons and form controls
Dashboard-style navigation
Status indicators
Modal-based review interfaces
Tables for structured data
Responsive layout foundations
Consistent typography and spacing
Project Structure
Frontend/
│
├── index.html
├── 01-login-register.html
├── login.html
├── admin.html
├── author.html
├── create-question-bank.html
├── moderator.html
├── student.html
│
└── README.md



You can run it by:

Clone the repository.
Switch to the Frontend branch.
Open index.html in a browser.

For a better development experience, the project can also be opened using VS Code Live Server.
