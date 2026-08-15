package com.quiz;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {

    private static final int PORT = 8080;

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/upload", new UploadHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("--------------------------------------------------");
        System.out.println("  Server running at: http://localhost:" + PORT + "/");
        System.out.println("  Database: PostgreSQL (EDI)");
        System.out.println("--------------------------------------------------");
    }

    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }

            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                    "<title>Quiz Question Importer</title>" +
                    "<style>" +
                    "body { font-family: sans-serif; background: #f4f6f8; padding: 40px; text-align: center; }" +
                    ".card { background: #fff; max-width: 500px; margin: 0 auto; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                    "h2 { color: #2c3e50; margin-bottom: 10px; }" +
                    ".box { border: 2px dashed #3498db; padding: 25px; margin: 20px 0; border-radius: 6px; background: #f7fbfe; }" +
                    "button { background: #27ae60; color: #fff; border: none; padding: 10px 20px; font-weight: bold; border-radius: 4px; cursor: pointer; }" +
                    "button:hover { background: #219150; }" +
                    "</style></head><body>" +
                    "<div class='card'>" +
                    "<h2>Quiz Question Bulk Importer</h2>" +
                    "<p>Select a CSV file to upload questions to database <strong>EDI</strong></p>" +
                    "<form action='/upload' method='POST' enctype='multipart/form-data'>" +
                    "<div class='box'>" +
                    "<input type='file' name='file' accept='.csv' required><br><br>" +
                    "<button type='submit'>Upload Questions</button>" +
                    "</div></form></div></body></html>";

            sendHtml(ex, html);
        }
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }

            byte[] body = readBytes(ex.getRequestBody());
            byte[] fileBytes = extractFile(body);

            CsvImporter importer = new CsvImporter();
            Question.ImportStats stats = importer.importCsv(new ByteArrayInputStream(fileBytes), 1);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Import Report</title>")
                .append("<style>body { font-family: sans-serif; background: #f4f6f8; padding: 40px; } ")
                .append(".card { background: #fff; max-width: 600px; margin: 0 auto; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); } ")
                .append(".badge { font-size: 20px; font-weight: bold; } .ok { color: #27ae60; } .err { color: #e74c3c; } ")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 15px; } th, td { padding: 8px; border-bottom: 1px solid #ddd; text-align: left; } ")
                .append("a { display: inline-block; margin-top: 20px; color: #3498db; text-decoration: none; font-weight: bold; }</style></head><body>")
                .append("<div class='card'><h2>Import Report (Database: EDI)</h2>")
                .append("<p><span class='badge ok'>").append(stats.success).append("</span> / ").append(stats.total).append(" questions imported successfully.</p>")
                .append("<p><strong>Lookups Created:</strong> Subjects: ").append(stats.subjectsCreated)
                .append(", Units: ").append(stats.unitsCreated)
                .append(", Topics: ").append(stats.topicsCreated).append("</p>");

            if (stats.errors > 0) {
                html.append("<h4 class='err'>Validation Errors (").append(stats.errors).append(")</h4><table><tr><th>Line</th><th>Error</th></tr>");
                for (Question.ImportStats.RowError err : stats.errorList) {
                    html.append("<tr><td>Line ").append(err.line).append("</td><td>").append(err.msg).append("</td></tr>");
                }
                html.append("</table>");
            }

            html.append("<a href='/'>← Back to Upload</a></div></body></html>");

            sendHtml(ex, html.toString());
        }

        private byte[] extractFile(byte[] body) {
            String str = new String(body, StandardCharsets.ISO_8859_1);
            int idx = str.indexOf("\r\n\r\n");
            if (idx != -1) {
                int start = idx + 4;
                int end = str.lastIndexOf("\r\n--");
                if (end > start) {
                    byte[] f = new byte[end - start];
                    System.arraycopy(body, start, f, 0, f.length);
                    return f;
                }
            }
            return body;
        }

        private byte[] readBytes(InputStream is) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;
            while ((n = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, n);
            }
            return buffer.toByteArray();
        }
    }

    private static void sendHtml(HttpExchange ex, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
