# Parser Modülü - Database Tablo Entegrasyonu

Parser modülünün kullandığı tüm database tablolarının detaylı dokümantasyonu.

---

## 📊 Özet

| Kategori | Tablo Sayısı | Kullanım Amacı |
|----------|--------------|----------------|
| **Configuration** | 4 | Engine ve component konfigürasyonu |
| **Metadata Management** | 3 | Tablo/kolon tanımları ve mapping |
| **Auto Discovery** | 1 | Otomatik counter/metrik keşfi |
| **Network Reference** | 1 | Node ID mapping (eNodeB, gNodeB, RNC, BSC) |
| **Processing Results** | 2 | İşlem sonuçları ve date discovery |
| **Data Tables** | 50+ | Parse edilen PM/CM/Conf verileri |
| **TOPLAM** | **60+** | |

---

## 1️⃣ CONFIGURATION TABLES (4 tablo)

### 📋 t_flow
**Kullanım:** Parse işleminin hangi flow'a ait olduğunu belirler

**Repository:** `FlowRepository`
**Kullanıldığı Sınıf:** `ParseConstructor.java:67`

**SQL:**
```sql
SELECT * FROM t_flow WHERE id = ?
```

**Kolonlar:**
- `id` - Flow ID (primary key)
- `flow_code` - Flow kodu (örn: PARSE_HW_ENB_PM)
- `flow_name` - Flow adı
- `branch_id` - Hangi branch'e ait (foreign key → t_branch)
- `is_active` - Aktif mi?

**Kod Kullanımı:**
```java
Flow flow = flowRepository.findById(argument.getFlowId()).get();
bean.startEngine(ParseEngineRecord.getRecord(branch, flow, ...));
```

---

### 🏢 t_branch
**Kullanım:** Hangi branch/şube için parse işlemi yapıldığını belirler

**Repository:** `BranchRepository`
**Kullanıldığı Sınıf:** `ParseConstructor.java:68`

**SQL:**
```sql
SELECT * FROM t_branch WHERE id = ?
```

**Kolonlar:**
- `id` - Branch ID (primary key)
- `branch_code` - Branch kodu
- `branch_name` - Branch adı
- `company_id` - Şirket ID (foreign key → t_company)
- `is_active` - Aktif mi?

**Kod Kullanımı:**
```java
Branch branch = branchRepository.findById(flow.getBranchId()).get();
```

**İlişkiler:**
- `t_network_node.branch_id` → Network node'ları branch'e göre filtrelenir

---

### ⚙️ t_parse_engine
**Kullanım:** Parse engine'in tüm konfigürasyonunu içerir

**Repository:** `ParseEngineRepository`
**Kullanıldığı Sınıf:** `ParseConstructor.java:46-47`

**SQL:**
```sql
SELECT * FROM t_parse_engine WHERE flow_id = ?
```

**Önemli Kolonlar:**
- `flow_id` - Hangi flow'a ait (foreign key → t_flow)
- `parse_component_id` - Hangi component kullanılacak (foreign key → t_parse_component)
- `is_active` - Engine aktif mi?
- `is_active_fetch_tables` - Metadata generate edilsin mi?
- `is_active_pre_parse` - Pre-processing aktif mi?
- `is_active_on_parse` - Ana parse aktif mi?
- `is_active_post_parse` - Post-processing aktif mi?
- `is_active_auto_counter` - Auto counter discovery aktif mi?
- `is_active_discover_content_date` - Content date discovery aktif mi?
- `on_parse_thread_count` - Kaç thread kullanılacak (default: 8)
- `loader_thread_count` - Loader kaç thread kullanacak (default: 8)
- `discover_content_date_thread_count` - Content date kaç thread (default: 8)

**Kod Kullanımı:**
```java
Optional<ParseEngine> engine = parseEngineRepository.findByFlowId(flowId);
if (engine.isEmpty() || !engine.get().getIsActive()) {
    log.error("! ParseEngine is not active");
    return;
}
```

---

### 🔧 t_parse_component
**Kullanım:** Hangi parser component'inin kullanılacağını belirler

**Repository:** `ParseComponentRepository`
**Kullanıldığı Sınıf:** `ParseConstructor.java:56-57`

**SQL:**
```sql
SELECT * FROM t_parse_component WHERE id = ?
```

**Önemli Kolonlar:**
- `id` - Component ID (primary key)
- `component_code` - Component kodu (örn: HW_ENB_PM_XML)
- `component_name` - Component adı
- `is_active` - Aktif mi?

**Kod Kullanımı:**
```java
Optional<ParseComponent> component = parseComponentRepository
    .findById(engine.get().getParseComponentId());

// Spring bean olarak component'i al
ParseBaseEngine bean = (ParseBaseEngine) applicationContext
    .getBean(component.get().getComponentCode().toUpperCase().trim());
```

**Component Örnekleri:**
- `HW_ENB_PM_XML` → HwEnbPmXmlParseEngine (Huawei eNodeB PM)
- `HW_GNB_PM_XML` → HwGnbPmXmlParseEngine (Huawei gNodeB PM)
- `HW_RNC_PM_XML` → HwRncPmXmlParseEngine (Huawei RNC PM)
- `HW_BSC_PM_XML` → HwBscPmXmlParseEngine (Huawei BSC PM)
- `ER_DRA_PM_XML` → ErDraPmXmlParseEngine (Ericsson DRA PM)

---

## 2️⃣ METADATA MANAGEMENT TABLES (3 tablo)

### 📊 t_parse_table
**Kullanım:** Parse edilecek tabloların metadata'sını içerir

**Repository:** `ParseTableRepository`
**Kullanıldığı Sınıf:** `ParseMapper.java:31`

**SQL:**
```sql
SELECT * FROM t_parse_table
WHERE flow_id = ? AND is_active = true
```

**Önemli Kolonlar:**
- `id` - Parse table ID (primary key)
- `flow_id` - Hangi flow'a ait (foreign key → t_flow)
- `table_name` - Hedef tablo adı (örn: t_pm_cell_huawei)
- `object_key` - XML'deki object key (örn: measInfo)
- `is_active` - Aktif mi?

**Kod Kullanımı:**
```java
parserMaps = parseTableRepository.findAllByFlowIdAndIsActive(flowId, true)
    .stream()
    .map(ParseMapRecord::getRecord)
    .collect(Collectors.toList());
```

**Örnek Kayıtlar:**
| table_name | object_key | flow_id |
|------------|------------|---------|
| t_pm_cell_huawei | Cell | 1 |
| t_pm_sector_huawei | Sector | 1 |
| t_pm_enodeb_huawei | ENodeB | 1 |

---

### 📝 t_parse_column
**Kullanım:** Her tablonun kolonlarının XML path mapping'lerini içerir

**Repository:** `ParseColumnRepository`
**Kullanıldığı Sınıf:** `ParseMapper.java:37`

**SQL:**
```sql
SELECT * FROM t_parse_column
WHERE flow_id = ? AND is_active = true
```

**Önemli Kolonlar:**
- `id` - Parse column ID (primary key)
- `parse_table_id` - Hangi tabloya ait (foreign key → t_parse_table)
- `flow_id` - Hangi flow'a ait
- `column_name` - Database kolon adı
- `xml_path` - XML'deki path (örn: measValue/r)
- `column_order_id` - CSV'deki kolon sırası
- `is_active` - Aktif mi?

**Kod Kullanımı:**
```java
Map<Long, List<ParseColumnRecord>> columns = parseColumnRepository
    .findAllByFlowIdAndIsActive(flowId, true)
    .stream()
    .map(ParseColumnRecord::getRecord)
    .collect(Collectors.groupingBy(ParseColumnRecord::getParseTableId));
```

**Örnek Kayıtlar:**
| parse_table_id | column_name | xml_path | column_order_id |
|----------------|-------------|----------|-----------------|
| 1 | fragment_date | @date | 1 |
| 1 | node_id | @nodeId | 2 |
| 1 | rsrp | measValue/r[0] | 3 |
| 1 | rsrq | measValue/r[1] | 4 |
| 1 | throughput_dl | measValue/r[2] | 5 |

---

### 📋 t_all_table
**Kullanım:** Sistemdeki tüm tabloların merkezi registry'si

**Repository:** İlgili metadata generation sınıflarında kullanılır
**Kullanım Durumu:** Metadata generation esnasında

**Önemli Kolonlar:**
- `id` - Table ID
- `table_name` - Tablo adı
- `table_description` - Açıklama
- `is_active` - Aktif mi?

---

## 3️⃣ AUTO DISCOVERY TABLE (1 tablo)

### 🔍 t_all_counter
**Kullanım:** XML'lerden otomatik keşfedilen counter/metrik tanımları

**Repository:** `AllCounterRepository`
**Kullanıldığı Sınıf:** `AutoCounterDefine.java:45, 65`

**SQL Read:**
```sql
SELECT * FROM t_all_counter WHERE flow_id = ?
```

**SQL Write:**
```sql
INSERT INTO t_all_counter (
    flow_id, node_group_type, counter_group_type,
    counter_group_key, counter_key, counter_name
) VALUES (?, ?, ?, ?, ?, ?)
```

**Önemli Kolonlar:**
- `id` - Counter ID (primary key)
- `flow_id` - Hangi flow tarafından keşfedildi
- `node_group_type` - Node grubu (örn: eNodeB, gNodeB)
- `counter_group_type` - Counter grubu (örn: Cell, Sector)
- `counter_group_key` - Grup key (örn: measInfoId)
- `counter_key` - Counter key (örn: measType değeri)
- `counter_name` - Counter adı/açıklaması

**Kod Kullanımı:**
```java
// Parse esnasında keşfedilen counter'lar collect edilir
autoCounterDefine.collect(CounterDefineRecord.builder()
    .nodeGroupType("eNodeB")
    .counterGroupType("Cell")
    .counterKey("L.Cell.RSRP.Mean")
    .build());

// Parse bittiğinde DB'ye kaydedilir
if (record.getIsActiveAutoCounter()) {
    autoCounterDefine.save(engineRecord);
}
autoCounterDefine.clear();
```

**Örnek Kayıtlar:**
| node_group_type | counter_group_type | counter_key | counter_name |
|-----------------|-------------------|-------------|--------------|
| eNodeB | Cell | L.Cell.RSRP.Mean | Average RSRP |
| eNodeB | Cell | L.Cell.Throughput.DL | DL Throughput |
| gNodeB | NRCell | N.Cell.SSB.RSRP | 5G SSB RSRP |

**Performans:**
- 150 XML dosyası parse edildiğinde ~1000+ yeni counter keşfedilebilir
- Duplicate check yapılır (var olan counter'lar tekrar insert edilmez)
- Concurrent safe (ConcurrentHashMap kullanılır)

---

## 4️⃣ NETWORK REFERENCE TABLE (1 tablo)

### 🌐 t_network_node
**Kullanım:** Network node'larının ID mapping'i (eNodeB, gNodeB, RNC, BSC)

**Repository:** `NetworkNodeRepository`
**Kullanıldığı Sınıflar:**
- `HwEnbPmXmlParseEngine.java`
- `HwGnbPmXmlParseEngine.java`
- `HwRncPmXmlParseEngine.java`
- `HwBscPmXmlParseEngine.java`
- `ErDraPmXmlParseEngine.java`

**SQL:**
```sql
SELECT node_id, node_name
FROM t_network_node
WHERE branch_id = ? AND is_active = true
```

**Önemli Kolonlar:**
- `node_id` - Node ID (primary key)
- `node_name` - Node adı (örn: eNodeB_001, gNodeB_TR_ANK_001)
- `branch_id` - Hangi branch'e ait (foreign key → t_branch)
- `node_type` - Node tipi (eNodeB, gNodeB, RNC, BSC, DRA)
- `vendor` - Vendor (Huawei, Ericsson, Nokia)
- `is_active` - Aktif mi?

**Kod Kullanımı:**
```java
// Engine başlarken node mapping'i yüklenir
Map<String, Long> networkNodeIds = networkNodeRepository
    .getNetworkNodesByBranchId(engineRecord.getBranchId());

// Handler'lara map olarak geçilir
HwEnbPmXmlParseHandler handler = new HwEnbPmXmlParseHandler(
    applicationContext,
    handlerRecord,
    networkNodeIds  // <-- Node name → Node ID mapping
);

// Parse esnasında node name'den node ID bulunur
String nodeName = parseNodeNameFromXml(xml);
Long nodeId = networkNodeIds.get(nodeName);
if (nodeId == null) {
    log.warn("! Node not found in DB: {}", nodeName);
}
```

**Örnek Kayıtlar:**
| node_id | node_name | node_type | vendor | branch_id |
|---------|-----------|-----------|--------|-----------|
| 12345 | eNodeB_TR_IST_001 | eNodeB | Huawei | 1 |
| 12346 | eNodeB_TR_ANK_001 | eNodeB | Huawei | 1 |
| 12347 | gNodeB_TR_IST_5G_001 | gNodeB | Huawei | 1 |
| 12348 | RNC_TR_IZM_001 | RNC | Huawei | 1 |

**Performans:**
- Her parse engine başlangıcında 1 kez yüklenir (memory cache)
- Tipik olarak 1000-5000 node içerir
- HashMap olarak tutulur: O(1) lookup time

---

## 5️⃣ PROCESSING RESULTS TABLES (2 tablo)

### 📅 t_content_date_result
**Kullanım:** Parse edilen dosyalardaki date range'leri kaydeder

**Repository:** `ContentDateResultRepository`
**Kullanıldığı Sınıf:** `ContentDateReader.java:64`

**SQL:**
```sql
INSERT INTO t_content_date_result (
    flow_id, flow_process_code, file_name,
    fragment_date, min_date, max_date
) VALUES (?, ?, ?, ?, ?, ?)
```

**Önemli Kolonlar:**
- `id` - Result ID (primary key)
- `flow_id` - Hangi flow
- `flow_process_code` - Flow process kodu
- `file_name` - Dosya adı (CSV)
- `fragment_date` - Fragment tarihi
- `min_date` - Dosyadaki minimum tarih
- `max_date` - Dosyadaki maximum tarih
- `created_time` - Kayıt zamanı

**Kod Kulanımı:**
```java
if (record.getIsActiveDiscoverContentDate()) {
    // Her CSV dosyası için parallel olarak date'ler analiz edilir
    ExecutorService executor = Executors.newFixedThreadPool(
        engineRecord.getDiscoverContentDateThreadCount());

    files.forEach(file -> {
        executor.execute(new ContentDateReader(
            applicationContext,
            new ContentDateReaderRecord().getRecord(file, engineRecord, parseMapper)
        ));
    });

    // ContentDateReader içinde saveAll yapılır
    contentDateResultRepository.saveAll(records.stream()
        .map(ContentDateResult::recordToEntity)
        .toList());
}
```

**Örnek Kayıtlar:**
| file_name | fragment_date | min_date | max_date |
|-----------|---------------|----------|----------|
| t_pm_cell_huawei-20260109.csv | 2026-01-09 00:00 | 2026-01-09 00:00 | 2026-01-09 23:45 |
| t_pm_sector_huawei-20260109.csv | 2026-01-09 00:00 | 2026-01-09 00:15 | 2026-01-09 23:45 |

**Kullanım Amacı:**
- Data quality kontrolü (eksik saatler var mı?)
- İzleme ve raporlama
- Data completeness metrikleri

---

### 💾 t_loader_result
**Kullanım:** Database bulk load sonuçlarını kaydeder

**Repository:** `LoaderResultRepository`
**Kullanıldığı Sınıf:** `Loader.java:103, 105`

**SQL:**
```sql
INSERT INTO t_loader_result (
    flow_id, flow_process_code, file_name,
    table_name, row_count, status,
    error_message, duration_ms
) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
```

**Önemli Kolonlar:**
- `id` - Result ID (primary key)
- `flow_id` - Hangi flow
- `flow_process_code` - Flow process kodu
- `file_name` - Yüklenen CSV dosyası
- `table_name` - Hedef tablo
- `row_count` - Yüklenen satır sayısı
- `status` - SUCCESS / FAILED
- `error_message` - Hata varsa mesaj
- `duration_ms` - Yükleme süresi (milisaniye)
- `created_time` - Kayıt zamanı

**Kod Kullanımı:**
```java
try {
    // Bulk load yap (PostgreSQL COPY, Oracle SQLLDR, vb.)
    loader.load();

    // Başarılı ise result kaydet
    LoaderResult result = LoaderResult.builder()
        .flowId(flowId)
        .fileName(csvFile.getName())
        .tableName(tableName)
        .rowCount(loadedRows)
        .status("SUCCESS")
        .durationMs(duration)
        .build();
    loaderResultRepository.save(result);

} catch (Exception e) {
    // Hata durumunda result kaydet
    loaderResultRepository.save(LoaderResult.builder()
        .status("FAILED")
        .errorMessage(e.getMessage())
        .build());
}
```

**Örnek Kayıtlar:**
| file_name | table_name | row_count | status | duration_ms |
|-----------|------------|-----------|--------|-------------|
| t_pm_cell_huawei-20260109.csv | t_pm_cell_huawei | 12500 | SUCCESS | 1850 |
| t_pm_sector_huawei-20260109.csv | t_pm_sector_huawei | 8300 | SUCCESS | 1200 |
| t_pm_enodeb_huawei-20260109.csv | t_pm_enodeb_huawei | 450 | FAILED | 500 |

**Kullanım Amacı:**
- Loader performans izleme
- Hata analizi ve troubleshooting
- Data volume metrikleri

---

## 6️⃣ DATA TABLES (50+ tablo)

Parse engine'ler tarafından parse edilen gerçek verilerin yazıldığı tablolar.

### Huawei eNodeB (4G) - 10+ tablo
| Tablo | Açıklama | Row Count (günlük) |
|-------|----------|-------------------|
| **t_pm_cell_huawei** | Cell-level performans metrikleri | ~10K-50K |
| **t_pm_sector_huawei** | Sector-level metrikleri | ~5K-20K |
| **t_pm_enodeb_huawei** | eNodeB sistem metrikleri | ~500-2K |
| **t_pm_ue_huawei** | User Equipment metrikleri | ~50K-200K |
| **t_pm_carrier_huawei** | Carrier metrikleri | ~5K-20K |
| **t_pm_erab_huawei** | E-RAB metrikleri | ~10K-40K |
| **t_pm_handover_huawei** | Handover metrikleri | ~20K-80K |
| **t_pm_volte_huawei** | VoLTE metrikleri | ~10K-50K |
| **t_pm_prb_huawei** | PRB utilization | ~10K-40K |
| **t_pm_qos_huawei** | QoS metrikleri | ~5K-20K |

### Huawei gNodeB (5G) - 10+ tablo
| Tablo | Açıklama | Row Count (günlük) |
|-------|----------|-------------------|
| **t_pm_nr_cell_huawei** | 5G NR cell performans | ~5K-20K |
| **t_pm_nr_sector_huawei** | 5G sector performans | ~3K-10K |
| **t_pm_gnodeb_huawei** | gNodeB sistem metrikleri | ~300-1K |
| **t_pm_nr_ue_huawei** | 5G UE metrikleri | ~20K-100K |
| **t_pm_nr_beam_huawei** | Beam management | ~10K-50K |
| **t_pm_nr_carrier_huawei** | 5G carrier metrikleri | ~5K-20K |
| **t_pm_nr_mimo_huawei** | Massive MIMO | ~5K-20K |

### Huawei RNC (3G) - 8+ tablo
### Huawei BSC (2G) - 6+ tablo
### Huawei Core (CS) - 5+ tablo
### Ericsson DRA - 3+ tablo

**Parse Flow:**
```
XML File (raw/)
  → SAX Parser
  → CSV Writer (result/)
  → Bulk Loader
  → Data Tables
```

---

## 🔄 PARSER FLOW VE DATABASE İLİŞKİLERİ

### Initialization Phase
```
1. ParseConstructor reads:
   ├── t_flow (flow bilgisi)
   ├── t_branch (branch bilgisi)
   ├── t_parse_engine (engine config)
   └── t_parse_component (hangi component)

2. ParseMapper reads:
   ├── t_parse_table (hedef tablolar)
   └── t_parse_column (kolon mappings)

3. NetworkNodeRepository reads:
   └── t_network_node (node ID mapping)
```

### Parse Phase
```
4. XML files parsed → CSV files generated
   (No DB interaction during parse)
```

### Auto Counter Phase
```
5. AutoCounterDefine writes:
   └── t_all_counter (discovered counters)
```

### Content Date Phase
```
6. ContentDateReader writes:
   └── t_content_date_result (date ranges)
```

### Loader Phase
```
7. LoaderFactory writes:
   ├── Data Tables (PM/CM/Conf tables - 50+)
   └── t_loader_result (load results)
```

---

## 📈 PERFORMANS METRİKLERİ

### Database Read Operations (Per Parse Run)
| Tablo | Read Count | Response Time |
|-------|-----------|---------------|
| t_flow | 1 | <10ms |
| t_branch | 1 | <10ms |
| t_parse_engine | 1 | <10ms |
| t_parse_component | 1 | <10ms |
| t_parse_table | 10-50 | <50ms |
| t_parse_column | 100-500 | <100ms |
| t_network_node | 1000-5000 | <200ms |
| t_all_counter | 1000-10000 | <500ms |

### Database Write Operations (Per Parse Run)
| Tablo | Write Count | Avg Duration |
|-------|-------------|--------------|
| t_all_counter | 0-1000 | <1s |
| t_content_date_result | 50-200 | <2s |
| t_loader_result | 50-200 | <1s |
| Data Tables (PM/CM/Conf) | 100K-5M rows | 2-10min |

### Bulk Load Performance
```
PostgreSQL COPY:
- 10K rows → ~1-2 seconds
- 100K rows → ~10-20 seconds
- 1M rows → ~100-200 seconds

Oracle SQLLDR:
- 10K rows → ~2-3 seconds
- 100K rows → ~20-30 seconds
- 1M rows → ~150-250 seconds
```

---

## 🛠️ SORUN GİDERME

### Problem: Node not found in DB
**Belirtiler:**
```
WARN: ! Node not found in DB: eNodeB_TR_IST_999
```

**Çözüm:**
1. t_network_node tablosuna node ekleyin:
```sql
INSERT INTO t_network_node (node_name, node_type, vendor, branch_id, is_active)
VALUES ('eNodeB_TR_IST_999', 'eNodeB', 'Huawei', 1, true);
```

2. Parse'ı yeniden çalıştırın

---

### Problem: Parse column not found
**Belirtiler:**
```
ERROR: ! Column mapping not found for: rsrp_new_metric
```

**Çözüm:**
1. t_parse_column tablosuna yeni kolon ekleyin:
```sql
INSERT INTO t_parse_column (
    parse_table_id, flow_id, column_name,
    xml_path, column_order_id, is_active
) VALUES (
    1, 1, 'rsrp_new_metric',
    'measValue/r[15]', 16, true
);
```

2. Hedef tabloya kolon ekleyin:
```sql
ALTER TABLE t_pm_cell_huawei ADD COLUMN rsrp_new_metric NUMERIC(10,2);
```

---

### Problem: Loader failed
**Belirtiler:**
```
ERROR: Loader failed for table t_pm_cell_huawei
```

**Kontrol:**
```sql
SELECT * FROM t_loader_result
WHERE status = 'FAILED'
ORDER BY created_time DESC
LIMIT 10;
```

**Olası Sebepler:**
- CSV format hatası
- Duplicate key violation
- Column mismatch
- Permission denied

---

## 📚 İLGİLİ DÖKÜMANLAR

- [PARSER_DATABASE_TABLES.md](PARSER_DATABASE_TABLES.md) - Tüm tablo şemaları
- [PARSE_DIAGRAMS_README.md](PARSE_DIAGRAMS_README.md) - UML diagramlar
- [ALL_DATABASE_TABLES.md](ALL_DATABASE_TABLES.md) - Tüm sistem tabloları
- [PARSER_TABLES_MISSING_IN_GIT.md](PARSER_TABLES_MISSING_IN_GIT.md) - Git karşılaştırması

---

**Son Güncelleme:** 2026-01-09
**Döküman Versiyonu:** 1.0
**Analiz Edilen Kod:** etl-master/parse & etl-master/library
