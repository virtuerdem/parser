package com.telecom.parser.loader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Factory for creating appropriate loaders
 * Activity Diagram: LoaderFactory.load(csv)
 */
public class LoaderFactory {

    /**
     * Load CSV file to database
     * Activity Diagram: LoaderFactory.load(csv1/csv2/csvN)
     *
     * @param csvFile CSV file to load
     * @param connection Database connection
     * @param schemaName Schema name
     * @return Number of rows loaded
     */
    public static int load(File csvFile, Connection connection, String schemaName) throws IOException, SQLException {
        String fileName = csvFile.getName();
        String tableName = extractTableName(fileName);

        // Determine database type and use appropriate loader
        String dbType = getDatabaseType(connection);

        switch (dbType) {
            case "PostgreSQL":
                return loadPostgreSQL(csvFile, connection, schemaName, tableName);

            case "Oracle":
                return loadOracle(csvFile, connection, schemaName, tableName);

            case "SQLServer":
                return loadSQLServer(csvFile, connection, schemaName, tableName);

            default:
                return loadGeneric(csvFile, connection, schemaName, tableName);
        }
    }

    /**
     * PostgreSQL bulk load using COPY command
     */
    private static int loadPostgreSQL(File csvFile, Connection connection, String schemaName, String tableName)
            throws IOException, SQLException {

        String copySQL = String.format(
            "COPY %s.%s FROM STDIN WITH (FORMAT csv, HEADER true, DELIMITER ',')",
            schemaName, tableName
        );

        // Implementation: Use COPY command for bulk insert
        // This is the fastest method for PostgreSQL

        System.out.println("Loading to PostgreSQL: " + tableName);
        // TODO: Implement PostgreSQL COPY logic

        return 0; // Return number of rows loaded
    }

    /**
     * Oracle bulk load using prepared statements
     */
    private static int loadOracle(File csvFile, Connection connection, String schemaName, String tableName)
            throws IOException, SQLException {

        System.out.println("Loading to Oracle: " + tableName);
        // TODO: Implement Oracle batch insert logic

        return 0;
    }

    /**
     * SQL Server bulk load
     */
    private static int loadSQLServer(File csvFile, Connection connection, String schemaName, String tableName)
            throws IOException, SQLException {

        System.out.println("Loading to SQL Server: " + tableName);
        // TODO: Implement SQL Server bulk insert API

        return 0;
    }

    /**
     * Generic JDBC batch insert (slower but works everywhere)
     */
    private static int loadGeneric(File csvFile, Connection connection, String schemaName, String tableName)
            throws IOException, SQLException {

        System.out.println("Loading using generic JDBC: " + tableName);
        // TODO: Implement generic batch insert logic

        return 0;
    }

    /**
     * Extract table name from CSV filename
     * Example: "cell_metrics-20260201120000.csv" -> "cell_metrics"
     */
    private static String extractTableName(String fileName) {
        if (fileName.contains("-")) {
            return fileName.substring(0, fileName.indexOf('-'));
        }
        return fileName.replace(".csv", "");
    }

    /**
     * Detect database type from connection
     */
    private static String getDatabase Type(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();

        if (productName.contains("PostgreSQL")) {
            return "PostgreSQL";
        } else if (productName.contains("Oracle")) {
            return "Oracle";
        } else if (productName.contains("SQL Server")) {
            return "SQLServer";
        }

        return "Generic";
    }
}
