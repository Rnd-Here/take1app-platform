# Take One Backend Application

This is the backend Spring Boot application for the **Take One** platform—a talent management system where artists showcase their portfolios, scouts discover talent, and opportunities connect through secure, end-to-end encrypted messaging.

## 🚀 Welcome to Take One
Visit the [Landing Page](http://localhost:8080/) for a visual tour of the platform features, architecture flow, and tech stack.

## 🛠 Technology Stack
- **Framework**: Spring Boot 3.4.13
- **API Documentation**: [SpringDoc OpenAPI 2.x](https://springdoc.org/) (Swagger UI)
- **Security**: Spring Security + Firebase Admin SDK
- **Real-time**: Spring WebSockets (E2EE Relay)
- **Database**: MySQL 8.0 (Managed via Flyway)
- **Presence & Cache**: Redis
- **Observability**: Prometheus, Grafana, Loki, Splunk HEC
- **PII Protection**: Custom Logback Masking + Lombok Exclusions

## 📚 API Documentation
The API is fully documented using OpenAPI 3.1.0 standards. You can interact with the endpoints directly via Swagger UI.

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Docs (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Contract Definition**: [take-one-app.yaml](file:///e:/Work/Spring%20Boot/take-one-app-backend/src/main/resources/take-one-app.yaml)

> [!TIP]
> Use the "Authorize" button in Swagger UI to provide your Bearer Token for testing secured endpoints.

## 💬 E2E Messaging Service
The platform implements a **WhatsApp-style Store-and-Forward** messaging relay.
- **WebSocket Relay**: Real-time communication via `/ws-relay`. Messages are encrypted by the client (E2E) and never decrypted by the server.
- **Presence Tracking**: Redis-backed `UserStatusService` tracks real-time online/offline status and "last seen" timestamps.
- **Durable Storage**: If a recipient is offline, messages are stored in a MySQL `pending_messages` table and automatically synced upon reconnection.
- **Push Fallback**: Integrated FCM notifies offline users of new messages.
- **Reliability**: Messages are only purged from the relay database after a `DELIVERY_ACK` is received from the recipient's device.

## 🔔 Enhanced Push Notifications
FCM (Firebase Cloud Messaging) integration supports intelligent multi-device delivery.

### Device Metadata Tracking
When registering an FCM token via `POST /api/notifications/fcm-token`, the system now gathers rich device metadata:
- **Device Model** (e.g., iPhone 15 Pro, Samsung S24)
- **OS Version** (e.g., iOS 17.5, Android 14)
- **App Version** (e.g., v1.0.4)
- **Timezone & Language**: For localized and appropriately timed notifications.

## 🔒 Security & Traceability
### Mandatory Trace ID
Every business API request must include a unique transaction identifier in the header for system-wide observability.
- **Header**: `X-Trace-Id`
- **Value**: Any UUID string.
- **Exceptions**: The home page, Swagger UI, API docs, and actuator endpoints do **not** require this header.

### Session Authentication
Authentication is session-based, validated via `Authorization: Bearer <token>` or `X-Session-Token` headers. The `SessionAuthenticationFilter` ensures stateless validation against high-performance Redis/MySQL storage.

## 🚀 Running Locally with Docker

### Prerequisites
- **Docker** and **Docker Compose** installed.
- **Firebase Service Account**: A JSON file or Base64 string from the Firebase Console.

### 1. Configuration Setup
The application needs your Firebase credentials and can be configured for Splunk.
1.  Create a `.env` file in the root directory.
2.  Add the following:
```env
FIREBASE_CONFIG_BASE64=<your_base64_string_here>

# Optional Splunk Configuration
SPLUNK_URL=http://your-splunk-instance:8088/services/collector/event
SPLUNK_TOKEN=your-hec-token
SPLUNK_INDEX=main
GRAFANA_LOKI_TOKEN=<your_loki_token>
```

### 2. Run the Application
```bash
docker compose -f docker-compose-dev.yml up --build
```

## 🔒 Security, Traceability & Authentication
### WebSocket Authentication
The WebSocket handshake requires a valid session token passed via:
- URL Parameter: `/ws-relay?token=YOUR_TOKEN`
- Custom Header: `X-Session-Token`

### Mandatory Trace ID
Every API request (except monitoring and docs) must include a unique transaction identifier in the header:
- **Header**: `X-Trace-Id`
- **Value**: UUID (e.g., `550e8400-e29b-41d4-a716-446655440000`)
- **Reason**: All logs are prefixed with this ID, allowing end-to-end request tracing across the system.

### PII Protection
The system is designed to prevent PII leakage:
- **Masking**: Emails and Phone numbers are automatically masked in all logs (e.g., `u******e@example.com`).
- **Exclusion**: Sensitive fields are excluded from object string representations (`toString`).

## 📊 Observability

### Grafana & Prometheus
The application exposes metrics in Prometheus format for Grafana dashboards.
- **Metrics Endpoint**: `http://localhost:8080/actuator/prometheus`
- **Grafana Placeholder**: [View Dashboards Here](http://your-grafana-instance:3000) (Configure Prometheus data source pointing to your app).

### Splunk Logging
Logs are sent asynchronously to Splunk via HTTP Event Collector (HEC).
- **Log Format**: `ApplicationName:Endpoint:UUID:Java Class:Method:message`
- **Splunk Placeholder**: [Check Logs in Splunk](http://your-splunk-instance:8000) (Search by `X-Trace-Id` for specific request flows).

## 📚 API Documentation
1.  Import `docs/postman_collection.json` into Postman.
2.  Ensure you set the `trace_id` variable in your environment.
3.  Follow the **Firebase Phone Auth** folder instructions to get a token first.

## 🚢 Production Deployment
Deployment is automated via GitHub Actions to your VPS. 
- **CI/CD**: Pushes to `main` trigger a build, package (GHCR), and deploy (SSH) cycle.
- **Session Cleanup**: A background scheduler runs daily at 3 AM to deactivate expired sessions and hard-delete records older than 60 days.

## 📊 Monitoring
- **Metrics**: `http://localhost:8080/actuator/prometheus`
- **Logs**: Asynchronous delivery to **Splunk** and **Grafana Loki** with automatic PII masking.

---
&copy; 2026 Take One Platform. All rights reserved.