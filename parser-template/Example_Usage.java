import com.telecom.parser.engine.ParseEngine;
import com.telecom.parser.model.ParseEngineRecord;
import com.telecom.parser.repository.MetadataRepository;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Example Usage of Parse Engine
 *
 * This demonstrates how to use the Parse Engine template
 * following the Activity Diagram flow
 */
public class Example_Usage {

    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("Parse Engine Example Usage");
            System.out.println("========================================\n");

            // ========== Step 1: Create Configuration ==========
            System.out.println("Step 1: Creating configuration...\n");

            ParseEngineRecord record = new ParseEngineRecord();

            // Flow identification
            record.setFlowId(1001L);
            record.setFlowName("HW_ENB_PM_DAILY_FLOW");
            record.setVendor("HUAWEI");
            record.setTechnology("4G");
            record.setDataType("PM");

            // Paths (prepare these directories beforehand)
            record.setRawPath("/data/parser/flows/hw_enb_pm/raw");
            record.setResultPath("/data/parser/flows/hw_enb_pm/result");
            record.setErrorPath("/data/parser/flows/hw_enb_pm/error");

            // Database configuration
            record.setBranchId(1L);  // Network branch ID
            record.setSchemaName("telecom_pm");

            // Thread pool configuration
            record.setThreadPoolSize(8);  // 8 parallel threads
            record.setMaxMemoryMB(4096);  // 4GB max memory

            // Feature flags (Activity Diagram decision nodes)
            record.setIsActiveFetchTables(false);            // Skip metadata generation
            record.setIsActivePreParse(true);                // Run pre-processing
            record.setIsActiveOnParse(true);                 // Main parsing phase (required)
            record.setIsActivePostParse(true);               // Run post-processing
            record.setIsActiveAutoCounter(true);             // Auto-discover counters
            record.setIsActiveDiscoverContentDate(true);     // Discover date ranges
            record.setIsActiveCleanDuplicateBefore(false);   // Skip pre-load dedup
            record.setIsActiveCleanDuplicateAfter(true);     // Clean after load
            record.setIsActiveCallProcedure(false);          // Skip procedures
            record.setIsActiveCallAggregate(true);           // Run aggregations
            record.setIsActiveCallExport(false);             // Skip export

            System.out.println("Configuration created:");
            System.out.println("  Flow: " + record.getFlowName());
            System.out.println("  Vendor: " + record.getVendor());
            System.out.println("  Technology: " + record.getTechnology());
            System.out.println("  Thread Pool: " + record.getThreadPoolSize() + " threads");
            System.out.println();

            // ========== Step 2: Initialize Dependencies ==========
            System.out.println("Step 2: Initializing dependencies...\n");

            // Database connection
            Connection dbConnection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/telecom_db",
                "username",
                "password"
            );
            System.out.println("  Database connection established");

            // Metadata repository (you need to implement this)
            MetadataRepository metadataRepository = new MetadataRepositoryImpl();
            System.out.println("  Metadata repository initialized");
            System.out.println();

            // ========== Step 3: Create Parse Engine ==========
            System.out.println("Step 3: Creating Parse Engine...\n");

            ParseEngine parseEngine = new ParseEngine(metadataRepository, dbConnection);
            System.out.println("  Parse Engine created");
            System.out.println();

            // ========== Step 4: Start Engine ==========
            System.out.println("Step 4: Starting Parse Engine...\n");
            System.out.println("This will execute the complete Activity Diagram flow:");
            System.out.println("  1. preparePaths()");
            System.out.println("  2. getTables()");
            System.out.println("  3. preEngine()");
            System.out.println("  4. Main Parsing Phase (PARALLEL)");
            System.out.println("     - getNetworkNodesByBranchId()");
            System.out.println("     - Read XML files");
            System.out.println("     - Create thread pool");
            System.out.println("     - Loop: Create & Submit Handlers");
            System.out.println("     - Fork: Parallel XML parsing");
            System.out.println("     - Join: Wait for all handlers");
            System.out.println("     - Close all CSV streams");
            System.out.println("  5. postEngine()");
            System.out.println("  6. Save auto counter definitions");
            System.out.println("  7. Content Date Discovery (PARALLEL)");
            System.out.println("  8. Data Loading Phase (PARALLEL)");
            System.out.println("  9. callAggregate()");
            System.out.println();

            // This is the main call - follows Activity Diagram
            parseEngine.startEngine(record);

            System.out.println();
            System.out.println("========================================");
            System.out.println("Parse Engine completed successfully!");
            System.out.println("========================================");

            // Clean up
            dbConnection.close();

        } catch (Exception e) {
            System.err.println("Parse Engine failed with error:");
            e.printStackTrace();
        }
    }

    // ========== Example: Vendor-Specific Engine ==========

    /**
     * Example of extending ParseEngine for vendor-specific behavior
     */
    public static class HuaweiParseEngine extends ParseEngine {

        public HuaweiParseEngine(MetadataRepository metadataRepository, Connection dbConnection) {
            super(metadataRepository, dbConnection);
        }

        @Override
        protected void preEngine() {
            System.out.println("=== Huawei Pre-Processing ===");
            System.out.println("  - Validating Huawei XML format");
            System.out.println("  - Checking vendor-specific requirements");
            System.out.println("  - Preparing Huawei counter mappings");
        }

        @Override
        protected void postEngine() {
            System.out.println("=== Huawei Post-Processing ===");
            System.out.println("  - Running Huawei-specific validations");
            System.out.println("  - Generating vendor reports");
            System.out.println("  - Cleaning up temporary files");
        }
    }

    // ========== Example: Scheduled Flow ==========

    /**
     * Example of running parser on a schedule
     */
    public static void scheduledParseFlow() {
        // This would be called by a scheduler (e.g., Quartz, Spring @Scheduled)

        ParseEngineRecord record = createDailyFlowConfig();

        MetadataRepository repository = new MetadataRepositoryImpl();
        Connection connection = getDatabaseConnection();

        ParseEngine engine = new ParseEngine(repository, connection);

        try {
            engine.startEngine(record);
            System.out.println("Scheduled parse completed at: " + java.time.LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Scheduled parse failed: " + e.getMessage());
            // Send alert/notification
        }
    }

    // ========== Example: Multiple Flows in Parallel ==========

    /**
     * Example of running multiple flows in parallel
     */
    public static void parallelFlows() throws Exception {
        // Run multiple vendor/technology combinations in parallel

        ExecutorService flowExecutor = Executors.newFixedThreadPool(4);

        // Flow 1: Huawei 4G PM
        flowExecutor.submit(() -> runFlow("HUAWEI", "4G", "PM"));

        // Flow 2: Huawei 5G PM
        flowExecutor.submit(() -> runFlow("HUAWEI", "5G", "PM"));

        // Flow 3: Ericsson 4G PM
        flowExecutor.submit(() -> runFlow("ERICSSON", "4G", "PM"));

        // Flow 4: Nokia 5G PM
        flowExecutor.submit(() -> runFlow("NOKIA", "5G", "PM"));

        flowExecutor.shutdown();
        flowExecutor.awaitTermination(2, TimeUnit.HOURS);

        System.out.println("All parallel flows completed");
    }

    // ========== Helper Methods ==========

    private static void runFlow(String vendor, String technology, String dataType) {
        try {
            ParseEngineRecord record = new ParseEngineRecord();
            record.setVendor(vendor);
            record.setTechnology(technology);
            record.setDataType(dataType);
            // ... set other config

            MetadataRepository repository = new MetadataRepositoryImpl();
            Connection connection = getDatabaseConnection();

            ParseEngine engine = new ParseEngine(repository, connection);
            engine.startEngine(record);

            System.out.println("Flow completed: " + vendor + " " + technology + " " + dataType);

        } catch (Exception e) {
            System.err.println("Flow failed: " + vendor + " " + technology + " " + dataType);
            e.printStackTrace();
        }
    }

    private static ParseEngineRecord createDailyFlowConfig() {
        // Create configuration for daily scheduled run
        ParseEngineRecord record = new ParseEngineRecord();
        // ... configure
        return record;
    }

    private static Connection getDatabaseConnection() {
        // Get connection from pool or create new
        try {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/telecom_db", "user", "pass");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get DB connection", e);
        }
    }

    // ========== Stub Implementation ==========

    /**
     * Stub implementation of MetadataRepository for example
     * You need to implement this properly
     */
    static class MetadataRepositoryImpl implements MetadataRepository {

        @Override
        public void fetchTables(String schemaName) {
            System.out.println("[Repository] fetchTables() - TODO: Implement");
        }

        @Override
        public Map<String, TableMetadata> getTables(String vendor, String technology, String dataType) {
            System.out.println("[Repository] getTables() - TODO: Implement");
            // Return stub data
            return new HashMap<>();
        }

        @Override
        public Map<String, Long> getNetworkNodesByBranchId(Long branchId) {
            System.out.println("[Repository] getNetworkNodesByBranchId() - TODO: Implement");
            // Return stub data
            Map<String, Long> nodes = new HashMap<>();
            nodes.put("eNodeB001", 1001L);
            nodes.put("eNodeB002", 1002L);
            nodes.put("gNodeB001", 2001L);
            return nodes;
        }

        @Override
        public void saveAutoCounterDefinitions(Map<String, String> counterDefinitions) {
            System.out.println("[Repository] saveAutoCounterDefinitions() - TODO: Implement");
            System.out.println("  Saving " + counterDefinitions.size() + " counter definitions");
        }
    }
}
