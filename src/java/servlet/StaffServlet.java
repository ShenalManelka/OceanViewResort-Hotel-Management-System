package servlet;

import dao.StaffDAO;
import model.Staff;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "StaffServlet", urlPatterns = { "/admin/staff" })
public class StaffServlet extends HttpServlet {

    private StaffDAO staffDAO = new StaffDAO();

    // ✅ GET — Return all staff as JSON
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        List<Staff> staffList = staffDAO.getAllStaff();
        JSONArray array = new JSONArray();
        for (Staff s : staffList) {
            JSONObject obj = new JSONObject();
            obj.put("id", s.getId());
            obj.put("fullName", s.getFullName());
            obj.put("email", s.getEmail());
            obj.put("role", s.getRole());
            array.put(obj);
        }
        response.getWriter().print(array.toString());
    }

    // ✅ POST — Add or Delete staff
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        try {
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JSONObject json = new JSONObject(body);
            String action = json.optString("action", "");

            if ("add".equalsIgnoreCase(action)) {
                String fullName = json.getString("fullName");
                String email = json.getString("email");
                String password = json.getString("password");

                if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print("{\"error\": \"All fields are required\"}");
                    return;
                }

                if (staffDAO.addStaff(fullName, email, password)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().print("{\"message\": \"Receptionist added successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().print("{\"error\": \"Failed to add receptionist. Email may already exist.\"}");
                }

            } else if ("delete".equalsIgnoreCase(action)) {
                int id = json.getInt("id");
                if (staffDAO.deleteStaff(id)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().print("{\"message\": \"Staff member removed\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter()
                            .print("{\"error\": \"Cannot delete this account (Admin accounts are protected)\"}");
                }

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\": \"Unknown action\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}
