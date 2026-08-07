# Question-Bank-and-Quiz-Engine

This repo currently contains the frontend prototype static HTML,CSS,JS pages styled to a shared maroon and gold theme, with mock data and localStorage free in memory state standing in for the backend until the API is wired up


Project objectives
Centralize question storage across subjects, units, and topics
Simplify quiz creation with reusable question pools
Provide immediate scoring, feedback, and result analysis
Enforce role-based access and an approval workflow before questions go live
Keep a full audit trail of account and content actions

Roles
Role	Responsibilities

Administrator	Manage user accounts, assign roles, view audit logs
Question Author	Create MCQ / Descriptive questions manually or via CSV import
Moderator / Reviewer	Approve, reject, or return questions before they enter the live bank
Exam Designer	Assemble approved questions into scheduled quizzes
Quiz Taker	Attempt timed quizzes and view results

Pages in this repository
File	Description
admin-dashboard.html	Admin panel — create/edit/activate/deactivate/delete users, filter by role & status, and a per-user activity timeline. Includes an in-session Audit Log view (mirrors the AUDIT_LOG table: log_id, user_id, action, timestamp).

create-question-bank.html	Author flow for building a question batch — choose MCQ or Descriptive, then add questions manually or via CSV upload, review them in a staged batch table, and submit for moderation.

Pages not yet built: Moderator review queue, Quiz attempt engine, Analytics & reports.

Create Question Bank — details
Step 1: pick question type (MCQ or Descriptive)
Step 2: add questions manually or upload a CSV
Manual entry fields: subject_name, unit, topic_name, question, author, marks, BTL — MCQ adds four options (A–D) plus a required correct-answer selection
CSV import expects headers matching the template exactly:
Descriptive: subject_name,Question,unit,topic_name,author,marks,BTL
MCQ: subject_name,Question,unit,topic_name,author,marks,BTL,option_a,option_b,option_c,option_d,correct_answer
A "Download CSV template" button generates the correct header row + a sample row for the selected type
Rows are validated (missing fields, invalid correct_answer) before import, with per-row error messages
All manually- and CSV-added questions land in one shared batch queue table, which is what gets submitted for moderation
