package servlet;

import dao.PaymentDAO;
import model.Payment;
import dao.BookingDAO;
import model.Booking;
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

@WebServlet(name = "PaymentServlet", urlPatterns = { "/admin/payments" })
public class PaymentServlet extends HttpServlet {

    private PaymentDAO paymentDAO = new PaymentDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        String bookingIdStr = request.getParameter("bookingId");

        if (bookingIdStr != null) {
            int bookingId = Integer.parseInt(bookingIdStr);
            Payment payment = paymentDAO.getPaymentByBookingId(bookingId);

            if (payment != null) {
                response.getWriter().print(new JSONObject(payment).toString());
            } else {
                // If payment doesn't exist, fetch booking details for new payment
                Booking target = bookingDAO.getBookingById(bookingId);
                if (target != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("found", true);
                    obj.put("booking", new JSONObject(target));
                    response.getWriter().print(obj.toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().print("{\"error\": \"Booking not found\"}");
                }
            }
            return;
        }

        // Return all payments
        List<Payment> payments = paymentDAO.getAllPayments();
        JSONArray array = new JSONArray();
        for (Payment p : payments) {
            array.put(new JSONObject(p));
        }
        response.getWriter().print(array.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        try {
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            if (body == null || body.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\": \"Empty request body\"}");
                return;
            }

            JSONObject json = new JSONObject(body);

            Payment payment = new Payment();
            payment.setBookingId(json.getInt("bookingId"));
            payment.setPaymentMethod(json.optString("paymentMethod", "Cash"));
            payment.setAmount(json.getDouble("amount"));
            payment.setPaymentStatus(json.optString("paymentStatus", "Paid"));

            if (paymentDAO.addPayment(payment)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print("{\"message\": \"Payment recorded successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\": \"Failed to record payment in database\"}");
            }

        } catch (Exception e) {
            System.err.println("Error in PaymentServlet.doPost: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}