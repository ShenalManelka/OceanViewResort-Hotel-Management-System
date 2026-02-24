package servlet;

import dao.AdminDAO;
import modal.Admin;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "AdminLoginServlet", urlPatterns = { "/admin/login" })
public class AdminLoginServlet extends HttpServlet {

    private AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Use hardcoded credentials as requested
        if ("admin".equals(username) && "admin@1234".equals(password)) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setFullName("Administrator");

            HttpSession session = request.getSession();
            session.setAttribute("admin", admin);
            response.setStatus(200); // 200 OK
            response.getWriter().write("Login successful");
        } else {
            response.setStatus(401); // 401 Unauthorized
            response.getWriter().write("Invalid username or password");
        }
    }
}
