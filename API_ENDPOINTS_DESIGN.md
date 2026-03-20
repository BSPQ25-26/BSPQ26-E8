# REST API Endpoints Design

## Base URL
```
http://localhost:8080/api
```

---

## 1. User Management (`/users`)

### GET /api/users/{id}
**Descripción:** Obtener un usuario por su ID
**Respuesta:** 200 OK
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "username": "john_doe"
}
```

### GET /api/users/username/{username}
**Descripción:** Obtener un usuario por su username
**Parámetro:** username (string)
**Respuesta:** 200 OK (mismo formato que arriba)

### POST /api/users
**Descripción:** Crear un nuevo usuario
**Body:**
```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "secure_password"
}
```
**Respuesta:** 201 Created
**Errores:**
- 400: Email ya existe o username inválido

---

## 2. Problems (`/problems`)

### GET /api/problems
**Descripción:** Listar todos los problemas (con paginación)
**Query Params:**
- `page` (int, default=0)
- `size` (int, default=20)
- `difficulty` (EASY, MEDIUM, HARD, optional)

**Respuesta:** 200 OK
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
**Descripción:** Obtener detalles completos de un problema
**Respuesta:** 200 OK
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
**Descripción:** Obtener test cases (solo samples públicos)
**Respuesta:** 200 OK
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
**Descripción:** Crear un nuevo problema (solo para admin/author)
**Autorización:** JWT Token requerido
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
**Respuesta:** 201 Created

---

## 3. Submissions (`/submissions`)

### GET /api/submissions
**Descripción:** Listar envíos del usuario autenticado
**Query Params:**
- `page` (int)
- `size` (int)
- `problemId` (UUID, optional)
- `status` (QUEUED, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR, optional)

**Autorización:** JWT Token requerido
**Respuesta:** 200 OK
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
**Descripción:** Obtener detalles de un envío
**Autorización:** JWT Token requerido (solo owner puede ver)
**Respuesta:** 200 OK (mismo objeto que en lista)

### POST /api/submissions
**Descripción:** Crear un nuevo envío/solución
**Autorización:** JWT Token requerido
**Body:**
```json
{
  "problemId": "uuid",
  "languageId": 1,
  "sourceCode": "public class Solution { ... }"
}
```
**Respuesta:** 201 Created
```json
{
  "id": "uuid",
  "status": "QUEUED"
}
```

### GET /api/submissions/{id}/source
**Descripción:** Obtener el código fuente de un envío
**Autorización:** JWT Token requerido
**Respuesta:** 200 OK
```json
{
  "sourceCode": "public class Solution { ... }",
  "language": "java"
}
```

---

## 4. Languages (`/languages`)

### GET /api/languages
**Descripción:** Listar lenguajes disponibles
**Respuesta:** 200 OK
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
**Descripción:** Login con email/username y password
**Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**Respuesta:** 200 OK
```json
{
  "accessToken": "jwt.token.here",
  "refreshToken": "refresh.token.here",
  "expiresIn": 3600
}
```

### POST /api/auth/refresh
**Descripción:** Obtener nuevo access token
**Body:**
```json
{
  "refreshToken": "refresh.token.here"
}
```
**Respuesta:** 200 OK
```json
{
  "accessToken": "new.jwt.token.here",
  "expiresIn": 3600
}
```

---

## 6. User Progress (`/users/{userId}/progress`)

### GET /api/users/{userId}/progress
**Descripción:** Obtener progreso del usuario en los problemas
**Respuesta:** 200 OK
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
**Descripción:** Obtener progreso del usuario en un problema específico
**Respuesta:** 200 OK
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

- `200 OK` - Éxito
- `201 Created` - Recurso creado
- `400 Bad Request` - Datos inválidos
- `401 Unauthorized` - No autenticado
- `403 Forbidden` - No autorizado
- `404 Not Found` - Recurso no existe
- `409 Conflict` - Conflicto (ej: email duplicado)
- `500 Internal Server Error` - Error del servidor

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

## Próximos Pasos

1. ✅ UserController - CREADO
2. ⏳ ProblemController - POR CREAR
3. ⏳ SubmissionController - POR CREAR
4. ⏳ AuthController - POR CREAR
5. ⏳ LanguageController - POR CREAR
6. ⏳ Crear JPA Entities para Problem, TestCase, Submission, Language
7. ⏳ Crear Repositories
8. ⏳ Implementar seguridad con JWT
9. ⏳ Tests para todos los endpoints

---

## Notas de Implementación

- Usar DTOs para separar la capa REST de la persistencia
- Validación de entrada en controladores
- Manejo de excepciones centralizado
- Paginación en listas grandes
- Logging para debugging
