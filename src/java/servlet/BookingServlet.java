package servlet;

import dao.BookingDAO;
import modal.Booking;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "BookingServlet", urlPatterns = { "/admin/bookings" })
public class BookingServlet extends HttpServlet {

    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Booking> bookings = bookingDAO.getAllBookings();
        response.setContentType("application/json");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            json.append(String.format(
                    "{\"bookingId\": %d, \"guestName\": \"%s\", \"roomNumber\": \"%s\", \"checkIn\": \"%s\", \"checkOut\": \"%s\", \"totalPrice\": %.2f, \"status\": \"%s\"}",
                    b.getBookingId(), b.getGuestName(), b.getRoomNumber(), b.getCheckIn(), b.getCheckOut(),
                    b.getTotalPrice(), b.getStatus()));
            if (i < bookings.size() - 1)
                json.append(",");
        }
        json.append("]");

        response.getWriter().print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            int bookingId = Integer.parseInt(request.getParameter("bookingId"));
            String status = request.getParameter("status");
            if (bookingDAO.updateBookingStatus(bookingId, status)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
