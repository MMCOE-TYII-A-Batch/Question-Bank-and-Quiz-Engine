# CSV Question Import Utility

A Java-based utility developed for the **Question Bank and Quiz Engine** to automate the bulk import of descriptive questions from a CSV file into the PostgreSQL database.

The importer parses a structured CSV file, validates the data, maps it with existing database entities, and inserts the records while maintaining relationships between subjects, units, topics, authors, and questions.

---

## Features

- Bulk import of descriptive questions from CSV
- Automatic mapping of:
  - Subject
  - Unit
  - Topic
  - Author (Faculty)
- Reuses existing master records where applicable
- Maintains relational integrity using foreign keys
- Displays detailed import summary after execution
- Continues importing even if invalid rows are encountered

---

## Folder Structure

```text
CSVExtractor/
│
├── lib/
│   ├── commons-lang3-3.17.0.jar
│   ├── opencsv-5.9.jar
│   └── postgresql-42.7.7.jar
│
├── src/
│   └── csvimport/
│       └── CSVQuestionImporter.java
│
└── questions.csv
```

---

## CSV Format

The CSV file must contain the following columns:

| Column | Description |
|--------|-------------|
| Question | Descriptive question |
| Topic | Topic name |
| Unit | Unit number |
| Subject | Subject name |
| Author | Faculty name |

### Example

```csv
Question,Topic,Unit,Subject,Author
Analyze and evaluate the role of a Database Administrator (DBA).,Database Administrator (DBA) Role,1,Database Management Systems,Ms. S.H. Kuche
```

---

## Database Requirements

The following tables must already exist:

- user
- subject
- unit
- topic
- question

### Important

- Faculty names in the CSV must already exist in the `user` table.
- Subjects must exist before units are inserted.
- Units must exist before topics are inserted.
- Topics must exist before questions are inserted.

---

## Configuration

Update the database configuration in `CSVQuestionImporter.java` before running.

```java
private static final String DB_URL = "...";
private static final String DB_USER = "...";
private static final String DB_PASSWORD = "...";
```

---

## Import Workflow

```text
questions.csv
      │
      ▼
Read CSV Records
      │
      ▼
Validate Data
      │
      ▼
Find/Create Subject
      │
      ▼
Find/Create Unit
      │
      ▼
Find/Create Topic
      │
      ▼
Find Author
      │
      ▼
Insert Question
      │
      ▼
Display Import Summary
```

---

## Running the Importer

Compile and execute:

```text
CSVQuestionImporter.java
```

The program automatically reads:

```text
questions.csv
```

and imports all valid records into the PostgreSQL database.

---

## Sample Output

```text
--- Import Summary ---

Total rows parsed: 693
Successfully imported: 693
Skipped rows: 0
Failed rows: 0
```

---

## Notes

- Existing subjects are reused.
- Existing units are reused.
- Existing topics are reused.
- Questions are linked with the corresponding faculty.
- Duplicate master records are avoided.
- Invalid rows are skipped without stopping the import process.

---

## Dependencies

- Java
- PostgreSQL JDBC Driver
- OpenCSV
- Apache Commons Lang