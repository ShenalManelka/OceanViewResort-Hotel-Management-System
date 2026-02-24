package dao;

import modal.Booking;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, r.room_number FROM bookings b JOIN rooms r ON b.room_id = r.room_id ORDER BY b.check_in DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getString("guest_name"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in"),
                        rs.getDate("check_out"),
                        rs.getDouble("total_price"),
                        rs.getString("status"));
                b.setRoomNumber(rs.getString("room_number"));
                bookings.add(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String query = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getActiveBookingCount() {
        String query = "SELECT COUNT(*) FROM bookings WHERE status = 'Confirmed'";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenue() {
        String query = "SELECT SUM(total_price) FROM bookings WHERE status = 'Completed'";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
