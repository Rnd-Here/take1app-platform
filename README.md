# Artist Backend Application

This is the backend Spring Boot application for the Artist Portfolio platform. It provides authentication (via Firebase), user profile management, and session handling.

## 🚀 Quick Start with Docker

The easiest way to run the application is using Docker. This will spin up the API, MySQL database, and Redis cache automatically.

### Prerequisites
- **Docker** and **Docker Compose** installed on your machine.

### 1. Configuration Setup

The application needs your Firebase credentials. You have two options:

#### Option A: Using `.env` file (Recommended)
1.  Create a `.env` file in the root directory.
2.  Convert your `firebase-service-account.json` to Base64.
    *   **Mac/Linux**: `base64 -i path/to/firebase-service-account.json | tr -d '\n'`
    *   **Windows (PowerShell)**: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\firebase-service-account.json"))`
3.  Add the content to your `.env` file:
    ```env
    FIREBASE_CONFIG_BASE64=<your_base64_string_here>
    ```

#### Option B: Using a File
1.  Place your `firebase-service-account.json` in the root directory of the project.
2.  Uncomment the volume mapping in `docker-compose.yml`:
    ```yaml
    volumes:
      - ./firebase-service-account.json:/app/firebase-service-account.json
    ```

### 2. Run the Application

```bash
docker-compose up --build
```
*   The application will be available at: `http://localhost:8080`
*   MySQL is accessible on port `3306`
*   Redis is accessible on port `6379`

## 📚 API Documentation

A Postman collection is available for testing the API.
1.  Import `docs/postman_collection.json` into Postman.
2.  Set the `firebase_id_token` variable in the collection to a valid ID token from your client app.
3.  Start testing endpoints!

### Key Endpoints
- **Auth**: `/api/auth/token` (Login), `/api/auth/logout`
- **User**: `/api/user/profile` (Get/Create/Update Profile)

## 🛠 Troubleshooting

- **Service Account Not Found**: Ensure you set the `FIREBASE_CONFIG_BASE64` env var correctly or mapped the file volume.
- **Port Conflicts**: Ensure ports 8080, 3306, and 6379 are free.

## 🚢 Production Deployment

Automated deployment is handled via GitHub Actions to a Hostinger VPS (or any server with SSH/Docker).

### 1. VPS Setup
- **OS**: Ubuntu (recommended) or any Linux distro.
- **Dependencies**: Install `docker` and `docker-compose`.
- **Directory**: Create a project folder (e.g., `/home/user/take-one-app-backend`).

### 2. GitHub Repository Secrets
Go to **Settings > Secrets and variables > Actions** and add:

| Secret | Description |
|--------|-------------|
| `VPS_HOST` | IP address of your VPS |
| `VPS_USERNAME` | SSH username (e.g., `root` or `ubuntu`) |
| `VPS_SSH_KEY` | Private SSH Key (ensure public key is in VPS `~/.ssh/authorized_keys`) |
| `TAKE_ONE_APP_FIREBASE_SERVICE_ACCOUNT_JSON` | **Base64 encoded** Firebase JSON. See "Configuration Setup" above. |

### 3. Workflow (`.github/workflows/deploy.yml`)
The included workflow will:
1.  Connect to your VPS via SSH.
2.  Navigate to the project directory (update script path in `.github/workflows/deploy.yml` if needed).
3.  Pull the latest code.
4.  Inject the Base64 Firebase secret into a `.env` file.
5.  Restart the application using Docker Compose.

**Note**: Ensure the target directory on your VPS is a git repository initialized with `git clone`.


