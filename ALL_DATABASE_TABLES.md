# ETL Sistem - Tüm Database Tabloları

Sistemdeki tüm database tablolarının kategorize edilmiş tam listesi.

## 📊 Toplam Tablo Sayısı

**Metadata & Configuration:** 58 tablo (Model dosyalarında tanımlı)
**Data Tables:** 50+ tablo (Vendor/teknoloji spesifik - dinamik)

**TOPLAM:** ~110 tablo

---

## 1️⃣ PARSE MODULE TABLES (10 tablo)

### Parse Configuration & Metadata (5 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_parse_engine** | Parse engine konfigürasyonu (thread counts, flags) |
| **t_parse_component** | Parse component bilgileri (paths, naming) |
| **t_parse_table** | Parse edilecek tabloların metadata'sı |
| **t_parse_column** | Kolon tanımları ve XML path mappings |
| **t_parse_process_history** | Parse çalıştırma istatistikleri |

### Metadata Registry (5 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_all_table** | Merkezi tablo registry (tüm tablolar) |
| **t_all_column** | Merkezi kolon registry |
| **t_all_counter** | Auto-discovered counter/metrik tanımları |
| **t_all_constraint** | Constraint tanımları |
| **t_all_index** | Index tanımları |
| **t_all_partition** | Partition tanımları |

---

## 2️⃣ TRANSFER MODULE TABLES (7 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_transfer_engine** | Transfer engine konfigürasyonu |
| **t_transfer_component** | Transfer component bilgileri |
| **t_transfer_connection_result** | Transfer dosya sonuçları |
| **t_transfer_connection_history** | Transfer connection geçmişi |
| **t_transfer_process_history** | Transfer çalıştırma istatistikleri |
| **t_connection** | SFTP connection tanımları |
| **t_connection_error** | Connection hataları |

---

## 3️⃣ AGGREGATE MODULE TABLES (5 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_aggregate_engine** | Aggregate engine konfigürasyonu |
| **t_aggregate_component** | Aggregate component bilgileri |
| **t_aggregate_description** | Aggregation tanımları |
| **t_aggregate_jobs** | Aggregate job'ları |
| **t_aggregate_process_history** | Aggregate çalıştırma istatistikleri |

---

## 4️⃣ EXPORT MODULE TABLES (3 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_export_engine** | Export engine konfigürasyonu |
| **t_export_component** | Export component bilgileri |
| **t_export_process_history** | Export çalıştırma istatistikleri |

---

## 5️⃣ ARCHIVE MODULE TABLES (3 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_archive_engine** | Archive engine konfigürasyonu |
| **t_archive_component** | Archive component bilgileri |
| **t_archive_process_history** | Archive çalıştırma istatistikleri |

---

## 6️⃣ NODIUS MODULE TABLES (3 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_nodius_engine** | Nodius engine konfigürasyonu |
| **t_nodius_component** | Nodius component bilgileri |
| **t_nodius_process_history** | Nodius çalıştırma istatistikleri |

---

## 7️⃣ NETWORK TABLES (3 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_network_node** | Network node'ları (eNodeB, gNodeB, RNC, BSC) |
| **t_network_element** | Network element tanımları |
| **t_network_item** | Network item'lar |

---

## 8️⃣ FLOW & REFERENCE TABLES (10 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_flow** | Flow tanımları (Parse, Transfer, Aggregate, vb.) |
| **t_flow_detail** | Flow detay bilgileri |
| **t_flow_process_history** | Flow çalıştırma geçmişi |
| **t_company** | Company/şirket bilgileri |
| **t_domain** | Domain tanımları |
| **t_organisation** | Organizasyon bilgileri |
| **t_branch** | Şube/bölge bilgileri |
| **t_unit** | Birim tanımları |
| **t_vendor** | Vendor bilgileri (Huawei, Ericsson, Nokia) |
| **t_path** | Path tanımları |

---

## 9️⃣ INFRASTRUCTURE TABLES (6 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_server** | SFTP server tanımları |
| **t_machine** | Machine/sunucu bilgileri |
| **t_manager** | Manager process bilgileri |
| **t_manager_monitoring** | Manager monitoring |
| **t_notification** | Notification tanımları |
| **t_notification_queue** | Notification kuyruğu |

---

## 🔟 PROCESSING RESULT TABLES (5 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_loader_result** | Bulk load sonuçları |
| **t_content_date_result** | Content date discovery sonuçları |
| **t_decompress_result** | Decompress işlem sonuçları |
| **t_decompress_error** | Decompress hataları |
| **t_query_column** | Query kolon tanımları |
| **t_query_table** | Query tablo tanımları |

---

## 1️⃣1️⃣ CONFIGURATION TABLES (1 tablo)

| Tablo | Açıklama |
|-------|----------|
| **t_app_environment** | Uygulama environment ayarları |

---

## 1️⃣2️⃣ DATA TABLES - HUAWEI eNodeB (4G) PM

Parse edilen gerçek verilerin yazıldığı tablolar:

| Tablo | Açıklama |
|-------|----------|
| **t_pm_cell_huawei** | Cell-level performans metrikleri |
| **t_pm_sector_huawei** | Sector-level metrikleri |
| **t_pm_enodeb_huawei** | eNodeB sistem metrikleri |
| **t_pm_ue_huawei** | User Equipment metrikleri |
| **t_pm_carrier_huawei** | Carrier metrikleri |
| **t_pm_erab_huawei** | E-RAB metrikleri |
| **t_pm_handover_huawei** | Handover metrikleri |
| **t_pm_volte_huawei** | VoLTE metrikleri |
| **t_pm_prb_huawei** | PRB utilization metrikleri |
| **t_pm_qos_huawei** | QoS metrikleri |

---

## 1️⃣3️⃣ DATA TABLES - HUAWEI eNodeB (4G) CM/CONF

| Tablo | Açıklama |
|-------|----------|
| **t_cm_cell_huawei** | Cell konfigürasyon |
| **t_cm_sector_huawei** | Sector konfigürasyon |
| **t_cm_enodeb_huawei** | eNodeB konfigürasyon |
| **t_cm_neighbor_huawei** | Neighbor relations |
| **t_conf_cell_params_huawei** | Cell parametreleri |
| **t_conf_enodeb_params_huawei** | eNodeB parametreleri |
| **t_conf_qos_huawei** | QoS parametreleri |
| **t_conf_mobility_huawei** | Mobility parametreleri |

---

## 1️⃣4️⃣ DATA TABLES - HUAWEI gNodeB (5G) PM

| Tablo | Açıklama |
|-------|----------|
| **t_pm_nr_cell_huawei** | 5G NR cell performans |
| **t_pm_nr_sector_huawei** | 5G sector performans |
| **t_pm_gnodeb_huawei** | gNodeB sistem metrikleri |
| **t_pm_nr_ue_huawei** | 5G UE metrikleri |
| **t_pm_nr_beam_huawei** | Beam management metrikleri |
| **t_pm_nr_carrier_huawei** | 5G carrier metrikleri |
| **t_pm_nr_mimo_huawei** | Massive MIMO metrikleri |
| **t_pm_nr_volte_huawei** | 5G VoLTE/VoNR metrikleri |
| **t_pm_nr_qos_huawei** | 5G QoS metrikleri |
| **t_pm_nr_sa_huawei** | 5G SA (Standalone) metrikleri |

---

## 1️⃣5️⃣ DATA TABLES - HUAWEI gNodeB (5G) CM/CONF

| Tablo | Açıklama |
|-------|----------|
| **t_cm_nr_cell_huawei** | 5G cell konfigürasyon |
| **t_cm_nr_sector_huawei** | 5G sector konfigürasyon |
| **t_cm_gnodeb_huawei** | gNodeB konfigürasyon |
| **t_cm_nr_neighbor_huawei** | 5G neighbor relations |
| **t_conf_nr_cell_params_huawei** | 5G cell parametreleri |
| **t_conf_gnodeb_params_huawei** | gNodeB parametreleri |
| **t_conf_nr_beam_huawei** | Beam management parametreleri |

---

## 1️⃣6️⃣ DATA TABLES - HUAWEI RNC (3G) PM

| Tablo | Açıklama |
|-------|----------|
| **t_pm_cell_3g_huawei** | 3G cell performans |
| **t_pm_rnc_huawei** | RNC sistem metrikleri |
| **t_pm_nodeb_huawei** | NodeB metrikleri |
| **t_pm_ue_3g_huawei** | 3G UE metrikleri |
| **t_pm_rab_huawei** | RAB metrikleri |
| **t_pm_handover_3g_huawei** | 3G handover metrikleri |
| **t_pm_hsdpa_huawei** | HSDPA metrikleri |
| **t_pm_hsupa_huawei** | HSUPA metrikleri |

---

## 1️⃣7️⃣ DATA TABLES - HUAWEI RNC (3G) CM/CONF

| Tablo | Açıklama |
|-------|----------|
| **t_cm_cell_3g_huawei** | 3G cell konfigürasyon |
| **t_cm_rnc_huawei** | RNC konfigürasyon |
| **t_cm_nodeb_huawei** | NodeB konfigürasyon |
| **t_cm_neighbor_3g_huawei** | 3G neighbor relations |
| **t_conf_cell_3g_params_huawei** | 3G cell parametreleri |
| **t_conf_rnc_params_huawei** | RNC parametreleri |

---

## 1️⃣8️⃣ DATA TABLES - HUAWEI BSC (2G) PM

| Tablo | Açıklama |
|-------|----------|
| **t_pm_cell_2g_huawei** | 2G cell performans |
| **t_pm_bsc_huawei** | BSC sistem metrikleri |
| **t_pm_bts_huawei** | BTS metrikleri |
| **t_pm_trx_huawei** | TRX metrikleri |
| **t_pm_tch_huawei** | TCH metrikleri |
| **t_pm_handover_2g_huawei** | 2G handover metrikleri |

---

## 1️⃣9️⃣ DATA TABLES - HUAWEI BSC (2G) CM/CONF

| Tablo | Açıklama |
|-------|----------|
| **t_cm_cell_2g_huawei** | 2G cell konfigürasyon |
| **t_cm_bsc_huawei** | BSC konfigürasyon |
| **t_cm_bts_huawei** | BTS konfigürasyon |
| **t_cm_neighbor_2g_huawei** | 2G neighbor relations |
| **t_conf_cell_2g_params_huawei** | 2G cell parametreleri |

---

## 2️⃣0️⃣ DATA TABLES - HUAWEI CORE (CS) PM

| Tablo | Açıklama |
|-------|----------|
| **t_pm_mme_huawei** | MME performans metrikleri |
| **t_pm_sgw_huawei** | SGW metrikleri |
| **t_pm_pgw_huawei** | PGW metrikleri |
| **t_pm_hss_huawei** | HSS metrikleri |
| **t_pm_pcrf_huawei** | PCRF metrikleri |

---

## 2️⃣1️⃣ DATA TABLES - ERICSSON PM

| Tablo | Açıklama |
|-------|----------|
| **t_pm_dra_ericsson** | DRA (Diameter Routing Agent) performans |
| **t_pm_pcrf_ericsson** | PCRF metrikleri |
| **t_pm_hss_ericsson** | HSS metrikleri |

---

## 2️⃣2️⃣ DATA TABLES - ERICSSON CM

| Tablo | Açıklama |
|-------|----------|
| **t_cm_dra_ericsson** | DRA konfigürasyon |
| **t_cm_pcrf_ericsson** | PCRF konfigürasyon |

---

## 📊 Tablo Sayısı Özeti

| Kategori | Tablo Sayısı |
|----------|--------------|
| **1. Parse Module** | 10 |
| **2. Transfer Module** | 7 |
| **3. Aggregate Module** | 5 |
| **4. Export Module** | 3 |
| **5. Archive Module** | 3 |
| **6. Nodius Module** | 3 |
| **7. Network Tables** | 3 |
| **8. Flow & Reference** | 10 |
| **9. Infrastructure** | 6 |
| **10. Processing Results** | 5 |
| **11. Configuration** | 1 |
| **12-22. Data Tables (Vendor Specific)** | 50+ |
| **TOPLAM** | **~110 tablo** |

---

## 🎯 Parser Modülünün Kullandığı Tablolar

### Doğrudan Kullanılan (15 tablo):

```
✓ t_parse_engine
✓ t_parse_component
✓ t_parse_table
✓ t_parse_column
✓ t_parse_process_history
✓ t_all_table
✓ t_all_column
✓ t_all_counter
✓ t_network_node
✓ t_flow
✓ t_vendor
✓ t_branch
✓ t_content_date_result
✓ t_loader_result
✓ t_app_environment
```

### Parse Output Tabloları (50+ tablo):

```
✓ t_pm_cell_huawei (ve tüm PM tabloları)
✓ t_cm_cell_huawei (ve tüm CM tabloları)
✓ t_conf_cell_params_huawei (ve tüm Conf tabloları)
... (vendor ve teknolojiye göre 50+ tablo)
```

### Dolaylı Kullanılan (Referans):

```
✓ t_company
✓ t_domain
✓ t_organisation
✓ t_unit
```

**Parser Toplam:** ~65+ tablo kullanıyor

---

## 📋 Alfabetik Tam Liste (Metadata Tables)

```
1.  t_aggregate_component
2.  t_aggregate_description
3.  t_aggregate_engine
4.  t_aggregate_jobs
5.  t_aggregate_process_history
6.  t_all_column
7.  t_all_constraint
8.  t_all_counter
9.  t_all_index
10. t_all_partition
11. t_all_table
12. t_app_environment
13. t_archive_component
14. t_archive_engine
15. t_archive_process_history
16. t_branch
17. t_company
18. t_connection
19. t_connection_error
20. t_content_date_result
21. t_decompress_error
22. t_decompress_result
23. t_domain
24. t_export_component
25. t_export_engine
26. t_export_process_history
27. t_flow
28. t_flow_detail
29. t_flow_process_history
30. t_loader_result
31. t_machine
32. t_manager
33. t_manager_monitoring
34. t_network_element
35. t_network_item
36. t_network_node
37. t_nodius_component
38. t_nodius_engine
39. t_nodius_process_history
40. t_notification
41. t_notification_queue
42. t_organisation
43. t_parse_column
44. t_parse_component
45. t_parse_engine
46. t_parse_process_history
47. t_parse_table
48. t_path
49. t_query_column
50. t_query_table
51. t_server
52. t_transfer_component
53. t_transfer_connection_history
54. t_transfer_connection_result
55. t_transfer_engine
56. t_transfer_process_history
57. t_unit
58. t_vendor
```

**+ 50+ Data Tables (PM, CM, Conf)**

---

Son güncelleme: 2026-01-07
