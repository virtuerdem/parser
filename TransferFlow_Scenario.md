# Transfer Flow Execution Scenario
## Real-World Example: Huawei eNodeB Configuration Management Transfer

**Date:** 2025-11-18
**Flow:** HW_ENB_CM_TRANSFER
**Purpose:** Transfer configuration management files from 3 Huawei eNodeB nodes

---

## Scenario Parameters

### Transfer Configuration
- **Flow ID:** 1001
- **Flow Code:** HW_ENB_CM
- **Connections:** 3 active eNodeB nodes
- **Thread Configuration:**
  - onTransfer threads: 3 (one per connection)
  - decompress threads: 4
  - validation threads: 4

### Network Topology
```
┌─────────────────┐
│  Transfer Engine │
│  (Application)   │
└────────┬─────────┘
         │
    ┌────┴─────┬──────────┬──────────┐
    │          │          │          │
┌───▼────┐ ┌──▼─────┐ ┌──▼─────┐ ┌──▼─────┐
│eNodeB_1│ │eNodeB_2│ │eNodeB_3│ │Database│
│10.1.1.1│ │10.1.1.2│ │10.1.1.3│ │        │
└────────┘ └────────┘ └────────┘ └────────┘
```

### Database State (Before)
**t_connection** table:
| id  | flow_id | node_name | ip_address | protocol | last_modified_time     | is_active |
|-----|---------|-----------|------------|----------|------------------------|-----------|
| 101 | 1001    | eNodeB_1  | 10.1.1.1   | SFTP     | 2025-11-18 08:00:00+00 | true      |
| 102 | 1001    | eNodeB_2  | 10.1.1.2   | SFTP     | 2025-11-18 08:00:00+00 | true      |
| 103 | 1001    | eNodeB_3  | 10.1.1.3   | SFTP     | 2025-11-18 08:00:00+00 | true      |

---

## Execution Timeline

### ⏰ 10:00:00 - PHASE 1: Engine Startup

#### Step 1-3: Engine Initialization
```
10:00:00.100 [main] INFO  TransferBaseEngine - * TransferBaseEngine startEngine
10:00:00.105 [main] INFO  TransferBaseEngine - * TransferBaseEngine preparePaths
10:00:00.110 [main] INFO  FileLib - Created directory: /data/transfer/raw/HW_ENB_CM/
10:00:00.112 [main] INFO  FileLib - Created directory: /data/transfer/processed/HW_ENB_CM/
10:00:00.114 [main] INFO  FileLib - Created directory: /data/transfer/archive/HW_ENB_CM/
10:00:00.120 [main] INFO  TransferBaseEngine - * TransferBaseEngine preEngine
```

#### Step 4-6: Connection Retrieval
```
10:00:00.130 [main] INFO  TransferBaseEngine - * TransferBaseEngine onEngine
10:00:00.135 [main] INFO  TransferBaseEngine - * TransferBaseEngine getConnections
10:00:00.140 [main] INFO  ConnectionRepository - Query: findByFlowIdAndIsActive(1001, true)
10:00:00.150 [main] INFO  TransferBaseEngine - * TransferBaseEngine getConnections connectionSize: 3
```

#### Step 7-8: Handler Creation (Parallel)
```
10:00:00.160 [pool-1-thread-1] INFO  TransferBaseHandler - Created handler for connection: 101
10:00:00.161 [pool-1-thread-2] INFO  TransferBaseHandler - Created handler for connection: 102
10:00:00.162 [pool-1-thread-3] INFO  TransferBaseHandler - Created handler for connection: 103
10:00:00.165 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler run (connection: 101)
10:00:00.165 [pool-1-thread-2] INFO  TransferBaseHandler - * TransferBaseHandler run (connection: 102)
10:00:00.165 [pool-1-thread-3] INFO  TransferBaseHandler - * TransferBaseHandler run (connection: 103)
```

---

### ⏰ 10:00:01 - PHASE 2: Handler Execution (Parallel)

#### Handler 1 (eNodeB_1 - Success Flow)

**Step 9-10: Connection**
```
10:00:01.000 [pool-1-thread-1] INFO  TransferConnectionFactory - Connecting to 10.1.1.1:22 (SFTP)
10:00:01.200 [pool-1-thread-1] INFO  TransferConnectionFactory - Connection successful: eNodeB_1
```

**Step 11-13: Check Last Modified Time**
```
10:00:01.205 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler preHandler
10:00:01.210 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler checkLastModifiedTime
10:00:01.215 [pool-1-thread-1] INFO  ResultRepository - getMaxModifiedTime(101) = 2025-11-18 09:30:00+00
10:00:01.220 [pool-1-thread-1] INFO  TransferBaseHandler - Updated lastModifiedTime: 09:30:00
```

**Step 14-15: Read Files**
```
10:00:01.500 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler readFiles
10:00:02.800 [pool-1-thread-1] INFO  TransferConnectionFactory - Remote path: /opt/ericsson/cm/export/
10:00:02.850 [pool-1-thread-1] INFO  TransferConnectionFactory - Found 5 files
10:00:02.851 [pool-1-thread-1] INFO  TransferBaseHandler - Read 5 files from remote server
```

**Remote Files Found:**
```
eNodeB_1_CM_20251118_090000.xml.gz (Modified: 09:00:15) ❌ OLD
eNodeB_1_CM_20251118_093000.xml.gz (Modified: 09:30:45) ✓ NEW
eNodeB_1_CM_20251118_100000.xml.gz (Modified: 10:00:30) ✓ NEW
eNodeB_1_PM_20251118_093000.xml.gz (Modified: 09:35:20) ✓ NEW
eNodeB_1_CM_20251118_100030.xml.gz (Modified: 10:00:35) ❌ IGNORED (latest second)
```

**Step 16-17: Filter & Set File Info**
```
10:00:02.855 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler filterFiles
10:00:02.860 [pool-1-thread-1] INFO  TransferBaseHandler - Filtered: 3 files (2 too old, 1 too recent)
10:00:02.865 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler setFileInfo
```

**Step 18: Cache Results**
```
10:00:02.900 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler cacheResults
10:00:02.950 [pool-1-thread-1] INFO  ResultRepository - Saved 3 records to t_transfer_connection_result
```

**Database After cacheResults:**
| id | connection_id | file_id | remote_file_name                    | is_downloaded | transfer_try_count |
|----|---------------|---------|-------------------------------------|---------------|--------------------|
| 1  | 101           | 001     | eNodeB_1_CM_20251118_093000.xml.gz  | false         | 0                  |
| 2  | 101           | 002     | eNodeB_1_CM_20251118_100000.xml.gz  | false         | 0                  |
| 3  | 101           | 003     | eNodeB_1_PM_20251118_093000.xml.gz  | false         | 0                  |

**Step 19-20: Update Last Modified & Clear**
```
10:00:02.955 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler setLastModifiedTime
10:00:02.960 [pool-1-thread-1] INFO  ConnectionRepository - Updated connection 101 lastModifiedTime: 10:00:30
10:00:02.965 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler clearRemoteFiles
```

**Step 21-25: Download (Loop)**
```
10:00:03.000 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler download
10:00:03.050 [pool-1-thread-1] INFO  ResultRepository - getFileListToTransfer: 3 files

File 1/3:
10:00:03.100 [pool-1-thread-1] INFO  TransferConnectionFactory - Downloading: eNodeB_1_CM_20251118_093000.xml.gz
10:00:04.500 [pool-1-thread-1] INFO  TransferConnectionFactory - Downloaded: 2.5 MB in 1.4s
10:00:04.505 [pool-1-thread-1] INFO  ResultRepository - Updated result id=1: is_downloaded=true, size=2621440

File 2/3:
10:00:04.600 [pool-1-thread-1] INFO  TransferConnectionFactory - Downloading: eNodeB_1_CM_20251118_100000.xml.gz
10:00:06.100 [pool-1-thread-1] INFO  TransferConnectionFactory - Downloaded: 2.8 MB in 1.5s
10:00:06.105 [pool-1-thread-1] INFO  ResultRepository - Updated result id=2: is_downloaded=true, size=2936012

File 3/3:
10:00:06.200 [pool-1-thread-1] INFO  TransferConnectionFactory - Downloading: eNodeB_1_PM_20251118_093000.xml.gz
10:00:07.800 [pool-1-thread-1] INFO  TransferConnectionFactory - Downloaded: 3.1 MB in 1.6s
10:00:07.805 [pool-1-thread-1] INFO  ResultRepository - Updated result id=3: is_downloaded=true, size=3251200
```

**Step 26-28: Cleanup & Complete**
```
10:00:07.850 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler postHandler
10:00:07.900 [pool-1-thread-1] INFO  TransferBaseHandler - * TransferBaseHandler closeConnection
10:00:07.950 [pool-1-thread-1] INFO  TransferConnectionFactory - Disconnected from eNodeB_1
10:00:07.955 [pool-1-thread-1] INFO  TransferBaseHandler - Handler completed successfully for connection: 101
```

#### Handler 2 (eNodeB_2 - Success Flow)
```
[Similar flow as Handler 1]
10:00:01.000 - 10:00:08.200 [pool-1-thread-2]
Result: 4 files downloaded successfully
```

#### Handler 3 (eNodeB_3 - Connection Failed)
```
10:00:01.000 [pool-1-thread-3] INFO  TransferConnectionFactory - Connecting to 10.1.1.3:22 (SFTP)
10:00:03.000 [pool-1-thread-3] ERROR TransferConnectionFactory - Connection timeout: eNodeB_3
10:00:03.005 [pool-1-thread-3] WARN  TransferBaseHandler - Connection failed for: 103
10:00:03.010 [pool-1-thread-3] INFO  TransferBaseHandler - * TransferBaseHandler closeConnection
10:00:03.015 [pool-1-thread-3] INFO  TransferConnectionFactory - Disconnected (connection was not established)
10:00:03.020 [pool-1-thread-3] ERROR TransferBaseHandler - Handler failed for connection: 103 (END - CONNECTION FAILED)
```

---

### ⏰ 10:00:10 - SYNCHRONIZATION: Wait for All Handlers

```
10:00:08.300 [main] INFO  TransferBaseEngine - Waiting for all handlers to complete...
10:00:08.305 [main] INFO  TransferBaseEngine - Handler completion status: 2/3 successful, 1/3 failed
10:00:08.310 [main] INFO  TransferBaseEngine - All handlers completed
```

**Handler Summary:**
- ✅ Handler 1 (eNodeB_1): SUCCESS - 3 files downloaded
- ✅ Handler 2 (eNodeB_2): SUCCESS - 4 files downloaded
- ❌ Handler 3 (eNodeB_3): FAILED - Connection timeout

---

### ⏰ 10:00:10 - PHASE 3: Engine Post-Processing

#### Step 32-34: Decompress

**Files in /data/transfer/raw/HW_ENB_CM/:**
```
001^^eNodeB_1_CM_20251118_093000.xml.gz (2.5 MB)
002^^eNodeB_1_CM_20251118_100000.xml.gz (2.8 MB)
003^^eNodeB_1_PM_20251118_093000.xml.gz (3.1 MB)
004^^eNodeB_2_CM_20251118_093000.xml.gz (2.6 MB)
005^^eNodeB_2_CM_20251118_100000.xml.gz (2.7 MB)
006^^eNodeB_2_PM_20251118_093000.xml.gz (3.2 MB)
007^^eNodeB_2_PM_20251118_100000.xml.gz (3.3 MB)
```

```
10:00:10.500 [main] INFO  TransferBaseEngine - * TransferBaseEngine decompress
10:00:10.505 [main] INFO  TransferBaseEngine - decompress fileSize: 7

10:00:10.600 [pool-2-thread-1] INFO  DecompressFactory - Decompressing: 001^^eNodeB_1_CM_20251118_093000.xml.gz
10:00:10.601 [pool-2-thread-2] INFO  DecompressFactory - Decompressing: 002^^eNodeB_1_CM_20251118_100000.xml.gz
10:00:10.602 [pool-2-thread-3] INFO  DecompressFactory - Decompressing: 003^^eNodeB_1_PM_20251118_093000.xml.gz
10:00:10.603 [pool-2-thread-4] INFO  DecompressFactory - Decompressing: 004^^eNodeB_2_CM_20251118_093000.xml.gz

10:00:12.100 [pool-2-thread-1] INFO  DecompressFactory - Decompressed: 12.8 MB (ratio: 5.1x)
10:00:12.200 [pool-2-thread-2] INFO  DecompressFactory - Decompressed: 14.3 MB (ratio: 5.1x)
10:00:12.300 [pool-2-thread-3] INFO  DecompressFactory - Decompressed: 15.8 MB (ratio: 5.1x)
10:00:12.400 [pool-2-thread-4] INFO  DecompressFactory - Decompressed: 13.3 MB (ratio: 5.1x)

... (remaining 3 files)

10:00:14.800 [main] INFO  TransferBaseEngine - Decompression complete: 7 files, 0 errors
```

#### Step 35-37: Validation

**XML Files in /data/transfer/raw/HW_ENB_CM/:**
```
001^^eNodeB_1_CM_20251118_093000.xml (12.8 MB)
002^^eNodeB_1_CM_20251118_100000.xml (14.3 MB)
003^^eNodeB_1_PM_20251118_093000.xml (15.8 MB)
004^^eNodeB_2_CM_20251118_093000.xml (13.3 MB)
005^^eNodeB_2_CM_20251118_100000.xml (13.9 MB)
006^^eNodeB_2_PM_20251118_093000.xml (16.4 MB)
007^^eNodeB_2_PM_20251118_100000.xml (16.9 MB)
```

```
10:00:15.000 [main] INFO  TransferBaseEngine - * TransferBaseEngine validation
10:00:15.005 [main] INFO  TransferBaseEngine - validation fileSize: 7

10:00:15.100 [pool-3-thread-1] INFO  XmlValidation - Validating: 001^^eNodeB_1_CM_20251118_093000.xml
10:00:15.101 [pool-3-thread-2] INFO  XmlValidation - Validating: 002^^eNodeB_1_CM_20251118_100000.xml
10:00:15.102 [pool-3-thread-3] INFO  XmlValidation - Validating: 003^^eNodeB_1_PM_20251118_093000.xml
10:00:15.103 [pool-3-thread-4] INFO  XmlValidation - Validating: 004^^eNodeB_2_CM_20251118_093000.xml

10:00:16.500 [pool-3-thread-1] INFO  XmlValidation - Valid XML: ericsson_cm_v2.xsd ✓
10:00:16.600 [pool-3-thread-2] INFO  XmlValidation - Valid XML: ericsson_cm_v2.xsd ✓
10:00:16.700 [pool-3-thread-3] INFO  XmlValidation - Valid XML: ericsson_pm_v1.xsd ✓
10:00:16.800 [pool-3-thread-4] INFO  XmlValidation - Valid XML: ericsson_cm_v2.xsd ✓

... (remaining 3 files)

10:00:18.500 [main] INFO  TransferBaseEngine - Validation complete: 7 valid, 0 invalid
```

#### Step 38: Post Engine

```
10:00:18.600 [main] INFO  TransferBaseEngine - * TransferBaseEngine postEngine
10:00:18.650 [main] INFO  TransferBaseEngine - Archiving files...
10:00:18.900 [main] INFO  TransferBaseEngine - Files archived to: /data/transfer/archive/HW_ENB_CM/2025-11-18/
10:00:18.950 [main] INFO  TransferBaseEngine - Generating summary report...
```

---

## Final Results

### Transfer Summary
```
=== TRANSFER SUMMARY ===
Flow: HW_ENB_CM_TRANSFER (1001)
Start Time: 2025-11-18 10:00:00
End Time: 2025-11-18 10:00:19
Duration: 19 seconds

Connections Processed: 3
  ✓ eNodeB_1 (10.1.1.1): SUCCESS
  ✓ eNodeB_2 (10.1.1.2): SUCCESS
  ✗ eNodeB_3 (10.1.1.3): FAILED (Connection timeout)

Files Discovered: 10
Files Filtered: 7 (3 excluded by time filter)
Files Downloaded: 7 (100% success rate)
  - Total Size: 19.1 MB (compressed)
  - Download Time: ~11s
  - Average Speed: 1.74 MB/s

Files Decompressed: 7 (100% success rate)
  - Total Size: 103.4 MB (uncompressed)
  - Compression Ratio: 5.4x
  - Decompression Time: 4.2s

Files Validated: 7 (100% valid)
  - CM Files: 4
  - PM Files: 3
  - Validation Time: 3.4s

Next Step: Parser Module (scheduled)
```

### Database State (After)

**t_connection**:
| id  | last_modified_time     | is_active |
|-----|------------------------|-----------|
| 101 | 2025-11-18 10:00:30+00 | true      |
| 102 | 2025-11-18 10:00:30+00 | true      |
| 103 | 2025-11-18 08:00:00+00 | true      |

**t_transfer_connection_result** (7 records):
| id | connection_id | is_downloaded | transfer_try_count | file_size |
|----|---------------|---------------|--------------------|-----------|
| 1  | 101           | true          | 1                  | 2621440   |
| 2  | 101           | true          | 1                  | 2936012   |
| 3  | 101           | true          | 1                  | 3251200   |
| 4  | 102           | true          | 1                  | 2728960   |
| 5  | 102           | true          | 1                  | 2831155   |
| 6  | 102           | true          | 1                  | 3356672   |
| 7  | 102           | true          | 1                  | 3461120   |

### File System State

```
/data/transfer/
├── raw/HW_ENB_CM/
│   ├── 001^^eNodeB_1_CM_20251118_093000.xml (12.8 MB)
│   ├── 002^^eNodeB_1_CM_20251118_100000.xml (14.3 MB)
│   ├── 003^^eNodeB_1_PM_20251118_093000.xml (15.8 MB)
│   ├── 004^^eNodeB_2_CM_20251118_093000.xml (13.3 MB)
│   ├── 005^^eNodeB_2_CM_20251118_100000.xml (13.9 MB)
│   ├── 006^^eNodeB_2_PM_20251118_093000.xml (16.4 MB)
│   └── 007^^eNodeB_2_PM_20251118_100000.xml (16.9 MB)
├── processed/HW_ENB_CM/
│   └── (empty - awaiting parser)
└── archive/HW_ENB_CM/2025-11-18/
    ├── 001^^eNodeB_1_CM_20251118_093000.xml.gz
    ├── 002^^eNodeB_1_CM_20251118_100000.xml.gz
    └── ... (7 files archived)
```

---

## Error Handling Demonstrated

### Connection Failure (eNodeB_3)
- **Error:** Connection timeout
- **Handler Action:** Immediately closed connection, notified engine
- **Impact:** Other handlers continued unaffected
- **Result:** Partial success - 2/3 nodes transferred

### Retry Mechanism (Not Triggered This Time)
If download had failed:
- `transfer_try_count` would increment
- Next run would retry (max 3 attempts)
- File would remain `is_downloaded=false`

### Time Filter Edge Cases
- File `eNodeB_1_CM_20251118_090000.xml.gz`: Too old (before lastModifiedTime)
- File `eNodeB_1_CM_20251118_100030.xml.gz`: Too recent (in current second)
- Both correctly excluded from processing

---

## Performance Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| Total Duration | 19s | Start to finish |
| Handler Parallel Execution | 3 threads | Simultaneous connections |
| Decompress Parallel | 4 threads | Batch processing |
| Validation Parallel | 4 threads | Batch processing |
| Network Throughput | 1.74 MB/s | Average download speed |
| Decompression Rate | 24.6 MB/s | Average unzip speed |
| Validation Rate | 30.4 MB/s | Average XML check speed |
| Success Rate | 100% | For connected nodes |
| Connection Success | 67% | 2/3 nodes connected |

---

## Next Steps

1. **Retry Failed Connection** (eNodeB_3)
   - Scheduled for next run (every 15 minutes)
   - Network team notified for investigation

2. **Parser Module Trigger**
   - Input: 7 validated XML files
   - Expected: Parse configuration data into database

3. **Monitoring**
   - Alert sent for failed connection
   - Success metrics logged to monitoring system
