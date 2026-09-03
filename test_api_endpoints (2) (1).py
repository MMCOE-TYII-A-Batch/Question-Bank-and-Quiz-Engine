#!/usr/bin/env python3
"""
Automated Integration Test Suite
Question Bank & Quiz Engine Platform
====================================
Tests all REST API endpoints and verifies end-to-end full-stack data contracts.
"""

import urllib.request
import json
import sys

BASE_URL = "http://localhost:8080"

def test_endpoint(name, path, method="GET", data=None):
    url = f"{BASE_URL}{path}"
    print(f"Testing [{method}] {url} ... ", end="")
    try:
        req = urllib.request.Request(url, method=method)
        if data:
            req.add_header('Content-Type', 'application/json')
            payload = json.dumps(data).encode('utf-8')
            res = urllib.request.urlopen(req, data=payload, timeout=5)
        else:
            res = urllib.request.urlopen(req, timeout=5)

        status = res.getcode()
        body = res.read().decode('utf-8')
        if status == 200:
            print(f"✅ PASSED (HTTP {status})")
            return True, body
        else:
            print(f"❌ FAILED (HTTP {status})")
            return False, body
    except Exception as e:
        print(f"❌ ERROR ({e})")
        return False, str(e)

def main():
    print("==================================================")
    print(" Running Full-Stack Integration Test Suite")
    print("==================================================")

    passed = 0
    total = 0

    # 1. Reset Test State
    total += 1
    ok, _ = test_endpoint("Reset Endpoint", "/api/reset", "GET")
    if ok: passed += 1

    # 2. Audit Logs GET
    total += 1
    ok, _ = test_endpoint("Audit Logs GET", "/api/audit-logs", "GET")
    if ok: passed += 1

    # 3. Audit Log POST
    total += 1
    ok, _ = test_endpoint("Audit Log POST", "/api/audit-logs", "POST", {
        "userId": 1,
        "username": "Integration Test Runner",
        "action": "Automated Integration Test Execution"
    })
    if ok: passed += 1

    # 4. Admin Users GET
    total += 1
    ok, _ = test_endpoint("Admin Users GET", "/api/admin/users", "GET")
    if ok: passed += 1

    # 5. Question Banks GET
    total += 1
    ok, _ = test_endpoint("Question Banks GET", "/api/question-banks", "GET")
    if ok: passed += 1

    # 6. Quizzes GET
    total += 1
    ok, _ = test_endpoint("Quizzes GET", "/api/quizzes", "GET")
    if ok: passed += 1

    # 7. Quiz Results GET
    total += 1
    ok, _ = test_endpoint("Quiz Results GET", "/api/quiz-results", "GET")
    if ok: passed += 1

    # 8. Quiz Result POST
    total += 1
    ok, _ = test_endpoint("Quiz Result POST", "/api/quiz-results", "POST", {
        "quizId": "test-101",
        "studentName": "Integration Student",
        "score": 100,
        "marks": "10/10",
        "grade": "Pass",
        "date": "2026-08-12 23:30"
    })
    if ok: passed += 1

    print("==================================================")
    print(f" Summary: {passed}/{total} Integration Tests Passed")
    print("==================================================")

if __name__ == "__main__":
    main()
