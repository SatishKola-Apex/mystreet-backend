
# MyStreet Sneaker Store - Backend API
 
A robust Spring Boot backend service powering the MyStreet E-Commerce Sneaker Store platform. Provides secure RESTful APIs for user authentication, product catalog management, shopping cart operations, order processing, and administrative controls.
 
---
 
## 🚀 Tech Stack
* **Java 17 / 21**
* **Spring Boot 4.x** (Spring Web, Spring Data JPA, Spring Security)
* **Database:** H2 Database (In-Memory for development) / PostgreSQL compatible
* **Documentation:** SpringDoc OpenAPI / Swagger UI
* **Build Tool:** Maven
* **Testing:** JUnit 5, MockMvc
 
---
 
## 📌 Key Features
* **Authentication & Authorization:** Secure JWT/Session-based authentication with role-based access control (Admin & Customer).
* **Automated Data Seeding:** Initializer that automatically seeds administrative credentials and a catalog of sneaker products from a JSON resource file.
* **RESTful Endpoints:** Fully tested endpoints for products, authentication, orders, and inventory.
* **API Documentation:** Interactive Swagger UI integration.
 
---
 
## 🛠️ Getting Started & Setup
 
### Prerequisites
* Java Development Kit (JDK 17+)
* Maven installed
 
### Running the Application
1. Clone the repository and navigate to the backend directory.
2. Run the application using Maven:
   ```bash
   mvn spring-boot:run
 
* The server will start on port 8080.
📄 API Documentation
Access the interactive Swagger UI documentation locally at:
👉 http://localhost:8080/swagger-ui/index.html
 
---
