import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AlterDb {
    public static void main(String[] args) {
        String url = "jdbc:mysql://38.55.34.56:3306/agritrace?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        try (Connection conn = DriverManager.getConnection(url, "root", "yjd999999");
                Statement stmt = conn.createStatement()) {
            // altering qr_code to TEXT since it can contain very large base64 encoded image
            // strings
            stmt.executeUpdate("ALTER TABLE products MODIFY COLUMN qr_code TEXT");
            System.out.println("products table modified successfully.");

            // Just in case, also checking other potentially long base64 fields like
            // public_key and private_key in users
            stmt.executeUpdate("ALTER TABLE users MODIFY COLUMN public_key TEXT, MODIFY COLUMN private_key TEXT");
            System.out.println("users table modified successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
