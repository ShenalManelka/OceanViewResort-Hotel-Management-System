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

            String sql = "ALTER TABLE rooms MODIFY COLUMN status ENUM('Available', 'Occupied', 'Maintenance', 'Booked', 'Cleaning') DEFAULT 'Available'";
            stmt.executeUpdate(sql);

            response.getWriter().write(
                    "<h2>Success!</h2><p>Database schema updated successfully. You can now use the 'Booked' and 'Cleaning' statuses.</p><a href='../admin-dashboard.html'>Go back to Dashboard</a>");

        } catch (Exception e) {
            response.getWriter().write("<h2>Error!</h2><p>Failed to update database: " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }
}
