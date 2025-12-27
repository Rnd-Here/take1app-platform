# Take One Backend Application

This is the backend Spring Boot application for the Take One platform. It provides authentication (via Firebase), creator profile management, and session handling with automated security and deployment workflows.

## 🛠 Technology Stack
- **Framework**: Spring Boot 3.4.13
- **Security**: Spring Security + Firebase Admin SDK
- **Database**: MySQL 8.0 (Managed via Flyway)
- **Cache**: Redis
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions + GitHub Container Registry (GHCR)

## 🚀 Quick Start with Docker

The easiest way to run the application is using Docker. This will spin up the API, MySQL database, and Redis cache automatically.

### Prerequisites
- **Docker** and **Docker Compose** installed.

### 1. Configuration Setup
The application needs your Firebase credentials.
1.  Create a `.env` file in the root directory.
2.  Convert your `firebase-service-account.json` to Base64.
    *   **Mac/Linux**: `cat firebase-service-account.json | base64`
    *   **Windows (PowerShell)**: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("firebase-service-account.json"))`
3.  Add the content to your `.env` file:
    ```env
    FIREBASE_CONFIG_BASE64=<your_base64_string_here>
    ```

### 2. Run the Application

#### For Local Development (Build from source)
Use this if you want to test code changes locally on your machine.
```bash
docker compose -f docker-compose-dev.yml up --build
```

#### For Production-like Testing (Using Registry Image)
Use this if you want to test the exact image that will be deployed to the VPS.
```bash
docker compose up
```

*   The application will be available at: `http://localhost:8080`
*   **Database Management**: Flyway automatically handles schema migration on startup using scripts in `src/main/resources/db/migration`.

## 📚 API Documentation
1.  Import `docs/postman_collection.json` into Postman.
2.  Set the `firebase_id_token` variable in the collection to a valid ID token.
3.  **Key Endpoints**:
    - **Auth**: `/api/auth/token` (Login), `/api/auth/logout`
    - **Profile**: `/api/user/profile` (Get/Create/Update Creator Profiles)

## 🚢 Production Deployment

Automated deployment is handled via GitHub Actions using a **Registry-Based Strategy (GHCR)**. This keeps your source code secure and your VPS deployments fast.

### 1. VPS Setup (Initial Only)
1.  SSH into your VPS.
2.  Install Docker and Docker Compose.
3.  Login to GHCR (requires a GitHub PAT with `read:packages` permission):
    ```bash
    echo "YOUR_PAT_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
    ```

### 2. GitHub Configuration
Go to **Settings > Secrets and variables > Actions** and add the following:

#### Repository Secrets
| Secret | Description |
|--------|-------------|
| `VPS_HOST` | IP address of your VPS |
| `VPS_USERNAME` | SSH username (e.g., `root`) |
| `VPS_SSH_KEY` | Private SSH Key (e.g., contents of `id_rsa`) |
| `TAKE_ONE_APP_FIREBASE_SERVICE_ACCOUNT_JSON` | **Base64 encoded** Firebase JSON. |

#### Repository Variables
| Variable | Value |
|----------|-------|
| `PROJECT_PATH` | The absolute path on your VPS (e.g., `/var/www/take-one`) |

### 3. Workflow
Every push to `main` will:
1.  Compile the app with Maven.
2.  Build and push a Docker image to **GHCR**.
3.  SSH into the VPS.
4.  Update the `docker-compose.yml` and `.env` on the server.
5.  Pull the new image and restart the service (`docker-compose pull && docker-compose up -d`).



