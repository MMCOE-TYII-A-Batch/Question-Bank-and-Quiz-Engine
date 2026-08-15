package com.quiz;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            try {
                Server.start();
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        } else {
            File csvFile = new File(args[0]);
            System.out.println("Processing CSV: " + csvFile.getAbsolutePath());

            CsvImporter importer = new CsvImporter();
            Question.ImportStats stats = importer.importCsv(csvFile);

            System.out.println("--------------------------------------------------");
            System.out.println("Total Rows:  " + stats.total);
            System.out.println("Imported:    " + stats.success);
            System.out.println("Errors:      " + stats.errors);
            System.out.println("Subjects:    " + stats.subjectsCreated);
            System.out.println("Units:       " + stats.unitsCreated);
            System.out.println("Topics:      " + stats.topicsCreated);
            System.out.println("--------------------------------------------------");

            if (stats.errors > 0) {
                for (Question.ImportStats.RowError err : stats.errorList) {
                    System.out.println("Line " + err.line + ": " + err.msg);
                }
            } else {
                System.out.println("SUCCESS: All questions imported with 0 errors!");
            }
        }
    }
}
