package dao;

import model.Booking;
import org.json.JSONObject;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, u.first_name, u.last_name, u.email, r.room_number " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.user_id " +
                "JOIN rooms r ON b.room_id = r.room_id " +
                "ORDER BY b.check_in DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in"),
                        rs.getDate("check_out"),
                        rs.getDouble("total_price"),
                        rs.getString("status"));
                b.setGuestName(rs.getString("first_name") + " " + rs.getString("last_name"));
                b.setGuestEmail(rs.getString("email"));
                b.setRoomNumber(rs.getString("room_number"));
                bookings.add(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean addBooking(Booking b) {
        String query = "INSERT INTO bookings (user_id, room_id, check_in, check_out, total_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, b.getGuestId());
                ps.setInt(2, b.getRoomId());
                ps.setDate(3, b.getCheckIn());
                ps.setDate(4, b.getCheckOut());
                ps.setDouble(5, b.getTotalPrice());
                ps.setString(6, b.getStatus());

                if (ps.executeUpdate() > 0) {
                    // Always set room to 'Occupied' for any check-in
                    String updateRoom = "UPDATE rooms SET status = 'Occupied' WHERE room_id = ?";
                    try (PreparedStatement psRoom = conn.prepareStatement(updateRoom)) {
                        psRoom.setInt(1, b.getRoomId());
                        psRoom.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }
            } catch (Exception e) {
                if (conn != null)
                    conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
        return false;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String query = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Get room_id first to automate status
            int roomId = -1;
            String getRoomQuery = "SELECT room_id FROM bookings WHERE booking_id = ?";
            try (PreparedStatement psGet = conn.prepareStatement(getRoomQuery)) {
                psGet.setInt(1, bookingId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next())
                        roomId = rs.getInt("room_id");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, status);
                ps.setInt(2, bookingId);

                if (ps.executeUpdate() > 0 && roomId != -1) {
                    // Logic for room status automation
                    String roomStatus = null;
                    if ("Checked-in".equalsIgnoreCase(status))
                        roomStatus = "Occupied";
                    else if ("Completed".equalsIgnoreCase(status))
                        roomStatus = "Available";
                    else if ("Cancelled".equalsIgnoreCase(status))
                        roomStatus = "Available";

                    if (roomStatus != null) {
                        String updateRoom = "UPDATE rooms SET status = ? WHERE room_id = ?";
                        try (PreparedStatement psRoom = conn.prepareStatement(updateRoom)) {
                            psRoom.setString(1, roomStatus);
                            psRoom.setInt(2, roomId);
                            psRoom.executeUpdate();
                        }
                    }
                    conn.commit();
                    return true;
                }
            } catch (Exception e) {
                if (conn != null)
                    conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
        return false;
    }

    public int getActiveBookingCount() {
        String query = "SELECT COUNT(*) FROM bookings WHERE status IN ('Confirmed', 'Checked-in')";
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

    public List<JSONObject> getMonthlyRevenue() {
        List<JSONObject> results = new ArrayList<>();
        String query = "SELECT MONTHNAME(check_in) as month, SUM(total_price) as revenue " +
                "FROM bookings WHERE status = 'Completed' " +
                "GROUP BY MONTH(check_in) ORDER BY MONTH(check_in)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("month", rs.getString("month"));
                obj.put("revenue", rs.getDouble("revenue"));
                results.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public JSONObject getCancellationStats() {
        JSONObject stats = new JSONObject();
        String query = "SELECT " +
                "COUNT(CASE WHEN status = 'Cancelled' THEN 1 END) as cancelled, " +
                "COUNT(*) as total " +
                "FROM bookings";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int cancelled = rs.getInt("cancelled");
                int total = rs.getInt("total");
                stats.put("cancelled", cancelled);
                stats.put("total", total);
                stats.put("rate", total > 0 ? (double) cancelled / total * 100 : 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    public boolean deleteBooking(int bookingId) {
        String query = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Booking getBookingById(int bookingId) {
    String query = "SELECT b.*, u.first_name, u.last_name, u.email, r.room_number " +
                   "FROM bookings b " +
                   "JOIN users u ON b.user_id = u.user_id " +
                   "JOIN rooms r ON b.room_id = r.room_id " +
                   "WHERE b.booking_id = ?";

    try (Connection conn = DBConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {

        ps.setInt(1, bookingId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Booking b = new Booking(
                    rs.getInt("booking_id"),
                    rs.getInt("user_id"),
                    rs.getInt("room_id"),
                    rs.getDate("check_in"),
                    rs.getDate("check_out"),
                    rs.getDouble("total_price"),
                    rs.getString("status")
                );
                b.setGuestName(rs.getString("first_name") + " " + rs.getString("last_name"));
                b.setGuestEmail(rs.getString("email"));
                b.setRoomNumber(rs.getString("room_number"));
                return b;
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
}
