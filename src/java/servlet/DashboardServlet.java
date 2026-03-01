package servlet;

import dao.BookingDAO;
import dao.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        // Room stats
        int totalRooms = roomDAO.getRoomCount();
        int availableRooms = roomDAO.getCountByStatus("Available");
        int occupiedRooms = roomDAO.getCountByStatus("Occupied");
        int maintenanceRooms = roomDAO.getCountByStatus("Maintenance");
        int cleaningRooms = roomDAO.getCountByStatus("Cleaning");

        // Booking stats
        int activeBookings = bookingDAO.getActiveBookingCount();
        double totalRevenue = bookingDAO.getTotalRevenue();
        int confirmedBookings = bookingDAO.getBookingCountByStatus("Confirmed");
        int checkedInBookings = bookingDAO.getBookingCountByStatus("Checked-in");
        int completedBookings = bookingDAO.getBookingCountByStatus("Completed");
        int cancelledBookings = bookingDAO.getBookingCountByStatus("Cancelled");
        int totalGuests = bookingDAO.getTotalGuestCount();

        String json = String.format(
                "{\"totalRooms\": %d, \"availableRooms\": %d, \"occupiedRooms\": %d, " +
                        "\"maintenanceRooms\": %d, \"cleaningRooms\": %d, " +
                        "\"activeBookings\": %d, \"totalRevenue\": %.2f, " +
                        "\"confirmedBookings\": %d, \"checkedInBookings\": %d, " +
                        "\"completedBookings\": %d, \"cancelledBookings\": %d, \"totalGuests\": %d}",
                totalRooms, availableRooms, occupiedRooms, maintenanceRooms, cleaningRooms,
                activeBookings, totalRevenue,
                confirmedBookings, checkedInBookings, completedBookings, cancelledBookings, totalGuests);

        out.print(json);
        out.flush();
    }
}
