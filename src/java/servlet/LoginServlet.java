package servlet;

import model.Staff;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = { "/admin/login" })
public class LoginServlet extends HttpServlet {

    private dao.StaffDAO staffDAO = new dao.StaffDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect direct GET requests to the actual login page
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("Login attempt for user: " + username);
        try {
            Staff staff = staffDAO.authenticate(username, password);

            if (staff != null) {
                System.out.println("Login successful for: " + staff.getEmail() + " with role: " + staff.getRole());
                HttpSession session = request.getSession();
                session.setAttribute("user", staff);
                session.setAttribute("role", staff.getRole());

                response.setStatus(200);
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"message\": \"Login successful\", \"role\": \"" + staff.getRole() + "\"}");
            } else {
                response.setStatus(401);
                response.getWriter().write("Invalid email or password");
            }
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
