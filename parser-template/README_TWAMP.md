# TWAMP Parser - Implementation Guide

## 📋 Overview

This is the **TWAMP (Two-Way Active Measurement Protocol) Parser** implementation, adapted from `old_etl_codes` to the new template system.

TWAMP is a network protocol for measuring performance metrics:
- **Latency**: Round-trip time
- **Jitter**: Variation in latency
- **Packet Loss**: Percentage of lost packets

## 🔄 Code Migration

### Original Files (old_etl_codes)

```
old_etl_codes/
├── Systems/
│   └── ParserEngine_TWAMP.java          ❌ Old system
└── Parsers/
    └── TwampNewCsvFileHandler.java      ❌ Old system
```

### New Template Files

```
parser-template/src/main/java/com/telecom/parser/
├── handler/
│   ├── CsvParseHandler.java             ✅ Generic CSV base
│   └── twamp/
│       └── TwampCsvParseHandler.java    ✅ TWAMP-specific
└── engine/
    └── twamp/
        └── TwampParseEngine.java        ✅ TWAMP engine
```

## 🎯 Key Improvements

| Feature | Old Code | New Template |
|---------|----------|--------------|
| **Architecture** | Custom AbsParserEngine | Activity Diagram compliant |
| **CSV Parsing** | Manual string operations | OpenCSV library |
| **Threading** | ExecutorService (basic) | Enhanced with monitoring |
| **Error Handling** | System.err.println | Structured exception handling |
| **Configuration** | Hard-coded properties | ParseEngineRecord (configurable) |
| **Code Quality** | Legacy Java | Modern Java practices |
| **Documentation** | Minimal comments | Comprehensive JavaDoc |

## 📦 Components

### 1️⃣ CsvParseHandler (Base)

Generic CSV parser for any CSV file type.

**Features:**
- OpenCSV integration
- Configurable delimiter
- Line-by-line processing
- Header detection
- Data cleaning

**Usage:**
```java
public class MyCsvHandler extends CsvParseHandler {
    @Override
    protected void lineProgress(long lineIndex, String[] line) {
        // Process each CSV line
    }
}
```

### 2️⃣ TwampCsvParseHandler

TWAMP-specific CSV parser.

**Features:**
- Table name extraction from filename
- Header mapping to database schema
- TimeGroup date formatting
- Column ordering based on table metadata
- Auto counter collection

**Key Methods:**
```java
preHandler()           // Extract table name, load metadata
lineProgress()         // Process header (row 1) and data (row 2+)
processHeaderRow()     // Store column positions
processDataRow()       // Format date, map columns, write CSV
postHandler()          // Cleanup
```

**TimeGroup Formatting:**
```
Input:  "2026-02-11T15:30:00"
Output: "202602111530"

Steps:
1. Remove 'T'
2. Remove dashes and colons
3. Take first 12 characters
```

### 3️⃣ TwampParseEngine

TWAMP engine with aggregation procedures.

**Features:**
- Parallel CSV parsing
- Operator-specific logic (Vodafone vs KKTC-Telsim)
- 15-minute aggregation procedures
- Hourly aggregations (at 45th minute)
- Quarter minute calculation

**Workflow:**
```
1. preEngine()         → TWAMP preparation
2. mainParsingPhase()  → Parallel CSV parsing
3. postEngine()        → TWAMP cleanup
4. callProcedure()     → Aggregation procedures
```

## 🚀 Usage Example

### Basic Usage

```java
// 1. Create configuration
ParseEngineRecord record = new ParseEngineRecord();
record.setFlowName("TWAMP_PM_VODAFONE");
record.setTechnology("TWAMP");
record.setDataType("PM");

// TWAMP-specific config
Map<String, Object> customConfig = new HashMap<>();
customConfig.put("operatorName", "VODAFONE");  // or "KKTC-TELSIM"
record.setCustomConfig(customConfig);

// 2. Create engine
TwampParseEngine engine = new TwampParseEngine(repository, connection);

// 3. Start parsing
engine.startEngine(record);  // Executes full Activity Diagram flow
```

See `Example_TWAMP_Usage.java` for complete example.

## 📊 CSV File Structure

### Input File Format

**Filename:** `prefix_TABLENAME_suffix.csv`

Example: `vodafone_TWAMP_PERF_20260211.csv`

**CSV Structure:**
```csv
TimeGroup,NodeName,Latency,Jitter,PacketLoss,...
2026-02-11T15:00:00,NODE001,25.5,2.1,0.01,...
2026-02-11T15:15:00,NODE001,26.3,2.3,0.02,...
...
```

### Output File Format

**Filename:** `TABLENAME-YYYYMMDDHHMMSS.csv`

Example: `TWAMP_PERF-20260211150000.csv`

**Delimiter:** `|` (pipe)

**Format:**
```
202602111500|NODE001|25.5|2.1|0.01|...
202602111515|NODE001|26.3|2.3|0.02|...
```

## 🔧 Configuration

### Feature Flags

```java
record.setIsActivePreParse(true);         // TWAMP preparation
record.setIsActiveOnParse(true);          // CSV parsing (required)
record.setIsActivePostParse(true);        // TWAMP cleanup
record.setIsActiveCallProcedure(true);    // Aggregations (required)
```

### Operator Configuration

```java
// Vodafone
customConfig.put("operatorName", "VODAFONE");
// Executes 15 procedures (7 for 15-min + 8 for hourly at 45th minute)

// KKTC-Telsim
customConfig.put("operatorName", "KKTC-TELSIM");
// Executes 4 procedures (15-min only)
```

### Thread Pool

```java
record.setThreadPoolSize(8);  // 8 parallel CSV parsers
```

## 📈 Aggregation Procedures

### Vodafone Procedures

#### 15-Minute Aggregations (Every run)
1. `P_TWAMP_PERF` - Main performance table
2. `P_TWAMP_PERF_SUB_REGION` - Sub-region aggregation
3. `P_TWAMP_PERF_MAIN_REGION` - Main region aggregation
4. `P_TWAMP_PERF_ILCE` - District aggregation
5. `P_TWAMP_PERF_CITY` - City aggregation
6. `P_TWAMP_PERF_NW` - Network aggregation
7. `P_TWAMP_PERF_15MIN` - 15-minute summary

#### Hourly Aggregations (At 45th minute only)
8. `P_TWAMP_PERF_H` - Hourly performance
9. `P_NNI_PERF` - NNI performance
10. `P_NNI_PERF_CITY` - NNI city
11. `P_NNI_PERF_REGION` - NNI region
12. `P_NNI_PERF_VENDOR` - NNI vendor
13. `P_NNI_PERF_NW` - NNI network
14. `P_NNI_PERF_15MIN` - NNI 15-min
15. `P_NNI_PERF_H` - NNI hourly

### KKTC-Telsim Procedures

#### 15-Minute Aggregations
1. `P_KKTC_TWAMP_PERF`
2. `P_KKTC_TWAMP_PERF_SUB_REGION`
3. `P_KKTC_TWAMP_PERF_NW`
4. `P_KKTC_TWAMP_PERF_15MIN`

## ⏰ Quarter Minute Calculation

Maps current minute to 15-minute intervals:

| Current Minute | Calculated Timestamp | Description |
|---------------|----------------------|-------------|
| 00-05 | Current Hour :15 | Start of quarter |
| 06-20 | Previous Hour :30 | Previous quarter |
| 21-35 | Previous Hour :45 | Previous quarter |
| 36-50 | Current Hour :00 | Start of hour |
| 51-59 | Current Hour :15 | Start of quarter |

**Example:**
```
Current time:    2026-02-11 15:27
Quarter minute:  202602111445  (previous hour :45)
```

**Implementation:**
```java
private String calculateQuarterMinute() {
    int minute = Calendar.getInstance().get(Calendar.MINUTE);

    if (minute >= 6 && minute < 21) {
        return previousHour + "30";
    } else if (minute >= 21 && minute < 36) {
        return previousHour + "45";
    } else if (minute >= 36 && minute < 51) {
        return currentHour + "00";
    } else {  // 51-59 or 00-05
        return currentHour + "15";
    }
}
```

## 🗄️ Database Tables

### Main Tables

- `TWAMP_PERF` - Raw performance data
- `TWAMP_PERF_15MIN` - 15-minute aggregations
- `TWAMP_PERF_H` - Hourly aggregations
- `NNI_PERF` - Network-to-Network Interface metrics

### Regional Aggregation Tables

- `TWAMP_PERF_SUB_REGION` - Sub-region level
- `TWAMP_PERF_MAIN_REGION` - Main region level
- `TWAMP_PERF_ILCE` - District level
- `TWAMP_PERF_CITY` - City level
- `TWAMP_PERF_NW` - Network level

## 🔍 Troubleshooting

### Common Issues

#### 1. Table Metadata Not Found

**Error:** `WARNING: Table metadata not found for: TABLENAME`

**Solution:** Ensure table name in filename matches database table.

**Filename format:** `prefix_TABLENAME_suffix.csv`

#### 2. TimeGroup Formatting Error

**Error:** `StringIndexOutOfBoundsException` in date formatting

**Solution:** Verify TimeGroup column format is `YYYY-MM-DDTHH:MM:SS`

#### 3. Procedure Execution Fails

**Error:** `SQLException` when calling procedures

**Solutions:**
- Verify database schema permissions (NORTHI_LOADER)
- Check if procedures exist in database
- Validate date parameter format (YYYYMMDDHHMM)
- Check Oracle connection settings

#### 4. No CSV Files to Process

**Error:** `Found 0 CSV files to process`

**Solutions:**
- Verify raw path exists and contains .csv files
- Check file permissions
- Ensure files don't have "-" in name (output file indicator)

## 📝 Implementation Checklist

- [x] CsvParseHandler base class
- [x] TwampCsvParseHandler implementation
- [x] TwampParseEngine implementation
- [ ] MetadataRepository implementation (TODO)
- [ ] LoaderFactory CSV to DB loading (TODO)
- [ ] Error file handling (TODO)
- [ ] Table metadata mapping (TODO)
- [ ] Integration tests (TODO)

## 🆚 Old vs New Comparison

| Aspect | Old Code | New Template |
|--------|----------|--------------|
| **Base Class** | AbsParserEngine | ParseEngine (Activity Diagram) |
| **CSV Parsing** | CsvFileHandler + manual split | CsvParseHandler + OpenCSV |
| **Configuration** | System properties | ParseEngineRecord |
| **Threading** | Basic ExecutorService | Enhanced thread pool management |
| **Error Handling** | printStackTrace | Structured exceptions |
| **Table Metadata** | TableWatcher singleton | MetadataRepository interface |
| **File Operations** | CommonLibrary static methods | Instance methods |
| **Code Structure** | Monolithic | Modular with clear separation |
| **Testing** | Hard to test | Easy to unit test |

## 📚 References

- **Activity Diagram**: `sparx/Parser_Activity_Diagram.xml`
- **PlantUML Source**: `ParseFlow_ActivityDiagram.puml`
- **Original Code**: GitHub - `old_etl_codes/Systems/ParserEngine_TWAMP.java`
- **Template Base**: `parser-template/README.md`

## 🎓 Next Steps

1. **Implement MetadataRepository** - Load table metadata from database
2. **Test with Sample Data** - Create test CSV files
3. **Database Setup** - Create/verify TWAMP tables
4. **Procedure Validation** - Test aggregation procedures
5. **Performance Tuning** - Optimize thread pool size
6. **Monitoring** - Add metrics and logging

## 📞 Support

For questions:
1. Review `Example_TWAMP_Usage.java`
2. Check Activity Diagram flow
3. Compare with original code in `old_etl_codes`

---

**Adapted From:** `old_etl_codes/Systems/ParserEngine_TWAMP.java`
**Template Version:** 1.0
**Last Updated:** 2026-02-12
