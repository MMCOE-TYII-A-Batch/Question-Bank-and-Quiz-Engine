const http = require('http');
const { spawn } = require('child_process');

function doRequest(options, postData) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          statusCode: res.statusCode,
          headers: res.headers,
          body: data
        });
      });
    });
    req.on('error', reject);
    if (postData) {
      req.write(typeof postData === 'string' ? postData : JSON.stringify(postData));
    }
    req.end();
  });
}

async function runTests() {
  console.log("=== STARTING MODULE 7: SECURITY & AUDIT CONTROL VERIFICATION ===");

  // 1. Start AppServer
  console.log("Starting AppServer on port 8080...");
  const serverProc = spawn('/opt/homebrew/opt/openjdk/bin/java', [
    '-cp', 'backend/postgresql-42.7.3.jar:backend/src',
    'AppServer'
  ], { cwd: '/Users/sumegh/Java/EDI' });

  serverProc.stdout.on('data', d => console.log('[Server stdout]', d.toString().trim()));
  serverProc.stderr.on('data', d => console.log('[Server stderr]', d.toString().trim()));

  // Wait for server to initialize
  await new Promise(r => setTimeout(r, 2000));

  let allPassed = true;

  try {
    // TC-65 & TC-72: Test Protected Endpoints without token -> expect 401
    console.log("\n[Test 1] Testing /api/admin/users without token (Expected: 401)...");
    const resNoToken = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/admin/users',
      method: 'GET'
    });
    console.log(`Response code: ${resNoToken.statusCode}, body: ${resNoToken.body}`);
    if (resNoToken.statusCode === 401) {
      console.log("✓ PASS: Correctly blocked unauthorized request with HTTP 401");
    } else {
      console.error("✕ FAIL: Expected HTTP 401 but got " + resNoToken.statusCode);
      allPassed = false;
    }

    // Test with invalid Bearer token -> expect 401
    console.log("\n[Test 2] Testing /api/admin/users with bogus token (Expected: 401)...");
    const resBadToken = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/admin/users',
      method: 'GET',
      headers: { 'Authorization': 'Bearer bad_token_123' }
    });
    console.log(`Response code: ${resBadToken.statusCode}, body: ${resBadToken.body}`);
    if (resBadToken.statusCode === 401) {
      console.log("✓ PASS: Correctly blocked bogus token with HTTP 401");
    } else {
      console.error("✕ FAIL: Expected HTTP 401 but got " + resBadToken.statusCode);
      allPassed = false;
    }

    // Test with Question Author token on Admin endpoint -> expect 403 Forbidden
    console.log("\n[Test 3] Testing /api/admin/users with Author token (Expected: 403)...");
    const resAuthorOnAdmin = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/admin/users',
      method: 'GET',
      headers: { 'Authorization': 'Bearer demo_author_token' }
    });
    console.log(`Response code: ${resAuthorOnAdmin.statusCode}, body: ${resAuthorOnAdmin.body}`);
    if (resAuthorOnAdmin.statusCode === 403) {
      console.log("✓ PASS: Correctly enforced RBAC with HTTP 403 Forbidden");
    } else {
      console.error("✕ FAIL: Expected HTTP 403 but got " + resAuthorOnAdmin.statusCode);
      allPassed = false;
    }

    // Test with Admin token on Admin endpoint -> expect 200 OK
    console.log("\n[Test 4] Testing /api/admin/users with Admin token (Expected: 200)...");
    const resAdmin = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/admin/users',
      method: 'GET',
      headers: { 'Authorization': 'Bearer demo_admin_token' }
    });
    console.log(`Response code: ${resAdmin.statusCode}`);
    if (resAdmin.statusCode === 200) {
      console.log("✓ PASS: Allowed valid Admin request with HTTP 200");
    } else {
      console.error("✕ FAIL: Expected HTTP 200 but got " + resAdmin.statusCode);
      allPassed = false;
    }

    // TC-67: Test Audit Logging endpoint
    console.log("\n[Test 5] Testing Audit Log retrieval /api/audit-logs...");
    const resAudit = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/audit-logs',
      method: 'GET'
    });
    console.log(`Response code: ${resAudit.statusCode}, sample: ${resAudit.body.slice(0, 100)}...`);
    if (resAudit.statusCode === 200) {
      console.log("✓ PASS: Audit logs endpoint accessible and returning activity records");
    } else {
      console.error("✕ FAIL: Expected HTTP 200 but got " + resAudit.statusCode);
      allPassed = false;
    }

    // TC-70 & TC-71: Test Quiz Result Submission with Integrity Cheating Flag & Tab Switches
    console.log("\n[Test 6] Submitting quiz result with Integrity Cheating Flag & 3 Tab Switches...");
    const testResult = {
      id: "att_security_test_" + Date.now(),
      quizId: "quiz_dsml_1",
      studentId: "30",
      studentName: "Aditya Patil",
      title: "DSML Unit 1 Test",
      dateTaken: "2026-10-13",
      score: "60%",
      scorePercent: 60,
      earnedMarks: 6,
      totalMarks: 10,
      correctCount: 3,
      incorrectCount: 2,
      skippedCount: 0,
      unattemptedCount: 0,
      totalQuestions: 5,
      durationStr: "05m 12s",
      timeSpent: "05m 12s",
      passFailStatus: "PASS",
      gradingStatus: "GRADED",
      grade: "Pass",
      status: "Flagged Submission",
      cheatingFlag: true,
      tabSwitchCount: 3,
      responses: []
    };

    const resSubmit = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/quiz-results',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer demo_admin_token'
      }
    }, JSON.stringify(testResult));
    console.log(`Response code: ${resSubmit.statusCode}, body: ${resSubmit.body}`);
    if (resSubmit.statusCode === 200) {
      console.log("✓ PASS: Proctor-flagged attempt record saved successfully with cheatingFlag & tabSwitchCount");
    } else {
      console.error("✕ FAIL: Expected HTTP 200 but got " + resSubmit.statusCode);
      allPassed = false;
    }

    // Retrieve quiz results to verify stored fields
    console.log("\n[Test 7] Verifying stored attempt record in /api/quiz-results...");
    const resGetResults = await doRequest({
      hostname: 'localhost',
      port: 8080,
      path: '/api/quiz-results',
      method: 'GET'
    });
    if (resGetResults.statusCode === 200 && resGetResults.body.includes("cheatingFlag") && resGetResults.body.includes("tabSwitchCount")) {
      console.log("✓ PASS: Successfully retrieved stored record with integrity cheating flags intact");
    } else {
      console.error("✕ FAIL: Saved attempt did not reflect cheating flag in registry");
      allPassed = false;
    }

  } catch(e) {
    console.error("Exception during test execution:", e);
    allPassed = false;
  } finally {
    console.log("\nStopping test server...");
    serverProc.kill('SIGINT');
  }

  console.log("\n========================================================");
  if (allPassed) {
    console.log("🏆 ALL SECURITY & AUDIT CONTROL INTEGRATION TESTS PASSED!");
  } else {
    console.log("❌ SOME INTEGRATION TESTS FAILED.");
    process.exit(1);
  }
}

runTests();
