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
The Postman collection (`docs/postman_collection.json`) provides a comprehensive set of requests for testing the Take One App Backend API. To get started:

1.  Import `docs/postman_collection.json` into Postman.
2.  **Firebase Phone Auth Setup**:
    *   **Test Phone Number**: The collection includes requests for Firebase Phone Auth using a test mobile number (`+917639950611`) and OTP (`123456`).
    *   **Firebase Configuration**: For these requests to work, you *must* configure the test phone number (`+917639950611`) in your Firebase project's Authentication settings under "Phone numbers for testing".
    *   **Get Firebase ID Token**:
        1.  Run the `1. Get Verification Info` request to obtain `sessionInfo`. This will automatically be saved to the `firebase_session_info` collection variable.
        2.  Run the `2. Sign In With Phone & OTP` request. This will use the `sessionInfo` and the test OTP (`123456`) to sign in and save the resulting Firebase ID Token to the `firebase_id_token` collection variable. This token represents the Firebase UID.
3.  **Backend Authentication**:
    *   Once you have the `firebase_id_token`, run the `Exchange Token` request under `Take One App API > Auth`. This will exchange the Firebase ID Token for a `session_token` which is required for authenticating with the backend API. The `session_token` will be automatically saved to the `session_token` collection variable.
    *   You can then use the `session_token` in subsequent backend API requests (e.g., `Validate Session`, `Get Profile`).

**Key Endpoints**:
    - **Auth**: `/api/auth/token` (Login), `/api/auth/logout`
    - **Profile**: `/api/user/profile` (Get/Create/Update Creator Profiles)

## 🚢 Production Deployment

Deployment is automated via GitHub Actions. The strategy involves building a Docker image, pushing it to the GitHub Container Registry (GHCR), and then deploying it to the VPS. This keeps your source code secure and deployments fast and consistent.

### 1. GitHub Configuration
Go to your repository's **Settings > Secrets and variables > Actions** and add the following:

#### Repository Secrets
| Secret | Description |
|--------|-------------|
| `VPS_HOST` | IP address of your production VPS. |
| `VPS_USERNAME` | SSH username for the VPS (e.g., `root`). |
| `VPS_SSH_KEY` | The private SSH key used to access the VPS. |
| `TAKE_ONE_APP_FIREBASE_SERVICE_ACCOUNT_JSON` | The **Base64 encoded** content of your Firebase service account JSON file. |

#### Repository Variables
| Variable | Value |
|----------|-------|
| `PROJECT_PATH` | The absolute path on the VPS where the project will be deployed (e.g., `/home/user/take-one-app`). |

### 2. Initial VPS Setup
Before the first deployment, you need to prepare your VPS:

1.  **Install Docker**: Ensure Docker and Docker Compose are installed on your VPS. If you are using a managed hosting service like Hostinger, it is recommended to use their control panel for this.
2.  **GHCR Login (Optional but Recommended)**: If your container images are private, you must log your VPS's Docker daemon into GHCR. This requires a GitHub Personal Access Token (PAT) with `read:packages` permission.
    ```bash
    echo "YOUR_PAT_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
    ```

> **Troubleshooting Note:** If you encounter errors like `blob not found` or `no such file or directory` during the `docker compose pull` step of a deployment, it indicates your Docker installation on the VPS may be corrupted. The most reliable solution is to use your hosting provider's panel to completely **uninstall and reinstall Docker**.

### 3. Automated Workflow
Every push to the `main` branch triggers the following automated workflow:
1.  **Build**: The application is compiled into a JAR file using Maven.
2.  **Package**: A Docker image is built and pushed to the GitHub Container Registry (GHCR).
3.  **Deploy**: The workflow connects to your VPS via SSH and:
    a. Creates the project directory.
    b. Dynamically generates a `.env` file containing the Firebase credentials.
    c. Dynamically generates a `docker-compose.yml` file configured with the correct image name and services.
    d. Executes `docker compose pull` to download the latest application image.
    e. Restarts the services with `docker compose up -d` to apply the update.



