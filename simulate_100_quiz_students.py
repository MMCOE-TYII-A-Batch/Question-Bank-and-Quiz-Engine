#!/usr/bin/env python3
"""
Module 4 Load Test: Simulate 100 Simultaneous Active Quiz Takers
Benchmarking AppServer.java under 100 concurrent student quiz submissions.
"""

import time
import json
import random
import urllib.request
import urllib.error
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8080"
TOTAL_STUDENTS = 140

def simulate_student_quiz(student_idx):
    student_id = 1000 + student_idx
    username = f"Student_B24CE{student_id}"
    score = random.randint(70, 100)
    
    payload = {
        "studentId": student_id,
        "studentName": username,
        "subject": "Data Science and Machine Learning",
        "quizTitle": "DSML End-Semester Exam",
        "score": score,
        "totalQuestions": 10,
        "correctCount": int(score / 10),
        "timeTakenSeconds": random.randint(300, 1200),
        "submittedAt": time.strftime("%Y-%m-%d %H:%M:%S")
    }

    req_data = json.dumps(payload).encode('utf-8')
    headers = {'Content-Type': 'application/json'}
    
    start_time = time.time()
    try:
        req = urllib.request.Request(f"{BASE_URL}/api/quiz-results", data=req_data, headers=headers, method='POST')
        with urllib.request.urlopen(req, timeout=10) as response:
            res_code = response.getcode()
            elapsed_ms = (time.time() - start_time) * 1000
            return {
                "student_idx": student_idx,
                "status": res_code,
                "latency_ms": elapsed_ms,
                "success": res_code == 200
            }
    except Exception as e:
        elapsed_ms = (time.time() - start_time) * 1000
        return {
            "student_idx": student_idx,
            "status": str(e),
            "latency_ms": elapsed_ms,
            "success": False
        }

def main():
    print("================================================================")
    print(f"🚀 Starting Module 4 Load Test: {TOTAL_STUDENTS} Concurrent Active Quiz Takers")
    print("================================================================")
    
    start_all = time.time()
    results = []
    
    with ThreadPoolExecutor(max_workers=TOTAL_STUDENTS) as executor:
        futures = [executor.submit(simulate_student_quiz, i + 1) for i in range(TOTAL_STUDENTS)]
        for future in as_completed(futures):
            results.append(future.result())
            
    total_duration = time.time() - start_all
    successes = [r for r in results if r['success']]
    latencies = [r['latency_ms'] for r in results if r['success']]
    
    avg_latency = sum(latencies) / len(latencies) if latencies else 0
    max_latency = max(latencies) if latencies else 0
    min_latency = min(latencies) if latencies else 0
    rps = TOTAL_STUDENTS / total_duration if total_duration > 0 else 0

    print("\n📊 LOAD TEST RESULTS SUMMARY:")
    print(f"  • Total Students Simulated : {TOTAL_STUDENTS}")
    print(f"  • Successful Submissions   : {len(successes)} / {TOTAL_STUDENTS} ({len(successes)/TOTAL_STUDENTS * 100:.1f}%)")
    print(f"  • Total Load Duration     : {total_duration:.2f} seconds")
    print(f"  • Throughput (RPS)         : {rps:.1f} requests/sec")
    print(f"  • Average Latency          : {avg_latency:.2f} ms")
    print(f"  • Min / Max Latency        : {min_latency:.2f} ms / {max_latency:.2f} ms")
    print("================================================================")

    if len(successes) == TOTAL_STUDENTS:
        print("✅ PASSED: Server successfully handled 100 simultaneous quiz sessions with 0 errors!")
    else:
        print("❌ FAILED: Some requests failed during concurrent load.")

if __name__ == "__main__":
    main()
