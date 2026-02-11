package com.telecom.parser.engine;

import com.telecom.parser.handler.ParseHandler;
import com.telecom.parser.handler.ParseHandlerFactory;
import com.telecom.parser.loader.LoaderFactory;
import com.telecom.parser.model.ParseEngineRecord;
import com.telecom.parser.model.TableMetadata;
import com.telecom.parser.repository.MetadataRepository;
import com.telecom.parser.writer.CsvWriter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main Parse Engine
 * Implements the complete parse flow from Activity Diagram
 *
 * Flow Steps (from Activity Diagram):
 * 1. startEngine(ParseEngineRecord)
 * 2. preparePaths()
 * 3. fetchTables() [optional]
 * 4. getTables()
 * 5. preEngine() [optional]
 * 6. Main Parsing Phase [optional]
 *    - getNetworkNodesByBranchId()
 *    - Read XML files
 *    - Create thread pool
 *    - Loop: Create & Submit Handlers
 *    - Fork: Parallel handler execution
 *    - shutdownExecutorService()
 *    - closeAllStreams()
 * 7. postEngine() [optional]
 * 8. Save auto counter definitions [optional]
 * 9. Content Date Discovery [optional]
 * 10. Data Loading Phase
 *     - cleanDuplicateBeforeLoader() [optional]
 *     - Parallel load to database
 *     - cleanDuplicateAfterLoader() [optional]
 * 11. callProcedure() [optional]
 * 12. callAggregate() [optional]
 * 13. callExport() [optional]
 */
public class ParseEngine {

    private final MetadataRepository metadataRepository;
    private final Connection dbConnection;

    // Shared resources
    private ParseEngineRecord record;
    private CsvWriter csvWriter;
    private Map<String, TableMetadata> tables;
    private Map<String, Long> networkNodes;
    private Map<String, String> autoCounterDefine;

    public ParseEngine(MetadataRepository metadataRepository, Connection dbConnection) {
        this.metadataRepository = metadataRepository;
        this.dbConnection = dbConnection;
        this.autoCounterDefine = new ConcurrentHashMap<>();
    }

    /**
     * Main entry point - Start Engine
     * Activity Diagram: startEngine(ParseEngineRecord)
     *
     * Triggered by Transfer Module or Scheduler
     * Contains: flowId, paths, config
     */
    public void startEngine(ParseEngineRecord record) {
        this.record = record;

        System.out.println("=== Parse Engine Started ===");
        System.out.println("Flow ID: " + record.getFlowId());
        System.out.println("Flow Name: " + record.getFlowName());
        System.out.println("Vendor: " + record.getVendor());
        System.out.println("Technology: " + record.getTechnology());

        try {
            // Step 1: Prepare paths
            preparePaths();

            // Step 2: Fetch tables (optional)
            if (Boolean.TRUE.equals(record.getIsActiveFetchTables())) {
                fetchTables();
            }

            // Step 3: Get tables
            getTables();

            // Step 4: Pre-parse (optional)
            if (Boolean.TRUE.equals(record.getIsActivePreParse())) {
                preEngine();
            }

            // Step 5: Main parsing phase (optional)
            if (Boolean.TRUE.equals(record.getIsActiveOnParse())) {
                mainParsingPhase();
            }

            // Step 6: Post-parse (optional)
            if (Boolean.TRUE.equals(record.getIsActivePostParse())) {
                postEngine();
            }

            // Step 7: Auto counter (optional)
            if (Boolean.TRUE.equals(record.getIsActiveAutoCounter())) {
                saveAutoCounterDefinitions();
                autoCounterDefine.clear();
            }

            // Step 8: Discover content date (optional)
            if (Boolean.TRUE.equals(record.getIsActiveDiscoverContentDate())) {
                discoverContentDate();
            }

            // Step 9: Data loading phase
            dataLoadingPhase();

            // Step 10: Call procedure (optional)
            if (Boolean.TRUE.equals(record.getIsActiveCallProcedure())) {
                callProcedure();
            }

            // Step 11: Call aggregate (optional)
            if (Boolean.TRUE.equals(record.getIsActiveCallAggregate())) {
                callAggregate();
            }

            // Step 12: Call export (optional)
            if (Boolean.TRUE.equals(record.getIsActiveCallExport())) {
                callExport();
            }

            System.out.println("=== Parse Engine Completed Successfully ===");

        } catch (Exception e) {
            System.err.println("=== Parse Engine Failed ===");
            e.printStackTrace();
            throw new RuntimeException("Parse engine failed", e);
        }
    }

    /**
     * Activity Diagram: preparePaths()
     *
     * Create directories:
     * - /raw/ (input XML files)
     * - /result/ (parsed output)
     * - /error/ (failed parses)
     */
    private void preparePaths() {
        System.out.println("Preparing paths...");

        try {
            createDirectoryIfNotExists(record.getRawPath());
            createDirectoryIfNotExists(record.getResultPath());
            createDirectoryIfNotExists(record.getErrorPath());

            System.out.println("  Raw path: " + record.getRawPath());
            System.out.println("  Result path: " + record.getResultPath());
            System.out.println("  Error path: " + record.getErrorPath());

        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare paths", e);
        }
    }

    /**
     * Activity Diagram: fetchTables()
     *
     * Generate metadata tables from DB schema
     */
    private void fetchTables() {
        System.out.println("Fetching tables from database schema...");
        metadataRepository.fetchTables(record.getSchemaName());
    }

    /**
     * Activity Diagram: getTables()
     *
     * Load table metadata from repository
     */
    private void getTables() {
        System.out.println("Getting table metadata...");

        this.tables = metadataRepository.getTables(
            record.getVendor(),
            record.getTechnology(),
            record.getDataType()
        );

        System.out.println("  Loaded " + tables.size() + " table definitions");
    }

    /**
     * Activity Diagram: preEngine()
     *
     * Pre-processing tasks (vendor-specific)
     */
    protected void preEngine() {
        System.out.println("Running pre-engine tasks...");
        // Override in vendor-specific engine
        // Example: Data validation, file preparation
    }

    /**
     * Activity Diagram: Main Parsing Phase partition
     *
     * This is the core parsing logic with parallel execution
     */
    private void mainParsingPhase() {
        System.out.println("\n=== Main Parsing Phase ===");

        // Step 1: Get network nodes from DB
        System.out.println("Getting network nodes by branch ID...");
        this.networkNodes = metadataRepository.getNetworkNodesByBranchId(record.getBranchId());
        System.out.println("  Loaded " + networkNodes.size() + " network nodes");
        System.out.println("  Examples: eNodeB, gNodeB, RNC, BSC");

        // Step 2: Read XML files from /raw/
        System.out.println("\nReading XML files from " + record.getRawPath());
        List<File> xmlFiles = getXMLFiles(record.getRawPath());
        System.out.println("  Found " + xmlFiles.size() + " XML files");
        System.out.println("  File patterns: *_eNodeB_*.xml (4G PM), *_gNodeB_*.xml (5G PM), *_RNC_*.xml (3G PM)");

        if (xmlFiles.isEmpty()) {
            System.out.println("  No XML files to process, skipping parsing phase");
            return;
        }

        // Step 3: Create thread pool ExecutorService
        int threadPoolSize = record.getThreadPoolSize();
        System.out.println("\nCreating thread pool with " + threadPoolSize + " threads");
        System.out.println("  Default: 8 threads, Configurable per flow");
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        // Initialize CSV writer
        this.csvWriter = new CsvWriter(record.getResultPath());

        // Step 4: Loop - Create & Submit Handlers
        System.out.println("\n<<loop>> Create & Submit Handlers");
        System.out.println("[Setup] Initialize XML file list: " + xmlFiles.size() + " files");

        // Loop structure from Activity Diagram:
        // [Setup] Initialize XML file list
        Iterator<File> iterator = xmlFiles.iterator();

        int submittedCount = 0;

        // [Test] next XML file exists?
        while (iterator.hasNext()) { // Decision: next XML file exists?

            // [Body] - 4 steps
            // 1. Get next XML file
            File xmlFile = iterator.next();

            // 2. Determine parser type (based on filename/vendor)
            ParseHandler handler = ParseHandlerFactory.createHandler(
                xmlFile,
                record.getVendor(),
                record.getTechnology(),
                record.getDataType()
            );

            // Set handler dependencies
            handler.setNetworkNodes(networkNodes);
            handler.setTables(tables);
            handler.setCsvWriter(csvWriter);
            handler.setAutoCounterDefine(autoCounterDefine);
            handler.setAutoCounterEnabled(Boolean.TRUE.equals(record.getIsActiveAutoCounter()));

            // 3. Create ParseHandler(file, nodeIds) - already created above

            // 4. Submit handler to ExecutorService (non-blocking)
            executorService.submit(handler);
            submittedCount++;

            if (submittedCount % 10 == 0) {
                System.out.println("  [Test] next XML file exists? YES - Submitted " + submittedCount + " handlers");
            }
        }

        System.out.println("  [Test] next XML file exists? NO - All " + submittedCount + " handlers submitted");
        System.out.println("\n*** PARALLELIZATION POINT ***");
        System.out.println("All handlers now run concurrently in thread pool");

        // Step 5: Fork - Parallel handler execution
        // (Implicit - happens automatically in ExecutorService)
        // Each handler thread executes:
        //   - run() - Parse XML file
        //   - preHandler()
        //   - Open XML file with SAX parser
        //   - Parse XML elements
        //   - Loop: Read measInfo section
        //     - Loop: Read measValue record
        //       - Extract metrics (RSRP, Throughput, etc.)
        //       - Map to table columns
        //       - Write to CSV buffer
        //       - Auto counter (if enabled)
        //   - postHandler()

        // Step 6: Synchronization - shutdownExecutorService()
        System.out.println("\n*** SYNCHRONIZATION POINT ***");
        System.out.println("Waiting for all handlers to complete parsing...");
        shutdownExecutorService(executorService);

        // Step 7: writer.closeAllStreams()
        System.out.println("\nClosing all CSV streams...");
        csvWriter.closeAllStreams();
        System.out.println("  Flush all CSV buffers");
        System.out.println("  Close file writers");

        System.out.println("\n=== Main Parsing Phase Completed ===");
    }

    /**
     * Activity Diagram: shutdownExecutorService()
     *
     * executor.shutdown()
     * await termination
     */
    private void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                System.err.println("Executor service did not terminate in time");
                executorService.shutdownNow();
            } else {
                System.out.println("  All handler threads completed successfully");
            }
        } catch (InterruptedException e) {
            System.err.println("Executor service interrupted");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Activity Diagram: postEngine()
     *
     * Post-processing tasks (vendor-specific)
     * - Aggregations
     * - Validations
     */
    protected void postEngine() {
        System.out.println("Running post-engine tasks...");
        // Override in vendor-specific engine
    }

    /**
     * Activity Diagram: Save auto counter definitions
     *
     * Store discovered metrics to metadata tables
     */
    private void saveAutoCounterDefinitions() {
        System.out.println("Saving auto counter definitions...");
        System.out.println("  Discovered " + autoCounterDefine.size() + " new counters");

        if (!autoCounterDefine.isEmpty()) {
            metadataRepository.saveAutoCounterDefinitions(autoCounterDefine);
        }
    }

    /**
     * Activity Diagram: Content Date Discovery partition
     *
     * Parallel discover content dates from CSV files
     */
    private void discoverContentDate() {
        System.out.println("\n=== Content Date Discovery ===");
        System.out.println("Reading parsed CSV files from " + record.getResultPath());

        List<File> csvFiles = getCSVFiles(record.getResultPath());
        System.out.println("  Found " + csvFiles.size() + " CSV files");

        if (csvFiles.isEmpty()) {
            System.out.println("  No CSV files to analyze");
            return;
        }

        // Parallel Discover Content Dates (Fork)
        ExecutorService executorService = Executors.newFixedThreadPool(record.getThreadPoolSize());

        System.out.println("\n<<fork>> Parallel - Discover Content Dates");

        List<DateRange> dateRanges = new ArrayList<>();

        for (File csvFile : csvFiles) {
            executorService.submit(() -> {
                // Analyze CSV file - Extract min/max dates
                DateRange range = analyzeCsvFileDates(csvFile);
                synchronized (dateRanges) {
                    dateRanges.add(range);
                }
                System.out.println("  Analyzed: " + csvFile.getName());
            });
        }

        shutdownExecutorService(executorService);

        // Aggregate date ranges
        System.out.println("\nAggregating date ranges...");
        DateRange totalRange = aggregateDateRanges(dateRanges);

        // Print discovered dates
        System.out.println("\nDiscovered Date Range:");
        System.out.println("  Min: " + totalRange.getMinDate());
        System.out.println("  Max: " + totalRange.getMaxDate());
    }

    /**
     * Activity Diagram: Data Loading Phase partition
     */
    private void dataLoadingPhase() {
        System.out.println("\n=== Data Loading Phase ===");

        // Step 1: Clean duplicate before loader (optional)
        if (Boolean.TRUE.equals(record.getIsActiveCleanDuplicateBefore())) {
            cleanDuplicateBeforeLoader();
        }

        // Step 2: Read CSV files from /result/
        System.out.println("Reading CSV files from " + record.getResultPath());
        List<File> csvFiles = getCSVFiles(record.getResultPath());
        System.out.println("  Found " + csvFiles.size() + " CSV files to load");

        if (csvFiles.isEmpty()) {
            System.out.println("  No CSV files to load");
            return;
        }

        // Step 3: Parallel Load to Database (Fork)
        System.out.println("\n<<fork>> Parallel - Load to Database");

        ExecutorService executorService = Executors.newFixedThreadPool(record.getThreadPoolSize());

        for (File csvFile : csvFiles) {
            executorService.submit(() -> {
                try {
                    // LoaderFactory.load(csv)
                    int rowsLoaded = LoaderFactory.load(csvFile, dbConnection, record.getSchemaName());
                    System.out.println("  Loaded " + csvFile.getName() + ": " + rowsLoaded + " rows");
                } catch (Exception e) {
                    System.err.println("  Failed to load " + csvFile.getName() + ": " + e.getMessage());
                }
            });
        }

        // Synchronization
        shutdownExecutorService(executorService);

        // Step 4: Clean duplicate after loader (optional)
        if (Boolean.TRUE.equals(record.getIsActiveCleanDuplicateAfter())) {
            cleanDuplicateAfterLoader();
        }

        System.out.println("\n=== Data Loading Phase Completed ===");
    }

    /**
     * Activity Diagram: cleanDuplicateBeforeLoader()
     *
     * Remove duplicate records before DB load
     */
    private void cleanDuplicateBeforeLoader() {
        System.out.println("Cleaning duplicates before load...");
        // TODO: Implement duplicate removal logic
    }

    /**
     * Activity Diagram: cleanDuplicateAfterLoader()
     *
     * Remove duplicates after load (DB constraints)
     */
    private void cleanDuplicateAfterLoader() {
        System.out.println("Cleaning duplicates after load...");
        // TODO: Implement duplicate removal using DB constraints
    }

    /**
     * Activity Diagram: callProcedure()
     *
     * Execute stored procedures for data transformations
     */
    private void callProcedure() {
        System.out.println("Calling stored procedures...");
        // TODO: Implement procedure calls
    }

    /**
     * Activity Diagram: callAggregate()
     *
     * Run aggregation queries (hourly, daily KPIs)
     */
    private void callAggregate() {
        System.out.println("Running aggregation queries...");
        System.out.println("  Hourly KPIs");
        System.out.println("  Daily KPIs");
        // TODO: Implement aggregation logic
    }

    /**
     * Activity Diagram: callExport()
     *
     * Export processed data to external systems
     */
    private void callExport() {
        System.out.println("Exporting data to external systems...");
        // TODO: Implement export logic
    }

    // ==================== Helper Methods ====================

    private void createDirectoryIfNotExists(String path) throws Exception {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    private List<File> getXMLFiles(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    private List<File> getCSVFiles(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    private DateRange analyzeCsvFileDates(File csvFile) {
        // TODO: Implement CSV date analysis
        return new DateRange();
    }

    private DateRange aggregateDateRanges(List<DateRange> ranges) {
        // TODO: Implement date range aggregation
        return new DateRange();
    }

    // Inner class for date range
    private static class DateRange {
        private String minDate;
        private String maxDate;

        public String getMinDate() {
            return minDate;
        }

        public String getMaxDate() {
            return maxDate;
        }
    }
}
