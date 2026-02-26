package servlet;

import dao.RoomDAO;
import modal.Room;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modal.Staff;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "RoomServlet", urlPatterns = { "/admin/rooms" })
public class RoomServlet extends HttpServlet {

    private RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String roomNumber = request.getParameter("roomNumber");
        String type = request.getParameter("type");
        String status = request.getParameter("status");

        List<Room> rooms = roomDAO.searchRooms(roomNumber, type, status);
        response.setContentType("application/json");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            json.append(String.format(
                    "{\"roomId\": %d, \"roomNumber\": \"%s\", \"type\": \"%s\", \"price\": %.2f, \"status\": \"%s\", \"description\": \"%s\"}",
                    r.getRoomId(), r.getRoomNumber(), r.getType(), r.getPrice(), r.getStatus(), r.getDescription()));
            if (i < rooms.size() - 1)
                json.append(",");
        }
        json.append("]");

        response.getWriter().print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Staff user = (Staff) request.getSession().getAttribute("user");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JSONObject jsonInput = null;
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            try (BufferedReader reader = request.getReader()) {
                String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                jsonInput = new JSONObject(body);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid JSON input");
                return;
            }
        }

        String action = (jsonInput != null) ? jsonInput.optString("action") : request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if ("add".equals(action)) {
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                Room room = new Room();
                room.setRoomNumber(
                        (jsonInput != null) ? jsonInput.optString("roomNumber") : request.getParameter("roomNumber"));
                room.setType((jsonInput != null) ? jsonInput.optString("type") : request.getParameter("type"));

                String priceStr = (jsonInput != null) ? jsonInput.optString("price") : request.getParameter("price");
                if (priceStr == null || priceStr.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Price is required");
                    return;
                }
                room.setPrice(Double.parseDouble(priceStr));
                room.setStatus((jsonInput != null) ? jsonInput.optString("status") : request.getParameter("status"));
                room.setDescription(
                        (jsonInput != null) ? jsonInput.optString("description") : request.getParameter("description"));

                if (roomDAO.addRoom(room)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            } else if ("update".equals(action)) {
                String roomIdStr = (jsonInput != null) ? jsonInput.optString("roomId") : request.getParameter("roomId");
                if (roomIdStr == null || roomIdStr.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                int roomId = Integer.parseInt(roomIdStr);
                Room existingRoom = roomDAO.getRoomById(roomId);

                if (existingRoom == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                if ("RECEPTIONIST".equalsIgnoreCase(user.getRole())) {
                    // Restricted update: Only status
                    String newStatus = (jsonInput != null) ? jsonInput.optString("status")
                            : request.getParameter("status");
                    existingRoom.setStatus(newStatus);
                    if (roomDAO.updateRoom(existingRoom)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    // Full update
                    existingRoom.setRoomNumber((jsonInput != null) ? jsonInput.optString("roomNumber")
                            : request.getParameter("roomNumber"));
                    existingRoom
                            .setType((jsonInput != null) ? jsonInput.optString("type") : request.getParameter("type"));

                    String priceStr = (jsonInput != null) ? jsonInput.optString("price")
                            : request.getParameter("price");
                    if (priceStr != null && !priceStr.isEmpty()) {
                        existingRoom.setPrice(Double.parseDouble(priceStr));
                    }

                    existingRoom.setStatus(
                            (jsonInput != null) ? jsonInput.optString("status") : request.getParameter("status"));
                    existingRoom.setDescription((jsonInput != null) ? jsonInput.optString("description")
                            : request.getParameter("description"));

                    if (roomDAO.updateRoom(existingRoom)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }

            } else if ("delete".equals(action)) {
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                String roomIdStr = (jsonInput != null) ? jsonInput.optString("roomId") : request.getParameter("roomId");
                int roomId = Integer.parseInt(roomIdStr);
                if (roomDAO.deleteRoom(roomId)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().write("Cannot delete room. It might be Occupied or Booked.");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
