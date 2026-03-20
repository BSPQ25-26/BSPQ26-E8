# REST API Endpoints Design

## Base URL
```
http://localhost:8080/api
```

---

## 1. User Management (`/users`)

### GET /api/users/{id}
**Description:** Get a user by their ID
**Response:** 200 OK
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "username": "john_doe"
}
```

### GET /api/users/username/{username}
**Description:** Get a user by their username
**Parameter:** username (string)
**Response:** 200 OK (same format as above)

### POST /api/users
**Description:** Create a new user
**Body:**
```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "secure_password"
}
```
**Response:** 201 Created
**Errors:**
- 400: Email already exists or invalid username

---

## 2. Problems (`/problems`)

### GET /api/problems
**Description:** List all problems (with pagination)
**Query Params:**
- `page` (int, default=0)
- `size` (int, default=20)
- `difficulty` (EASY, MEDIUM, HARD, optional)

**Response:** 200 OK
```json
{
  "content": [
    {
      "id": "uuid",
      "slug": "two-sum",
      "title": "Two Sum",
      "difficulty": "EASY",
      "authorId": "uuid",
      "createdAt": "2026-03-20T10:00:00Z"
    }
  ],
  "totalElements": 150,
  "totalPages": 8
}
```

### GET /api/problems/{id}
**Description:** Get complete problem details
**Response:** 200 OK
```json
{
  "id": "uuid",
  "slug": "two-sum",
  "title": "Two Sum",
  "statementMd": "markdown content...",
  "inputSpecMd": "markdown content...",
  "outputSpecMd": "markdown content...",
  "constraintsMd": "markdown content...",
  "difficulty": "EASY",
  "authorId": "uuid",
  "solutionTemplate": "code template...",
  "createdAt": "2026-03-20T10:00:00Z"
}
```

### GET /api/problems/{id}/testcases
**Description:** Get test cases (public samples only)
**Response:** 200 OK
```json
{
  "testCases": [
    {
      "input": "1 2 3",
      "output": "0 1",
      "isSample": true
    }
  ]
}
```

### POST /api/problems
**Description:** Create a new problem (admin/author only)
**Authorization:** JWT Token required
**Body:**
```json
{
  "slug": "two-sum",
  "title": "Two Sum",
  "statementMd": "markdown...",
  "difficulty": "EASY",
  "languages": ["java", "python"]
}
```
**Response:** 201 Created

---

## 3. Submissions (`/submissions`)

### GET /api/submissions
**Description:** List authenticated user's submissions
**Query Params:**
- `page` (int)
- `size` (int)
- `problemId` (UUID, optional)
- `status` (QUEUED, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR, optional)

**Authorization:** JWT Token required
**Response:** 200 OK
```json
{
  "content": [
    {
      "id": "uuid",
      "problemId": "uuid",
      "userId": "uuid",
      "language": "java",
      "status": "ACCEPTED",
      "verdict": "Accepted",
      "runtimeMs": 125,
      "memoryMb": 45,
      "testcasesPassed": 10,
      "testcasesTotal": 10,
      "submittedAt": "2026-03-20T10:00:00Z",
      "evaluatedAt": "2026-03-20T10:00:05Z"
    }
  ],
  "totalElements": 25
}
```

### GET /api/submissions/{id}
**Description:** Get submission details
**Authorization:** JWT Token required (owner only)
**Response:** 200 OK (same object as in list)

### POST /api/submissions
**Description:** Create a new submission/solution
**Authorization:** JWT Token required
**Body:**
```json
{
  "problemId": "uuid",
  "languageId": 1,
  "sourceCode": "public class Solution { ... }"
}
```
**Response:** 201 Created
```json
{
  "id": "uuid",
  "status": "QUEUED"
}
```

### GET /api/submissions/{id}/source
**Description:** Get the source code of a submission
**Authorization:** JWT Token required
**Response:** 200 OK
```json
{
  "sourceCode": "public class Solution { ... }",
  "language": "java"
}
```

---

## 4. Languages (`/languages`)

### GET /api/languages
**Description:** List available languages
**Response:** 200 OK
```json
{
  "languages": [
    {
      "id": 1,
      "name": "Java",
      "version": "21",
      "extension": "java"
    },
    {
      "id": 2,
      "name": "Python",
      "version": "3.11",
      "extension": "py"
    }
  ]
}
```

---

## 5. Authentication (`/auth`)

### POST /api/auth/login
**Description:** Login with email and password
**Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**Response:** 200 OK
```json
{
  "accessToken": "jwt.token.here",
  "refreshToken": "refresh.token.here",
  "expiresIn": 3600
}
```

### POST /api/auth/refresh
**Description:** Get a new access token
**Body:**
```json
{
  "refreshToken": "refresh.token.here"
}
```
**Response:** 200 OK
```json
{
  "accessToken": "new.jwt.token.here",
  "expiresIn": 3600
}
```

---

## 6. User Progress (`/users/{userId}/progress`)

### GET /api/users/{userId}/progress
**Description:** Get user's progress on problems
**Response:** 200 OK
```json
{
  "userId": "uuid",
  "problemsAttempted": 25,
  "problemsSolved": 18,
  "solveRate": 0.72,
  "byDifficulty": {
    "EASY": { "attempted": 10, "solved": 9 },
    "MEDIUM": { "attempted": 10, "solved": 7 },
    "HARD": { "attempted": 5, "solved": 2 }
  }
}
```

### GET /api/users/{userId}/progress/{problemId}
**Description:** Get user's progress on a specific problem
**Response:** 200 OK
```json
{
  "problemId": "uuid",
  "status": "ACCEPTED",
  "attempts": 3,
  "solved": true,
  "solvedAt": "2026-03-20T10:00:00Z"
}
```

---

## HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid data
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not authorized
- `404 Not Found` - Resource not found
- `409 Conflict` - Conflict (e.g., duplicate email)
- `500 Internal Server Error` - Server error

---

## Error Response Format

```json
{
  "error": "Error message",
  "timestamp": "2026-03-20T10:00:00Z",
  "path": "/api/users"
}
```

---

## Next Steps

1. ✅ UserController - CREATED
2. ⏳ ProblemController - TO CREATE
3. ⏳ SubmissionController - TO CREATE
4. ✅ AuthController - CREATED
5. ⏳ LanguageController - TO CREATE
6. ⏳ Create JPA Entities for Problem, TestCase, Submission, Language
7. ⏳ Create Repositories
8. ✅ Implement JWT Security
9. ⏳ Tests for all endpoints

---

## Implementation Notes

- Use DTOs to separate REST layer from persistence layer
- Input validation in controllers
- Centralized exception handling
- Pagination for large lists
- Logging for debugging
