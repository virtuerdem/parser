package com.telecom.parser.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSV Writer for parsed data
 * Thread-safe, manages multiple CSV files simultaneously
 */
public class CsvWriter {

    private final String resultPath;
    private final Map<String, BufferedWriter> writers;
    private final DateTimeFormatter formatter;

    public CsvWriter(String resultPath) {
        this.resultPath = resultPath;
        this.writers = new ConcurrentHashMap<>();
        this.formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    }

    /**
     * Write data to CSV buffer
     * Activity Diagram: Write to CSV buffer
     *
     * @param tableName Target table name
     * @param data CSV row data
     */
    public synchronized void write(String tableName, String data) throws IOException {
        BufferedWriter writer = getOrCreateWriter(tableName);
        writer.write(data);
        writer.newLine();
    }

    /**
     * Write multiple rows
     *
     * @param tableName Target table name
     * @param rows List of CSV rows
     */
    public synchronized void writeAll(String tableName, Iterable<String> rows) throws IOException {
        BufferedWriter writer = getOrCreateWriter(tableName);
        for (String row : rows) {
            writer.write(row);
            writer.newLine();
        }
    }

    /**
     * Get or create a writer for specific table
     */
    private BufferedWriter getOrCreateWriter(String tableName) throws IOException {
        return writers.computeIfAbsent(tableName, key -> {
            try {
                String timestamp = LocalDateTime.now().format(formatter);
                String fileName = String.format("%s-%s.csv", tableName, timestamp);
                Path filePath = Paths.get(resultPath, fileName);

                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true));

                // Write CSV header (if file is new)
                // writeHeader(writer, tableName);

                return writer;
            } catch (IOException e) {
                throw new RuntimeException("Failed to create CSV writer for table: " + tableName, e);
            }
        });
    }

    /**
     * Close all streams
     * Activity Diagram: writer.closeAllStreams()
     */
    public void closeAllStreams() {
        writers.forEach((tableName, writer) -> {
            try {
                writer.flush();
                writer.close();
                System.out.println("Closed CSV writer for table: " + tableName);
            } catch (IOException e) {
                System.err.println("Error closing writer for table " + tableName + ": " + e.getMessage());
            }
        });
        writers.clear();
    }

    /**
     * Flush all buffers
     */
    public void flushAll() {
        writers.forEach((tableName, writer) -> {
            try {
                writer.flush();
            } catch (IOException e) {
                System.err.println("Error flushing writer for table " + tableName + ": " + e.getMessage());
            }
        });
    }

    /**
     * Get number of active writers
     */
    public int getActiveWriterCount() {
        return writers.size();
    }
}
