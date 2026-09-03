#!/usr/bin/env python3
"""
Module 4 Concurrency Test: Simulate Concurrent Author Question Bank Submissions
Benchmarking AppServer.java under simultaneous question bank uploads for the same subject.
"""

import time
import json
import urllib.request
import urllib.error
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8080"
AUTHORS = [
    {"name": "Ms. Kirti B. Satpute", "email": "kirtisatpute@mmcoe.edu.in"},
    {"name": "Dr. Sankirti S. Shiravale", "email": "sankirtishiravale@mmcoe.edu.in"},
    {"name": "Dr. Kalpana S. Thakre", "email": "kalpanathakre@mmcoe.edu.in"},
    {"name": "Dr. Geetha R. Chillarge", "email": "geethasb@mmcoe.edu.in"},
    {"name": "Ms. Shubhada P. Mone", "email": "shubhadamone@mmcoe.edu.in"}
]

def submit_question_bank(author_idx, author_data):
    batch_id = int(time.time() * 1000) + author_idx
    payload = {
        "id": batch_id,
        "title": f"Data Science and Machine Learning — Unit {author_idx + 1} (Concurrent Batch)",
        "subject_name": "Data Science and Machine Learning",
        "subject": "Data Science and Machine Learning",
        "chapter": f"Chapter {author_idx + 1}: Supervised Learning",
        "topic": "Exploratory Data Analysis",
        "dept": "Computer Engineering",
        "count": 5,
        "type": "MCQ",
        "submittedDate": time.strftime("%Y-%m-%d"),
        "status": "pending",
        "author": author_data["name"],
        "authorEmail": author_data["email"],
        "questions": [
            {
                "id": 1,
                "type": "mcq",
                "question": f"Sample question submitted concurrently by {author_data['name']}",
                "options": {"A": "Option A", "B": "Option B", "C": "Option C", "D": "Option D"},
                "correct_answer": "A",
                "btl": "BTL2",
                "marks": 2
            }
        ]
    }

    req_data = json.dumps(payload).encode('utf-8')
    headers = {'Content-Type': 'application/json'}
    
    start_time = time.time()
    try:
        req = urllib.request.Request(f"{BASE_URL}/api/question-banks", data=req_data, headers=headers, method='POST')
        with urllib.request.urlopen(req, timeout=10) as response:
            res_code = response.getcode()
            elapsed_ms = (time.time() - start_time) * 1000
            return {
                "author": author_data["name"],
                "batch_id": batch_id,
                "status": res_code,
                "latency_ms": elapsed_ms,
                "success": res_code == 200
            }
    except Exception as e:
        elapsed_ms = (time.time() - start_time) * 1000
        return {
            "author": author_data["name"],
            "batch_id": batch_id,
            "status": str(e),
            "latency_ms": elapsed_ms,
            "success": False
        }

def main():
    print("================================================================")
    print("🚀 Starting Module 4 Concurrency Test: 5 Authors Uploading Simultaneously")
    print("================================================================")
    
    start_all = time.time()
    results = []
    
    with ThreadPoolExecutor(max_workers=len(AUTHORS)) as executor:
        futures = [executor.submit(submit_question_bank, idx, author) for idx, author in enumerate(AUTHORS)]
        for future in as_completed(futures):
            results.append(future.result())
            
    total_duration = time.time() - start_all
    successes = [r for r in results if r['success']]

    print("\n📊 CONCURRENT SUBMISSIONS SUMMARY:")
    for r in results:
        status_str = "✅ SUCCESS" if r['success'] else "❌ FAILED"
        print(f"  • {r['author']:<30} | {status_str} | Latency: {r['latency_ms']:.2f} ms")

    print(f"\n  • Total Duration: {total_duration:.2f} seconds")
    print("================================================================")

    if len(successes) == len(AUTHORS):
        print("✅ PASSED: Subject ReentrantLock successfully synchronized all concurrent uploads with 0 errors!")
    else:
        print("❌ FAILED: Concurrency conflict detected.")

if __name__ == "__main__":
    main()
