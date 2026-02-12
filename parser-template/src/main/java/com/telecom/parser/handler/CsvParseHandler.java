package com.telecom.parser.handler;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.telecom.parser.model.TableMetadata;
import com.telecom.parser.writer.CsvWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * CSV Parse Handler - Base class for CSV file parsing
 *
 * This handler reads CSV files line by line and processes them.
 * Similar to etl-master's ParseCsvHandler but adapted to new template.
 *
 * Uses OpenCSV library for CSV parsing.
 *
 * Subclasses must implement:
 * - lineProgress(lineIndex, line) - Process each CSV line
 */
public abstract class CsvParseHandler implements Runnable {

    // File to parse
    protected File csvFile;

    // CSV delimiter
    protected char delimiter;

    // Shared resources (injected by ParseEngine)
    protected Map<String, Long> networkNodes;
    protected Map<String, TableMetadata> tables;
    protected CsvWriter csvWriter;
    protected Map<String, String> autoCounterDefine;
    protected boolean autoCounterEnabled;

    // Metadata
    protected String fragmentDate;
    protected String nodeName;
    protected Long nodeId;

    // Constructor
    public CsvParseHandler(File csvFile, char delimiter) {
        this.csvFile = csvFile;
        this.delimiter = delimiter;
    }

    /**
     * Main entry point for thread execution
     */
    @Override
    public void run() {
        try {
            System.out.println("[CSV Handler Thread] Starting parse: " + csvFile.getName());

            // Step 1: preHandler()
            preHandler();

            // Step 2: Parse CSV file
            onHandler();

            // Step 3: postHandler()
            postHandler();

            System.out.println("[CSV Handler Thread] Completed: " + csvFile.getName());

        } catch (Exception e) {
            System.err.println("[CSV Handler Thread] Failed to parse " + csvFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
            // Move file to error directory
            moveToErrorDirectory();
        }
    }

    /**
     * Pre-processing before CSV parse
     *
     * Extract metadata from filename, prepare resources, etc.
     */
    protected void preHandler() {
        System.out.println("[CSV Handler] preHandler() - Extracting metadata");

        String fileName = csvFile.getName();

        // Extract metadata from filename
        this.fragmentDate = extractFragmentDate(fileName);
        this.nodeName = extractNodeName(fileName);

        // Lookup node ID from network nodes map
        if (networkNodes != null && nodeName != null) {
            this.nodeId = networkNodes.get(nodeName);
        }

        System.out.println("  Fragment Date: " + fragmentDate);
        System.out.println("  Node Name: " + nodeName);
        System.out.println("  Node ID: " + nodeId);
    }

    /**
     * Main CSV parsing logic
     *
     * Reads CSV file line by line using OpenCSV
     */
    protected void onHandler() {
        long lineIndex = 0L;

        try (FileReader fileReader = new FileReader(csvFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader);
             CSVReader csvReader = new CSVReaderBuilder(bufferedReader)
                     .withCSVParser(new CSVParserBuilder()
                             .withSeparator(delimiter)
                             .build())
                     .build()) {

            System.out.println("[CSV Handler] Parsing CSV with delimiter: '" + delimiter + "'");

            // Read CSV line by line
            for (String[] line : csvReader) {
                lineProgress(lineIndex, line);
                lineIndex++;
            }

            System.out.println("[CSV Handler] Parsed " + lineIndex + " lines");

        } catch (Exception e) {
            System.err.println("[CSV Handler] Parse error at line " + lineIndex + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("CSV parse failed", e);
        }
    }

    /**
     * Post-processing after CSV parse
     *
     * Cleanup resources, flush buffers, etc.
     */
    protected void postHandler() {
        System.out.println("[CSV Handler] postHandler() - Cleanup");
        // Override in subclasses if needed
    }

    // ==================== Abstract Methods ====================

    /**
     * Process each CSV line
     *
     * This is the core method that subclasses must implement.
     * Called for each line in the CSV file.
     *
     * @param lineIndex Line number (0-based)
     * @param line Array of CSV columns
     */
    protected abstract void lineProgress(long lineIndex, String[] line);

    /**
     * Extract fragment date from filename
     * Override in subclasses for vendor-specific format
     */
    protected abstract String extractFragmentDate(String fileName);

    /**
     * Extract node name from filename
     * Override in subclasses for vendor-specific format
     */
    protected abstract String extractNodeName(String fileName);

    // ==================== Helper Methods ====================

    /**
     * Write to CSV buffer
     */
    protected void writeToCsv(String tableName, String csvRow) {
        try {
            csvWriter.write(tableName, csvRow);
        } catch (Exception e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }

    /**
     * Collect counter definition for auto counter
     */
    protected void collectCounterDefinition(String counterName, String definition) {
        if (autoCounterEnabled && autoCounterDefine != null) {
            autoCounterDefine.put(counterName, definition);
        }
    }

    /**
     * Clean CSV value - remove tabs, newlines, trim
     */
    protected String cleanCsvValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\t", " ")
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .trim();
    }

    /**
     * Clean all values in a CSV line
     */
    protected void cleanCsvLine(String[] line) {
        for (int i = 0; i < line.length; i++) {
            line[i] = cleanCsvValue(line[i]);
        }
    }

    /**
     * Join CSV columns with delimiter
     */
    protected String joinCsvLine(String[] line, String delimiter) {
        return String.join(delimiter, line);
    }

    /**
     * Move failed file to error directory
     */
    protected void moveToErrorDirectory() {
        // TODO: Implement error file handling
        System.err.println("Moving to error directory: " + csvFile.getName());
    }

    // ==================== Getters and Setters ====================

    public void setNetworkNodes(Map<String, Long> networkNodes) {
        this.networkNodes = networkNodes;
    }

    public void setTables(Map<String, TableMetadata> tables) {
        this.tables = tables;
    }

    public void setCsvWriter(CsvWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    public void setAutoCounterDefine(Map<String, String> autoCounterDefine) {
        this.autoCounterDefine = autoCounterDefine;
    }

    public void setAutoCounterEnabled(boolean autoCounterEnabled) {
        this.autoCounterEnabled = autoCounterEnabled;
    }
}
