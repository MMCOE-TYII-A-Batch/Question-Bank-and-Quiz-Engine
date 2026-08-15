# Quiz CSV Import

## Overview
A lightweight, high-performance, pure Java application to bulk import quiz questions from CSV files into a PostgreSQL database (`EDI`). It validates incoming data, automatically creates missing hierarchy lookups (subjects, units, topics), and provides both a command-line interface and a built-in web interface for seamless CSV uploading.

## Features
- **Pure Java Implementation**: Built using standard Core Java SE and JDBC with zero heavy frameworks (no Spring Boot, no Tomcat).
- **Automated Lookup Resolution**: Dynamically resolves and creates missing `subject`, `unit`, and `topic` records on the fly.
- **Transactional Consistency**: Executes question and option insertions inside atomic database transactions (`setAutoCommit(false)`).
- **Robust Validation Engine**: Ensures question text, option counts, positive default marks, and correct option matching.
- **Built-in Web UI & CLI**: Web upload page at `http://localhost:8080/` and direct terminal CLI runner.

## Technologies Used
- **Java SE (17+)**: Core language logic, streaming I/O, and built-in HTTP server (`com.sun.net.httpserver`).
- **PostgreSQL**: Relational database storage.
- **JDBC (PostgreSQL Driver 42.7.2)**: Direct database connectivity and transaction control.
- **HTML5 & CSS3**: Basic Web UI showcase interface.

## Project Structure
```
quiz-csv-import/
├── bin/                                     # Compiled Java bytecode files (auto-generated)
├── lib/
│   └── postgresql-42.7.2.jar                # PostgreSQL JDBC Driver
├── src/
│   └── com/
│       └── quiz/
│           ├── Main.java                    # Entry point (Web server & CLI launcher)
│           ├── CsvImporter.java             # CSV parsing, validation, lookups, and DB insertion
│           ├── Db.java                      # PostgreSQL JDBC connection manager
│           ├── Question.java                # Data models and import statistics POJO
│           └── Server.java                  # Built-in Web UI server
├── .gitignore                               # Git ignored files configuration
├── README.md                                # Project documentation
└── run.bat                                  # Double-clickable Windows batch launcher
```

## Database Tables Used
The pipeline inserts data into the following tables in the PostgreSQL **`EDI`** database:
- **`subject`**: Stores subject details (`subject_id`, `subject_name`, timestamps).
- **`unit`**: Stores subject units (`unit_id`, `subject_id`, `unit_name`, timestamps).
- **`topic`**: Stores unit topics (`topic_id`, `unit_id`, `topic_name`, timestamps).
- **`user`**: Stores authors (`user_id`, `username`, `email`, `password_hash`, `status`).
- **`question`**: Stores quiz questions (`question_id`, `question`, `topic_id`, `author_id`, `question_type`, `btl_level`, `default_marks`, `status`, timestamps).
- **`question_option`**: Stores multiple choice options (`option_id`, `question_id`, `option_text`, `is_correct`, timestamps).

## CSV Format
The CSV file should contain the following headers (matching `MCQ_Questions_Data.csv`):

| Column | Required | Example | Description |
| :--- | :--- | :--- | :--- |
| `question` | **Yes** | `"What is Software Engineering?"` | The question prompt text |
| `unit` | No | `1` or `"Unit 1"` | Unit number or title |
| `topic_name` | No | `"Software Process Models"` | Topic name |
| `author` | No | `"Dr. Geetha Chillarge"` | Author teacher name |
| `marks` | No | `1` | Default marks (positive number) |
| `BLT` | No | `L1` or `1` | Bloom's Taxonomy Level |
| `subject_name` | No | `"SE"` | Subject code / name |
| `option_a` | **Yes** | `"Designing software"` | Option A text |
| `option_b` | **Yes** | `"Testing software"` | Option B text |
| `option_c` | No | `"Application of engineering principles"` | Option C text |
| `option_d` | No | `"None of the above"` | Option D text |
| `correct_option` | **Yes** | `C` | Correct option key (`A`, `B`, `C`, `D`, `1`, `2`, or text) |

## Import Workflow
1. **File Input**: The user uploads a `.csv` file via the Web UI or command line.
2. **CSV Parsing**: `CsvImporter` reads raw lines and parses quoted fields using RFC 4180 rules.
3. **Validation**: Each row is validated for required question text, valid marks, and matching correct option keys.
4. **Lookup Resolution**:
   - Fetches or creates `subject_id` in table `subject`.
   - Fetches or creates `unit_id` linked to the subject in table `unit`.
   - Fetches or creates `topic_id` linked to the unit in table `topic`.
   - Resolves or creates author `user_id` in table `user`.
5. **Database Transaction**:
   - Inserts question record into `question`.
   - Batch inserts options into `question_option`.
   - Commits transaction (`conn.commit()`) or rolls back on error (`conn.rollback()`).
6. **Reporting**: Generates execution metrics (total processed, successful count, error count, and row error breakdown).

## Validation Rules
- **Question Text**: Must not be empty or blank.
- **Marks**: Must be a valid positive numeric value (`default_marks > 0`). Defaults to `1.0` if omitted.
- **Options**: At least 2 non-empty options must be present per question.
- **Correct Option Matching**: `correct_option` (e.g. `A`, `B`, `C`, `D` or `1`, `2`, `3`, `4`) must match at least one option. If an option cell is blank in CSV but marked as correct, it defaults to `"None of the mentioned"`.

## How to Run

### Method 1: Double-Click `run.bat` (Windows)
Double-click `run.bat` in the project root folder. It compiles the project, starts the web server, and automatically opens `http://localhost:8080/` in your default browser.

### Method 2: Command Line Interface
```powershell
# 1. Compile Java files
if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin }
javac -cp "lib/postgresql-42.7.2.jar" -d bin src/com/quiz/*.java

# 2. Run Web UI Server (or pass CSV file path for direct CLI run)
java -cp "bin;lib/postgresql-42.7.2.jar" com.quiz.Main
```

## Sample CSV
```csv
question,unit,topic_name,author,marks,BLT,subject_name,option_a,option_b,option_c,option_d,correct_option
"Software is defined as ___________",1,"Introduction to Software Engineering","Dr. Geetha Chillarge",1,L1,SE,"set of programs, documentation & configuration data",set of programs,documentation and configuration of data,None of the mentioned,A
"What is Software Engineering?",1,"Introduction to Software Engineering","Dr. Geetha Chillarge",1,L1,SE,Designing a software,Testing a software,Application of engineering principles to the design of software,None of the above,C
"CASE stands for",1,"Introduction to Software Engineering","Vandana Rupnar",1,L2,SE,Computer-Aided Software Engineering,Control Aided Science and Engineering,Cost Aided System Experiments,None of the mentioned,A
```

## Output

### Command Line Output Example
```text
Processing CSV: MCQ_Questions_Data.csv
--------------------------------------------------
Total Rows:  594
Imported:    594
Errors:      0
Subjects:    3
Units:       5
Topics:      12
--------------------------------------------------
SUCCESS: All questions imported with 0 errors!
```

### Web Interface Output
When uploading via `http://localhost:8080/`, an interactive summary report card displays total processed rows, successfully imported questions, created lookups count, and a table detailing any row validation errors.
