package database;

import java.sql.*;

public class OracleConnector {
    private static Connection con;

    private static String dbIp = "192.168.56.101";
    private static String dbPort = "1521";
    private static String dbUser = "system";
    private static String dbPassword = "oracle";

    public static Connection getConnection() throws SQLException {
        OracleConnector.con = DriverManager.getConnection(
                "jdbc:oracle:thin:@" + OracleConnector.dbIp + ":" + OracleConnector.dbPort + "/orcl", OracleConnector.dbUser, OracleConnector.dbPassword);
        return OracleConnector.con;
    }
}
