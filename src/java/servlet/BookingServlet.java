package servlet;

import dao.BookingDAO;
import model.Booking;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Staff;
import model.User;
import dao.UserDAO;
import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.util.stream.Collectors;

@WebServlet(name = "BookingServlet", urlPatterns = { "/admin/bookings" })
public class BookingServlet extends HttpServlet {

    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String report = request.getParameter("report");
        response.setContentType("application/json");

        if ("revenue".equalsIgnoreCase(report)) {
            List<org.json.JSONObject> revenue = bookingDAO.getMonthlyRevenue();
            response.getWriter().print(revenue.toString());
            return;
        } else if ("cancellation".equalsIgnoreCase(report)) {
            org.json.JSONObject stats = bookingDAO.getCancellationStats();
            response.getWriter().print(stats.toString());
            return;
        }

        List<Booking> bookings = bookingDAO.getAllBookings();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            json.append(String.format(
                    "{\"bookingId\": %d, \"guestId\": %d, \"guestName\": \"%s\", \"roomNumber\": \"%s\", \"checkIn\": \"%s\", \"checkOut\": \"%s\", \"totalPrice\": %.2f, \"status\": \"%s\"}",
                    b.getBookingId(), b.getGuestId(), b.getGuestName(), b.getRoomNumber(), b.getCheckIn(),
                    b.getCheckOut(),
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

        JSONObject jsonInput = null;
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            try (BufferedReader reader = request.getReader()) {
                String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                jsonInput = new JSONObject(body);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        String action = (jsonInput != null) ? jsonInput.optString("action") : request.getParameter("action");

        try {
            if ("add".equalsIgnoreCase(action)) {
                Booking b = new Booking();
                b.setGuestId(jsonInput.getInt("userId"));
                b.setRoomId(jsonInput.getInt("roomId"));
                b.setCheckIn(java.sql.Date.valueOf(jsonInput.getString("checkIn")));
                b.setCheckOut(java.sql.Date.valueOf(jsonInput.getString("checkOut")));
                b.setTotalPrice(jsonInput.getDouble("totalPrice"));
                b.setStatus(jsonInput.optString("status", "Confirmed"));

                if (bookingDAO.addBooking(b)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            } else if ("updateStatus".equalsIgnoreCase(action)) {
                int bookingId = (jsonInput != null) ? jsonInput.getInt("bookingId")
                        : Integer.parseInt(request.getParameter("bookingId"));
                String status = (jsonInput != null) ? jsonInput.getString("status") : request.getParameter("status");

                if (bookingDAO.updateBookingStatus(bookingId, status)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else if ("delete".equalsIgnoreCase(action)) {
                int bookingId = (jsonInput != null) ? jsonInput.getInt("bookingId")
                        : Integer.parseInt(request.getParameter("bookingId"));
                if (bookingDAO.deleteBooking(bookingId)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
