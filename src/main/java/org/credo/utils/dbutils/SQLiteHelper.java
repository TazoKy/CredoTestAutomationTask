package org.credo.utils.dbutils;

import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SQLiteHelper {
    private static final Logger logger = LogManager.getLogger(SQLiteHelper.class);
    private static final String DB_URL = "jdbc:sqlite:test_results.db";

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS test_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "test_name TEXT UNIQUE," +
                    "status TEXT," +
                    "execution_time DATETIME)");
            logger.info("SQLite table 'test_results' created or already exists.");

        } catch (SQLException e) {
            logger.error("Error creating SQLite table 'test_results'", e);
        }
    }

    public static void saveTestResult(String testName, String status) {
        String sql = "INSERT INTO test_results(test_name, status, execution_time) " +
                "VALUES(?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT(test_name) DO UPDATE SET " +
                "status=excluded.status, execution_time=CURRENT_TIMESTAMP";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, testName);
            pstmt.setString(2, status);
            int rowsAffected = pstmt.executeUpdate();
            logger.info("Test result saved: {} = {} ({} rows affected)", testName, status, rowsAffected);

        } catch (SQLException e) {
            logger.error("Error saving test result for test: " + testName, e);
        }
    }
}

