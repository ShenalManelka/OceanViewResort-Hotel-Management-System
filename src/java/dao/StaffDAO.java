package dao;

import modal.Staff;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffDAO {

    public Staff authenticate(String email, String password) throws Exception {
        Staff staff = null;
        String query = "SELECT * FROM staff WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        staff = new Staff();
                        staff.setId(rs.getInt("id"));
                        staff.setUsername(rs.getString("email")); // Or username field if exists
                        staff.setFullName(rs.getString("full_name"));
                        staff.setEmail(rs.getString("email"));
                        staff.setRole(rs.getString("role"));
                    }
                }
            }
        }
        return staff;
    }
}
