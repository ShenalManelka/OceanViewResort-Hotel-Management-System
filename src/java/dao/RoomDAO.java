package dao;

import modal.Room;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
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
        String query = "SELECT COUNT(*) FROM rooms";
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
}
