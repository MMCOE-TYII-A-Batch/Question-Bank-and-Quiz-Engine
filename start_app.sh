#!/bin/bash
# ==============================================================================
# Full-Stack Application Launcher Script
# Question Bank & Quiz Engine Platform
# ==============================================================================

echo "=================================================="
echo " Starting Question Bank & Quiz Engine Platform"
echo "=================================================="

# 1. Navigate to project root
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

# 2. Compile Java Backend
echo "🔨 Compiling Java Backend..."
/opt/homebrew/Cellar/openjdk/26.0.2/bin/javac -cp "backend/postgresql-42.7.3.jar" -d backend/out backend/src/*.java backend/src/*/*.java 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Java compilation successful!"
else
    echo "⚠️ Warning: Java compilation exited with notices (check javac output)."
fi

# 3. Terminate any existing instance running on port 8080
echo "🧹 Cleaning up port 8080..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
sleep 1

# 4. Launch AppServer in background
echo "🚀 Launching AppServer..."
cd "$PROJECT_ROOT/backend"
/opt/homebrew/Cellar/openjdk/26.0.2/bin/java -cp "postgresql-42.7.3.jar:out" AppServer &

sleep 2

# 5. Verify Server Health
HEALTH=$(curl -s http://localhost:8080/api/audit-logs)
if [ -n "$HEALTH" ]; then
    echo ""
    echo "=================================================="
    echo " 🎉 Application Server is LIVE!"
    echo " 🌐 Portal Access: http://localhost:8080/login.html"
    echo " 👑 Admin Panel:  http://localhost:8080/admin.html"
    echo " ✏️ Author Panel: http://localhost:8080/author.html"
    echo " 🔍 Moderator:    http://localhost:8080/moderator.html"
    echo " 🎓 Student:      http://localhost:8080/student.html"
    echo "=================================================="
else
    echo "❌ Failed to verify server health. Please check backend logs."
fi
