package dao;

import modal.Admin;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDAO {

    public Admin authenticate(String username, String password) throws Exception {
        Admin admin = null;
        String query = "SELECT * FROM admins WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            // Self-check: Is the table empty?
            String checkQuery = "SELECT COUNT(*) FROM admins";
            try (Statement st = conn.createStatement(); ResultSet rsCheck = st.executeQuery(checkQuery)) {
                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                    System.out.println("No admins found. Creating default admin...");
                    String insertQuery = "INSERT INTO admins (username, password, full_name, email) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ips = conn.prepareStatement(insertQuery)) {
                        ips.setString(1, "admin");
                        ips.setString(2, "admin@1234");
                        ips.setString(3, "Default Admin");
                        ips.setString(4, "admin@oceanview.com");
                        ips.executeUpdate();
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        admin = new Admin();
                        admin.setId(rs.getInt("id"));
                        admin.setUsername(rs.getString("username"));
                        admin.setFullName(rs.getString("full_name"));
                        admin.setEmail(rs.getString("email"));
                    }
                }
            }
        }
        return admin;
    }
}
