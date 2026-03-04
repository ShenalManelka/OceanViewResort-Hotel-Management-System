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
1.Login Page 
<img width="975" height="483" alt="image" src="https://github.com/user-attachments/assets/77297971-8e28-45dc-8e40-9b638498163f" />

2.Admin Dashboard
<img width="975" height="456" alt="image" src="https://github.com/user-attachments/assets/b32a4570-db59-4c40-a878-c1fd959808c6" />

3.Room Management
<img width="975" height="458" alt="image" src="https://github.com/user-attachments/assets/6de6c6d4-5071-4b19-a765-4f1041b05735" />

4.Add New Room
<img width="975" height="480" alt="image" src="https://github.com/user-attachments/assets/a8ec3c3f-d4fc-425c-bcb1-2df52fab94db" />

5.Manage Reservations
<img width="975" height="482" alt="image" src="https://github.com/user-attachments/assets/89a68a3d-ae2e-4877-8d90-8a8f78da150e" />

6.View Payment History
<img width="975" height="466" alt="image" src="https://github.com/user-attachments/assets/c552d147-2389-4079-a9a9-929678255910" />

7.View Monthly Reports
<img width="975" height="483" alt="image" src="https://github.com/user-attachments/assets/e902b99f-2976-41b9-8221-b420932fd986" />

8.Staff Management
<img width="975" height="469" alt="image" src="https://github.com/user-attachments/assets/dc84bf17-8091-42c5-a4c1-5febd1955730" />

9.Add New Receptionist
<img width="975" height="476" alt="image" src="https://github.com/user-attachments/assets/a64b212e-7ef3-4d05-b4de-f3aeee9ab28a" />

10.Admin Guidelines
<img width="975" height="479" alt="image" src="https://github.com/user-attachments/assets/31d1378c-0bf3-4cb6-9223-7a0a8a77e4a8" />

11.Receptionist Dashboard
<img width="975" height="466" alt="image" src="https://github.com/user-attachments/assets/046924b8-bd36-41e7-b195-6f7a584b0520" />

12.View Rooms
<img width="975" height="478" alt="image" src="https://github.com/user-attachments/assets/7409422f-64be-4ced-b1b9-bc4851fed37c" />

13.View Reservations
<img width="975" height="484" alt="image" src="https://github.com/user-attachments/assets/1633ff5c-0d08-4d5f-a03c-4d917ecd6060" />

14. Add New Reservation
<img width="975" height="482" alt="image" src="https://github.com/user-attachments/assets/a37e2ba4-d961-47dd-8d3f-d5c0c0ad4124" />

15.Check-in Guest
<img width="975" height="649" alt="image" src="https://github.com/user-attachments/assets/d34d1b4e-1e43-44fa-8d93-f081009304ac" />

16.Generate Bill
<img width="975" height="465" alt="image" src="https://github.com/user-attachments/assets/c988430e-0c4f-48b7-bf60-5086282ba78d" />

17.Print Bill
<img width="975" height="478" alt="image" src="https://github.com/user-attachments/assets/6e45544b-d00d-4714-a94e-381705bdcfcc" />

18.Receptionist Guidelines
<img width="975" height="487" alt="image" src="https://github.com/user-attachments/assets/b931099b-b86f-4e54-b1ba-f9677a29ab63" />

19.Guest List
<img width="1868" height="913" alt="image" src="https://github.com/user-attachments/assets/e5dac0e8-8ade-41c1-b3bd-df3de7333a82" />

