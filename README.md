# Cinema Promotion - User Management API

A Spring Boot REST API for managing users with PostgreSQL database.

## Prerequisites

- Java 22
- Maven
- PostgreSQL (running on localhost:5432)
- Redis (running on localhost:6379)

## Database Setup

1. Create a PostgreSQL database named `cinema_promotion`:
```sql
CREATE DATABASE cinema_promotion;
```

2. The application will automatically create the `users` table when it starts.

## Configuration

The application is configured to connect to PostgreSQL and Redis with the following settings:

### PostgreSQL:
- Host: localhost
- Port: 5432
- Database: cinema_promotion
- Username: postgres
- Password: postgres

### Redis:
- Host: localhost
- Port: 6379
- Database: 0

You can modify these settings in `src/main/resources/application.properties`.

## Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

Once the application is running, you can access the Swagger UI at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

The Swagger UI provides an interactive interface to:
- View all available API endpoints
- Test API calls directly from the browser
- See request/response schemas
- View example requests and responses
- Understand error codes and their meanings

## API Endpoints

### Create User
- **POST** `/api/users`
- **Body:**
```json
{
    "name": "John Doe",
    "address": "123 Main Street, City, Country"
}
```

### Update User
- **PUT** `/api/users/{id}`
- **Body:**
```json
{
    "name": "John Doe Updated",
    "address": "456 New Street, City, Country"
}
```

### Get User by ID
- **GET** `/api/users/{id}`

### Get All Users
- **GET** `/api/users`

### Delete User
- **DELETE** `/api/users/{id}`

## Validation Rules

- **Name**: Required, 2-100 characters, must be unique
- **Address**: Required, maximum 255 characters

## Concurrency Handling

The API includes several mechanisms to handle concurrent user registration:

1. **Distributed locking with Redis**: Uses Redis-based distributed locks to prevent race conditions across multiple application instances
2. **Database-level constraints**: Unique constraint on the name field
3. **Transaction management**: All operations are wrapped in transactions
4. **Custom exception handling**: Proper error responses for duplicate users and lock failures

When multiple requests try to create users with the same name simultaneously:
- The first request will succeed
- Subsequent requests will receive a 409 Conflict response
- If Redis is unavailable, requests will receive a 503 Service Unavailable response

## Example Usage

### Create a new user:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "address": "123 Main Street, City, Country"
  }'
```

### Update a user:
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "address": "456 New Street, City, Country"
  }'
```

### Get all users:
```bash
curl -X GET http://localhost:8080/api/users
```

### Get user by ID:
```bash
curl -X GET http://localhost:8080/api/users/1
```

### Delete user:
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## Project Structure

```
src/main/java/com/mservice/cinema/promotion/cinema_promotion/
├── CinemaPromotionApplication.java
├── controller/
│   └── UserController.java
├── model/
│   └── User.java
├── repository/
│   └── UserRepository.java
├── service/
│   └── UserService.java
└── exception/
    └── GlobalExceptionHandler.java
```

## Technologies Used

- Spring Boot 3.5.4
- Spring Data JPA
- PostgreSQL
- Redis
- Swagger/OpenAPI 3
- Maven
- Java 22 