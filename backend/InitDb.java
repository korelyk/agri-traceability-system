import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InitDb {
    public static void main(String[] args) {
        String url = "jdbc:mysql://38.55.34.56:3306/?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        try (Connection conn = DriverManager.getConnection(url, "root", "yjd999999");
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS agritrace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            System.out.println("Database 'agritrace' created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
