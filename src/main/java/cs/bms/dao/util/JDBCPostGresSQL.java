package cs.bms.dao.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCPostGresSQL {

    public static final String DRIVER = "org.postgresql.Driver";
//    public static final String URL = "jdbc:postgresql://localhost:5432/bms_test";
//    public static final String PASSWORD = "admin";
    public static final String URL = "jdbc:postgresql://195.154.56.237:5432/bms";
    public static final String PASSWORD = "admin1986";
    public static final String USER = "postgres";
    private Connection connection = null;

    public JDBCPostGresSQL() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER,PASSWORD);
        } catch (SQLException ex) {
            Logger.getLogger(JDBCPostGresSQL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JDBCPostGresSQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null) {
            this.connection = DriverManager.getConnection(URL, USER,PASSWORD);
        } else if (this.connection.isClosed()) {
            this.connection = DriverManager.getConnection(URL, USER,PASSWORD);
        }
        return this.connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(JDBCPostGresSQL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
