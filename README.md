# Talent Management Platform

A comprehensive microservices-based talent management platform for artists, performers, and entertainment industry professionals. Built with Spring Boot and designed to scale to 59,000+ users.

## 🎯 Overview

This platform serves as an Instagram-like social network combined with professional portfolio management, job postings, and event scheduling for the entertainment industry.

## 🏗️ Architecture

### Microservices
- **API Gateway** (Port 8080) - Entry point for all client requests
- **Auth Service** (Port 8081) - OTP-based multi-device authentication
- **User Service** (Port 8082) - User profiles and verification
- **Content Service** (Port 8083) - Posts, stories, media management
- **Connection Service** (Port 8084) - Follow system and connections
- **Messaging Service** (Port 8085) - E2E encrypted chat
- **Job Service** (Port 8086) - Job postings and applications
- **Event Service** (Port 8087) - Event and audition scheduling
- **Notification Service** (Port 8088) - Push, email, in-app notifications
- **Search Service** (Port 8089) - Elasticsearch-powered search
- **Analytics Service** (Port 8090) - Profile stats and insights
- **Media Processing Service** (Port 8091) - Image/video processing

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Maven (Multi-module)

**Databases:**
- PostgreSQL - User accounts, jobs, events
- MongoDB - Posts, stories, analytics
- Cassandra - Messages, feeds (high write throughput)
- Redis - Caching, sessions
- Elasticsearch - Search indexing

**Message Broker:**
- Apache Kafka - Event streaming

**Authentication:**
- Firebase Auth - OTP and multi-device login

**Storage:**
- AWS S3 / Compatible storage

**Client:**
- Flutter (Mobile/Web)

## 📦 Project Structure

```
talent-management-platform/
├── common/                    # Shared utilities and DTOs
├── api-gateway/              # API Gateway
├── auth-service/             # Authentication service
├── user-service/             # User management
├── content-service/          # Content management
├── connection-service/       # Social connections
├── messaging-service/        # Chat system
├── job-service/              # Job postings
├── event-service/            # Events & auditions
├── notification-service/     # Notifications
├── search-service/           # Search functionality
├── analytics-service/        # Analytics & stats
├── media-processing-service/ # Media processing
├── scripts/                  # Database scripts
├── k8s/                      # Kubernetes configs
├── docs/                     # Documentation
├── docker-compose.yml        # Docker setup
└── pom.xml                   # Root Maven config
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### 1. Clone and Build

```bash
git clone <repository-url>
cd talent-management-platform

# Build all modules
mvn clean install
```

### 2. Start Infrastructure

```bash
# Start databases and message brokers
docker-compose up -d postgres mongodb redis cassandra elasticsearch kafka zookeeper
```

### 3. Run Services

**Option A: Using Maven (Development)**
```bash
# In separate terminals
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd content-service && mvn spring-boot:run
# ... repeat for other services
```

**Option B: Using Docker (Full Stack)**
```bash
# Build all services
docker-compose build

# Start everything
docker-compose up -d

# View logs
docker-compose logs -f user-service
```

## 🔧 Configuration

Each service has its own `application.yml` with environment-specific profiles:
- `application.yml` - Default configuration
- `application-dev.yml` - Development environment
- `application-docker.yml` - Docker environment
- `application-prod.yml` - Production environment

### Database Connections

**PostgreSQL:** `localhost:5432`
- talent_users
- talent_auth
- talent_jobs
- talent_events
- talent_connections

**MongoDB:** `localhost:27017`

**Redis:** `localhost:6379`

**Cassandra:** `localhost:9042`

**Elasticsearch:** `localhost:9200`

**Kafka:** `localhost:9092`

## 👥 Account Types

1. **Talent** - Artists, performers, technicians
   - Create portfolio content
   - Apply for jobs
   - Attend auditions
   - Full social features

2. **Industry Professional** - Casting directors, agents, scouts
   - View talent profiles
   - Post job openings
   - Schedule auditions
   - Direct messaging with talents

3. **Fanatic** - Fans and followers
   - View public talent content only
   - Limited interaction (if allowed by talent)
   - No access to job listings

## ✨ Key Features

- 🔐 OTP-based multi-device authentication
- 📱 Instagram-like feed and stories
- 💼 Professional portfolio pages
- ✅ Verified account badges
- 💬 E2E encrypted messaging
- 📋 Job posting and application system
- 📅 Event and audition scheduling
- 🔍 Advanced search capabilities
- 📊 Profile analytics and insights
- 🔔 Multi-channel notifications
- 👥 Follow system and connections
- 🎭 Profile managers (3rd party access)

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run tests for specific service
mvn test -pl user-service

# Integration tests
mvn verify
```

## 📚 API Documentation

Once services are running, access Swagger UI:
- Auth Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html
- Content Service: http://localhost:8083/swagger-ui.html
- (Repeat for other services)

## 🔄 Inter-Service Communication

Services communicate via:
1. **REST APIs** (Feign Clients) - Synchronous requests
2. **Kafka Events** - Asynchronous messaging
3. **Shared Common Module** - DTOs and utilities

Example:
```java
// Feign Client (Synchronous)
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    UserDto getUser(@PathVariable Long id);
}

// Kafka Event (Asynchronous)
kafkaTemplate.send("user.created", userCreatedEvent);
```

## 📈 Scaling

- Each service can be scaled independently
- Use Kubernetes for orchestration
- Load balancer in front of API Gateway
- Database read replicas for heavy read operations
- CDN for media content delivery

## 🔒 Security

- JWT-based authentication
- E2E encryption for messages
- Rate limiting per endpoint
- Role-based access control (RBAC)
- Input validation and sanitization
- HTTPS in production

## 🛠️ Development

### Build Single Service
```bash
mvn clean install -pl user-service -am
```

### Hot Reload
Use Spring Boot DevTools in each service for automatic restart on code changes.

### Debug
Attach debugger to service on ports 8081-8091.

## 📦 Deployment

### Docker Production Build
```bash
# Build with production profile
mvn clean package -P production

# Build Docker images
docker build -t talent/user-service:1.0.0 ./user-service

# Push to registry
docker push registry.example.com/talent/user-service:1.0.0
```

### Kubernetes
```bash
kubectl apply -f k8s/
```

## 📊 Monitoring

- Spring Boot Actuator endpoints
- Prometheus metrics
- Grafana dashboards
- ELK Stack for log aggregation

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Write tests
4. Submit pull request

## 📄 License

[Your License Here]

## 📞 Contact

[Your Contact Information]

## 🗺️ Roadmap

- [ ] ML-based job recommendations
- [ ] Video calling integration
- [ ] Advanced content moderation
- [ ] Multi-language support
- [ ] Mobile apps (iOS/Android)
- [ ] Payment integration
- [ ] Contract management

---

**Built with ❤️ for the Entertainment Industry**
