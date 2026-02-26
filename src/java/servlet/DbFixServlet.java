package servlet;

import util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

@WebServlet(name = "DbFixServlet", urlPatterns = { "/admin/dbfix" })
public class DbFixServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {

                response.setContentType("text/html");
                try (Connection conn = DBConnection.getInstance().getConnection();
                                Statement stmt = conn.createStatement()) {

                        // 1. Create users table if not exists
                        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                                        "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                        "first_name VARCHAR(50) NOT NULL, " +
                                        "last_name VARCHAR(50) NOT NULL, " +
                                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                                        "password VARCHAR(255) NOT NULL, " +
                                        "phone VARCHAR(20), " +
                                        "address TEXT, " +
                                        "nic_passport VARCHAR(50) NOT NULL)");

                        // 2. Adjust rooms (Remove 'Booked' and 'Cleaning', migrate accordingly)
                        stmt.execute("UPDATE rooms SET status = 'Occupied' WHERE status = 'Booked'");
                        stmt.execute("UPDATE rooms SET status = 'Available' WHERE status = 'Cleaning'");
                        stmt.execute(
                                        "ALTER TABLE rooms MODIFY COLUMN status ENUM('Available', 'Occupied', 'Maintenance') DEFAULT 'Available'");

                        // 3. Re-create bookings table with users relationship
                        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                        stmt.execute("DROP TABLE IF EXISTS bookings");
                        stmt.execute("CREATE TABLE bookings (" +
                                        "booking_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                        "user_id INT NOT NULL, " +
                                        "room_id INT NOT NULL, " +
                                        "check_in DATE NOT NULL, " +
                                        "check_out DATE NOT NULL, " +
                                        "total_price DECIMAL(10, 2) NOT NULL, " +
                                        "status ENUM('Confirmed', 'Cancelled', 'Checked-in', 'Completed') DEFAULT 'Confirmed', "
                                        +
                                        "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                                        "FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE)");
                        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

                        // 4. Create payments table
                        stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
                                        "payment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                        "booking_id INT NOT NULL, " +
                                        "payment_method ENUM('Cash', 'Card', 'Online') DEFAULT 'Cash', " +
                                        "amount DECIMAL(10, 2) NOT NULL, " +
                                        "tax_amount DECIMAL(10, 2) DEFAULT 0.00, " +
                                        "discount_amount DECIMAL(10, 2) DEFAULT 0.00, " +
                                        "payment_status VARCHAR(50) DEFAULT 'Paid', " +
                                        "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                        "FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE)");

                        response.getWriter().write(
                                        "<h2>Success!</h2><p>Database schema migrated to Users-based system.</p><a href='../admin-dashboard.html'>Go back to Dashboard</a>");

                } catch (Exception e) {
                        response.getWriter().write(
                                        "<h2>Error!</h2><p>Failed to update database: " + e.getMessage() + "</p>");
                        e.printStackTrace();
                }
        }
}
