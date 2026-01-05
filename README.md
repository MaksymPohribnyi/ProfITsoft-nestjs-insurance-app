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
