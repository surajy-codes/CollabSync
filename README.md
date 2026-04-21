# CollabSync

A full-stack team collaboration platform where teams manage projects, tasks, and communicate in real-time — with AI as a productivity layer on top.

Built with **Spring Boot · React · PostgreSQL · WebSocket · AI API**

---

## Features

- **Auth** — JWT-based authentication with access + refresh token rotation
- **Teams & Projects** — Create teams, invite members with roles (Owner / Member / Viewer), organize work into projects
- **Task Management** — Tasks with status, priority, assignee, due dates, subtasks, comments, and file attachments
- **Real-time** — Live task board updates and per-project group chat powered by WebSockets (STOMP)
- **AI Utilities** — Auto-generate task descriptions, suggest priority, and generate weekly project summaries
- **Notifications** — In-app notifications for assignments, updates, and comments
- **File Storage** — File attachments on tasks via MinIO (S3-compatible)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.x |
| Auth | Spring Security, JWT |
| Database | PostgreSQL, Spring Data JPA |
| Real-time | Spring WebSocket (STOMP) |
| File Storage | MinIO |
| AI | Anthropic Claude API |
| Frontend | React 18, TailwindCSS |
| DevOps | Docker Compose |

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose

### Run with Docker

```bash
git clone https://github.com/yourusername/collabsync.git
cd collabsync
docker-compose up -d
```

### Run Backend Locally

```bash
cd backend
./mvnw spring-boot:run
```

### Run Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

---

## Project Structure

```
collabsync/
├── backend/        # Spring Boot REST API + WebSocket + AI
├── frontend/       # React frontend
└── docker-compose.yml
```

---

## API Docs

Swagger UI available at `http://localhost:8080/swagger-ui.html` once the backend is running.

---

## Status

🚧 **In active development**
