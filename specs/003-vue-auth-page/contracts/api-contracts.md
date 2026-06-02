# REST API Contracts: Auth

**Base path**: `/api/auth`  
**Content-Type**: `application/json`  
**Auth**: Session-based (HttpSession, HTTP-only cookies)  
**Error format**: `{ "message": "string", "errorCode": "string", "timestamp": "ISO-8601" }`

## POST /api/auth/register

Create a new user account.

### Request
```json
{
  "email": "user@example.com",
  "password": "SecurePass1",
  "passwordConfirmation": "SecurePass1"
}
```

### Validation Rules
| Field | Rule |
|-------|------|
| email | Required, valid email format, max 255 chars |
| password | Required, min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit |
| passwordConfirmation | Required, must match password |

### Success Response (201 Created)
```json
{
  "success": true,
  "role": "USER",
  "redirectUrl": "/home"
}
```

### Error Responses
| HTTP | Condition | message |
|------|-----------|---------|
| 409 | Email already registered | "An account with this email already exists" |
| 400 | Validation failed | Validation errors per field |
| 500 | Server error | "Registration failed. Please try again." |

---

## POST /api/auth/login

Authenticate an existing user.

### Request
```json
{
  "email": "user@example.com",
  "password": "SecurePass1",
  "rememberMe": false
}
```

### Validation Rules
| Field | Rule |
|-------|------|
| email | Required, valid email format |
| password | Required, non-empty |
| rememberMe | Optional boolean |

### Success Response (200 OK)
```json
{
  "success": true,
  "role": "USER",
  "redirectUrl": "/home"
}
```

For admin users:
```json
{
  "success": true,
  "role": "ADMIN",
  "redirectUrl": "/admin"
}
```

### Error Responses
| HTTP | Condition | message |
|------|-----------|---------|
| 401 | Invalid credentials | "Invalid email or password" (generic, no enumeration) |
| 403 | Account blocked | "Your account is inactive. Contact support for assistance." |
| 423 | Rate limited | "Too many failed attempts. Try again later." |
| 400 | Validation failed | Validation errors per field |
| 500 | Server error | "Login failed. Please try again." |

---

## POST /api/auth/logout

End the current user session.

### Request
No body required. Session identified via cookie.

### Success Response (200 OK)
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## GET /api/auth/status

Check if the current session is authenticated.

### Success Response — Authenticated (200 OK)
```json
{
  "authenticated": true,
  "email": "user@example.com",
  "role": "USER"
}
```

### Success Response — Not Authenticated (200 OK)
```json
{
  "authenticated": false
}
```

---

## Session Management

| Aspect | Mechanism |
|--------|-----------|
| Cookie | `JSESSIONID`, HTTP-only, SameSite=Lax |
| Regeneration | On login: invalidate old session, create new (prevents session fixation) |
| Timeout | 30 minutes inactivity (configurable); 7 days with "Remember me" |
| Storage | In-memory HttpSession (server-side) |
| User context | `session.setAttribute("user", UserSession)` after login |

## AuthInterceptor Protected Paths

| Path Pattern | Auth Required | Role Required |
|---|---|---|
| `/api/auth/*` | No | — |
| `/api/**` | Yes | — |
| `/home` | Yes | USER |
| `/admin` | Yes | ADMIN |
| `/login`, `/register` | No (redirect if auth) | — |
