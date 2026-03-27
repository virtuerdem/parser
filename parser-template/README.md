# Parser Module Template

Telecom XML Parse Engine - Template implementation based on Activity Diagram

## 📋 Overview

This is a **template** implementation of the Parse Engine based on the Parser Activity Diagram (`Parser_Activity_Diagram.xml`).

The engine processes telecom XML files (PM/CM/CONF data) from network elements (eNodeB, gNodeB, RNC, BSC) and loads them into a database with parallel processing capabilities.

## 🏗️ Architecture

### Based on Activity Diagram Flow

```
startEngine()
    ↓
preparePaths()
    ↓
[isActiveFetchTables?] → fetchTables()
    ↓
getTables()
    ↓
[isActivePreParse?] → preEngine()
    ↓
[isActiveOnParse?] → Main Parsing Phase (PARALLEL)
    ↓
[isActivePostParse?] → postEngine()
    ↓
[isActiveAutoCounter?] → Save auto counter definitions
    ↓
[isActiveDiscoverContentDate?] → Content Date Discovery (PARALLEL)
    ↓
Data Loading Phase (PARALLEL)
    ↓
[isActiveCallProcedure?] → callProcedure()
    ↓
[isActiveCallAggregate?] → callAggregate()
    ↓
[isActiveCallExport?] → callExport()
    ↓
END
```

### Parallel Processing Points

1. **XML Parsing**: Thread pool parses multiple XML files concurrently
2. **Content Date Discovery**: Parallel CSV file date analysis
3. **Database Loading**: Parallel CSV to DB loading

## 📦 Package Structure

```
com.telecom.parser/
├── engine/
│   └── ParseEngine.java              # Main engine (Activity Diagram implementation)
├── handler/
│   ├── ParseHandler.java             # Base abstract handler (SAX parser)
│   ├── ParseHandlerFactory.java      # Handler factory
│   └── huawei/
│       ├── HwEnbPmXmlParseHandler.java   # Huawei 4G PM
│       ├── HwGnbPmXmlParseHandler.java   # Huawei 5G PM
│       └── HwRncCmXmlParseHandler.java   # Huawei 3G CM
├── model/
│   ├── ParseEngineRecord.java        # Configuration model
│   └── TableMetadata.java            # Table metadata model
├── repository/
│   └── MetadataRepository.java       # Repository interface
├── writer/
│   └── CsvWriter.java                # CSV writer (thread-safe)
├── loader/
│   └── LoaderFactory.java            # Database loader factory
└── util/
```

## 🚀 Quick Start

### 1. Create Configuration

```java
ParseEngineRecord record = new ParseEngineRecord();

// Flow identification
record.setFlowId(1001L);
record.setFlowName("HW_ENB_PM_FLOW");
record.setVendor("HUAWEI");
record.setTechnology("4G");
record.setDataType("PM");

// Paths
record.setRawPath("/data/parser/raw");
record.setResultPath("/data/parser/result");
record.setErrorPath("/data/parser/error");

// Database
record.setBranchId(1L);
record.setSchemaName("telecom_data");

// Thread pool
record.setThreadPoolSize(8);

// Feature flags (from Activity Diagram)
record.setIsActiveOnParse(true);
record.setIsActiveAutoCounter(true);
record.setIsActiveDiscoverContentDate(true);
record.setIsActiveCleanDuplicateAfter(true);
record.setIsActiveCallAggregate(true);
```

### 2. Initialize Dependencies

```java
// Create repository implementation
MetadataRepository metadataRepository = new MetadataRepositoryImpl();

// Get database connection
Connection dbConnection = dataSource.getConnection();

// Create parse engine
ParseEngine parseEngine = new ParseEngine(metadataRepository, dbConnection);
```

### 3. Start Parsing

```java
// This follows the complete Activity Diagram flow
parseEngine.startEngine(record);
```

## 🔧 Implementation Guide

### Adding New Vendor Handler

1. **Create vendor package**: `com.telecom.parser.handler.ericsson`

2. **Extend ParseHandler**:

```java
public class EricEnbPmXmlParseHandler extends ParseHandler {

    public EricEnbPmXmlParseHandler(File xmlFile) {
        super(xmlFile);
    }

    @Override
    protected void startMeasInfo(Attributes attributes) {
        // Ericsson-specific measInfo handling
    }

    @Override
    protected void endMeasValue() {
        // Map Ericsson metrics to CSV
        String csvRow = mapEricssonMetrics();
        writeToCsv("eric_enb_pm_metrics", csvRow);
    }

    // Implement other abstract methods...
}
```

3. **Update ParseHandlerFactory**:

```java
private static ParseHandler createEricssonHandler(...) {
    if (fileName.contains("enodeb") && "PM".equals(dataType)) {
        return new EricEnbPmXmlParseHandler(xmlFile);
    }
    // ...
}
```

### Implementing Repository

```java
public class MetadataRepositoryImpl implements MetadataRepository {

    @Override
    public Map<String, TableMetadata> getTables(
        String vendor, String technology, String dataType) {

        // Query database for table definitions
        String sql = "SELECT * FROM metadata_tables " +
                     "WHERE vendor = ? AND technology = ? AND data_type = ?";

        // Execute query and build TableMetadata map
        // ...
    }

    @Override
    public Map<String, Long> getNetworkNodesByBranchId(Long branchId) {
        // Query: SELECT node_name, node_id FROM network_nodes WHERE branch_id = ?
        // Return Map<nodeName, nodeId>
    }

    // Implement other methods...
}
```

### Custom Pre/Post Processing

Extend `ParseEngine` for vendor-specific operations:

```java
public class HuaweiParseEngine extends ParseEngine {

    @Override
    protected void preEngine() {
        System.out.println("Huawei pre-processing...");
        // Custom Huawei preparation
    }

    @Override
    protected void postEngine() {
        System.out.println("Huawei post-processing...");
        // Custom Huawei cleanup/aggregation
    }
}
```

## 📊 Activity Diagram Mapping

| Activity Diagram Element | Java Implementation |
|-------------------------|---------------------|
| `startEngine(ParseEngineRecord)` | `ParseEngine.startEngine()` |
| `preparePaths()` | `ParseEngine.preparePaths()` |
| `getTables()` | `MetadataRepository.getTables()` |
| `Repository.getNetworkNodesByBranchId()` | `MetadataRepository.getNetworkNodesByBranchId()` |
| `Create Thread Pool ExecutorService` | `Executors.newFixedThreadPool(size)` |
| **Loop: Create & Submit Handlers** | **While loop with iterator** |
| `Determine parser type` | `ParseHandlerFactory.createHandler()` |
| `Submit to ExecutorService` | `executorService.submit(handler)` |
| **Fork: Handler Threads** | **ExecutorService parallel execution** |
| `run() - Parse XML file` | `ParseHandler.run()` |
| `preHandler()` | `ParseHandler.preHandler()` |
| `SAX Parser` | `SAXParser.parse()` |
| `Read measInfo section` | `startMeasInfo()` / `endMeasInfo()` |
| `Read measValue record` | `startMeasValue()` / `endMeasValue()` |
| `Extract metrics` | `processMetric()` |
| `Write to CSV buffer` | `CsvWriter.write()` |
| `shutdownExecutorService()` | `executorService.shutdown()` + `awaitTermination()` |
| `writer.closeAllStreams()` | `CsvWriter.closeAllStreams()` |
| `LoaderFactory.load(csv)` | `LoaderFactory.load()` |

## 🎯 Loop Structure (from Activity Diagram)

The Activity Diagram shows a **Loop Node** with Setup/Test/Body sections:

```java
// [Setup] Initialize XML file list
List<File> xmlFiles = getXMLFiles(rawPath);
Iterator<File> iterator = xmlFiles.iterator();

// [Test] next XML file exists?
while (iterator.hasNext()) {  // Decision node

    // [Body] - 4 steps
    // 1. Get next XML file
    File xmlFile = iterator.next();

    // 2. Determine parser type
    ParseHandler handler = ParseHandlerFactory.createHandler(...);

    // 3. Create ParseHandler(file, nodeIds)
    handler.setNetworkNodes(networkNodes);
    handler.setTables(tables);
    handler.setCsvWriter(csvWriter);

    // 4. Submit to ExecutorService (non-blocking)
    executorService.submit(handler);
}
```

## ⚡ Performance

Based on Activity Diagram legend:

- **Sequential**: 150 files × 2 min = 300 min (5 hours)
- **Parallel (8 threads)**: 300 / 8 ≈ 38 min
- **Speedup**: **7.9x faster**

### Thread Pools

1. **Parse handler pool**: 8 threads (configurable)
2. **Content date pool**: 8 threads
3. **Loader pool**: 8 threads

### Memory Efficiency

- **SAX Parser**: Stream-based, low memory footprint
- **CSV Buffering**: Write-through buffering, no large memory accumulation
- **Thread-safe Writers**: Concurrent access without blocking

## 🔑 Configuration Flags

All flags from Activity Diagram decision nodes:

| Flag | Description | Default |
|------|-------------|---------|
| `isActiveFetchTables` | Generate metadata from DB schema | false |
| `isActivePreParse` | Run vendor-specific pre-processing | false |
| `isActiveOnParse` | Execute main parsing phase | true |
| `isActivePostParse` | Run vendor-specific post-processing | false |
| `isActiveAutoCounter` | Auto-discover and save counter definitions | false |
| `isActiveDiscoverContentDate` | Discover date ranges from CSV files | false |
| `isActiveCleanDuplicateBefore` | Clean duplicates before DB load | false |
| `isActiveCleanDuplicateAfter` | Clean duplicates after DB load | false |
| `isActiveCallProcedure` | Execute stored procedures | false |
| `isActiveCallAggregate` | Run aggregation queries | false |
| `isActiveCallExport` | Export to external systems | false |

## 📝 TODO List

This is a **template**. You need to implement:

### High Priority
- [ ] `MetadataRepository` implementation (database queries)
- [ ] `LoaderFactory` database-specific loaders (PostgreSQL COPY, Oracle, etc.)
- [ ] Complete `HwEnbPmXmlParseHandler` mapping logic
- [ ] Error handling and file movement to error directory

### Medium Priority
- [ ] `HwGnbPmXmlParseHandler` 5G-specific logic
- [ ] `HwRncCmXmlParseHandler` CM-specific logic
- [ ] Content date discovery implementation
- [ ] Duplicate cleaning logic

### Low Priority
- [ ] Ericsson/Nokia/ZTE handlers
- [ ] Procedure/Aggregate/Export implementations
- [ ] Performance monitoring and metrics
- [ ] Unit tests

## 📚 Related Documents

- **Activity Diagram**: `sparx/Parser_Activity_Diagram.xml` (Enterprise Architect)
- **PlantUML Source**: `ParseFlow_ActivityDiagram.puml`
- **Presentation**: `Parser_Activity_Diagram_Presentation.md`

## 🤝 Contributing

When adding new features:

1. Check if it matches Activity Diagram flow
2. Add corresponding decision flag if optional
3. Update this README
4. Add unit tests
5. Document vendor-specific behavior

## 📄 License

Internal use - Telecom Parser Module

---

**Template Version**: 1.0
**Based on**: Parser_Activity_Diagram.xml (2026-02-03)
**Last Updated**: 2026-02-11
