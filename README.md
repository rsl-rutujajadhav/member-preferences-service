# Member Preferences Service

A Spring Boot REST service for storing and retrieving member preferences.

## Overview

This service exposes a small in-memory preferences API for a single member. The current implementation uses:

- Spring Boot 4.1.0
- Spring Web MVC
- Spring Validation
- an in-memory `ConcurrentHashMap` store in the service layer
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

## Validation Behavior

The service validates:
- member ID path format
- required request fields
- language pattern
- timezone pattern
- enum values for `theme` and `profileVisibility`

Validation failures return a ProblemDetail-style error response.

## Running the Service

From the project root:

```bash
./mvnw spring-boot:run
```

## Testing

Run the current test suite:

```bash
./mvnw test
```

Current test status:
- One basic Spring Boot context test is present
- Full HTTP integration tests for the REST endpoints are not yet implemented in the repository

## Project Structure

- `src/main/java/.../controller` – REST endpoints
- `src/main/java/.../service` – business logic and in-memory storage
- `src/main/java/.../domain/dto` – request/response models
- `src/test/java` – test classes

## Notes

This service is currently an in-memory prototype and does not persist preferences to a database.
