# FoodBridge — Food Waste Management & Redistribution System

> Supporting **UN SDG 2: Zero Hunger** through technology-driven food waste redistribution.

## Overview

FoodBridge is a full-stack enterprise web application built with Spring Boot that connects food donors (restaurants, bakeries, individuals) with NGOs to redistribute surplus food and reduce waste.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Spring Security 6, Spring Data JPA, Hibernate |
| Database | MySQL 8.0 |
| Real-time | Java TCP ServerSocket (port 9090) |
| Async | @Async + @Scheduled + ExecutorService |
| JDBC | Raw PreparedStatement (SDG statistics) |
| Frontend | Thymeleaf, Bootstrap 5, Font Awesome 6, Custom CSS |

## Features

- **Authentication** — Login/Register with role-based access (ADMIN, DONOR, NGO)
- **Food Donations** — Full CRUD with image upload, status tracking, search & filter
- **NGO Requests** — Request, accept, reject food donations
- **Real-time Alerts** — TCP socket server broadcasts new donation alerts to NGOs
- **Expiry Checker** — Multithreaded background service auto-marks expired donations
- **Pickup Tracking** — Pending → Accepted → In Transit → Picked Up
- **SDG Dashboard** — Impact metrics using raw JDBC (meals saved, NGOs served)
- **Admin Panel** — User management, system statistics
- **Notifications** — In-app notification system with read/unread states

## Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.8+



### 1. Create Database
```sql
CREATE DATABASE food_waste_db;
```

### 2. Configure Database (already configured in application.properties)
- **URL**: `jdbc:mysql://localhost:3306/food_waste_db`
- **Username**: `root`
- **Password**: `root`

### 3. Build & Run
```bash
cd foodmanagement
mvn clean install -DskipTests
mvn spring-boot:run
```

### 4. Access Application
Open **http://localhost:8080** in your browser.

## Default Users

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Donor | donor | donor123 |
| Donor | donor2 | donor123 |
| NGO | ngouser | ngo123 |
| NGO | ngo2 | ngo123 |

## Socket Server

The real-time alert server runs on **port 9090**. When a donor creates a new donation, all connected NGO clients receive an instant JSON alert.

## Project Structure

```
src/main/java/com/project/foodwaste/
├── FoodWasteApplication.java     # Main application + data seeder
├── config/                       # Security, Async, Web config
├── controller/                   # 8 controllers (MVC + REST API)
├── entity/                       # JPA entities + enums
├── repository/                   # Spring Data JPA repositories
├── service/                      # Business logic layer
├── socket/                       # TCP ServerSocket implementation
└── util/                         # File upload + date utilities

src/main/resources/
├── application.properties
├── templates/                    # Thymeleaf HTML templates
└── static/                       # CSS, JS assets

```
## Social Impact

FoodBridge aims to reduce food wastage while improving access to surplus food for communities in need.

### Key Impact Areas

- Reduces unnecessary disposal of surplus food
- Connects food donors with NGOs through a centralized platform
- Enables faster communication through real-time alerts
- Tracks the movement of donations from availability to pickup
- Provides measurable impact statistics through the SDG dashboard

### SDG Alignment

The project supports **UN Sustainable Development Goal 2 — Zero Hunger** by using technology to improve the redistribution of surplus food to communities that need it.

## System Workflow

FoodBridge follows a donor-to-NGO redistribution workflow:

1. A donor registers and logs into the platform.
2. The donor creates a food donation with relevant details.
3. The donation is stored and made available to registered NGOs.
4. NGOs receive alerts about new available donations.




## License

This project is for academic purposes.
