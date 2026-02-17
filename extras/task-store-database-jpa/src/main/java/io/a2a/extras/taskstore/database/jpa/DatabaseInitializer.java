package io.a2a.extras.taskstore.database.jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import java.time.Instant;

/**
 * Utility class to initialize the database schema and insert sample data.
 * This class handles:
 * 1. Creation of 'a2a_tasks' table if it does not exist.
 * 2. Insertion of a sample task record.
 *
 * <p>Configuration via Environment Variables:
 * <ul>
 *   <li>{@code DB_URL}: JDBC URL (default: {@code jdbc:postgresql://localhost:5432/a2a_db})</li>
 *   <li>{@code DB_USER}: Database User (default: {@code a2a})</li>
 *   <li>{@code DB_PASSWORD}: Database Password (default: {@code a2a})</li>
 * </ul>
 * 
 * IMPORTANT: No deletion logic is implemented here to ensure data safety.
 *
 * @author Sandeep Belgavi
 * @since 2026-02-17
 */
public class DatabaseInitializer {

    private static final String JDBC_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/a2a_db");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "a2a");
    private static final String PASS = System.getenv().getOrDefault("DB_PASSWORD", "a2a");

    public static void main(String[] args) {
        System.out.println("Starting Database Initialization...");
        try {
            // Load PostgreSQL driver explicitly
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS)) {
                // 1. Create Table
                createTable(conn);

                // 2. Insert Sample Data
                insertSampleTask(conn);
            }
            
            System.out.println("Database Initialization Completed Successfully.");

        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found. Ensure 'org.postgresql:postgresql' is on the classpath.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Database Initialization Failed.");
            e.printStackTrace();
        }
    }

    private static void createTable(Connection conn) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS a2a_tasks (" +
                     "task_id VARCHAR(255) PRIMARY KEY, " +
                     "task_data TEXT NOT NULL" +
                     ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Verified table 'a2a_tasks'. Created if missing.");
        }
    }

    private static void insertSampleTask(Connection conn) throws Exception {
        String taskId = UUID.randomUUID().toString();
        // Sample JSON structure for task data
        String taskData = String.format("{\"status\": \"SUBMITTED\", \"created_at\": \"%s\", \"description\": \"Sample initialization task\"}", Instant.now());
        
        String sql = "INSERT INTO a2a_tasks (task_id, task_data) VALUES (?, ?) ON CONFLICT (task_id) DO NOTHING";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, taskId);
            stmt.setString(2, taskData);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Inserted sample task: " + taskId);
            } else {
                System.out.println("Sample task insertion skipped (conflict or no-op).");
            }
        }
    }
}
