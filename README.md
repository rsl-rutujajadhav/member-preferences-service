# Member Preferences Service

A Spring Boot REST service for storing and retrieving member preferences.

## Overview

This service exposes a small in-memory preferences API for a single member. The current implementation uses:

- Spring Boot 4.1.0
- Spring Web MVC
- Spring Validation
- `ConcurrentHashMap` in-memory persistence via a dedicated repository layer
- Thread-safe atomic operations for concurrent read/write
- OpenAPI documentation via SpringDoc

## Base URL

Local development:

```text
http://localhost:8080
```

## API Endpoints

### 1. Get preferences

GET `/v1/preferences/{memberId}`

Example:

```bash
curl http://localhost:8080/v1/preferences/usr_a1b2c3d4
```

Response:
- `200 OK`
- Returns the stored preferences for the member
- If the member does not yet exist, the service returns a default preferences payload

### 2. Create or replace preferences

PUT `/v1/preferences/{memberId}`

Example request body:

```json
{
  "theme": "LIGHT",
  "language": "fr-FR",
  "timezone": "Europe/Paris",
  "notifications": {
    "email": true,
    "sms": true,
    "push": false
  },
  "privacy": {
    "profileVisibility": "CONTACTS_ONLY",
    "showOnlineStatus": true
  }
}
```

Example:

```bash
curl -X PUT http://localhost:8080/v1/preferences/usr_a1b2c3d4 \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "LIGHT",
    "language": "fr-FR",
    "timezone": "Europe/Paris",
    "notifications": {
      "email": true,
      "sms": true,
      "push": false
    },
    "privacy": {
      "profileVisibility": "CONTACTS_ONLY",
      "showOnlineStatus": true
    }
  }'
```

Response:
- `201 Created` for a new member
- `200 OK` when replacing an existing member's preferences

### 3. Patch preferences

PATCH `/v1/preferences/{memberId}`

Example request body:

```json
{
  "theme": "DARK",
  "notifications": {
    "sms": true
  }
}
```

Example:

```bash
curl -X PATCH http://localhost:8080/v1/preferences/usr_a1b2c3d4 \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "DARK",
    "notifications": {
      "sms": true
    }
  }'
```

Response:
- `200 OK`
- Only the provided fields are updated

## Supported Values

Theme:
- `LIGHT`
- `DARK`
- `SYSTEM`

Language:
- Format: `xx-YY` such as `en-US`, `fr-FR`

Timezone:
- Format expected by validation: `Area/City`, such as `UTC`, `Europe/Paris`, `America/New_York`

Privacy visibility:
- `PUBLIC`
- `PRIVATE`
- `CONTACTS_ONLY`

## Error Responses

All errors are returned as JSON matching the RFC 7807 Problem Details format:

| Status | Title | When |
|--------|-------|------|
| 400 | Validation Error | Invalid `memberId`, missing or invalid request body fields |
| 400 | Malformed Request Body | Unparseable JSON or invalid enum values |
| 404 | Not Found | Resource at the requested URL does not exist |
| 500 | Internal Server Error | Unexpected server-side failure |

Example error payload:

```json
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "theme: must not be null; language: must not be null",
  "instance": "/v1/preferences/usr_a1b2c3d4"
}
```

## Configuration

Feature flags and service settings are managed via `@ConfigurationProperties` bound to `application.yml`.

| Property | Default | Description |
|----------|---------|-------------|
| `preferences.patch.enabled` | `true` | Enables or disables the PATCH endpoint |

To disable patching (e.g. during maintenance):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile sets `preferences.patch.enabled: false`. When disabled, PATCH requests return `422 Unprocessable Entity` with detail `"Patching preferences is currently disabled"` while GET and PUT continue working.

All configuration properties are visible at `/actuator/configprops` at runtime.

## Validation Behavior

The service validates:
- member ID path format (`^[A-Za-z0-9_-]+$`, 1–64 chars)
- required request fields (`@NotNull`)
- language pattern (`^[a-z]{2}-[A-Z]{2}$`)
- timezone pattern (`^[A-Za-z_]+/[A-Za-z_]+$`)
- enum values for `theme` and `profileVisibility`

## Running the Service

From the project root:

```bash
./mvnw spring-boot:run
```

## Testing

Run the full test suite:

```bash
./mvnw test
```

Current test status:
- Spring Boot context load test
- Concurrency tests (concurrent patch, upsert, read isolation)
- HTTP integration tests for error responses (400, 404, validation details)
- Feature flag test (PATCH disabled via `dev` profile returns 422)
- Config properties binding and actuator `/configprops` exposure

## Project Structure

- `src/main/java/.../controller` – REST endpoints and global error handler
- `src/main/java/.../service` – business logic
- `src/main/java/.../repository` – in-memory data access with thread-safe operations
- `src/main/java/.../config` – `@ConfigurationProperties` classes and feature flags
- `src/main/java/.../domain/dto` – request/response models and error response DTO
- `src/main/java/.../domain/exception` – custom exception types
- `src/main/resources/application.yml` – default config
- `src/main/resources/application-dev.yml` – dev profile override
- `src/test/java` – test classes

## Notes

This service is currently an in-memory prototype and does not persist preferences to a database. Concurrent access is safe via `ConcurrentHashMap.compute()` atomic operations.
