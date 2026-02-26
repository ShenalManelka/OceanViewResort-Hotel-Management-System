package servlet;

import dao.RoomDAO;
import modal.Room;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modal.Staff;
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

        String action = request.getParameter("action");
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if ("add".equals(action)) {
                if (!"ADMIN".equals(user.getRole())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                Room room = new Room();
                room.setRoomNumber(request.getParameter("roomNumber"));
                room.setType(request.getParameter("type"));
                room.setPrice(Double.parseDouble(request.getParameter("price")));
                room.setStatus(request.getParameter("status"));
                room.setDescription(request.getParameter("description"));

                if (roomDAO.addRoom(room)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            } else if ("update".equals(action)) {
                int roomId = Integer.parseInt(request.getParameter("roomId"));
                Room existingRoom = roomDAO.getRoomById(roomId);

                if (existingRoom == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                if ("RECEPTIONIST".equals(user.getRole())) {
                    // Restricted update: Only status and only specific transitions
                    String newStatus = request.getParameter("status");
                    existingRoom.setStatus(newStatus);
                    // In a real app, we'd validate the transition here
                    if (roomDAO.updateRoom(existingRoom)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else if ("ADMIN".equals(user.getRole())) {
                    // Full update
                    existingRoom.setRoomNumber(request.getParameter("roomNumber"));
                    existingRoom.setType(request.getParameter("type"));
                    existingRoom.setPrice(Double.parseDouble(request.getParameter("price")));
                    existingRoom.setStatus(request.getParameter("status"));
                    existingRoom.setDescription(request.getParameter("description"));

                    if (roomDAO.updateRoom(existingRoom)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }

            } else if ("delete".equals(action)) {
                if (!"ADMIN".equals(user.getRole())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                int roomId = Integer.parseInt(request.getParameter("roomId"));
                if (roomDAO.deleteRoom(roomId)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    // Might be because room is booked/occupied
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().write("Cannot delete room. It might be Occupied or Booked.");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
        }
    }
}
