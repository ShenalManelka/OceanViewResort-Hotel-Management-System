package servlet;

import dao.BookingDAO;
import dao.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modal.Staff;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DashboardServlet", urlPatterns = { "/admin/stats" })
public class DashboardServlet extends HttpServlet {

    private RoomDAO roomDAO = new RoomDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        int totalRooms = roomDAO.getRoomCount();
        int availableRooms = roomDAO.getCountByStatus("Available");
        int occupiedRooms = roomDAO.getCountByStatus("Occupied");
        int maintenanceRooms = roomDAO.getCountByStatus("Maintenance");
        int activeBookings = bookingDAO.getActiveBookingCount();
        double totalRevenue = bookingDAO.getTotalRevenue();

        // Manual JSON construction to avoid external dependencies
        String json = String.format(
                "{\"totalRooms\": %d, \"availableRooms\": %d, \"occupiedRooms\": %d, \"maintenanceRooms\": %d, \"activeBookings\": %d, \"totalRevenue\": %.2f}",
                totalRooms, availableRooms, occupiedRooms, maintenanceRooms, activeBookings, totalRevenue);

        out.print(json);
        out.flush();
    }
}
