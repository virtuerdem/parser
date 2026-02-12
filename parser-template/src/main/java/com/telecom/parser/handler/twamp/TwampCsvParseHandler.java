package com.telecom.parser.handler.twamp;

import com.telecom.parser.handler.CsvParseHandler;
import com.telecom.parser.model.TableMetadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TWAMP CSV Parse Handler
 *
 * Adapted from old_etl_codes/Parsers/TwampNewCsvFileHandler.java
 *
 * Parses TWAMP (Two-Way Active Measurement Protocol) CSV files.
 * TWAMP is used for measuring network performance metrics:
 * - Latency
 * - Jitter
 * - Packet loss
 *
 * File Structure:
 * - First row: Header with column names
 * - Subsequent rows: Data values
 * - TimeGroup column contains timestamp
 *
 * Processing:
 * 1. Extract table name from filename (2nd segment: filename_TABLENAME_...)
 * 2. Map CSV columns to database table columns
 * 3. Format TimeGroup date field
 * 4. Write mapped data to output CSV
 */
public class TwampCsvParseHandler extends CsvParseHandler {

    // Table information
    private TableMetadata tableMetadata;
    private String tableName;

    // CSV processing state
    private String[] headerColumns;      // CSV file header
    private Map<String, Integer> headerPositions;  // Column name -> index
    private int timeGroupPosition = -1;  // Position of TimeGroup column
    private long rowCount = 0;

    // Output delimiter (from Activity Diagram: resultParameter)
    private static final String OUTPUT_DELIMITER = "|";

    public TwampCsvParseHandler(File csvFile) {
        super(csvFile, ',');  // TWAMP CSV uses comma delimiter
        this.headerPositions = new HashMap<>();
    }

    // ==================== Metadata Extraction ====================

    /**
     * Extract fragment date from TWAMP filename
     *
     * TWAMP filename format may vary, implement based on actual format
     * Example: "20260211_1500_TWAMP_PM.csv" -> "202602111500"
     */
    @Override
    protected String extractFragmentDate(String fileName) {
        // TODO: Implement based on actual TWAMP filename format
        // For now, return current timestamp
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }

    /**
     * Extract node name from TWAMP filename
     *
     * TWAMP files may not have node name in filename
     * Extract from CSV data if needed
     */
    @Override
    protected String extractNodeName(String fileName) {
        // TWAMP files may not have node name in filename
        // Will be extracted from CSV data
        return "TWAMP_NODE";
    }

    // ==================== Handler Lifecycle ====================

    /**
     * Pre-processing: Extract table name and load metadata
     *
     * Original code:
     * - tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(
     *     currentFileProgress.getName().split("_")[1])
     */
    @Override
    protected void preHandler() {
        super.preHandler();

        System.out.println("[TWAMP Handler] Initializing TWAMP CSV handler");

        // Extract table name from filename
        // Format: prefix_TABLENAME_suffix.csv
        String fileName = csvFile.getName();
        String[] parts = fileName.split("_");

        if (parts.length > 1) {
            this.tableName = parts[1].toUpperCase();  // Second segment is table name
        } else {
            // Fallback: use filename without extension
            this.tableName = fileName.replace(".csv", "").toUpperCase();
        }

        System.out.println("  Table Name: " + tableName);

        // Get table metadata from repository
        if (tables != null && tables.containsKey(tableName)) {
            this.tableMetadata = tables.get(tableName);
            System.out.println("  Table metadata loaded: " + tableMetadata.getTableName());
        } else {
            System.err.println("  WARNING: Table metadata not found for: " + tableName);
            // Continue anyway - will use CSV headers as-is
        }
    }

    /**
     * Post-processing: Delete source file after successful parse
     *
     * Original code:
     * - deleteFile(currentFileProgress)
     */
    @Override
    protected void postHandler() {
        super.postHandler();

        System.out.println("[TWAMP Handler] Parsed " + rowCount + " rows for table: " + tableName);

        // TODO: Implement file deletion or archiving
        // For now, just log
        System.out.println("  Source file: " + csvFile.getAbsolutePath());
    }

    // ==================== Line Processing ====================

    /**
     * Process each CSV line
     *
     * Original code logic:
     * - Row 1: Header - store column positions
     * - Row 2+: Data - format and map to table columns
     */
    @Override
    protected void lineProgress(long lineIndex, String[] line) {
        rowCount++;

        try {
            // Clean all values in the line
            cleanCsvLine(line);

            if (rowCount == 1) {
                // ===== HEADER ROW =====
                processHeaderRow(line);
            } else {
                // ===== DATA ROW =====
                processDataRow(line);
            }

        } catch (Exception e) {
            System.err.println("[TWAMP Handler] Parse error at row " + rowCount + ": " + e.getMessage());
            System.err.println("  File: " + csvFile.getName());
            System.err.println("  Data: " + joinCsvLine(line, ","));
            // Continue processing - don't fail the entire file for one bad row
        }
    }

    /**
     * Process header row (first row)
     *
     * Store column names and find TimeGroup position
     */
    private void processHeaderRow(String[] headerRow) {
        System.out.println("[TWAMP Handler] Processing header row with " + headerRow.length + " columns");

        this.headerColumns = headerRow;

        // Build column position map
        for (int i = 0; i < headerRow.length; i++) {
            String columnName = headerRow[i].trim().toUpperCase();
            headerPositions.put(columnName, i);

            // Find TimeGroup column
            if ("TIMEGROUP".equals(columnName)) {
                timeGroupPosition = i;
                System.out.println("  Found TimeGroup at position: " + i);
            }
        }

        System.out.println("  Header columns: " + joinCsvLine(headerRow, ", "));
    }

    /**
     * Process data row
     *
     * Original logic:
     * 1. Format TimeGroup date (remove 'T', take first 12 chars)
     * 2. Map CSV columns to table columns (if metadata available)
     * 3. Fill missing columns with empty values
     * 4. Write to output CSV
     */
    private void processDataRow(String[] dataRow) {

        // ===== 1. Format TimeGroup Date =====
        if (timeGroupPosition >= 0 && timeGroupPosition < dataRow.length) {
            // Original: line[datePosition] = line[datePosition].replace("T", "").substring(0, 12);
            // Example: "2026-02-11T15:30:00" -> "202602111530"
            String timeGroup = dataRow[timeGroupPosition];
            if (timeGroup.contains("T")) {
                timeGroup = timeGroup.replace("T", "").replace("-", "").replace(":", "");
                // Take first 12 characters: YYYYMMDDHHMM
                if (timeGroup.length() >= 12) {
                    dataRow[timeGroupPosition] = timeGroup.substring(0, 12);
                }
            }
        }

        // ===== 2. Map Columns to Table Schema =====
        String mappedRecord;

        if (tableMetadata != null && tableMetadata.getCounterMapping() != null) {
            // Use table metadata for column mapping
            mappedRecord = mapColumnsToTableSchema(dataRow);
        } else {
            // No metadata - use CSV columns as-is
            mappedRecord = joinCsvLine(dataRow, OUTPUT_DELIMITER);
        }

        // ===== 3. Write to Output CSV =====
        if (tableName != null && mappedRecord != null && !mappedRecord.isEmpty()) {
            writeToCsv(tableName, mappedRecord);

            // Log every 1000 rows
            if (rowCount % 1000 == 0) {
                System.out.println("  Processed " + rowCount + " rows...");
            }
        }

        // ===== 4. Auto Counter (if enabled) =====
        if (autoCounterEnabled && rowCount == 2) {  // Collect from first data row
            collectCounterDefinitionsFromHeader();
        }
    }

    /**
     * Map CSV columns to database table schema
     *
     * Original code:
     * - CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, ...)
     *
     * This maps CSV columns to table columns and fills missing columns
     */
    private String mapColumnsToTableSchema(String[] dataRow) {
        // Build CSV record string
        String csvRecord = joinCsvLine(dataRow, OUTPUT_DELIMITER);

        // Get table column order
        StringBuilder mappedRecord = new StringBuilder();

        // If table metadata has column definitions, use them
        if (tableMetadata.getColumns() != null) {
            for (TableMetadata.ColumnMetadata column : tableMetadata.getColumns()) {
                String columnName = column.getColumnName().toUpperCase();

                // Find this column in CSV header
                if (headerPositions.containsKey(columnName)) {
                    int position = headerPositions.get(columnName);
                    if (position < dataRow.length) {
                        mappedRecord.append(dataRow[position]);
                    } else {
                        mappedRecord.append(""); // Missing value
                    }
                } else {
                    // Column not in CSV - use default value
                    mappedRecord.append("0"); // Default: 0
                }

                mappedRecord.append(OUTPUT_DELIMITER);
            }

            // Remove trailing delimiter
            if (mappedRecord.length() > 0) {
                mappedRecord.setLength(mappedRecord.length() - OUTPUT_DELIMITER.length());
            }

            return mappedRecord.toString();

        } else {
            // No column metadata - return CSV as-is
            return csvRecord;
        }
    }

    /**
     * Collect counter definitions from CSV header
     *
     * For auto counter discovery feature
     */
    private void collectCounterDefinitionsFromHeader() {
        if (headerColumns != null) {
            for (String columnName : headerColumns) {
                String definition = "TWAMP counter: " + columnName;
                collectCounterDefinition(columnName, definition);
            }
        }
    }
}
