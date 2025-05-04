# Flash Sale Application

A Spring Boot application that simulates a high-concurrency flash sale system using Redis distributed locking to handle up to 1,000,000 simultaneous requests without database overload.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Testing with the Simulator](#testing-with-the-simulator)
- [Performance Considerations](#performance-considerations)

## Overview

This application demonstrates how to build a robust flash sale system capable of handling extreme traffic spikes while maintaining data consistency. It uses Redis as a high-performance buffer layer in front of the database to prevent overloading the database during peak traffic.

The system is designed to sell a limited inventory of tickets (10 by default) while managing a potential customer base of up to 1,000,000 concurrent users.

## Features

- **High Concurrency Support**: Handles millions of simultaneous requests
- **Distributed Locking**: Prevents race conditions and data inconsistency
- **Redis Buffering**: Acts as a traffic filter to prevent database overload
- **Inventory Management**: Tracks available tickets in real-time
- **User Management**: Creates and tracks users
- **Purchase Processing**: Safely processes ticket purchases
- **Built-in Load Simulator**: Tests the system under high concurrent load

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.3**
- **Spring Data JPA**: For database operations
- **Redisson**: For Redis distributed locks and atomic operations
- **Redis**: For distributed locking and caching
- **Gradle**: For build management

## System Architecture

The application uses a multi-layered architecture to handle high-traffic flash sales:

1. **REST API Layer**: Handles incoming HTTP requests
2. **Redis Buffer Layer**: 
   - Acts as a traffic filter to prevent database overload
   - Uses distributed locks to ensure atomic operations
   - Caches ticket inventory for high-performance reads
3. **Service Layer**: Contains business logic and transaction management
4. **Persistence Layer**: H2 database for data storage

### Request Flow
1. User sends a purchase request
2. Redis distributed lock is acquired (or request is rejected if unavailable)
3. Available ticket count is verified in Redis
4. If valid, the purchase is persisted to the database
5. Redis counters are updated
6. Lock is released

## Project Structure

```
flash-sale/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── flashsale/
│   │   │               ├── FlashSaleApplication.java    # Main application entry point
│   │   │               ├── config/
│   │   │               │   └── RedisConfig.java         # Redis and Redisson configuration
│   │   │               ├── controller/
│   │   │               │   ├── PurchaseController.java  # Endpoint for ticket purchases
│   │   │               │   ├── TicketController.java    # Endpoints for ticket management
│   │   │               │   └── UserController.java      # Endpoints for user management
│   │   │               ├── exception/
│   │   │               │   └── ResourceNotFoundException.java  # For 404-type errors
│   │   │               ├── model/
│   │   │               │   ├── Purchase.java            # Purchase entity
│   │   │               │   ├── Ticket.java              # Ticket entity
│   │   │               │   ├── User.java                # User entity
│   │   │               │   └── dto/
│   │   │               │       ├── ApiResponse.java     # Standardized API response wrapper
│   │   │               │       └── PurchaseRequest.java # Purchase request payload
│   │   │               ├── repository/
│   │   │               │   ├── PurchaseRepository.java  # Purchase data access
│   │   │               │   ├── TicketRepository.java    # Ticket data access
│   │   │               │   └── UserRepository.java      # User data access
│   │   │               ├── service/
│   │   │               │   ├── PurchaseService.java     # Purchase service interface
│   │   │               │   ├── TicketService.java       # Ticket service interface
│   │   │               │   ├── UserService.java         # User service interface
│   │   │               │   └── impl/
│   │   │               │       ├── PurchaseServiceImpl.java  # Purchase logic with Redis locks
│   │   │               │       ├── TicketServiceImpl.java    # Ticket inventory management
│   │   │               │       └── UserServiceImpl.java      # User management
│   │   │               └── util/
│   │   │                   └── FlashSaleSimulator.java  # Load testing simulator
│   │   └── resources/
│   │       └── application.yml                          # Application configuration
├── build.gradle                                         # Gradle build configuration
├── gradlew                                              # Gradle wrapper script (Unix)
├── gradlew.bat                                          # Gradle wrapper script (Windows)
├── run-simulator.sh                                     # Script to run the load simulator
└── README.md                                            # Project documentation
```

## Prerequisites

- Java 17 or higher
- Redis server (running locally or accessible)
- Gradle build tool

## Getting Started

1. Clone the repository:
```
git clone https://github.com/misranrifat/flash-sale.git
cd flash-sale
```

2. Ensure Redis is running:
```
redis-server
```

3. Build and run the application:
```
./gradlew bootRun
```

## API Endpoints

### User Management
- **Create User**: `POST /api/users?userId={userId}&username={username}&email={email}`
- **Get User**: `GET /api/users/{userId}`
- **Check User Exists**: `GET /api/users/{userId}/exists`

### Ticket Management
- **Get All Tickets**: `GET /api/tickets`
- **Get Available Tickets**: `GET /api/tickets/available`
- **Check Ticket Status**: `GET /api/tickets/status`

### Purchase Management
- **Purchase Tickets**:
  ```
  POST /api/purchases
  Content-Type: application/json
  
  {
    "userId": "your-user-id",
    "quantity": 1
  }
  ```
- **Get User Purchases**: `GET /api/purchases/user/{userId}`
- **Count User Purchases**: `GET /api/purchases/user/{userId}/count`

## Testing with the Simulator

The project includes a built-in simulator for stress testing the system. The simulator creates a test user and sends thousands of concurrent purchase requests.

To run the simulator:

```bash
chmod +x run-simulator.sh
./run-simulator.sh
```

The simulator:
1. Creates a random test user
2. Checks initial ticket status
3. Launches multiple threads (50 by default) to simulate concurrent users
4. Sends thousands of purchase requests (1,000 by default)
5. Reports success/failure statistics

## Performance Considerations

The system uses several techniques to maintain high performance:

1. **Redis as a Buffer**: Only successful Redis operations reach the database
2. **Distributed Locking**: Prevents overselling and ensures data consistency
3. **Atomic Counters**: Fast inventory checking without database queries

By implementing these techniques, the system can handle a high volume of concurrent requests while maintaining data integrity and preventing database overload. 