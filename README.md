# Online Monopoly – Multiplayer Game with Lobby Support

This is a real-time online Monopoly game for up to **4 players** featuring a **3D board**, **lobby system**, and a **microservice-based backend**. The architecture is asynchronous, event-driven, and built using **Redis Streams**, **GraphQL**, and **Webflux**.

## Architecture Overview

The system is split into **six main services**:

---

### 1.  API Gateway

- Accepts all **GraphQL** client requests.
- Stores **`CompletableFuture`** objects associated with a **Correlation ID**.
- Publishes these requests into a **Redis Streams** channel.
- Waits for a corresponding response from the backend.
- Listens for **gameUpdate** and **diceUpdate** redis channels, sends the received data via GraphQL subscriptions(Websockets) to clients
---

### 2.  API Logic

- Subscribes to the Redis Streams channel.
- Listens for incoming game actions.
- Validates the game state and computes possible actions.
- Handles logic(dice roll request, bot's turns)
- Sends results back to the API Gateway using the correlation ID via redis.

---

### 3.  Frontend

- Uses **two WebSocket connections via apollo client** :
  - One for game state updates
  - One for dice updates
- Renders a **3D Monopoly board** and players using **Three.js**.
- Animates player movement with **GSAP**.
- Handles UI logic with **React** components.
---

### 4.  Dice Server

- Listens for dice roll requests via Redis.
- Simulates a roll for each individual game using Rapier3d (ensuring a **single source of truth**).
- Publishes roll results back to Redis for consumption by the API Logic.
- Published **Dice position and rotation** to Api-gateway
---
### 5. Persistence Module

### 6.  Shared Module

- Common config and service for redis. Shared Exceptions between API-Gateway and API-Logic

--- 

### 7.  GraphQL Module

- Centralized GraphQL schema and resolvers.
- Uses **graphqlcodegen-maven-plugin** for generating **DTO's,enums,query's**...
- Shared between Api-gateway, Api-logic, api-Persistence.

---

## ⚙️ Tech Stack

| Layer         | Technologies                                                                    |
|---------------|---------------------------------------------------------------------------------|
| Frontend      | React, Three.js, GSAP(for animations), Apollo-client, Node                      |
| Backend       | Spring-boot, WebFlux (Mono/Flux)                                                |
| Communication | Redis (Streams + Pub/Sub), CompletableFuture                                    |
| Architecture  | Microservices, Event-driven                                                     |
| Deployment    | Docker-files and docker-compose,Nginx (static frontend + reverse proxy for API) |
---

## 📦 Installation & Run
- Everything is packed into docker-compose.yml file you just need to copy it and pull the needed images.
---

```bash
  
docker-compose pull
docker-compose up
```
The server will run on http://localhost:80