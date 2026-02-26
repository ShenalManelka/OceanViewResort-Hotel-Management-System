package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBConnection;

@WebServlet(name = "TestConnectionServlet", urlPatterns = {"/TestConnectionServlet"})
public class TestConnectionServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Connection Test Results</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Database Connection Status</h1>");
            
            try {
                Connection conn = DBConnection.getInstance().getConnection();
                if (conn != null && !conn.isClosed()) {
                    out.println("<h2 style='color: green;'>Connection Successful!</h2>");
                    out.println("<p>Database: " + conn.getMetaData().getDatabaseProductName() + "</p>");
                    out.println("<p>Version: " + conn.getMetaData().getDatabaseProductVersion() + "</p>");
                    conn.close();
                } else {
                    out.println("<h2 style='color: red;'>Connection failed: Connection is null or closed.</h2>");
                }
            } catch (Exception e) {
                out.println("<h2 style='color: red;'>Connection Error!</h2>");
                out.println("<p>Message: " + e.getMessage() + "</p>");
                out.println("<pre>");
                e.printStackTrace(out);
                out.println("</pre>");
            }
            
            out.println("<br><a href='testConnection.jsp'>Back to Test Page</a>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
