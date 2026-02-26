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

@WebServlet(name = "UserServlet", urlPatterns = { "/admin/users" })
public class UserServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<User> users = userDAO.getAllUsers();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);

            json.append(String.format(
                "{\"userId\": %d, \"firstName\": \"%s\", \"lastName\": \"%s\", \"email\": \"%s\", \"phone\": \"%s\", \"address\": \"%s\", \"nicPassport\": \"%s\"}",
                u.getUserId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhone(),
                u.getAddress(),
                u.getNicPassport()
            ));

            if (i < users.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        response.getWriter().print(json.toString());
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

        User user = new User();
        user.setFirstName(jsonInput.optString("firstName"));
        user.setLastName(jsonInput.optString("lastName"));
        user.setEmail(jsonInput.optString("email"));
        user.setPassword(jsonInput.optString("password"));
        user.setPhone(jsonInput.optString("phone"));
        user.setAddress(jsonInput.optString("address"));
        user.setNicPassport(jsonInput.optString("nicPassport"));

        if (userDAO.addUser(user)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().print("{\"userId\": " + user.getUserId() + "}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}