# Parser Modülünün Kullandığı Tablolar - Git Karşılaştırması

## Git'te MEVCUT Tablolar (17 tablo)

Bu tablolar zaten XML formatında git'te var:

```
✓ t_branch
✓ t_company
✓ t_connection
✓ t_domain
✓ t_flow
✓ t_flow_detail
✓ t_network_element
✓ t_network_item
✓ t_network_node
✓ t_path
✓ t_server
✓ t_transfer_component
✓ t_transfer_connection_result
✓ t_transfer_engine
✓ t_transfer_process_history
✓ t_unit
✓ t_vendor
```

---

## Git'te OLMAYAN Parser Tabloları (45+ tablo)

### 1. Parse Metadata & Configuration Tables (7 tablo)

```
❌ t_parse_engine
   - Parse engine konfigürasyonu
   - Thread sayıları, aktif özellikler

❌ t_parse_component
   - Component bilgileri
   - Base path, output formatı

❌ t_parse_table
   - Parse edilecek tabloların metadata'sı
   - Table name, object mappings

❌ t_parse_column
   - Kolon tanımları
   - Column name, type, XML path

❌ t_all_counter
   - Auto-discovered metrikler
   - Counter definitions

❌ t_all_table
   - Merkezi tablo registry

❌ t_all_column
   - Merkezi kolon registry
```

---

### 2. Parse Monitoring & History Tables (2 tablo)

```
❌ t_parse_process_history
   - Parse çalıştırma istatistikleri
   - Total files, success/fail counts, duration

❌ t_content_date_result
   - Parse edilen dosyalardaki tarih aralıkları
   - Min/max dates per file

❌ t_loader_result
   - Database loader sonuçları
   - Records loaded, duration
```

---

### 3. Huawei eNodeB (4G) Data Tables (10+ tablo)

```
❌ t_pm_cell_huawei
   - Cell-level performans metrikleri
   - RSRP, RSRQ, throughput, PRB usage

❌ t_pm_sector_huawei
   - Sector-level metrikler

❌ t_pm_enodeb_huawei
   - eNodeB sistem metrikleri

❌ t_pm_ue_huawei
   - UE (User Equipment) metrikleri

❌ t_pm_carrier_huawei
   - Carrier metrikleri

❌ t_cm_cell_huawei
   - Cell konfigürasyon verileri
   - PCI, EARFCN, bandwidth, TX power

❌ t_cm_sector_huawei
   - Sector konfigürasyonu

❌ t_cm_enodeb_huawei
   - eNodeB konfigürasyonu

❌ t_conf_cell_params_huawei
   - Cell parametreleri

❌ t_conf_enodeb_params_huawei
   - eNodeB parametreleri
```

---

### 4. Huawei gNodeB (5G) Data Tables (10+ tablo)

```
❌ t_pm_nr_cell_huawei
   - 5G NR cell performans metrikleri
   - SSB-RSRP, SSB-RSRQ, SSB-SINR

❌ t_pm_nr_sector_huawei
   - 5G sector metrikleri

❌ t_pm_gnodeb_huawei
   - gNodeB sistem metrikleri

❌ t_pm_nr_ue_huawei
   - 5G UE metrikleri

❌ t_pm_nr_beam_huawei
   - Beam management metrikleri

❌ t_cm_nr_cell_huawei
   - 5G cell konfigürasyonu

❌ t_cm_nr_sector_huawei
   - 5G sector konfigürasyonu

❌ t_cm_gnodeb_huawei
   - gNodeB konfigürasyonu

❌ t_conf_nr_cell_params_huawei
   - 5G cell parametreleri

❌ t_conf_gnodeb_params_huawei
   - gNodeB parametreleri
```

---

### 5. Huawei RNC (3G) Data Tables (8+ tablo)

```
❌ t_pm_cell_3g_huawei
   - 3G cell performans metrikleri
   - RSCP, Ec/No

❌ t_pm_rnc_huawei
   - RNC sistem metrikleri

❌ t_pm_nodeb_huawei
   - NodeB metrikleri

❌ t_pm_ue_3g_huawei
   - 3G UE metrikleri

❌ t_cm_cell_3g_huawei
   - 3G cell konfigürasyonu

❌ t_cm_rnc_huawei
   - RNC konfigürasyonu

❌ t_conf_cell_3g_params_huawei
   - 3G cell parametreleri

❌ t_conf_rnc_params_huawei
   - RNC parametreleri
```

---

### 6. Huawei BSC (2G) Data Tables (6+ tablo)

```
❌ t_pm_cell_2g_huawei
   - 2G cell performans metrikleri
   - RXLEV, RXQUAL

❌ t_pm_bsc_huawei
   - BSC sistem metrikleri

❌ t_pm_bts_huawei
   - BTS metrikleri

❌ t_cm_cell_2g_huawei
   - 2G cell konfigürasyonu

❌ t_cm_bsc_huawei
   - BSC konfigürasyonu

❌ t_conf_cell_2g_params_huawei
   - 2G cell parametreleri
```

---

### 7. Huawei CS (Core System) Data Tables (4+ tablo)

```
❌ t_pm_mme_huawei
   - MME performans metrikleri

❌ t_pm_sgw_huawei
   - SGW metrikleri

❌ t_pm_pgw_huawei
   - PGW metrikleri

❌ t_pm_hss_huawei
   - HSS metrikleri
```

---

### 8. Ericsson DRA Data Tables (2+ tablo)

```
❌ t_pm_dra_ericsson
   - DRA (Diameter Routing Agent) performans
   - Request/response counts, latency

❌ t_cm_dra_ericsson
   - DRA konfigürasyonu
```

---

## 📊 Özet

| Kategori | Git'te Var | Git'te Yok | Toplam |
|----------|-----------|-----------|---------|
| **Metadata & Config** | 0 | 7 | 7 |
| **Monitoring & History** | 1 (t_transfer_process_history) | 2 | 3 |
| **Network & Reference** | 4 (t_network_node, t_flow, t_vendor, t_branch) | 0 | 4 |
| **Data Tables - Huawei 4G** | 0 | 10+ | 10+ |
| **Data Tables - Huawei 5G** | 0 | 10+ | 10+ |
| **Data Tables - Huawei 3G** | 0 | 8+ | 8+ |
| **Data Tables - Huawei 2G** | 0 | 6+ | 6+ |
| **Data Tables - Huawei CS** | 0 | 4+ | 4+ |
| **Data Tables - Ericsson** | 0 | 2+ | 2+ |
| **TOPLAM** | **17** | **45+** | **60+** |

---

## 🎯 Git'te Eksik Olan Önemli Tablolar

### Öncelik 1: Parse Metadata (Mutlaka Gerekli)
```
1. t_parse_engine
2. t_parse_component
3. t_parse_table
4. t_parse_column
5. t_all_counter
```

Bu tablolar **olmadan** parser çalışmaz!

### Öncelik 2: Parse Monitoring
```
6. t_parse_process_history
7. t_content_date_result
8. t_loader_result
```

İzleme ve raporlama için gerekli.

### Öncelik 3: Data Tables (Vendor/Tech Spesifik)
```
9. t_pm_cell_huawei (4G PM)
10. t_pm_nr_cell_huawei (5G PM)
11. t_cm_cell_huawei (4G CM)
... (40+ tablo daha)
```

Parse edilen verilerin yazılacağı hedef tablolar.

---

## 🔍 Tablo Detayları

### Git'te VAR ama Parser'ın da kullandığı:

| Tablo | Kullanım |
|-------|----------|
| `t_network_node` | ✓ Parser - Node name → node_id mapping |
| `t_flow` | ✓ Parser - Flow tanımları |
| `t_vendor` | ✓ Parser - Vendor referansı |
| `t_branch` | ✓ Parser - Şube referansı |
| `t_company` | ✓ Parser - Company referansı (dolaylı) |
| `t_domain` | ✓ Parser - Domain referansı (dolaylı) |
| `t_unit` | ✓ Parser - Unit referansı (dolaylı) |

### Git'te VAR ama Parser KULLANMAZ:

| Tablo | Amaç |
|-------|------|
| `t_connection` | Transfer modülü için |
| `t_flow_detail` | Flow detayları (her iki modül de kullanabilir) |
| `t_network_element` | Network element tanımları |
| `t_network_item` | Network item tanımları |
| `t_path` | Transfer modülü paths |
| `t_server` | Transfer modülü SFTP servers |
| `t_transfer_component` | Transfer modülü component |
| `t_transfer_connection_result` | Transfer modülü results |
| `t_transfer_engine` | Transfer modülü engine config |

---

## 📥 Eksik Tabloları Eklemek İçin

### Seçenek 1: Database'den Export
```bash
# Parse metadata tables
pg_dump -t t_parse_engine -t t_parse_component -t t_parse_table \
        -t t_parse_column -t t_all_counter -t t_all_table \
        -t t_all_column --data-only --inserts > parser_metadata.sql

# Parse monitoring tables
pg_dump -t t_parse_process_history -t t_content_date_result \
        -t t_loader_result --data-only --inserts > parser_monitoring.sql

# Data tables (sadece schema)
pg_dump -t t_pm_cell_huawei -t t_pm_nr_cell_huawei \
        -t t_cm_cell_huawei --schema-only > parser_data_tables.sql
```

### Seçenek 2: XML Export
```sql
-- PostgreSQL XML export
COPY (SELECT * FROM t_parse_engine)
TO '/path/t_parse_engine.xml'
WITH (FORMAT csv, HEADER true);
```

---

Son güncelleme: 2026-01-07
