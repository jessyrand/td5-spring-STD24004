import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getDBConnection() throws SQLException {
        String url = System.getenv("JDBC_URL");
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");

        return DriverManager.getConnection(url, username, password);
    }
}