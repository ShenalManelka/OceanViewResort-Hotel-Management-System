package servlet;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONArray;

@WebServlet(name = "UserServlet", urlPatterns = { "/admin/users" })
public class UserServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getParameter("query");
        String filter = request.getParameter("filter");
        List<User> users;

        try {
            if (query != null && !query.trim().isEmpty()) {
                users = userDAO.searchUsers(query.trim());
            } else if ("reservations".equals(filter)) {
                users = userDAO.getGuestsWithReservations();
            } else {
                users = userDAO.getAllUsers();
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                json.append("{")
                        .append("\"userId\":").append(u.getUserId()).append(",")
                        .append("\"firstName\":\"").append(escape(u.getFirstName())).append("\",")
                        .append("\"lastName\":\"").append(escape(u.getLastName())).append("\",")
                        .append("\"email\":\"").append(escape(u.getEmail())).append("\",")
                        .append("\"phone\":\"").append(escape(u.getPhone())).append("\",")
                        .append("\"address\":\"").append(escape(u.getAddress())).append("\",")
                        .append("\"nicPassport\":\"").append(escape(u.getNicPassport())).append("\"")
                        .append("}");
                if (i < users.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            response.getWriter().print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            try {
                response.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            } catch (Exception ignored) {
            }
        }
    }

    private String escape(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject jsonInput;

        try (BufferedReader reader = request.getReader()) {
            String body = reader.lines().collect(Collectors.joining());
            jsonInput = new JSONObject(body);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String action = jsonInput.optString("action", "add");

        if ("delete".equals(action)) {
            int userId = jsonInput.getInt("userId");
            if (userDAO.deleteUser(userId)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print("{\"message\": \"User deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        User user = new User();
        user.setFirstName(jsonInput.optString("firstName"));
        user.setLastName(jsonInput.optString("lastName"));
        user.setEmail(jsonInput.optString("email"));
        user.setPassword(jsonInput.optString("password"));
        user.setPhone(jsonInput.optString("phone"));
        user.setAddress(jsonInput.optString("address"));
        user.setNicPassport(jsonInput.optString("nicPassport"));

        if ("update".equals(action)) {
            user.setUserId(jsonInput.getInt("userId"));
            if (userDAO.updateUser(user)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().print("{\"message\": \"User updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            // Default: add
            if (userDAO.addUser(user)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().print("{\"userId\": " + user.getUserId() + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}