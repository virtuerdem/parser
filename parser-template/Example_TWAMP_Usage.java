import com.telecom.parser.engine.twamp.TwampParseEngine;
import com.telecom.parser.model.ParseEngineRecord;
import com.telecom.parser.repository.MetadataRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Example Usage: TWAMP Parser
 *
 * Demonstrates how to use the TWAMP parser engine adapted from old_etl_codes.
 *
 * Original Files:
 * - old_etl_codes/Systems/ParserEngine_TWAMP.java
 * - old_etl_codes/Parsers/TwampNewCsvFileHandler.java
 *
 * New Template Files:
 * - parser-template/src/main/java/com/telecom/parser/handler/CsvParseHandler.java
 * - parser-template/src/main/java/com/telecom/parser/handler/twamp/TwampCsvParseHandler.java
 * - parser-template/src/main/java/com/telecom/parser/engine/twamp/TwampParseEngine.java
 */
public class Example_TWAMP_Usage {

    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("TWAMP Parser Example Usage");
            System.out.println("========================================\n");

            // ========== Step 1: Create TWAMP Configuration ==========
            System.out.println("Step 1: Creating TWAMP configuration...\n");

            ParseEngineRecord record = new ParseEngineRecord();

            // Flow identification
            record.setFlowId(2001L);
            record.setFlowName("TWAMP_PM_VODAFONE");
            record.setVendor("OTHER");  // TWAMP is not vendor-specific
            record.setTechnology("TWAMP");  // Two-Way Active Measurement Protocol
            record.setDataType("PM");  // Performance Management

            // Paths
            record.setRawPath("/data/parser/flows/twamp_pm/raw");
            record.setResultPath("/data/parser/flows/twamp_pm/result");
            record.setErrorPath("/data/parser/flows/twamp_pm/error");

            // Database configuration
            record.setBranchId(1L);
            record.setSchemaName("northi_pm");  // TWAMP uses NORTHI_LOADER schema

            // Thread pool configuration
            record.setThreadPoolSize(8);  // 8 parallel CSV parsers

            // Feature flags for TWAMP
            record.setIsActiveFetchTables(false);            // Tables already exist
            record.setIsActivePreParse(true);                // TWAMP pre-processing
            record.setIsActiveOnParse(true);                 // CSV parsing (required)
            record.setIsActivePostParse(true);               // TWAMP post-processing
            record.setIsActiveAutoCounter(false);            // TWAMP has fixed counters
            record.setIsActiveDiscoverContentDate(false);    // Not needed for TWAMP
            record.setIsActiveCleanDuplicateBefore(false);   // Skip
            record.setIsActiveCleanDuplicateAfter(true);     // Clean after DB load
            record.setIsActiveCallProcedure(true);           // TWAMP aggregations (required)
            record.setIsActiveCallAggregate(false);          // Procedures handle this
            record.setIsActiveCallExport(false);             // Skip

            // TWAMP-specific configuration
            Map<String, Object> customConfig = new HashMap<>();
            customConfig.put("operatorName", "VODAFONE");  // or "KKTC-TELSIM"
            record.setCustomConfig(customConfig);

            System.out.println("Configuration created:");
            System.out.println("  Flow: " + record.getFlowName());
            System.out.println("  Technology: " + record.getTechnology());
            System.out.println("  Operator: " + customConfig.get("operatorName"));
            System.out.println("  Thread Pool: " + record.getThreadPoolSize() + " threads");
            System.out.println();

            // ========== Step 2: Initialize Dependencies ==========
            System.out.println("Step 2: Initializing dependencies...\n");

            // Database connection
            Connection dbConnection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:orcl",  // TWAMP uses Oracle
                "northi_loader",
                "password"
            );
            System.out.println("  Oracle database connection established");

            // Metadata repository
            MetadataRepository metadataRepository = new MetadataRepositoryImpl();
            System.out.println("  Metadata repository initialized");
            System.out.println();

            // ========== Step 3: Create TWAMP Parse Engine ==========
            System.out.println("Step 3: Creating TWAMP Parse Engine...\n");

            TwampParseEngine parseEngine = new TwampParseEngine(metadataRepository, dbConnection);
            System.out.println("  TWAMP Parse Engine created");
            System.out.println();

            // ========== Step 4: Start Engine ==========
            System.out.println("Step 4: Starting TWAMP Parse Engine...\n");
            System.out.println("This will execute:");
            System.out.println("  1. preparePaths()");
            System.out.println("  2. getTables()");
            System.out.println("  3. preEngine() - TWAMP preparation");
            System.out.println("  4. CSV Parsing Phase (PARALLEL)");
            System.out.println("     - Read CSV files from /raw/");
            System.out.println("     - Create TWAMP CSV handlers");
            System.out.println("     - Parse CSV with header mapping");
            System.out.println("     - Format TimeGroup dates");
            System.out.println("     - Write to output CSV");
            System.out.println("  5. postEngine() - TWAMP cleanup");
            System.out.println("  6. Data Loading Phase");
            System.out.println("  7. callProcedure() - TWAMP aggregations");
            System.out.println("     - P_TWAMP_PERF (15-min)");
            System.out.println("     - P_TWAMP_PERF_SUB_REGION");
            System.out.println("     - P_TWAMP_PERF_MAIN_REGION");
            System.out.println("     - P_TWAMP_PERF_ILCE");
            System.out.println("     - P_TWAMP_PERF_CITY");
            System.out.println("     - P_TWAMP_PERF_NW");
            System.out.println("     - P_TWAMP_PERF_15MIN");
            System.out.println("     - (+ Hourly at 45th minute)");
            System.out.println();

            // Execute TWAMP parser
            parseEngine.startEngine(record);

            System.out.println();
            System.out.println("========================================");
            System.out.println("TWAMP Parser completed successfully!");
            System.out.println("========================================");

            // Clean up
            dbConnection.close();

        } catch (Exception e) {
            System.err.println("TWAMP Parser failed with error:");
            e.printStackTrace();
        }
    }

    // ========== TWAMP-Specific Information ==========

    /**
     * TWAMP CSV File Structure
     *
     * Filename Format: prefix_TABLENAME_suffix.csv
     * Example: "vodafone_TWAMP_PERF_20260211.csv"
     *
     * CSV Structure:
     * - Row 1: Header (column names)
     * - Row 2+: Data values
     *
     * Key Columns:
     * - TimeGroup: Timestamp (format: "2026-02-11T15:30:00")
     * - Latency: Network latency in ms
     * - Jitter: Jitter in ms
     * - PacketLoss: Packet loss percentage
     * - ... (vendor-specific metrics)
     */

    /**
     * TWAMP Procedures (Vodafone)
     *
     * 15-Minute Aggregations:
     * - P_TWAMP_PERF: Main performance table
     * - P_TWAMP_PERF_SUB_REGION: Sub-region aggregation
     * - P_TWAMP_PERF_MAIN_REGION: Main region aggregation
     * - P_TWAMP_PERF_ILCE: District (ilce) aggregation
     * - P_TWAMP_PERF_CITY: City aggregation
     * - P_TWAMP_PERF_NW: Network aggregation
     * - P_TWAMP_PERF_15MIN: 15-minute summary
     *
     * Hourly Aggregations (executed at 45th minute):
     * - P_TWAMP_PERF_H: Hourly performance
     * - P_NNI_PERF: NNI (Network-to-Network Interface) performance
     * - P_NNI_PERF_CITY: NNI city aggregation
     * - P_NNI_PERF_REGION: NNI region aggregation
     * - P_NNI_PERF_VENDOR: NNI vendor aggregation
     * - P_NNI_PERF_NW: NNI network aggregation
     * - P_NNI_PERF_15MIN: NNI 15-minute summary
     * - P_NNI_PERF_H: NNI hourly summary
     */

    /**
     * TWAMP Procedures (KKTC-Telsim)
     *
     * 15-Minute Aggregations:
     * - P_KKTC_TWAMP_PERF
     * - P_KKTC_TWAMP_PERF_SUB_REGION
     * - P_KKTC_TWAMP_PERF_NW
     * - P_KKTC_TWAMP_PERF_15MIN
     */

    /**
     * Quarter Minute Calculation
     *
     * Maps current minute to 15-minute intervals:
     *
     * Current Minute  →  Calculated Timestamp
     * ----------------  -------------------
     * 00-05           →  Current Hour :15
     * 06-20           →  Previous Hour :30
     * 21-35           →  Previous Hour :45
     * 36-50           →  Current Hour :00
     * 51-59           →  Current Hour :15
     *
     * Example:
     * - Current time: 2026-02-11 15:27
     * - Quarter minute: 202602111445 (previous hour :45)
     */

    // ========== Comparison with Old Code ==========

    /**
     * Old vs New Code Mapping
     *
     * OLD (old_etl_codes):
     * - ParserEngine_TWAMP.setProperties() → (Not needed in new system)
     * - ParserEngine_TWAMP.prepareParser() → TwampParseEngine.mainParsingPhase()
     * - ParserEngine_TWAMP.loaderProcedures() → TwampParseEngine.callProcedure()
     * - TwampNewCsvFileHandler.onStartParseOperation() → TwampCsvParseHandler.preHandler()
     * - TwampNewCsvFileHandler.lineProgress() → TwampCsvParseHandler.lineProgress()
     * - TwampNewCsvFileHandler.onstopParseOperation() → TwampCsvParseHandler.postHandler()
     *
     * NEW (parser-template):
     * - Activity Diagram compliant
     * - Modern Java practices
     * - Better error handling
     * - More modular and extensible
     * - Thread-safe components
     */

    // ========== Stub Implementation ==========

    static class MetadataRepositoryImpl implements MetadataRepository {
        // ... (same as Example_Usage.java)
    }
}
