import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryLecturer {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/srpm_db";
        String user = "postgres";
        String password = "123456";

        String query = """
            SELECT u.user_id, u.username, u.email, l.lecturer_code
            FROM users u
            JOIN lecturers l ON u.user_id = l.user_id
            WHERE u.username = 'lecturer2' OR u.email = 'lecturer2@gmail.com'
            """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                System.out.println("Lecturer ID: " + rs.getLong("user_id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Lecturer Code: " + rs.getString("lecturer_code"));
            } else {
                System.out.println("Lecturer not found");
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
