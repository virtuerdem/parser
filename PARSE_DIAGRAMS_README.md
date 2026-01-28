# Parse Flow - UML Diagrams

Parser modülünün detaylı UML diagramları.

## 📊 Diagram Dosyaları

### 1. Activity Diagram - Parse Flow Aktivite Diyagramı
**Dosya:** `ParseFlow_ActivityDiagram.puml`

**İçerik:**
- Engine başlatma ve konfigürasyon
- XML parsing süreci (parallel execution)
- **Loop + Fork/Join** ile dinamik handler yönetimi
- Content date discovery
- Bulk data loading
- Post-processing (procedures, aggregations, export)

**Önemli Özellikler:**
- Parallel XML parsing (150 files → 8 threads)
- SAX parser kullanımı (memory-efficient)
- CSV output generation
- Thread pool synchronization
- Multiple processing phases

---

### 2. Sequence Diagram - Parse Flow Sıralı Etkileşim
**Dosya:** `ParseFlow_SequenceDiagram.puml`

**İçerik:**
- Tüm bileşenler arası detaylı mesajlaşma
- **Par blocks** ile concurrent XML parsing
- SAX parser events (startElement, characters, endElement)
- Database bulk loading operations
- Thread pool lifecycle

**5 Ana Fase:**
1. **Engine Initialization:** Config loading, path preparation
2. **Parse Phase:** Parallel XML parsing with SAX
3. **Content Date Discovery:** Date range extraction from CSVs
4. **Data Loading:** Bulk insert to database
5. **Post-Loading:** Procedures, aggregations, exports

---

### 3. Use Case Diagram - Kullanım Senaryoları
**Dosya:** `ParseFlow_UseCaseDiagram.puml`

**İçerik:**
- Sistemdeki aktörler (Transfer Module, Scheduler, DBA, Analyst, Monitor)
- Ana kullanım senaryoları
- Include/Extend ilişkileri

**7 Ana Paket:**
1. **Engine Management:** Initialization, threading
2. **Metadata Management:** Table metadata, auto-counter discovery
3. **XML Parsing:** SAX parsing, metric extraction, CSV writing
4. **Content Analysis:** Date range discovery
5. **Data Loading:** Bulk load, duplicate cleanup
6. **Post-Processing:** Procedures, KPIs, aggregations
7. **Monitoring & Logging:** Progress tracking, alerts, reports

---

## 🎯 Parser Modülü Nedir?

Parser modülü, **Transfer modülünden gelen XML dosyalarını** parse ederek **veritabanına yükler**.

### Temel Akış:
```
Transfer Module
    ↓ (XML files downloaded to /raw/)
Parse Engine
    ↓ (parallel XML parsing with SAX)
CSV Files (/result/)
    ↓ (bulk load)
Database
    ↓ (aggregations, procedures)
Reports & KPIs
```

---

## 📦 Desteklenen Vendor'lar ve Dosya Türleri

### Huawei:
| Teknoloji | Dosya Türleri | Parser Sınıfları |
|-----------|---------------|------------------|
| **eNodeB (4G)** | PM, CM, Conf | HwEnbPmXmlParseEngine/Handler |
| **gNodeB (5G)** | PM, CM, Conf | HwGnbPmXmlParseEngine/Handler |
| **RNC (3G)** | PM, CM, Conf | HwRncPmXmlParseEngine/Handler |
| **BSC (2G)** | PM, CM, Conf | HwBscPmXmlParseEngine/Handler |
| **CS (Core)** | PM, CM, Conf | HwCsPmXmlParseEngine/Handler |

### Ericsson:
| Teknoloji | Dosya Türleri | Parser Sınıfları |
|-----------|---------------|------------------|
| **DRA** | PM | ErDraPmXmlParseEngine/Handler |

### Dosya Türü Açıklamaları:
- **PM (Performance Management):** Performans metrikleri (RSRP, RSRQ, Throughput, vb.)
- **CM (Configuration Management):** Konfigürasyon verileri (cell params, neighbor relations)
- **Conf (Configuration):** Konfigürasyon parametreleri (system settings)

---

## 🔧 Teknik Detaylar

### Mimarisi:

```
ParseBaseEngine (Abstract)
    ↓ extends
HwEnbPmXmlParseEngine (Concrete)
    ↓ creates
HwEnbPmXmlParseHandler (SAX Handler)
    ↓ uses
SAXParser → XML parsing
Writer → CSV output
```

### SAX Parser Kullanımı:

**Neden SAX?**
- **Memory-efficient:** Stream-based, büyük dosyalar için ideal
- **Event-driven:** Element bazında işlem
- **Fast:** DOM'dan daha hızlı

**SAX Events:**
```java
startElement("measInfo")  → Start tag açıldı
characters("text")        → Tag içeriği okundu
endElement("measInfo")    → End tag kapandı
```

**Örnek XML → CSV:**
```xml
<measValue measObjLdn="PLMN:001-01/eNodeB:1/Cell:1">
  <measTypes>RSRP RSRQ Throughput</measTypes>
  <measResults>75 82 1024000</measResults>
</measValue>
```

↓ Parser

```csv
12345,001-01,1,1,75,82,1024000,2024-07-08 00:00:00
node_id,plmn,enodeb,cell,rsrp,rsrq,throughput,fragment_date
```

---

## ⚙️ Konfigürasyon Parametreleri

Parse engine her flow için ayrı konfigüre edilir:

| Parametre | Açıklama | Örnek Değer |
|-----------|----------|-------------|
| `onParseThreadCount` | XML parsing thread sayısı | 8 |
| `loaderThreadCount` | DB load thread sayısı | 8 |
| `discoverContentDateThreadCount` | Date discovery thread sayısı | 8 |
| `isActiveFetchTables` | Metadata tablo oluştur | true |
| `isActivePreParse` | Ön işlemler | false |
| `isActiveOnParse` | Ana parsing | true |
| `isActivePostParse` | Son işlemler | false |
| `isActiveAutoCounter` | Auto counter discovery | true |
| `isActiveDiscoverContentDate` | Date discovery | true |
| `isActiveCallProcedure` | SP çağır | true |
| `isActiveCallAggregate` | Aggregation çalıştır | true |
| `isActiveCallExport` | Export yap | false |

---

## 📊 Performance Metrikleri

### Tipik Bir Çalıştırma:

```
Input:
- 150 XML files
- Total size: 2.2 GB (compressed from Transfer)
- After decompression: ~11 GB

Processing:
- Thread count: 8 parallel
- Parse duration: ~38 minutes
- CSV files generated: ~50 files (~500 MB)

Database Loading:
- Records inserted: ~10 million
- Load duration: ~5 minutes
- Tables affected: ~15 tables

Total Duration: ~45 minutes
```

### Performance Karşılaştırma:

| Yaklaşım | Süre |
|----------|------|
| **Sequential** (tek thread) | 150 × 2 min = 300 min (5 saat) |
| **Parallel** (8 threads) | 300 / 8 ≈ 38 min ✅ |

**Kazanç:** ~87% daha hızlı!

---

## 🔄 Akış Detayları

### 1. Engine Initialization
```
startEngine(record)
  ↓
preparePaths()           // /raw/, /result/, /error/ oluştur
  ↓
fetchTables()            // (optional) Metadata tabloları oluştur
  ↓
getTables()              // Table metadata'ları yükle
  ↓
preEngine()              // (optional) Ön işlemler
```

### 2. Main Parsing (onEngine)
```
getNetworkNodes()        // Active node listesi (eNodeB, gNodeB)
  ↓
readXMLFiles()           // /raw/'dan XML'leri oku
  ↓
createThreadPool(8)      // 8 thread'lik pool
  ↓
Loop: for each XML
  createHandler()        // Vendor-specific handler
  submitToPool()         // Non-blocking submit
  ↓
Fork: Parallel execution
  Handler 1-8 → SAX parse → CSV write
  ↓
Join: Wait all complete
  ↓
closeAllStreams()        // Flush CSV buffers
```

### 3. Content Date Discovery
```
readCSVFiles()
  ↓
Parallel: for each CSV
  analyzeDateColumns()   // Min/max date extraction
  ↓
aggregateDateRanges()
  ↓
printDates()             // Log discovered ranges
```

### 4. Data Loading
```
cleanDuplicateBefore()   // (optional) CSV'den duplicate temizle
  ↓
Parallel: for each CSV
  bulkLoad(csv, table)   // COPY or batch INSERT
  ↓
cleanDuplicateAfter()    // (optional) DB'de duplicate temizle
```

### 5. Post-Processing
```
callProcedure()          // (optional) Stored procedures
  ↓
callAggregate()          // (optional) KPI calculations
  ↓
callExport()             // (optional) External export
```

---

## 🎨 Diagramları Görüntüleme

### PlantUML ile Render:

```bash
# PNG olarak
java -jar plantuml.jar ParseFlow_ActivityDiagram.puml

# SVG olarak (scalable)
java -jar plantuml.jar -tsvg ParseFlow_*.puml

# Hepsini render et
java -jar plantuml.jar ParseFlow_*.puml
```

### Online:
http://www.plantuml.com/plantuml/uml/
→ .puml dosyasını kopyala-yapıştır

### VS Code:
1. PlantUML extension yükle
2. .puml dosyasını aç
3. `Alt+D` → Preview

---

## 🔗 Transfer Modülü ile Entegrasyon

Parser modülü **Transfer modülü tarafından tetiklenir**:

```
Transfer Module (postEngine)
    ↓
Trigger: Parser Module
    ↓
Parse Engine starts
```

**Integration Point:**
```java
// Transfer Module - postEngine()
triggerParser(validFiles);  // 149 validated XML files

// Parser Module receives trigger
parseEngine.startEngine(parseEngineRecord);
```

---

## 📋 Auto Counter Discovery

Parser, XML'lerden **otomatik olarak** yeni metrikleri keşfeder:

**Örnek:**
```xml
<measTypes>RSRP RSRQ Throughput PRB_Usage NewMetric_XYZ</measTypes>
```

↓ Auto Counter Discovery

```sql
INSERT INTO t_auto_counter (counter_name, data_type, table_name)
VALUES
  ('RSRP', 'integer', 't_pm_cell'),
  ('RSRQ', 'integer', 't_pm_cell'),
  ('Throughput', 'bigint', 't_pm_cell'),
  ('PRB_Usage', 'integer', 't_pm_cell'),
  ('NewMetric_XYZ', 'integer', 't_pm_cell');  ← Yeni keşfedildi!
```

**Avantaj:** Vendor yeni metrik eklediğinde, sistem otomatik adapte olur.

---

## 📚 Kod Yapısı

```
etl-master/parse/src/main/java/com/ttgint/parse/
├── base/
│   ├── ParseBaseEngine.java          # Abstract engine
│   ├── ParseBaseHandler.java         # Abstract handler
│   ├── ParseXmlHandler.java          # XML (SAX) handler base
│   ├── ParseCsvHandler.java          # CSV handler base
│   └── ...
├── operation/
│   ├── engine/
│   │   ├── HwEnbPmXmlParseEngine.java
│   │   ├── HwGnbPmXmlParseEngine.java
│   │   ├── HwRncPmXmlParseEngine.java
│   │   └── ...
│   └── handler/
│       ├── HwEnbPmXmlParseHandler.java
│       ├── HwGnbPmXmlParseHandler.java
│       ├── HwRncPmXmlParseHandler.java
│       └── ...
└── ParseApplication.java             # Spring Boot app
```

---

## 🎯 Özet

Parser modülü:
1. ✅ **Transfer modülünden** XML dosyaları alır
2. ✅ **Parallel parsing** ile hızlı işler (8 threads)
3. ✅ **SAX parser** ile memory-efficient çalışır
4. ✅ **CSV formatında** parse eder
5. ✅ **Bulk load** ile veritabanına yükler
6. ✅ **Auto-discover** ile yeni metrikleri keşfeder
7. ✅ **Post-processing** ile KPI'ları hesaplar

**Sonuç:** ~45 dakikada 150 XML dosyası → 10M+ database record! 🎉

---

Son güncelleme: 2026-01-07
Oluşturan: Claude Code
