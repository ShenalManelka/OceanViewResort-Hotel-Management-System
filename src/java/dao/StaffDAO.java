package dao;

import model.Staff;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public Staff authenticate(String email, String password) throws Exception {
        String query = "SELECT * FROM staff WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Staff staff = new Staff();
                    staff.setId(rs.getInt("id"));
                    staff.setUsername(rs.getString("email"));
                    staff.setFullName(rs.getString("full_name"));
                    staff.setEmail(rs.getString("email"));
                    staff.setRole(rs.getString("role"));
                    return staff;
                }
            }
        }
        return null;
    }

    // ✅ Get All Staff
    public List<Staff> getAllStaff() {
        List<Staff> list = new ArrayList<>();
        String query = "SELECT * FROM staff ORDER BY role, full_name";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Staff s = new Staff();
                s.setId(rs.getInt("id"));
                s.setFullName(rs.getString("full_name"));
                s.setEmail(rs.getString("email"));
                s.setRole(rs.getString("role"));
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ✅ Add Receptionist Staff
    public boolean addStaff(String fullName, String email, String password) {
        String query = "INSERT INTO staff (full_name, email, password, role) VALUES (?, ?, ?, 'RECEPTIONIST')";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, password);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Delete Staff (only RECEPTIONIST role — cannot delete ADMIN)
    public boolean deleteStaff(int id) {
        String checkQuery = "SELECT role FROM staff WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
            checkPs.setInt(1, id);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && "ADMIN".equals(rs.getString("role"))) {
                    return false; // Block deletion of Admin accounts
                }
            }
            String deleteQuery = "DELETE FROM staff WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
