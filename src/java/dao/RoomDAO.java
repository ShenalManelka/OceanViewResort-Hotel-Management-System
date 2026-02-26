package dao;

import model.Room;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        return searchRooms(null, null, null);
    }

    public List<Room> searchRooms(String roomNumber, String type, String status) {
        List<Room> rooms = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
        if (roomNumber != null && !roomNumber.isEmpty())
            query.append(" AND room_number LIKE ?");
        if (type != null && !type.isEmpty())
            query.append(" AND type = ?");
        if (status != null && !status.isEmpty())
            query.append(" AND status = ?");

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;
            if (roomNumber != null && !roomNumber.isEmpty())
                ps.setString(paramIndex++, "%" + roomNumber + "%");
            if (type != null && !type.isEmpty())
                ps.setString(paramIndex++, type);
            if (status != null && !status.isEmpty())
                ps.setString(paramIndex++, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getString("type"),
                            rs.getDouble("price"),
                            rs.getString("status"),
                            rs.getString("description")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public Room getRoomById(int roomId) {
        String query = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getString("type"),
                            rs.getDouble("price"),
                            rs.getString("status"),
                            rs.getString("description"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addRoom(Room room) {
        String query = "INSERT INTO rooms (room_number, type, price, status, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getType());
            ps.setDouble(3, room.getPrice());
            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRoom(Room room) {
        String query = "UPDATE rooms SET room_number = ?, type = ?, price = ?, status = ?, description = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getType());
            ps.setDouble(3, room.getPrice());
            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());
            ps.setInt(6, room.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRoom(int roomId) {
        // Check status before deletion
        Room room = getRoomById(roomId);
        if (room == null || "Occupied".equals(room.getStatus())) {
            return false;
        }

        String query = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getRoomCount() {
        return getCountByStatus(null);
    }

    public int getCountByStatus(String status) {
        String query = "SELECT COUNT(*) FROM rooms" + (status != null ? " WHERE status = ?" : "");
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            if (status != null)
                ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
