package com.quiz;

import java.util.ArrayList;
import java.util.List;

public class Question {

    public int lineNum;
    public String text;
    public String subject;
    public String unit;
    public String topic;
    public String author;
    public double marks = 1.0;
    public String btl = "L1";
    public String type = "MCQ";
    public String status = "ACTIVE";
    public List<Option> options = new ArrayList<>();

    public static class Option {
        public String text;
        public boolean isCorrect;

        public Option(String text, boolean isCorrect) {
            this.text = text;
            this.isCorrect = isCorrect;
        }
    }

    public static class ImportStats {
        public int total = 0;
        public int success = 0;
        public int errors = 0;
        public int subjectsCreated = 0;
        public int unitsCreated = 0;
        public int topicsCreated = 0;
        public List<RowError> errorList = new ArrayList<>();

        public static class RowError {
            public int line;
            public String msg;

            public RowError(int line, String msg) {
                this.line = line;
                this.msg = msg;
            }
        }

        public void addError(int line, String msg) {
            errors++;
            errorList.add(new RowError(line, msg));
        }
    }
}
