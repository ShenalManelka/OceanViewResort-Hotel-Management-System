# 🌊 OceanView Resort - Hotel Management System

A modern, high-performance web application designed for seamless hotel operations, guest management, and real-time business analytics. Built with a focus on data integrity, security, and a premium user experience.

---

## 🚀 Key Features

### 🛡️ Secure Authentication
- **Role-Based Access Control (RBAC)**: Specialized dashboards for **Administrators** and **Receptionists**.
- **Secure Session Management**: Protected endpoints to ensure data privacy.

### 🏨 Comprehensive Management
- **Room Management (Admin Only)**: Full CRUD (Create, Read, Update, Delete) for the room inventory. Includes safety guardrails to prevent deletion of occupied rooms.
- **Guest Directory**: High-speed guest search and profile management with real-time updates.
- **Staff Control**: Manage receptionist accounts and permissions effortlessly.

### 📅 Booking & Operations
- **Lifecycle Tracking**: Seamless flow from **Reservation** → **Occupancy** → **Checkout**.
- **Atomic Transactions**: Guarantees that room availability always stays synchronized with bookings.
- **Automated Billing**: Integrated payment recording and instant bill generation.

### 📊 Business Intelligence
- **Live Analytics**: Real-time KPI cards for revenue, occupancy rates, and booking trends.
- **Visual Reports**: Interactive charts (Revenue & Booking Status) powered by parallelized backend data fetching.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java Servlets, DAO (Data Access Object) Pattern |
| **Frontend** | Vanilla JavaScript (ES6+), Modern HTML5, CSS3 (Glassmorphism) |
| **Database** | MySQL |
| **Communication** | Async AJAX / JSON API |
| **Icons & Fonts** | FontAwesome, Google Fonts (Inter/Outfit) |

---

## ⚙️ Setup & Installation

### Prerequisites
- **Java JDK**: 11 or higher
- **Server**: Apache Tomcat 9.0+ or GlassFish
- **Database**: MySQL 8.0+
- **IDE**: NetBeans, IntelliJ IDEA, or Eclipse

### Database Setup
1. Import the provided [schema.sql](schema.sql) into your MySQL instance.
2. Update the database credentials in `DBConnection.java` (found in `src/java/util/`).

### Deployment
1. Clone the repository: `git clone https://github.com/your-username/OceanViewResort.git`
2. Build the project using your IDE or Maven/Ant.
3. Deploy the generated `.war` file to your servlet container.
4. Access the application at `http://localhost:8080/OceanViewResort`.

---

## 🏗️ Architecture Summary
The project follows a rigorous **MVC (Model-View-Controller)** pattern:
- **Model**: Java classes and DAOs for database interaction.
- **View**: Responsive HTML/CSS with a premium "Glassmorphism" design system.
- **Controller**: Java Servlets handling business logic and API routing.

---

## 📸 Screenshots & Diagrams
Detailed technical documentation, including sequence diagrams and use-case analysis, can be found in the [docs/](docs/) directory.

*Designed with ❤️ for OceanView Resort.*
