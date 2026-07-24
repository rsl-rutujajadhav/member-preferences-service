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

All responses include a strong `ETag` header. Use `If-None-Match` to receive `304 Not Modified` when the resource hasn't changed.

Example:

```bash
curl http://localhost:8080/v1/preferences/usr_a1b2c3d4
```

Response:
- `200 OK` — Returns the stored preferences for the member with an `ETag` header
- `304 Not Modified` — If the request includes `If-None-Match` with the current ETag (no body)
- `404 Not Found` — When the member does not yet exist
- Example not-found response:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No member found with id usr_a1b2c3d4",
  "instance": "/v1/preferences/usr_a1b2c3d4"
}
```

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
- `201 Created` for a new member (includes `ETag` header)
- `200 OK` when replacing an existing member's preferences (includes `ETag` header)
- `412 Precondition Failed` when the request includes `If-Match` that doesn't match the current ETag

Use the `If-Match` header with the ETag from a prior GET to ensure you're updating the version you read. Sending `If-Match: *` ensures the resource already exists.

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
- `200 OK` — Only the provided fields are updated (includes `ETag` header)
- `412 Precondition Failed` — When the request includes `If-Match` that doesn't match the current ETag

## Supported Values

Theme:
- `LIGHT`
- `DARK`
- `SYSTEM`

Language:
- Format: `xx-YY` such as `en-US`, `fr-FR`

Timezone:
- Format expected by validation: `Area/City`, such as `Europe/Paris`, `America/New_York`, `Asia/Tokyo`
- Note: flat names like `UTC` are not accepted by input validation (must contain `/`)

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
| 304 | Not Modified | GET with `If-None-Match` matches current ETag (no body) |
| 404 | Not Found | Member does not exist yet or the requested route does not exist |
| 412 | Precondition Failed | PUT/PATCH with `If-Match` does not match current ETag |
| 429 | Too Many Requests | Rate limit exceeded (overall or per-member) |
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

### Conditional requests (ETag)

Responses include a strong `ETag` header (SHA-256 hash of the resource content, quoted per RFC 7232). Clients can use it for caching and concurrency control.

**GET with `If-None-Match`** — returns `304 Not Modified` when the resource hasn't changed:

```bash
# First request captures the ETag
ETAG=$(curl -sI http://localhost:8080/v1/preferences/usr_a1b2c3d4 | grep -i etag | cut -d' ' -f2 | tr -d '[:space:]')

# Subsequent request with If-None-Match returns 304 if unchanged
curl -s -o /dev/null -w "%{http_code}" -H "If-None-Match: $ETAG" http://localhost:8080/v1/preferences/usr_a1b2c3d4
# → 304
```

**PUT/PATCH with `If-Match`** — returns `412 Precondition Failed` when the ETag doesn't match:

```bash
ETAG=$(curl -sI http://localhost:8080/v1/preferences/usr_a1b2c3d4 | grep -i etag | cut -d' ' -f2 | tr -d '[:space:]')

# Replace only if the resource matches our ETag (succeeds)
curl -X PUT -H "Content-Type: application/json" -H "If-Match: $ETAG" \
  -d '{"theme":"DARK","language":"en-US","timezone":"America/New_York","notifications":{"email":true,"sms":true,"push":true},"privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}' \
  http://localhost:8080/v1/preferences/usr_a1b2c3d4

# Stale ETag returns 412
curl -X PUT -H "Content-Type: application/json" -H "If-Match: \"stale-etag\"" \
  -d '{"theme":"LIGHT"...}' \
  http://localhost:8080/v1/preferences/usr_a1b2c3d4
# → 412 Precondition Failed
```

Use `If-Match: *` on PUT to ensure the resource already exists before replacing.

### Rate limiting (429)

When a client exceeds the rate limit, the service responds with `429 Too Many Requests`:

```bash
# Simulate a burst: rapid requests cause 429 after exhausting the bucket
for i in $(seq 1 110); do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/v1/preferences/usr_$i
done | sort | uniq -c
# Output shows ~100x 200 and ~10x 429
```

Response:
```json
{
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Overall rate limit exceeded",
  "type": "about:blank"
}
```

The `Retry-After: 1` header indicates the client should wait at least one second before retrying.

## Configuration

Feature flags and service settings are managed via `@ConfigurationProperties` bound to `application.yml`.

| Property | Default | Description |
|----------|---------|-------------|
| `preferences.patch.enabled` | `true` | Enables or disables the PATCH endpoint |
| `preferences.rate-limit.overall.capacity` | `100` | Max overall requests in a burst before 429 |
| `preferences.rate-limit.overall.refill-per-minute` | `100` | Overall token refill rate per minute |
| `preferences.rate-limit.per-member.capacity` | `20` | Max requests per memberId in a burst before 429 |
| `preferences.rate-limit.per-member.refill-per-minute` | `20` | Per-member token refill rate per minute |

Rate limiting uses a token-bucket algorithm. Each request consumes one token. If the bucket is empty, the request is rejected with `429 Too Many Requests` and a `Retry-After: 1` header. There are two independent buckets: one for all requests (overall) and one per `memberId`. A single member cannot starve other members.

Timeout defaults are configured via `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.tomcat.connection-timeout` | `5s` | Maximum time to wait for a request body |
| `server.tomcat.keep-alive-timeout` | `30s` | Keep-alive timeout for persistent connections |
| `server.tomcat.threads.max` | `200` | Maximum number of worker threads |

To disable patching (e.g. during maintenance):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile sets `preferences.patch.enabled: false`. When disabled, PATCH requests return `422 Unprocessable Entity` with detail `"Patching preferences is currently disabled"` while GET and PUT continue working.

All configuration properties are visible at `/actuator/configprops` at runtime.

## Metrics

Custom Micrometer counters and timers are registered per endpoint and visible at `/actuator/metrics`:

| Metric | Type | Description |
|--------|------|-------------|
| `preferences.get.count` | Counter | Number of GET preference requests |
| `preferences.put.count` | Counter | Number of PUT preference requests |
| `preferences.patch.count` | Counter | Number of PATCH preference requests |
| `preferences.get.duration` | Timer | Duration of GET preference requests |
| `preferences.put.duration` | Timer | Duration of PUT preference requests |
| `preferences.patch.duration` | Timer | Duration of PATCH preference requests |

Spring Boot's built-in `http.server.requests` metric is also available.

## Structured Logging

Logs are emitted as JSON via Logstash encoder with a `correlationId` field. Every response includes an `X-Correlation-Id` header. If the client sends one via the request header, it is preserved; otherwise a UUID is generated.

Example log line:

```json
{"timestamp":"...","logger":"...","message":"Getting preferences for member usr_a1b2c3d4","correlationId":"a1b2c3d4-...","level":"INFO"}
```

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
- HTTP integration tests for happy path: PUT upsert (201/200), GET defaults, PATCH partial update
- Rate limiting tests: overall burst → 429, per-member burst → 429, member isolation
- ETag tests: GET returns ETag header, matching `If-None-Match` → 304, `If-Match` mismatch → 412
- Feature flag test (PATCH disabled via `dev` profile returns 422)
- Config properties binding and actuator `/configprops` exposure
- Micrometer metrics (counters, timers) and structured JSON logging with correlation ID

## Project Structure

- `src/main/java/.../controller` – REST endpoints and global error handler
- `src/main/java/.../service` – business logic
- `src/main/java/.../repository` – in-memory data access with thread-safe operations
- `src/main/java/.../config` – `@ConfigurationProperties` classes and feature flags
- `src/main/java/.../domain/dto` – request/response models and error response DTO
- `src/main/java/.../domain/exception` – custom exception types (MemberNotFound, PreconditionFailed, UnprocessableEntity)
- `src/main/java/.../filter` – servlet filters (correlation ID, rate limiting, token bucket)
- `src/main/java/.../util` – utility classes (ETag generator)
- `src/main/resources/application.yml` – default config
- `src/main/resources/application-dev.yml` – dev profile override
- `src/main/resources/logback-spring.xml` – structured JSON logging
- `src/test/java` – test classes

## Notes

This service is currently an in-memory prototype and does not persist preferences to a database. Concurrent access is safe via `ConcurrentHashMap.compute()` atomic operations.
