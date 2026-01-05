# 🎯 About The Project
## 💳 Insurance Payments Service
A sample microservice built with **NestJS** for managing insurance policy payments. This service handles payment processing, historical data retrieval, and aggregation logic, serving as a key component of the distributed Insurance Policy System.

### 🔗 Related Services
This project consists of two microservices:
- `nestjs-payments` — Insurance Payments Service (NestJS)
- `rest-insurance` — Insurance Policy Service (Java Spring Boot)

> **Note:** The Insurance Policy Service implementation resides in a separate repository and **cloned into this repository as a folder** for convenient local development and deploy.
>
> 👉 **[Insurance Policy Service (Original repository)](https://github.com/MaksymPohribnyi/ProfITsoft-spring-rest)**

---

## 🛠️ Built With

* **NestJS 11.0.1** 
* **Node.js v18+**
* **MongoDB (via Mongoose)** 
* **Validation:** `class-validator`, `class-transformer`
* **HTTP Client:** Axios (for inter-service communication)
* **Jest, Supertest**
* **Testcontainers**

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### 1.  Clone the repository
```bash
git clone https://github.com/MaksymPohribnyi/ProfITsoft-nestjs-insurance-app.git
cd nestjs-payments
npm install
```
### 2. Environment configuration
Create a .env file in the root directory. You can use .env.origin as a reference

### 3. Running the application using Docker Compose
```bash
# Build and start services
docker-compose up -d --build
```

## 📡 API Endpoints

The service exposes the following REST endpoints:

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST**|/payments|Create a new payment (validates Policy ID existence)|
| **GET** |/payments|List payments with pagination and filtering by Policy ID|
| **POST**|/payments/_counts|Get payment counts for a list of Policy IDs with aggregation|

### Healthcheck
| Method | Endpoint | Description |
|--------|----------|-------------|
| **GET** |/health |Check service status|
| **GET** |/ping |Check service status|

## 🌐 API Reference
### Base URL: http://localhost:7777

### 1. Create Payment

**Example Request**

```bash
# Linux/macOS
curl -X POST http://localhost:7777/payments \
  -H "Content-Type: application/json" \
  -d '{
    "policyId": "22222222-0000-0000-0000-000000000001",
    "paymentAmount": 1500,
    "paymentMethod": "credit_card",
    "status": "completed",
    "description": "Annual premium payment"
  }'
```

```bash
# Windows
curl -X POST http://localhost:7777/payments ^
  -H "Content-Type: application/json" ^
  -d "{\"policyId\":\"22222222-0000-0000-0000-000000000001\",\"paymentAmount\":1500,\"paymentMethod\":\"credit_card\",\"status\":\"completed\",\"description\":\"Annual premium payment\"}"
```

**Example Response**

```json
{
  "id": "695bf19ac838542f467581a8"
}
```

### 2. List Payments

**Example Request**

```bash
# Linux/macOS/Windows
curl "http://localhost:7777/payments?policyId=22222222-0000-0000-0000-000000000001&size=10&from=0"
```

**Example Response**

```json
[
  {
    "_id": "695bf25cc838542f467581ac",
    "policyId": "22222222-0000-0000-0000-000000000001",
    "paymentAmount": 1500,
    "paymentDate": "2026-01-05T17:18:20.661Z",
    "paymentMethod": "credit_card",
    "status": "completed",
    "description": "Annual premium payment",
    "createdAt": "2026-01-05T17:18:20.662Z",
    "updatedAt": "2026-01-05T17:18:20.662Z"
  },
  {
    "_id": "695bf250c838542f467581aa",
    "policyId": "22222222-0000-0000-0000-000000000001",
    "paymentAmount": 1500,
    "paymentDate": "2026-01-05T17:18:08.529Z",
    "paymentMethod": "credit_card",
    "status": "completed",
    "description": "Annual premium payment",
    "createdAt": "2026-01-05T17:18:08.530Z",
    "updatedAt": "2026-01-05T17:18:08.530Z"
  },
  {
    "_id": "695bf19ac838542f467581a8",
    "policyId": "22222222-0000-0000-0000-000000000001",
    "paymentAmount": 1500,
    "paymentDate": "2026-01-05T17:15:06.013Z",
    "paymentMethod": "credit_card",
    "status": "completed",
    "description": "Annual premium payment",
    "createdAt": "2026-01-05T17:15:06.018Z",
    "updatedAt": "2026-01-05T17:15:06.018Z"
  }
]
```

### 3. Get Payment Counts

**Example Request**

```bash
# Linux/macOS
curl -X POST http://localhost:7777/payments/_counts \
  -H "Content-Type: application/json" \
  -d '{"policyIds": ["POLICY_ID_1", "POLICY_ID_2"]}'
```

```bash
# Windows
```bash
curl -X POST http://localhost:7777/payments/_counts ^
-H "Content-Type: application/json" ^
-d "{\"policyIds\":[\"22222222-0000-0000-0000-000000000001\",\"22222222-0000-0000-0000-000000000005\"]}"
```

**Example Response**

```json
{
  "22222222-0000-0000-0000-000000000001": 3,
  "22222222-0000-0000-0000-000000000005": 4
}
```
