package dao;

import model.Payment;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // ✅ Add Payment
    public boolean addPayment(Payment payment) {

        String query = "INSERT INTO payments " +
                "(booking_id, payment_method, amount, payment_status, payment_date) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, payment.getBookingId());
            ps.setString(2, payment.getPaymentMethod());
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getPaymentStatus());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                // ✅ Update status to Completed
                String updateBooking = "UPDATE bookings SET status = 'Completed' WHERE booking_id = ?";
                try (PreparedStatement psBooking = conn.prepareStatement(updateBooking)) {
                    psBooking.setInt(1, payment.getBookingId());
                    psBooking.executeUpdate();
                }

                // ✅ Get room_id from booking
                String getRoomId = "SELECT room_id FROM bookings WHERE booking_id = ?";
                int roomId = -1;

                try (PreparedStatement psRoomId = conn.prepareStatement(getRoomId)) {
                    psRoomId.setInt(1, payment.getBookingId());
                    try (ResultSet rs = psRoomId.executeQuery()) {
                        if (rs.next()) {
                            roomId = rs.getInt("room_id");
                        }
                    }
                }

                // ✅ Update room status to Available (better logic)
                if (roomId != -1) {
                    String updateRoom = "UPDATE rooms SET status = 'Available' WHERE room_id = ?";
                    try (PreparedStatement psRoom = conn.prepareStatement(updateRoom)) {
                        psRoom.setInt(1, roomId);
                        psRoom.executeUpdate();
                    }
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ Get All Payments
    public List<Payment> getAllPayments() {

        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                payments.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("booking_id"),
                        rs.getString("payment_method"),
                        rs.getDouble("amount"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("payment_date")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return payments;
    }

    // ✅ Get Payment By Booking ID
    public Payment getPaymentByBookingId(int bookingId) {

        String query = "SELECT * FROM payments WHERE booking_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                            rs.getInt("payment_id"),
                            // user_id and room_id fixed
                            rs.getInt("booking_id"),
                            rs.getString("payment_method"),
                            rs.getDouble("amount"),
                            rs.getString("payment_status"),
                            rs.getTimestamp("payment_date"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ Delete Payment
    public boolean deletePayment(int paymentId) {
        String query = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}