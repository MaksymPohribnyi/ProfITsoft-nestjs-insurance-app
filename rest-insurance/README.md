# 🎯 About The Project

### This project demonstrates a full-cycle application with CRUD operations, advanced filtering, CSV reporting, and bulk data import, built using Spring Boot.

## **Key Features:**
### 📄 Insurance Policies
* **Lifecycle Management**: Create and update insurance policies linked to specific clients.
* **Validation**: policy numbers remain unique.

### 🔍 Search & Reporting
* **Advanced Filtering**: Search by Client ID, Policy Type, or Policy Number.
* **Pagination**: Efficient handling of large datasets.
* **CSV Exports**: Generate reports based on specific search criteria.

### 📤 Bulk Data Import
* **JSON Upload**: Import multiple policies via a single JSON file.
* **Atomic Processing**: The system processes records individually, tracking success and failure counts.
* **Robust Validation**: Invalid records are skipped and counted without stopping the entire process.

## 🛠 Built With

* **Java 21**
* **Spring boot 3.5.8**
* **Docker** (Docker Compose, Testcontainers)
* **PostgreSQL 15**
* **Liquibase** (SQL Migration)
* **JUnit 5, MockMvc**
* **Lombok** 
* **Gradle**

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### 1.  Clone the repository
 ```sh
git clone https://github.com/MaksymPohribnyi/ProfITsoft-spring-rest.git
cd ProfITsoft-spring-rest
```
### 2. Build the project

**Option 1: Docker (Recommended)**

The easiest way to run the application with the database.

```bash
# Build and start services
docker-compose up --build
```
**The API will be available at: http://localhost:8080**

**Option 2: Local Run (Gradle)**
```sh
# Linux/macOS
./gradlew bootRun

# Windows
.\gradlew.bat bootRun
```
**Ensure you have a PostgreSQL database running on port 5433 (or update application.yml)**

## 📡 API Endpoints

### Clients (/api/client)
| Method | Endpoint | Description |
|--------|----------|-------------|
| **GET**|          |Get all clients|
| **POST**|         |Create new client|
| **PUT**|  /{id}   |Update a client|
| **DELETE**| /{id} |Remove a client|


### Insurance Policies (/api/insurance_policy)
| Method | Endpoint | Description |
|--------|----------|-------------|
| **GET**|  /{id}   |Get policy by ID|
| **POST**|         |Create new policy|
| **PUT**|  /{id}   |Update an existing policy|
| **DELETE**| /{id} |Remove a policy|
| **POST**| /_list  |Search: Filter and paginate policies|
| **POST**| /_report |Report: Download filtered CSV report|
| **POST**| /upload |Upload policies via JSON file|


> **A sample JSON file for testing the `/upload` endpoint is included in the project resources:**
`src/main/resources/upload/import.json`
