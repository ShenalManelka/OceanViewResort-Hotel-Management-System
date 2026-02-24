package servlet;

import modal.Staff;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = { "/admin/login" })
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Staff staff = null;

        // Hardcoded credentials for ADMIN and RECEPTIONIST as requested
        if ("admin".equals(username) && "admin@1234".equals(password)) {
            staff = new Staff(1, "admin", "Primary Admin", "admin@oceanview.com", "ADMIN");
        } else if ("reception".equals(username) && "staff@1234".equals(password)) {
            staff = new Staff(2, "reception", "Front Desk Staff", "reception@oceanview.com", "RECEPTIONIST");
        }

        if (staff != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", staff); // Unified session attribute
            session.setAttribute("role", staff.getRole());

            response.setStatus(200);
            response.getWriter().write("Login successful");
        } else {
            response.setStatus(401);
            response.getWriter().write("Invalid username or password");
        }
    }
}
