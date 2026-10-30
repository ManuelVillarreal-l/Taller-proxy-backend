# AI Gateway - Backend

Spring Boot 3.2.5 / Java 17 service that proxies AI generation requests through a
rate limiter and a monthly token quota.

## Run locally

```bash
mvn spring-boot:run
```

## Build

```bash
mvn package
java -jar target/ia-gateway-1.0.0.jar
```

## Endpoints

| Method | Path                                  | Description                                  |
|--------|---------------------------------------|----------------------------------------------|
| POST   | `/api/ai/generate`                    | Body `{prompt, userId}`; optional `X-User-Id` header |
| GET    | `/api/quota/status?userId=demo-user`  | Plan, tokens and rate limit status            |
| GET    | `/api/quota/history?userId=demo-user` | Token usage for the last 7 days               |
| POST   | `/api/quota/upgrade?userId=demo-user` | Upgrades FREE to PRO                          |

Error responses: `429` (+ `Retry-After` header, body `{error, retryAfter}`) and `402` (`{error: "Quota exceeded"}`).

## Configuration

- `PORT` - HTTP port (Railway sets it automatically). Default `8080`.
- `FREE_TOKEN_LIMIT` - monthly tokens for the FREE plan. Default `50000`. Set e.g. `200` to trigger the quota error quickly during demos.

## Deploy on Railway

Create a new project from this repository. Railway detects Maven, runs the build and
starts the generated jar using the `PORT` variable.
