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

        List<Room> rooms = roomDAO.getAllRooms();
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

        String action = request.getParameter("action");
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if ("add".equals(action) || "update".equals(action)) {
                Room room = new Room();
                if ("update".equals(action)) {
                    room.setRoomId(Integer.parseInt(request.getParameter("roomId")));
                }
                room.setRoomNumber(request.getParameter("roomNumber"));
                room.setType(request.getParameter("type"));
                room.setPrice(Double.parseDouble(request.getParameter("price")));
                room.setStatus(request.getParameter("status"));
                room.setDescription(request.getParameter("description"));

                boolean success = "add".equals(action) ? roomDAO.addRoom(room) : roomDAO.updateRoom(room);
                if (success) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else if ("delete".equals(action)) {
                int roomId = Integer.parseInt(request.getParameter("roomId"));
                if (roomDAO.deleteRoom(roomId)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
        }
    }
}
