# Ocean View Resort Management System

The Ocean View Resort Management System is a comprehensive internal management suite designed to streamline resort operations, including guest registration, room management, booking, and payments. It provides a secure and efficient platform for resort staff to manage daily tasks and view real-time statistics.

## Key Features

### Admin
- **Authentication**: Secure login and logout.
- **Guest Management**: Register and view all guest details.
- **Room Management**: Add, edit, delete, and search rooms.
- **Booking Management**: Create, view, update status, and cancel bookings.
- **Payment Management**: Record and view payment history.
- **Dashboard & Reports**: View real-time stats, monthly revenue reports, and cancellation rates.

### Receptionist
- **Authentication**: Secure login and logout.
- **Guest Management**: Register and view all guest details.
- **Room Management**: View and search for rooms.
- **Booking Management**: Create, view, and update booking status.
- **Payment Management**: Record and view payment history.
- **Dashboard**: View basic dashboard stats.

## Technology Stack
- **Backend**: Java, Jakarta EE Servlets
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla), Font Awesome
- **Database**: MySQL
- **Build Tool**: Apache Ant (NetBeans project)
- **Libraries**: GSON, JSON-Java, MySQL Connector/J

## Database Schema
The database `oceanviewresortdb` consists of the following tables:
- `staff`: Stores staff credentials and roles (ADMIN, RECEPTIONIST).
- `rooms`: Contains room details (number, type, price, status).
- `users`: Stores guest information.
- `bookings`: Tracks guest bookings and room assignments.
- `payments`: Records payment transactions linked to bookings.

## Project Structure
- `src/java/`: Backend source code (Models, DAOs, Servlets, Utils).
- `web/`: Frontend assets (HTML, CSS, JS) and WEB-INF configuration.
- `test/`: Unit tests for DAOs.
- `schema.sql`: Database initialization script.
- `nbproject/`: NetBeans project configuration files.
- `build.xml`: Ant build script.

## Setup and Running
1.  **Database Setup**: Execute `schema.sql` in your MySQL server to create the database and seed initial data.
2.  **Configuration**: Update `src/java/util/DBConnection.java` with your database credentials if necessary.
3.  **Build**: Use Apache Ant to build the project.
4.  **Deployment**: Deploy the generated `.war` file to a servlet container like Apache Tomcat.

## Documentation
Detailed UML diagrams (Class, Use Case, Sequence Diagrams) are available in [uml_diagrams.md.resolved](uml_diagrams.md.resolved).
