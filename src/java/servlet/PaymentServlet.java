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

        String bookingIdStr = request.getParameter("bookingId");
        response.setContentType("application/json");

        if (bookingIdStr != null) {
            int bookingId = Integer.parseInt(bookingIdStr);
            Payment payment = paymentDAO.getPaymentByBookingId(bookingId);
            if (payment != null) {
                response.getWriter().print(new JSONObject(payment).toString());
            } else {
                // If payment doesn't exist, fetch booking details for new payment
                List<Booking> all = bookingDAO.getAllBookings();
                Booking target = all.stream().filter(b -> b.getBookingId() == bookingId).findFirst().orElse(null);
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

        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject json = new JSONObject(body);

        Payment payment = new Payment();
        payment.setBookingId(json.getInt("bookingId"));
        payment.setPaymentMethod(json.optString("paymentMethod", "Cash"));
        payment.setAmount(json.getDouble("amount"));
        payment.setTaxAmount(json.optDouble("taxAmount", 0.0));
        payment.setDiscountAmount(json.optDouble("discountAmount", 0.0));
        payment.setPaymentStatus(json.optString("paymentStatus", "Paid"));

        if (paymentDAO.addPayment(payment)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("{\"message\": \"Payment recorded successfully\"}");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to record payment");
        }
    }
}
