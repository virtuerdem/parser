# Parser Modülü - Database Tabloları

Parser modülünde kullanılan tüm database tablolarının detaylı listesi.

## 📊 Tablo Kategorileri

Parser modülü **3 kategoride** database tabloları kullanır:

1. **Metadata & Configuration Tables** - Parser konfigürasyonu ve metadata
2. **Monitoring & History Tables** - İzleme, log ve geçmiş
3. **Data Tables** - Parse edilen verinin yazıldığı hedef tablolar

---

## 1️⃣ Metadata & Configuration Tables

Parser'ın çalışması için gerekli konfigürasyon ve metadata tabloları.

### t_parse_engine
**Amaç:** Parse engine konfigürasyonu

**Kolonlar:**
- `id` - Primary key
- `flow_id` - Hangi flow için (FK → t_flow)
- `is_active_fetch_tables` - Metadata tabloları oluştur mu?
- `is_active_pre_parse` - Ön işlem aktif mi?
- `is_active_on_parse` - Ana parsing aktif mi?
- `is_active_post_parse` - Son işlem aktif mi?
- `is_active_auto_counter` - Auto counter discovery aktif mi?
- `is_active_discover_content_date` - Date discovery aktif mi?
- `is_active_call_procedure` - SP çağır mı?
- `is_active_call_aggregate` - Aggregation yap mı?
- `is_active_call_export` - Export et mi?
- `on_parse_thread_count` - XML parsing thread sayısı (default: 8)
- `loader_thread_count` - DB load thread sayısı (default: 8)
- `discover_content_date_thread_count` - Date discovery thread (default: 8)
- `created_time`, `created_by`, `updated_time`, `updated_by`

**Kullanım:**
```java
ParseEngineRecord config = parseEngineRepository.findByFlowId(flowId);
// Parse engine'i bu config'e göre çalıştır
```

**Örnek Data:**
```
flow_id: 111
is_active_on_parse: true
on_parse_thread_count: 8
is_active_auto_counter: true
is_active_discover_content_date: true
```

---

### t_parse_component
**Amaç:** Parse component bilgisi (flow path, naming, vb.)

**Kolonlar:**
- `id` - Primary key
- `flow_id` - Flow referansı
- `component_name` - Component ismi (örn: "HW_ENB_PM_PARSE")
- `component_code` - Unique code
- `base_path` - Ana klasör yolu
- `result_file_extension` - Output dosya uzantısı (örn: ".csv")
- `result_file_delimiter` - CSV delimiter (örn: ",")
- `is_active`

**Kullanım:**
```java
ParseComponent component = parseComponentRepository.findByFlowId(flowId);
String basePath = component.getBasePath(); // /data/parse/HW_ENB_PM/
```

---

### t_parse_table
**Amaç:** Parse edilecek XML'lerdeki her bir tablo için metadata

**Kolonlar:**
- `id` - Primary key
- `flow_id` - Flow referansı
- `all_table_id` - Referans (FK → t_all_table)
- `schema_name` - Database schema (örn: "public", "vendor")
- `table_name` - Hedef tablo ismi (örn: "t_pm_cell_huawei")
- `object_type` - Obje tipi (örn: "Cell", "eNodeB")
- `object_key` - Anahtar (örn: "measInfo")
- `object_key_lookup` - Lookup değer
- `object_key_description` - Açıklama
- `date_column_index` - Tarih kolonunun index'i
- `date_column_name` - Tarih kolonu ismi
- `result_file_delimiter` - CSV delimiter
- `node_type`, `sub_node_type` - Node tipleri (eNodeB, gNodeB, RNC, BSC)
- `element_type`, `sub_element_type` - Element tipleri (Cell, Sector)
- `item_type`, `sub_item_type` - Item tipleri
- `table_type`, `sub_table_type` - Tablo tipleri (PM, CM, Conf)
- `network_type`, `sub_network_type` - Network tipleri (4G, 5G, 3G, 2G)
- `group_type` - Grup tipi
- `data_type` - Veri tipi (Performance, Configuration)
- `data_source` - Veri kaynağı (Huawei, Ericsson)
- `table_group` - Tablo grubu
- `data_group` - Veri grubu
- `loader_target` - Loader hedefi
- `is_active`

**Kullanım:**
```java
List<ParseTable> tables = parseTableRepository.findAllByFlowIdAndIsActive(flowId, true);
// Her tablo için mapping bilgisi
```

**Örnek Data:**
```
table_name: t_pm_cell_huawei
object_key: Cell
node_type: eNodeB
table_type: PM
network_type: 4G
data_source: Huawei
```

---

### t_parse_column
**Amaç:** Her tablodaki kolonların metadata'sı

**Kolonlar:**
- `id` - Primary key
- `parse_table_id` - FK → t_parse_table
- `all_column_id` - FK → t_all_column
- `column_name` - Kolon ismi (örn: "rsrp", "rsrq", "throughput")
- `column_type` - Veri tipi (integer, bigint, varchar, timestamp)
- `column_index` - Sıra numarası
- `xml_path` - XML'deki path (örn: "measValue/measResults")
- `default_value` - Default değer
- `is_nullable` - Null olabilir mi?
- `is_primary_key` - Primary key mi?
- `column_description` - Açıklama
- `is_active`

**Kullanım:**
```java
List<ParseColumn> columns = parseColumnRepository.findByParseTableId(tableId);
// XML'den bu kolonları extract et
```

**Örnek Data:**
```
table: t_pm_cell_huawei
column_name: rsrp
column_type: integer
xml_path: measResults[0]
```

---

### t_all_counter
**Amaç:** Auto-discovery ile bulunan counter/metrik tanımları

**Kolonlar:**
- `id` - Primary key
- `company_id`, `domain_id`, `organisation_id` - Organizasyon bilgileri
- `vendor_id` - Vendor (Huawei, Ericsson)
- `unit_id`, `branch_id` - Birim ve şube
- `flow_id` - Flow referansı
- `node_group_type` - Node grubu (eNodeB, gNodeB, RNC)
- `counter_group_type` - Counter grubu (Cell, Sector)
- `counter_group_key` - Grup anahtarı (measInfo ID)
- `counter_key` - Counter anahtarı (metric name: RSRP, RSRQ)
- `model_type` - Model tipi
- `counter_group_lookup` - Lookup
- `counter_lookup` - Counter lookup
- `counter_group_description` - Grup açıklaması
- `counter_description` - Counter açıklaması
- `data_type` - Veri tipi
- `counter_unit` - Birim (dBm, Mbps, %)
- `is_active`

**Kullanım:**
```java
// Parse sırasında yeni metric bulunduğunda
autoCounterDefine.collect(new CounterDefineRecord(
    nodeGroupType: "eNodeB",
    counterGroupType: "Cell",
    counterKey: "RSRP"
));

// Engine bitiminde kaydet
autoCounterDefine.save(engineRecord);
```

**Örnek Auto-Discovery:**
```xml
<!-- XML'de yeni metrik bulundu -->
<measTypes>RSRP RSRQ NewMetric_XYZ</measTypes>
```

↓ Auto-discovery

```sql
-- Otomatik olarak kaydedilir
INSERT INTO t_all_counter (counter_key, ...)
VALUES ('NewMetric_XYZ', ...);
```

---

### t_all_table
**Amaç:** Tüm sistem tablolarının merkezi kayıt yeri

**Kolonlar:**
- `id` - Primary key
- `schema_name` - Schema
- `table_name` - Tablo ismi
- `table_type` - Tablo tipi
- `table_description` - Açıklama
- `is_active`

**Kullanım:**
Parse table tanımlarken referans olarak kullanılır.

---

### t_all_column
**Amaç:** Tüm kolon tanımlarının merkezi kaydı

**Kolonlar:**
- `id` - Primary key
- `all_table_id` - FK → t_all_table
- `column_name` - Kolon ismi
- `column_type` - Veri tipi
- `column_description` - Açıklama
- `is_active`

---

## 2️⃣ Monitoring & History Tables

Parse işlemlerinin izlenmesi ve geçmişi.

### t_parse_process_history
**Amaç:** Her parse çalıştırmasının istatistikleri

**Kolonlar:**
- `id` - Primary key
- `flow_process_code` - Unique run ID (örn: "20240708100000000111")
- `flow_id` - Flow referansı
- `total_files` - Toplam parse edilen dosya sayısı
- `success_count` - Başarılı dosya sayısı
- `failure_count` - Başarısız dosya sayısı
- `total_size_bytes` - İşlenen toplam veri boyutu
- `total_records` - Toplam kayıt sayısı
- `execution_duration_ms` - Çalışma süresi (milisaniye)
- `start_time` - Başlangıç zamanı
- `end_time` - Bitiş zamanı
- `error_message` - Hata mesajı (varsa)
- `is_success` - Başarılı mı?
- `created_time`

**Kullanım:**
```java
ParseProcessHistory history = ParseProcessHistory.builder()
    .flowId(flowId)
    .totalFiles(150)
    .successCount(149)
    .failureCount(1)
    .executionDurationMs(2280000) // 38 minutes
    .build();

parseProcessHistoryRepository.save(history);
```

**Örnek Data:**
```
flow_process_code: 20240708100000000111
total_files: 150
success_count: 149
failure_count: 1
execution_duration_ms: 2280000 (38 min)
start_time: 2024-07-08 10:00:00
end_time: 2024-07-08 10:38:00
```

---

### t_content_date_result
**Amaç:** Parse edilen dosyalardaki tarih aralıkları

**Kolonlar:**
- `id` - Primary key
- `flow_id` - Flow referansı
- `table_name` - Tablo ismi
- `file_name` - Dosya ismi
- `min_date` - En eski tarih
- `max_date` - En yeni tarih
- `record_count` - Kayıt sayısı
- `created_time`

**Kullanım:**
Parse sonrası CSV dosyalarındaki tarih kolonları analiz edilir.

**Örnek Data:**
```
table_name: t_pm_cell_huawei
file_name: t_pm_cell_huawei-20240708.csv
min_date: 2024-07-08 00:00:00
max_date: 2024-07-08 23:45:00
record_count: 96000 (96 periods × 1000 cells)
```

---

### t_loader_result
**Amaç:** Database loader sonuçları

**Kolonlar:**
- `id` - Primary key
- `flow_id` - Flow referansı
- `table_name` - Yüklenen tablo
- `file_name` - CSV dosya ismi
- `records_loaded` - Yüklenen kayıt sayısı
- `load_duration_ms` - Yükleme süresi
- `is_success` - Başarılı mı?
- `error_message` - Hata (varsa)
- `created_time`

**Örnek Data:**
```
table_name: t_pm_cell_huawei
file_name: t_pm_cell_huawei-20240708.csv
records_loaded: 96000
load_duration_ms: 5400 (5.4 seconds)
is_success: true
```

---

## 3️⃣ Data Tables (Parse Output)

Parse edilen verilerin yazıldığı **gerçek veri tabloları**.

Bu tablolar **vendor ve teknolojiye göre** değişir. Parser modülü bu tablolara **CSV formatında** veri yazar, sonra bulk load eder.

### Huawei eNodeB (4G) PM Tabloları

#### t_pm_cell_huawei
**Amaç:** eNodeB cell-level performans metrikleri

**Tipik Kolonlar:**
- `node_id` - FK → t_network_node
- `fragment_date` - Ölçüm zamanı (15 dakikalık period)
- `plmn` - PLMN ID
- `enodeb_id` - eNodeB ID
- `cell_id` - Cell ID
- `rsrp` - Reference Signal Received Power (dBm)
- `rsrq` - Reference Signal Received Quality (dB)
- `rssi` - Received Signal Strength Indicator
- `sinr` - Signal to Interference plus Noise Ratio
- `throughput_dl` - Downlink throughput (Kbps)
- `throughput_ul` - Uplink throughput (Kbps)
- `prb_usage_dl` - Downlink PRB usage (%)
- `prb_usage_ul` - Uplink PRB usage (%)
- `active_users` - Aktif kullanıcı sayısı
- `rrc_connections` - RRC connection sayısı
- `handover_success_rate` - Handover başarı oranı (%)

**Örnek Data:**
```
node_id: 12345
fragment_date: 2024-07-08 10:00:00
enodeb_id: 1
cell_id: 1
rsrp: -75
rsrq: -8
throughput_dl: 102400
prb_usage_dl: 45.2
active_users: 120
```

---

#### t_pm_sector_huawei
**Amaç:** Sector-level metrikler

**Kolonlar:**
- Sector bazlı performans metrikleri
- Carrier aggregation metrikleri
- MIMO statistics

---

#### t_pm_enodeb_huawei
**Amaç:** eNodeB-level sistem metrikleri

**Kolonlar:**
- CPU usage
- Memory usage
- Board temperature
- Link status

---

### Huawei eNodeB (4G) CM/Conf Tabloları

#### t_cm_cell_huawei
**Amaç:** Cell konfigürasyon verileri

**Kolonlar:**
- `cell_name` - Cell ismi
- `pci` - Physical Cell ID
- `earfcn_dl` - Downlink EARFCN
- `bandwidth` - Channel bandwidth (MHz)
- `tx_power` - Transmit power (dBm)
- `tac` - Tracking Area Code
- `neighbor_relations` - Komşu cell'ler

---

#### t_conf_cell_params_huawei
**Amaç:** Cell parametreleri

**Kolonlar:**
- Detaylı cell parametreleri
- Algoritma ayarları
- Threshold değerleri

---

### Huawei gNodeB (5G) Tabloları

#### t_pm_nr_cell_huawei
**Amaç:** 5G NR cell performans metrikleri

**Kolonlar:**
- `nr_cell_id` - NR Cell ID
- `ssb_rsrp` - SSB-RSRP
- `ssb_rsrq` - SSB-RSRQ
- `ssb_sinr` - SSB-SINR
- `throughput_dl` - 5G downlink throughput
- `beam_management_metrics` - Beam yönetim metrikleri
- `massive_mimo_metrics` - Massive MIMO metrikleri

---

### Huawei RNC (3G) Tabloları

#### t_pm_cell_3g_huawei
**Amaç:** 3G cell performans metrikleri

**Kolonlar:**
- `rnc_id` - RNC ID
- `cell_id_3g` - 3G Cell ID
- `rscp` - Received Signal Code Power
- `ec_no` - Ec/No (chip energy to noise ratio)
- `soft_handover_rate` - Soft handover oranı

---

### Huawei BSC (2G) Tabloları

#### t_pm_cell_2g_huawei
**Amaç:** 2G cell performans metrikleri

**Kolonlar:**
- `bsc_id` - BSC ID
- `cell_id_2g` - 2G Cell ID
- `rxlev` - Received Signal Level
- `rxqual` - Received Signal Quality
- `tch_seizure_rate` - TCH seizure rate

---

### Ericsson DRA Tabloları

#### t_pm_dra_ericsson
**Amaç:** DRA (Diameter Routing Agent) performans metrikleri

**Kolonlar:**
- `dra_node` - DRA node ismi
- `diameter_requests` - Diameter request sayısı
- `diameter_responses` - Response sayısı
- `response_time_avg` - Ortalama yanıt süresi
- `error_rate` - Hata oranı

---

## 📋 Network & Reference Tables

Parse işlemi sırasında kullanılan referans tabloları.

### t_network_node
**Amaç:** Aktif network node'ları (eNodeB, gNodeB, RNC, BSC)

**Kolonlar:**
- `id` - Primary key (node_id)
- `node_name` - Node ismi (örn: "eNodeB_001")
- `branch_id` - Şube referansı
- `vendor_id` - Vendor (Huawei, Ericsson)
- `node_type` - Tip (eNodeB, gNodeB, RNC, BSC)
- `ip_address` - IP adresi
- `location` - Lokasyon
- `is_active` - Aktif mi?

**Kullanım:**
```java
Map<String, Long> nodeIds = networkNodeRepository
    .findByBranchIdAndIsActive(branchId, true)
    .stream()
    .collect(Collectors.toMap(
        NetworkNode::getNodeName,
        NetworkNode::getId
    ));

// Parse sırasında node_name'den node_id'ye mapping
Long nodeId = nodeIds.get("eNodeB_001"); // → 12345
```

---

### t_flow
**Amaç:** Flow tanımları

**Kolonlar:**
- `id` - Flow ID
- `flow_code` - Flow code (örn: "VF_TR_D_HW_ENB-PM-PARSE")
- `flow_name` - Flow ismi
- `is_active`

---

### t_vendor
**Amaç:** Vendor bilgileri

**Kolonlar:**
- `id` - Vendor ID
- `vendor_name` - Vendor ismi (Huawei, Ericsson, Nokia)

---

### t_branch
**Amaç:** Şube/bölge bilgileri

---

## 📊 Tablo İlişkileri

```
t_flow
  ↓ (1:1)
t_parse_engine ────→ Configuration
  ↓ (1:1)
t_parse_component ─→ Paths & Naming
  ↓ (1:N)
t_parse_table ─────→ Table Metadata
  ↓ (1:N)
t_parse_column ────→ Column Metadata

t_network_node ────→ Active Nodes (for node_id lookup)

Parse Process:
  ↓
CSV Files (t_pm_cell_huawei-20240708.csv)
  ↓
Bulk Load
  ↓
t_pm_cell_huawei ──→ Actual Data
t_pm_sector_huawei
t_cm_cell_huawei
... (vendor/tech specific tables)

Monitoring:
t_parse_process_history ─→ Run statistics
t_content_date_result ───→ Date ranges
t_loader_result ─────────→ Load results

Auto-Discovery:
t_all_counter ───────────→ Discovered metrics
```

---

## 🎯 Toplam Tablo Sayısı

| Kategori | Tablo Sayısı |
|----------|-------------|
| **Metadata & Configuration** | 7 tablo |
| **Monitoring & History** | 3 tablo |
| **Network & Reference** | 4 tablo |
| **Data Tables (örnek)** | ~50+ tablo (vendor/tech'e göre) |
| **TOPLAM** | **60+ tablo** |

---

## 💾 Tipik Veri Boyutları

| Tablo Tipi | Kayıt Sayısı | Boyut |
|------------|--------------|-------|
| **t_parse_table** | ~50 per flow | KB seviyesi |
| **t_parse_column** | ~500 per flow | KB seviyesi |
| **t_all_counter** | ~5,000 | MB seviyesi |
| **t_pm_cell_huawei** | ~10M per month | **GB seviyesi** |
| **t_parse_process_history** | ~1,000 per year | MB seviyesi |

---

## 🔄 Parse Flow & Table Usage

```
1. startEngine()
   ↓ READ
   t_parse_engine (config)
   t_parse_component (paths)

2. getTables()
   ↓ READ
   t_parse_table (table mappings)
   t_parse_column (column mappings)

3. onEngine()
   ↓ READ
   t_network_node (active nodes)

   ↓ PARSE XML

   ↓ WRITE CSV
   t_pm_cell_huawei-20240708.csv
   t_pm_sector_huawei-20240708.csv
   ...

4. Auto Counter Discovery
   ↓ WRITE
   t_all_counter (new metrics)

5. Content Date Discovery
   ↓ WRITE
   t_content_date_result

6. Bulk Load
   ↓ BULK INSERT
   t_pm_cell_huawei
   t_pm_sector_huawei
   t_cm_cell_huawei
   ... (all data tables)

7. Save Statistics
   ↓ WRITE
   t_parse_process_history
   t_loader_result
```

---

## 📚 Örnek Query'ler

### Parse Configuration Sorgulama
```sql
SELECT pe.*, pc.*
FROM t_parse_engine pe
JOIN t_parse_component pc ON pc.flow_id = pe.flow_id
WHERE pe.flow_id = 111;
```

### Table Metadata Alma
```sql
SELECT pt.table_name, pt.object_key, pc.column_name, pc.column_type
FROM t_parse_table pt
JOIN t_parse_column pc ON pc.parse_table_id = pt.id
WHERE pt.flow_id = 111
  AND pt.is_active = true
ORDER BY pt.table_name, pc.column_index;
```

### Parse İstatistikleri
```sql
SELECT
    flow_id,
    COUNT(*) as total_runs,
    AVG(total_files) as avg_files,
    AVG(execution_duration_ms / 1000 / 60) as avg_duration_minutes,
    SUM(total_records) as total_records_parsed
FROM t_parse_process_history
WHERE created_time >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY flow_id;
```

### Auto-Discovered Counters
```sql
SELECT
    node_group_type,
    counter_group_type,
    counter_key,
    counter_description,
    data_type,
    counter_unit
FROM t_all_counter
WHERE flow_id = 111
ORDER BY node_group_type, counter_group_type, counter_key;
```

### Parse Edilen Veri Sorgulama
```sql
SELECT
    n.node_name,
    p.fragment_date,
    p.cell_id,
    p.rsrp,
    p.rsrq,
    p.throughput_dl,
    p.active_users
FROM t_pm_cell_huawei p
JOIN t_network_node n ON n.id = p.node_id
WHERE p.fragment_date >= '2024-07-08 00:00:00'
  AND p.fragment_date < '2024-07-09 00:00:00'
  AND n.node_name = 'eNodeB_001'
ORDER BY p.fragment_date;
```

---

Son güncelleme: 2026-01-07
Oluşturan: Claude Code
