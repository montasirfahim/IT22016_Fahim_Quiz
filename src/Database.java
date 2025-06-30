import java.sql.*;

public class Database {
    public Connection conn;
    protected PreparedStatement statement;
    protected ResultSet resultSet;

    private static final String url = "jdbc:mysql://localhost:3307/quiz_db";
    private static final String username = "root";
    private static final String password = "Fahim.1128@";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void closeConnection(){
        try {
            if(resultSet != null){
                this.resultSet.close();
            }
            if(conn != null){
                this.conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
