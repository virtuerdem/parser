package com.telecom.parser.engine.twamp;

import com.telecom.parser.engine.ParseEngine;
import com.telecom.parser.handler.twamp.TwampCsvParseHandler;
import com.telecom.parser.model.ParseEngineRecord;
import com.telecom.parser.repository.MetadataRepository;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TWAMP Parse Engine
 *
 * Adapted from old_etl_codes/Systems/ParserEngine_TWAMP.java
 *
 * TWAMP (Two-Way Active Measurement Protocol) Parser Engine
 * Processes TWAMP CSV files containing network performance metrics.
 *
 * Operators supported:
 * - VODAFONE
 * - KKTC-TELSIM
 *
 * Features:
 * - Parallel CSV file processing
 * - 15-minute aggregation procedures
 * - Hourly aggregation (at 45th minute)
 * - Multiple regional aggregations (NW, CITY, REGION, ILCE)
 *
 * Processing Flow (from Activity Diagram):
 * 1. preparePaths()
 * 2. getTables()
 * 3. preEngine() - TWAMP-specific preparation
 * 4. Main Parsing Phase - Parallel CSV parsing
 * 5. postEngine() - TWAMP-specific cleanup
 * 6. Data Loading Phase
 * 7. callProcedure() - TWAMP aggregation procedures
 * 8. callAggregate() - Additional aggregations
 */
public class TwampParseEngine extends ParseEngine {

    // Operator name (VODAFONE or KKTC-TELSIM)
    private String operatorName;

    public TwampParseEngine(MetadataRepository metadataRepository, Connection dbConnection) {
        super(metadataRepository, dbConnection);
    }

    // ==================== Pre-Processing ====================

    /**
     * TWAMP-specific pre-processing
     *
     * Override from ParseEngine
     */
    @Override
    protected void preEngine() {
        System.out.println("=== TWAMP Pre-Processing ===");

        // Get operator name from record
        this.operatorName = record.getCustomConfig() != null
                ? (String) record.getCustomConfig().get("operatorName")
                : "VODAFONE";  // Default

        System.out.println("  Operator: " + operatorName);
        System.out.println("  TWAMP-specific validations...");

        // Additional TWAMP-specific preparations
        // - Validate operator configuration
        // - Check TWAMP-specific tables exist
        // - Prepare aggregation parameters
    }

    // ==================== Main Parsing Phase Override ====================

    /**
     * Override main parsing phase for CSV-specific logic
     *
     * Original code: prepareParser() method
     * - Lists all CSV files
     * - Creates TwampNewCsvFileHandler for each
     * - Executes in thread pool
     */
    @Override
    protected void mainParsingPhase() {
        System.out.println("\n=== TWAMP CSV Parsing Phase ===");

        // Step 1: Get network nodes (same as base engine)
        System.out.println("Getting network nodes...");
        this.networkNodes = metadataRepository.getNetworkNodesByBranchId(record.getBranchId());
        System.out.println("  Loaded " + (networkNodes != null ? networkNodes.size() : 0) + " network nodes");

        // Step 2: Read CSV files from /raw/
        System.out.println("\nReading CSV files from " + record.getRawPath());
        List<File> csvFiles = getCSVFiles(record.getRawPath());

        // Filter out already processed files (integratedFileExtension)
        List<File> filesToProcess = new ArrayList<>();
        for (File file : csvFiles) {
            // Skip files that are output files (already processed)
            if (!file.getName().contains("-")) {  // Output files have format: tablename-timestamp.csv
                filesToProcess.add(file);
            }
        }

        System.out.println("  Found " + filesToProcess.size() + " CSV files to process");

        if (filesToProcess.isEmpty()) {
            System.out.println("  No CSV files to process");
            return;
        }

        // Step 3: Create thread pool
        int threadPoolSize = record.getThreadPoolSize();
        System.out.println("\nCreating thread pool with " + threadPoolSize + " threads");
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        // Step 4: Loop - Create & Submit TWAMP CSV Handlers
        System.out.println("\n<<loop>> Create & Submit TWAMP CSV Handlers");
        System.out.println("[Setup] Initialize CSV file list: " + filesToProcess.size() + " files");

        int submittedCount = 0;
        for (File csvFile : filesToProcess) {
            // Create TWAMP CSV handler
            TwampCsvParseHandler handler = new TwampCsvParseHandler(csvFile);

            // Set handler dependencies
            handler.setNetworkNodes(networkNodes);
            handler.setTables(tables);
            handler.setCsvWriter(csvWriter);
            handler.setAutoCounterDefine(autoCounterDefine);
            handler.setAutoCounterEnabled(Boolean.TRUE.equals(record.getIsActiveAutoCounter()));

            // Submit to thread pool (non-blocking)
            executorService.submit(handler);
            submittedCount++;

            if (submittedCount % 10 == 0) {
                System.out.println("  Submitted " + submittedCount + " handlers");
            }
        }

        System.out.println("  All " + submittedCount + " handlers submitted");

        // Step 5: Synchronization
        System.out.println("\n*** SYNCHRONIZATION POINT ***");
        shutdownExecutorService(executorService);

        // Step 6: Close CSV writers
        System.out.println("\nClosing CSV writers...");
        csvWriter.closeAllStreams();

        System.out.println("\n=== TWAMP CSV Parsing Phase Completed ===");
    }

    // ==================== Post-Processing ====================

    /**
     * TWAMP-specific post-processing
     */
    @Override
    protected void postEngine() {
        System.out.println("=== TWAMP Post-Processing ===");
        System.out.println("  TWAMP-specific cleanup and validations");

        // Additional TWAMP-specific post-processing
        // - Validate parsed data
        // - Generate statistics
        // - Prepare for aggregation
    }

    // ==================== Procedures ====================

    /**
     * TWAMP aggregation procedures
     *
     * Original code: loaderProcedures() method
     *
     * Calls Oracle stored procedures for:
     * - 15-minute aggregations
     * - Regional aggregations (NW, CITY, REGION, ILCE)
     * - Hourly aggregations (at 45th minute)
     */
    @Override
    protected void callProcedure() {
        System.out.println("=== TWAMP Aggregation Procedures ===");

        try {
            // Calculate time parameters
            String todayQuarterMinute = calculateQuarterMinute();
            String todayBeforeOneHour = calculateHourBeforeNow();

            System.out.println("  Current quarter minute: " + todayQuarterMinute);
            System.out.println("  Hour before now: " + todayBeforeOneHour);

            // Execute procedures based on operator
            if ("VODAFONE".equalsIgnoreCase(operatorName)) {
                executeVodafoneProcedures(todayQuarterMinute, todayBeforeOneHour);
            } else if ("KKTC-TELSIM".equalsIgnoreCase(operatorName)) {
                executeKktcTelsimProcedures(todayQuarterMinute);
            } else {
                System.err.println("  Unknown operator: " + operatorName);
            }

        } catch (Exception e) {
            System.err.println("Failed to execute TWAMP procedures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Execute Vodafone-specific procedures
     *
     * Original code: 7 procedures for 15-min aggregations
     * + 8 additional procedures for hourly aggregations (at 45th minute)
     */
    private void executeVodafoneProcedures(String quarterMinute, String hourBefore) throws SQLException {
        System.out.println("  Executing Vodafone TWAMP procedures...");

        // 15-minute aggregation procedures
        String[] procedures15Min = {
            "NORTHI_LOADER.P_TWAMP_PERF(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_TWAMP_PERF_SUB_REGION(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_TWAMP_PERF_MAIN_REGION(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_TWAMP_PERF_ILCE(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_TWAMP_PERF_CITY(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_TWAMP_PERF_NW(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_TWAMP_PERF_15MIN(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))"
        };

        for (String procedure : procedures15Min) {
            executeProcedure(procedure);
        }

        // Hourly aggregations - execute at 45th minute
        if (quarterMinute.endsWith("45")) {
            System.out.println("  45th minute detected - executing hourly aggregations");

            String[] proceduresHourly = {
                "NORTHI_LOADER.P_TWAMP_PERF_H(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF_CITY(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF_REGION(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF_VENDOR(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF_NW(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF_15MIN(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))",
                "NORTHI_LOADER.P_NNI_PERF_H(TO_DATE('" + hourBefore + "','YYYYMMDDHH24'))"
            };

            for (String procedure : proceduresHourly) {
                executeProcedure(procedure);
            }
        }
    }

    /**
     * Execute KKTC-Telsim-specific procedures
     */
    private void executeKktcTelsimProcedures(String quarterMinute) throws SQLException {
        System.out.println("  Executing KKTC-Telsim TWAMP procedures...");

        String[] procedures = {
            "NORTHI_LOADER.P_KKTC_TWAMP_PERF(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_KKTC_TWAMP_PERF_SUB_REGION(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_KKTC_TWAMP_PERF_NW(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))",
            "NORTHI_LOADER.P_KKTC_TWAMP_PERF_15MIN(TO_DATE('" + quarterMinute + "','YYYYMMDDHH24MI'))"
        };

        for (String procedure : procedures) {
            executeProcedure(procedure);
        }
    }

    /**
     * Execute a stored procedure
     */
    private void executeProcedure(String procedureCall) throws SQLException {
        System.out.println("    Executing: " + procedureCall);

        String sql = "{ call " + procedureCall + " }";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("    ✓ Completed");
        } catch (SQLException e) {
            System.err.println("    ✗ Failed: " + e.getMessage());
            throw e;
        }
    }

    // ==================== Time Calculation ====================

    /**
     * Calculate quarter minute timestamp
     *
     * Original code: parserDate() method
     * Maps current minute to 15-minute intervals: 15, 30, 45, 00
     *
     * Logic:
     * - 06-20 min -> previous hour :30
     * - 21-35 min -> previous hour :45
     * - 36-50 min -> current hour :00
     * - 51-59/00-05 min -> current hour :15
     */
    private String calculateQuarterMinute() {
        Calendar now = Calendar.getInstance();
        int minute = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR_OF_DAY);

        String today = new SimpleDateFormat("yyyyMMddHH").format(now.getTime());

        // Calculate one hour before
        Calendar beforeCal = Calendar.getInstance();
        beforeCal.add(Calendar.HOUR_OF_DAY, -1);
        String todayBeforeOneHour = new SimpleDateFormat("yyyyMMddHH").format(beforeCal.getTime());

        // Original parserDate logic
        if (minute >= 6 && minute < 21) {
            return todayBeforeOneHour + "30";
        } else if (minute >= 21 && minute < 36) {
            return todayBeforeOneHour + "45";
        } else if (minute >= 36 && minute < 51) {
            return today + "00";
        } else if (minute >= 51 || minute < 6) {
            return today + "15";
        }

        return today + "00";  // Fallback
    }

    /**
     * Calculate hour before current time
     * Format: YYYYMMDDHH
     */
    private String calculateHourBeforeNow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        return new SimpleDateFormat("yyyyMMddHH").format(cal.getTime());
    }

    // ==================== Helper Methods ====================

    /**
     * Get CSV files from directory
     */
    private List<File> getCSVFiles(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }
}
