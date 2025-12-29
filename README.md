## eBuy – Distributed High-Availability E-Commerce Platform

Appliction with hardcoded data(Deployed in CDN):
https://ebuystore.pages.dev/

### eBuy Overview 

**eBuy** is a cloud-native, high-availability e-commerce platform built using **microservices architecture**, designed to demonstrate **scalability, reliability, low latency, and fault tolerance**—key requirements for large-scale distributed systems.

This project focuses on **real-world system design principles**, domain-driven design, polyglot persistence, event-driven communication, and horizontal scalability.

![Architecture Diagram](eBuy-Architecture.png)

---

### Key Highlights

- Distributed microservices architecture with independent deployment
- Designed for high availability and fault isolation
- Optimized for low-latency global access
- Event-driven and horizontally scalable system design
- Production-grade technology stack

---

### Architecture Overview

- **Architecture Style:** Microservices
- **Communication:** REST (sync), Kafka (async – in progress)
- **Containerization:** Docker
- **Scalability:** Horizontal scaling with load balancing
- **Resilience:** Service isolation, caching, and async processing

---

### Technology Stack

#### Frontend
- **Framework:** React.js
- **CDN:** Cloudflare

#### Backend
- **Language & Framework:** Java (Spring Boot)
- **Architecture:** Domain-driven microservices
- **Containerization:** Docker

#### Data & Search
- **PostgreSQL:** Strongly consistent transactional data
- **MongoDB:** Flexible document-based storage
- **OpenSearch:** Low-latency, scalable product search

---

### Implemented Microservices

All services are **Dockerized** and designed with clear ownership boundaries:

- **Product Service**
- **Product Catalog (Read) Service**
- **Product Search Service**
- **Cart Service**
- **Order Service**

Each service can be deployed, scaled, and evolved independently.

---

### Functional Capabilities

- Product management
- Product catalog and search
- Cart and order processing
- Designed to support high read/write traffic scenarios

---

### Future Enhancements

The following components are being integrated to complete a production-grade HA setup:

- **Centralized Authentication & Authorization** (Keycloak, RBAC)
- **API Gateway** (routing, rate limiting, cross-cutting concerns)
- **Load Balancer**
- **Distributed Caching** (Redis)
- **Kafka Integration** for event-driven workflows
- **Asynchronous order and payment processing**

---

### Engineering Focus Areas

- Distributed Systems Design
- High Availability & Fault Tolerance
- Low-Latency System Architecture
- Horizontal Scalability
- Event-Driven Architecture
- Polyglot Persistence
- Cloud-Native Best Practices

This project continues to evolve with additional scalability, observability, and reliability features.
