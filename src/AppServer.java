import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import admin.AdminService;
import admin.AdminServiceImpl;
import admin.UserDTO;
import dao.AuditLogDAO;
import dao.AuditLogDAOImpl;
import dao.QuestionBankDAO;
import dao.QuestionBankDAOImpl;
import model.QuestionBank;
import model.Quiz;
import service.AuthService;
import service.AuthServiceImpl;
import service.QuestionService;
import service.QuestionServiceImpl;
import service.QuizAttemptService;
import service.QuizAttemptServiceImpl;
import service.QuizService;
import service.QuizServiceImpl;
import service.SyllabusService;
import service.SyllabusServiceImpl;
import model.Subject;
import model.Unit;
import model.Topic;
import util.PasswordUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AppServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        util.DBConnection.initSchema();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/force-change-password", new ForceChangePasswordHandler());
        server.createContext("/api/auth/request-reset", new RequestResetHandler());
        server.createContext("/api/auth/reset-password", new ResetPasswordHandler());
        server.createContext("/api/auth/profile", new ProfileHandler());
        server.createContext("/api/auth/logout", new LogoutHandler());

        server.createContext("/api/admin/users", new AdminUsersHandler());
        server.createContext("/api/audit-logs", new AuditLogsHandler());

        server.createContext("/api/question-banks", new QuestionBankHandler());
        server.createContext("/api/quizzes", new QuizHandler());
        server.createContext("/api/quiz-results", new QuizResultsHandler());
        server.createContext("/api/reset", new ResetHandler());
        server.createContext("/api/syllabus/", new SyllabusHandler());

        // Static Web Server for Frontend (http://localhost:8080/)
        String frontendPath = System.getProperty("user.dir");
        java.io.File feFile = new java.io.File(frontendPath, "frontend");
        if (!feFile.exists()) feFile = new java.io.File(frontendPath, "../frontend");
        server.createContext("/", new StaticFileHandler(feFile.getAbsolutePath()));

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("==================================================");
        System.out.println("  Question Bank & Quiz Engine Full-Stack App Server");
        System.out.println("  Running at: http://localhost:" + PORT + "/login.html");
        System.out.println("==================================================");
    }

    private static void enableCORS(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange ex, int statusCode, String jsonResponse) throws IOException {
        enableCORS(ex);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static class LoginHandler implements HttpHandler {
        private final AuthService authService = new AuthServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
                return;
            }
            try {
                String body = readRequestBody(ex);
                String identifier = extractJsonField(body, "identifier");
                if (identifier == null || identifier.isEmpty()) {
                    identifier = extractJsonField(body, "username");
                }
                String password = extractJsonField(body, "password");

                UserDTO user = authService.login(identifier, password);
                String resp = String.format(
                        "{\"status\":\"success\",\"message\":\"Login successful\",\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"division\":\"%s\",\"subject\":\"%s\",\"forcePasswordChange\":%b}",
                        user.getUserId(), escapeJson(user.getUsername()), escapeJson(user.getEmail()),
                        user.getRoleName() != null ? user.getRoleName() : "Student",
                        user.getDivision() != null ? user.getDivision() : "",
                        user.getSubject() != null ? escapeJson(user.getSubject()) : "",
                        user.isForcePasswordChange()
                );
                sendJsonResponse(ex, 200, resp);
            } catch (Exception e) {
                sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    static class ForceChangePasswordHandler implements HttpHandler {
        private final AuthService authService = new AuthServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
                return;
            }
            try {
                String body = readRequestBody(ex);
                int userId = Integer.parseInt(extractJsonField(body, "userId"));
                String newPassword = extractJsonField(body, "newPassword");

                boolean success = authService.forceChangePassword(userId, newPassword);
                if (success) {
                    sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Password updated successfully\"}");
                } else {
                    sendJsonResponse(ex, 400, "{\"status\":\"error\",\"message\":\"Failed to update password\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    static class RequestResetHandler implements HttpHandler {
        private final AuthService authService = new AuthServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String body = readRequestBody(ex);
                String email = extractJsonField(body, "email");
                boolean success = authService.requestPasswordReset(email);
                sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Password reset link sent\"}");
            } catch (Exception e) {
                sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    static class ResetPasswordHandler implements HttpHandler {
        private final AuthService authService = new AuthServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String body = readRequestBody(ex);
                String token = extractJsonField(body, "token");
                String newPassword = extractJsonField(body, "newPassword");
                boolean success = authService.resetPassword(token, newPassword);
                sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Password reset successfully\"}");
            } catch (Exception e) {
                sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    static class ProfileHandler implements HttpHandler {
        private final AuthService authService = new AuthServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String body = readRequestBody(ex);
                int userId = Integer.parseInt(extractJsonField(body, "userId"));
                String email = extractJsonField(body, "email");
                String department = extractJsonField(body, "department");
                String contactNumber = extractJsonField(body, "contactNumber");
                String subject = extractJsonField(body, "subject");
                String name = extractJsonField(body, "name");
                if (name == null) name = extractJsonField(body, "username");

                UserDTO user = authService.updateProfileFull(userId, email, department, contactNumber, subject, name);
                sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Profile updated successfully\"}");
            } catch (Exception e) {
                sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    static class LogoutHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
                return;
            }
            try {
                String body = readRequestBody(ex);
                String userIdStr = extractJsonField(body, "userId");
                if (userIdStr == null) {
                    userIdStr = extractJsonField(body, "user_id");
                }
                if (userIdStr == null || userIdStr.isEmpty()) {
                    sendJsonResponse(ex, 400, "{\"status\":\"error\",\"message\":\"User ID is required\"}");
                    return;
                }
                int userId = Integer.parseInt(userIdStr);

                String username = extractJsonField(body, "username");
                if (username == null) username = extractJsonField(body, "user_name");
                if (username == null) username = "User";

                boolean loggedInDb = false;
                try {
                    dao.AuditLogDAO auditLogDAO = new dao.AuditLogDAOImpl();
                    auditLogDAO.logAction(userId, "LOGOUT");
                    loggedInDb = true;
                } catch (Exception ignored) {}

                if (!loggedInDb) {
                    String memJson = String.format("{\"logId\":%d,\"userId\":%d,\"username\":\"%s\",\"action\":\"LOGOUT\",\"timestamp\":\"%s\"}",
                            System.currentTimeMillis(), userId,
                            escapeJson(username),
                            java.time.LocalDateTime.now().toString().replace('T', ' ').substring(0, 16));
                    auditLogMemoryRegistry.add(0, memJson);
                }

                sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Logged out successfully\"}");
            } catch (Exception e) {
                sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    static class AdminUsersHandler implements HttpHandler {
        private final AdminService adminService = new AdminServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                try {
                    List<UserDTO> users = adminService.getAllUsers();
                    StringBuilder json = new StringBuilder("[");
                    if (users != null) {
                        for (int i = 0; i < users.size(); i++) {
                            UserDTO u = users.get(i);
                            json.append(String.format("{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"status\":\"%s\",\"division\":\"%s\",\"subject\":\"%s\",\"forcePasswordChange\":%b}",
                                    u.getUserId(), escapeJson(u.getUsername()), escapeJson(u.getEmail()),
                                    u.getRoleName() != null ? u.getRoleName() : "Student",
                                    u.getStatus() != null ? u.getStatus() : "ACTIVE",
                                    u.getDivision() != null ? u.getDivision() : "",
                                    u.getSubject() != null ? escapeJson(u.getSubject()) : "",
                                    u.isForcePasswordChange()));
                            if (i < users.size() - 1) json.append(",");
                        }
                    }
                    json.append("]");
                    sendJsonResponse(ex, 200, json.toString());
                } catch (Throwable t) {
                    sendJsonResponse(ex, 200, "[]");
                }
                return;
            } else if ("PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                try {
                    String body = readRequestBody(ex);
                    String userIdStr = extractJsonField(body, "userId");
                    String subject = extractJsonField(body, "subject");
                    String role = extractJsonField(body, "role");
                    if (userIdStr != null) {
                        int userId = Integer.parseInt(userIdStr);
                        if (subject != null) {
                            adminService.updateUserSubject(userId, subject, 1);
                        }
                        if (role != null) {
                            adminService.updateUserDesignation(userId, role, 1);
                        }
                    }
                    sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"User updated\"}");
                } catch (Exception e) {
                    sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
                }
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                try {
                    String body = readRequestBody(ex);
                    UserDTO user = new UserDTO();
                    user.setUsername(extractJsonField(body, "username"));
                    user.setEmail(extractJsonField(body, "email"));
                    user.setRoleName(extractJsonField(body, "role"));
                    user.setDivision(extractJsonField(body, "division"));
                    user.setSubject(extractJsonField(body, "subject"));

                    int adminId = 1;
                    int id = adminService.addUser(user, adminId);
                    sendJsonResponse(ex, 200, String.format("{\"status\":\"success\",\"userId\":%d}", id));
                } catch (Exception e) {
                    sendJsonResponse(ex, 400, String.format("{\"status\":\"error\",\"message\":\"%s\"}", escapeJson(e.getMessage())));
                }
            }
        }
    }

    static class AuditLogsHandler implements HttpHandler {
        private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readRequestBody(ex);
                    String userIdStr = extractJsonField(body, "userId");
                    if (userIdStr == null) userIdStr = extractJsonField(body, "user_id");
                    int userId = 1;
                    if (userIdStr != null) {
                        try {
                            long parsedVal = Long.parseLong(userIdStr);
                            userId = (int) (parsedVal > 2147483647L ? (parsedVal % 10000L) : parsedVal);
                        } catch (Exception ignored) {}
                    }
                    String action = extractJsonField(body, "action");
                    String username = extractJsonField(body, "username");
                    if (username == null) username = extractJsonField(body, "user_name");

                    if (action != null && !action.isEmpty()) {
                        boolean loggedInDb = false;
                        try {
                            auditLogDAO.logAction(userId, (username != null ? username + ": " : "") + action);
                            loggedInDb = true;
                        } catch (Exception ignored) {}

                        if (!loggedInDb) {
                            String memJson = String.format("{\"logId\":%d,\"userId\":%d,\"username\":\"%s\",\"action\":\"%s\",\"timestamp\":\"%s\"}",
                                    System.currentTimeMillis(), userId,
                                    escapeJson(username != null ? username : "User"),
                                    escapeJson(action),
                                    java.time.LocalDateTime.now().toString().replace('T', ' ').substring(0, 16));
                            auditLogMemoryRegistry.add(0, memJson);
                        }
                    }
                    sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Audit log created\"}");
                    return;
                }
                List<AuditLogDAO.AuditEntry> logs = new java.util.ArrayList<>();
                try {
                    logs = auditLogDAO.getAllLogs();
                } catch (Exception ignored) {}

                // Merge in-memory logs and DB logs
                List<String> allJsonLogs = new java.util.ArrayList<>(auditLogMemoryRegistry);
                if (logs != null && !logs.isEmpty()) {
                    for (AuditLogDAO.AuditEntry e : logs) {
                        allJsonLogs.add(String.format("{\"logId\":%d,\"userId\":%d,\"username\":\"%s\",\"action\":\"%s\",\"timestamp\":\"%s\"}",
                                e.getLogId(), e.getUserId() != null ? e.getUserId() : 0,
                                escapeJson(e.getUsername() != null ? e.getUsername() : "System"),
                                escapeJson(e.getAction() != null ? e.getAction() : ""),
                                e.getTimestamp() != null ? e.getTimestamp().toString() : ""));
                    }
                }

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                for (String logStr : allJsonLogs) {
                    if (!first) json.append(",");
                    json.append(logStr);
                    first = false;
                }
                json.append("]");
                sendJsonResponse(ex, 200, json.toString());
            } catch (Throwable t) {
                sendJsonResponse(ex, 200, "[]");
            }
        }
    }

    private static final List<String> questionBankRegistry = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<String> quizRegistry = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<String> quizResultsRegistry = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<String> auditLogMemoryRegistry = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        auditLogMemoryRegistry.add("{\"logId\":100,\"userId\":1,\"username\":\"System Admin\",\"action\":\"User \\\"System Admin\\\" signed in\",\"timestamp\":\"2026-08-12 09:30\"}");
        auditLogMemoryRegistry.add("{\"logId\":101,\"userId\":1,\"username\":\"System Admin\",\"action\":\"Updated system security configuration\",\"timestamp\":\"2026-08-12 09:42\"}");
        auditLogMemoryRegistry.add("{\"logId\":102,\"userId\":2,\"username\":\"Dr. Sankirti S. Shiravale\",\"action\":\"User \\\"Dr. Sankirti S. Shiravale\\\" signed in\",\"timestamp\":\"2026-08-12 10:15\"}");
        auditLogMemoryRegistry.add("{\"logId\":103,\"userId\":2,\"username\":\"Dr. Sankirti S. Shiravale\",\"action\":\"Submitted Question Bank \\\"Data Science and Machine Learning — Unit 1\\\" for review\",\"timestamp\":\"2026-08-12 10:41\"}");
        auditLogMemoryRegistry.add("{\"logId\":104,\"userId\":3,\"username\":\"Dr. Kalpana S. Thakre\",\"action\":\"User \\\"Dr. Kalpana S. Thakre\\\" signed in\",\"timestamp\":\"2026-08-12 10:55\"}");
        auditLogMemoryRegistry.add("{\"logId\":105,\"userId\":3,\"username\":\"Dr. Kalpana S. Thakre\",\"action\":\"Submitted Question Bank \\\"Database Management Systems — Unit 2\\\" for review\",\"timestamp\":\"2026-08-12 11:05\"}");
        auditLogMemoryRegistry.add("{\"logId\":106,\"userId\":5,\"username\":\"Dr. Geetha R. Chillarge\",\"action\":\"User \\\"Dr. Geetha R. Chillarge\\\" signed in\",\"timestamp\":\"2026-08-12 11:15\"}");
        auditLogMemoryRegistry.add("{\"logId\":107,\"userId\":5,\"username\":\"Dr. Geetha R. Chillarge\",\"action\":\"Approved Question Bank \\\"Data Science and Machine Learning — Unit 1\\\"\",\"timestamp\":\"2026-08-12 11:20\"}");
        auditLogMemoryRegistry.add("{\"logId\":108,\"userId\":30,\"username\":\"Tushar Borate (Student)\",\"action\":\"User \\\"Tushar Borate\\\" signed in\",\"timestamp\":\"2026-08-12 14:00\"}");
        auditLogMemoryRegistry.add("{\"logId\":109,\"userId\":30,\"username\":\"Tushar Borate (Student)\",\"action\":\"Submitted Quiz Attempt for Data Science (Score: 100%)\",\"timestamp\":\"2026-08-12 14:30\"}");
    }

    static class QuestionBankHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                if ("POST".equalsIgnoreCase(ex.getRequestMethod()) || "PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readRequestBody(ex);
                    String idStr = extractJsonField(body, "id");
                    if (idStr != null) {
                        // Remove ALL existing entries with this ID first (prevent duplicates)
                        questionBankRegistry.removeIf(item ->
                            item.contains("\"id\":" + idStr) || item.contains("\"id\": " + idStr)
                        );
                    }
                    // Always insert the new/updated entry at position 0
                    questionBankRegistry.add(0, body);
                    sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Question bank saved\"}");
                    return;
                }
                // GET — deduplicate by ID, newest entry wins
                java.util.LinkedHashMap<String, String> deduped = new java.util.LinkedHashMap<>();
                for (String item : questionBankRegistry) {
                    String id = extractJsonField(item, "id");
                    if (id != null && !deduped.containsKey(id)) {
                        deduped.put(id, item);
                    }
                }
                StringBuilder json = new StringBuilder("[");
                int i = 0;
                for (String val : deduped.values()) {
                    if (i++ > 0) json.append(",");
                    json.append(val);
                }
                json.append("]");
                sendJsonResponse(ex, 200, json.toString());
            } catch (Throwable t) {
                sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Saved\"}");
            }
        }
    }

    static class QuizHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readRequestBody(ex);
                    quizRegistry.add(0, body);
                    sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Quiz assigned\"}");
                    return;
                }
                // GET
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < quizRegistry.size(); i++) {
                    json.append(quizRegistry.get(i));
                    if (i < quizRegistry.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(ex, 200, json.toString());
            } catch (Throwable t) {
                sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Quiz recorded\"}");
            }
        }
    }

    static class QuizResultsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            try {
                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readRequestBody(ex);
                    quizResultsRegistry.add(0, body);
                    sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Result saved\"}");
                    return;
                }
                // GET — return all results
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < quizResultsRegistry.size(); i++) {
                    json.append(quizResultsRegistry.get(i));
                    if (i < quizResultsRegistry.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(ex, 200, json.toString());
            } catch (Throwable t) {
                sendJsonResponse(ex, 200, "{\"status\":\"success\"}");
            }
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            questionBankRegistry.clear();
            quizRegistry.clear();
            quizResultsRegistry.clear();
            auditLogMemoryRegistry.clear();
            auditLogMemoryRegistry.add("{\"logId\":100,\"userId\":1,\"username\":\"System Admin\",\"action\":\"User \\\"System Admin\\\" signed in\",\"timestamp\":\"2026-08-12 09:30\"}");
            sendJsonResponse(ex, 200, "{\"status\":\"success\",\"message\":\"Server state reset cleanly\"}");
        }
    }

    static class StaticFileHandler implements HttpHandler {
        private final String frontendDir;

        public StaticFileHandler(String frontendDir) {
            this.frontendDir = frontendDir;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            String path = ex.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/login.html";
            }
            if (path.startsWith("/frontend/")) {
                path = path.substring(9);
            }
            java.io.File file = new java.io.File(frontendDir, path);
            if (!file.exists() || file.isDirectory()) {
                sendJsonResponse(ex, 404, "{\"status\":\"error\",\"message\":\"File not found\"}");
                return;
            }
            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";

            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static String extractJsonField(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + key.length());
        if (colon == -1) return null;
        int p = colon + 1;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        if (p >= json.length()) return null;
        if (json.charAt(p) == '"') {
            // Read string, respecting escaped characters
            StringBuilder sb = new StringBuilder();
            int i = p + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    if (next == '"') { sb.append('"'); i += 2; }
                    else if (next == '\\') { sb.append('\\'); i += 2; }
                    else if (next == 'n') { sb.append('\n'); i += 2; }
                    else if (next == 'r') { sb.append('\r'); i += 2; }
                    else { sb.append(next); i += 2; }
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c); i++;
                }
            }
            return sb.toString();
        } else {
            int end = p;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') end++;
            return json.substring(p, end).trim();
        }
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    static class SyllabusHandler implements HttpHandler {
        private final SyllabusService syllabusService = new SyllabusServiceImpl();

        @Override
        public void handle(HttpExchange ex) throws IOException {
            enableCORS(ex);
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    if (path.endsWith("/subjects")) {
                        List<Subject> subjects = syllabusService.getAllSubjects();
                        StringBuilder json = new StringBuilder("[");
                        for (int i = 0; i < subjects.size(); i++) {
                            Subject s = subjects.get(i);
                            json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"version\":\"%s\",\"isActive\":%b}",
                                    s.getSubjectId(), escapeJson(s.getSubjectName()), escapeJson(s.getVersion()), s.isActive()));
                            if (i < subjects.size() - 1) json.append(",");
                        }
                        json.append("]");
                        sendJsonResponse(ex, 200, json.toString());
                        return;
                    } else if (path.endsWith("/units")) {
                        String query = ex.getRequestURI().getQuery();
                        int subjectId = 0;
                        if (query != null && query.contains("subjectId=")) {
                            subjectId = Integer.parseInt(query.split("subjectId=")[1].split("&")[0]);
                        }
                        List<Unit> units = syllabusService.getUnitsBySubjectId(subjectId);
                        StringBuilder json = new StringBuilder("[");
                        for (int i = 0; i < units.size(); i++) {
                            Unit u = units.get(i);
                            json.append(String.format("{\"id\":%d,\"subjectId\":%d,\"name\":\"%s\",\"questionQuota\":%d}",
                                    u.getUnitId(), u.getSubjectId(), escapeJson(u.getUnitName()), u.getQuestionQuota()));
                            if (i < units.size() - 1) json.append(",");
                        }
                        json.append("]");
                        sendJsonResponse(ex, 200, json.toString());
                        return;
                    } else if (path.endsWith("/topics")) {
                        String query = ex.getRequestURI().getQuery();
                        int unitId = 0;
                        if (query != null && query.contains("unitId=")) {
                            unitId = Integer.parseInt(query.split("unitId=")[1].split("&")[0]);
                        }
                        List<Topic> topics = syllabusService.getTopicsByUnitId(unitId);
                        StringBuilder json = new StringBuilder("[");
                        for (int i = 0; i < topics.size(); i++) {
                            Topic t = topics.get(i);
                            json.append(String.format("{\"id\":%d,\"unitId\":%d,\"name\":\"%s\",\"questionQuota\":%d}",
                                    t.getTopicId(), t.getUnitId(), escapeJson(t.getTopicName()), t.getQuestionQuota()));
                            if (i < topics.size() - 1) json.append(",");
                        }
                        json.append("]");
                        sendJsonResponse(ex, 200, json.toString());
                        return;
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(ex);
                    if (path.endsWith("/subjects/clone")) {
                        int oldSubjectId = Integer.parseInt(extractJsonField(body, "subjectId"));
                        String newVersion = extractJsonField(body, "version");
                        int newId = syllabusService.cloneSubjectVersion(oldSubjectId, newVersion);
                        sendJsonResponse(ex, 200, "{\"status\":\"success\",\"newSubjectId\":" + newId + "}");
                        return;
                    } else if (path.endsWith("/subjects/active")) {
                        int subjectId = Integer.parseInt(extractJsonField(body, "subjectId"));
                        boolean isActive = Boolean.parseBoolean(extractJsonField(body, "isActive"));
                        syllabusService.setSubjectActive(subjectId, isActive);
                        sendJsonResponse(ex, 200, "{\"status\":\"success\"}");
                        return;
                    } else if (path.endsWith("/subjects")) {
                        String name = extractJsonField(body, "name");
                        String version = extractJsonField(body, "version");
                        Subject s = new Subject();
                        s.setSubjectName(name);
                        s.setVersion(version);
                        s.setActive(true);
                        s = syllabusService.createSubject(s);
                        if (s == null) {
                            sendJsonResponse(ex, 500, "{\"status\":\"error\",\"message\":\"Failed to save subject. Database may be offline.\"}");
                            return;
                        }
                        sendJsonResponse(ex, 200, "{\"status\":\"success\",\"id\":" + s.getSubjectId() + "}");
                        return;
                    } else if (path.endsWith("/units")) {
                        int subjectId = Integer.parseInt(extractJsonField(body, "subjectId"));
                        String name = extractJsonField(body, "name");
                        String quotaStr = extractJsonField(body, "questionQuota");
                        int quota = (quotaStr != null && !quotaStr.isEmpty()) ? Integer.parseInt(quotaStr) : 0;
                        Unit u = new Unit();
                        u.setSubjectId(subjectId);
                        u.setUnitName(name);
                        u.setQuestionQuota(quota);
                        u = syllabusService.createUnit(u);
                        if (u == null) {
                            sendJsonResponse(ex, 500, "{\"status\":\"error\",\"message\":\"Failed to save unit. Database may be offline.\"}");
                            return;
                        }
                        sendJsonResponse(ex, 200, "{\"status\":\"success\",\"id\":" + u.getUnitId() + "}");
                        return;
                    } else if (path.endsWith("/topics")) {
                        int unitId = Integer.parseInt(extractJsonField(body, "unitId"));
                        String name = extractJsonField(body, "name");
                        String quotaStr = extractJsonField(body, "questionQuota");
                        int quota = (quotaStr != null && !quotaStr.isEmpty()) ? Integer.parseInt(quotaStr) : 0;
                        Topic t = new Topic();
                        t.setUnitId(unitId);
                        t.setTopicName(name);
                        t.setQuestionQuota(quota);
                        t = syllabusService.createTopic(t);
                        if (t == null) {
                            sendJsonResponse(ex, 500, "{\"status\":\"error\",\"message\":\"Failed to save topic. Database may be offline.\"}");
                            return;
                        }
                        sendJsonResponse(ex, 200, "{\"status\":\"success\",\"id\":" + t.getTopicId() + "}");
                        return;
                    }
                }
                sendJsonResponse(ex, 404, "{\"status\":\"error\",\"message\":\"Not Found\"}");
            } catch (Exception e) {
                sendJsonResponse(ex, 400, "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }
}
