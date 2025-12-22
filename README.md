# eBuy – Distributed High-Availability E-Commerce Platform

## Overview

**eBuy** is a cloud-native, high-availability e-commerce platform built using **microservices architecture**, designed to demonstrate **scalability, reliability, low latency, and fault tolerance**—key requirements for large-scale distributed systems.

This project focuses on **real-world system design principles** commonly used at FAANG, Uber, Amazon, and NVIDIA, including domain-driven design, polyglot persistence, event-driven communication, and horizontal scalability.

---

## Key Highlights

- Distributed microservices architecture with independent deployment
- Designed for high availability and fault isolation
- Optimized for low-latency global access
- Event-driven and horizontally scalable system design
- Production-grade technology stack

---

## Architecture Overview

- **Architecture Style:** Microservices
- **Communication:** REST (sync), Kafka (async – in progress)
- **Containerization:** Docker
- **Scalability:** Horizontal scaling with load balancing
- **Resilience:** Service isolation, caching, and async processing

---

## Technology Stack

### Frontend
- **Framework:** React.js
- **CDN:** Cloudflare
- **Authentication:** Keycloak (in progress)

### Backend
- **Language & Framework:** Java (Spring Boot)
- **Architecture:** Domain-driven microservices
- **Containerization:** Docker

### Data & Search
- **PostgreSQL:** Strongly consistent transactional data
- **MongoDB:** Flexible document-based storage
- **OpenSearch:** Low-latency, scalable product search

---

## Implemented Microservices

All services are **Dockerized** and designed with clear ownership boundaries:

- **User Service**
- **Product Service**
- **Product Catalog (Read) Service**
- **Product Search Service**
- **Cart Service**
- **Order Service**
- **Review Service**
- **Payment Service**

Each service can be deployed, scaled, and evolved independently.

---

## Functional Capabilities

- User and product management
- Product catalog and search
- Cart and order processing
- Reviews and payments
- Designed to support high read/write traffic scenarios

---

## In-Progress / Planned Enhancements

The following components are being integrated to complete a production-grade HA setup:

- **Centralized Authentication & Authorization** (Keycloak, RBAC)
- **API Gateway** (routing, rate limiting, cross-cutting concerns)
- **Load Balancer**
- **Distributed Caching** (Redis)
- **Kafka Integration** for event-driven workflows
- **Asynchronous order and payment processing**

---

## Engineering Focus Areas

- Distributed Systems Design
- High Availability & Fault Tolerance
- Low-Latency System Architecture
- Horizontal Scalability
- Event-Driven Architecture
- Polyglot Persistence
- Cloud-Native Best Practices

---

## System Design Talking Points (Interview Ready)

This project supports deep discussion around:

- Microservice decomposition and data ownership
- Handling traffic spikes (sales, promotions)
- Caching strategies and consistency trade-offs
- Sync vs async communication patterns
- Failure handling, retries, and idempotency
- Scaling search, catalog, and order services independently

---

## Project Status

🚧 **Actively in development**

This project continues to evolve with additional scalability, observability, and reliability features.

---

## Author

**Balaji Katta Venkataratnam**  
Senior Software Engineer / Technical Architect
Specializing in Distributed Systems, Cloud Architecture, and Backend Engineering
