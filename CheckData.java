import dao.UserDAO;
import util.DBConnection;
import java.sql.*;

public class CheckData {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            System.out.println("--- Database Status ---");

            // Count Users
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next())
                    System.out.println("Total Users: " + rs.getInt(1));
            }

            // Count Bookings
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM bookings")) {
                if (rs.next())
                    System.out.println("Total Bookings: " + rs.getInt(1));
            }

            // Count Guests with Bookings
            try (Statement s = conn.createStatement();
                    ResultSet rs = s.executeQuery("SELECT COUNT(DISTINCT user_id) FROM bookings")) {
                if (rs.next())
                    System.out.println("Guests with Bookings: " + rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
