# Online Monopoly – Multiplayer Game with Lobby Support

This is a real-time online Monopoly game for up to **4 players** featuring a **3D board**, **lobby system**, and a **microservice-based backend**. The architecture is asynchronous, event-driven, and built using **Redis Streams**, **GraphQL**, and **WebSockets**.

## Architecture Overview

The system is split into **six main services**:

---

### 1.  API Gateway

- Accepts all **GraphQL** client requests.
- Stores **`CompletableFuture`** objects associated with a **Correlation ID**.
- Publishes these requests into a **Redis Streams** channel.
- Waits for a corresponding response from the backend.

---

### 2.  API Logic

- Subscribes to the Redis Streams channel.
- Listens for incoming game actions.
- Validates the game state and computes possible actions.
- Sends results back to the API Gateway using the correlation ID.

---

### 3.  Frontend

- Built with **React**, **Three.js**, **GSAP**, and **JavaScript**.
- Uses **two WebSocket connections**:
    - One for game state updates
    - One for dice roll events
- Renders a **3D Monopoly board** and players using **Three.js**.
- Animates player movement with **GSAP**.
- Handles UI logic with **React** components.

---

### 4.  Dice Server

- Listens for dice roll requests via Redis.
- Simulates a roll (ensuring a **single source of truth**).
- Publishes results back to Redis for consumption by the API Logic and Gateway.

---

### 5.  Shared Module

- Common logic, enums, DTOs, and utilities used across all backend services.

---

### 6.  GraphQL Module

- Centralized GraphQL schema and resolvers.
- Shared between the API Gateway and API Logic services.

---

## ⚙️ Tech Stack

| Layer         | Technologies                                 |
|---------------|----------------------------------------------|
| Frontend      | React, Three.js, GSAP, Apollo-client, Node   |
| Backend       | Spring-boot, Project Reactor (Mono/Flux)     |
| Communication | Redis (Streams + Pub/Sub), CompletableFuture |
| Architecture  | Microservices, Event-driven                  |

---

## 📦 Installation & Run

### 🐳 Prerequisites

- Redis server (`docker` or local)
- Java/Kotlin (for backend services)
- Node.js & npm (for frontend)

---

### 🚀 Backend Setup

```bash
# Start Redis server
docker run -p 6379:6379 redis

# API Gateway
cd api-gateway
./gradlew bootRun

# API Logic
cd ../api-logic
./gradlew bootRun

# Dice Server
cd ../dice-server
./gradlew bootRun
