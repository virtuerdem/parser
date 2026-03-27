# Parse Flow - Activity Diagram Sunumu

**Hazırlayan:** Parser Ekibi
**Tarih:** Şubat 2026
**Konu:** XML Parse Engine İş Akışı ve Paralel İşleme Mimarisi

---

## 📋 İçindekiler

1. [Genel Bakış](#genel-bakış)
2. [Parse Flow Aşamaları](#parse-flow-aşamaları)
3. [Paralel İşleme Mimarisi](#paralel-i̇şleme-mimarisi)
4. [Ana Fazlar Detayı](#ana-fazlar-detayı)
5. [Performans ve Ölçeklenebilirlik](#performans-ve-ölçeklenebilirlik)
6. [Konfigürasyon ve Esneklik](#konfigürasyon-ve-esneklik)
7. [Sonuç ve Faydalar](#sonuç-ve-faydalar)

---

## 🎯 Genel Bakış

### Parse Engine Nedir?

Parse Engine, telekomünikasyon ağ ekipmanlarından (4G/5G) gelen **XML formatındaki performans ve konfigürasyon verilerini** işleyen, dönüştüren ve veritabanına yükleyen bir ETL (Extract, Transform, Load) sistemidir.

### Temel Hedefler

- ✅ **Yüksek Performans:** 150 XML dosyasını paralel işleme ile 5 saatten 38 dakikaya indirme
- ✅ **Ölçeklenebilirlik:** Thread pool boyutu ile ayarlanabilir paralellik
- ✅ **Esneklik:** Vendor-specific ön/son işleme desteği
- ✅ **Güvenilirlik:** Hata yönetimi ve duplicate temizleme

### İşlenen Veri Türleri

| Veri Türü | Açıklama | Örnek Dosya |
|-----------|----------|-------------|
| **PM (Performance Management)** | Performans metrikleri | `*_eNodeB_*.xml`, `*_gNodeB_*.xml` |
| **CM (Configuration Management)** | Konfigürasyon verileri | `*_RNC_*.xml`, `*_BSC_*.xml` |
| **Conf (Configuration)** | Parametre ayarları | Vendor-specific formatlar |

---

## 🔄 Parse Flow Aşamaları

### Genel Akış Şeması

```
Start
  ↓
1. Motor Başlatma ve Hazırlık
  ↓
2. Metadata Yükleme
  ↓
3. Ön İşleme (Opsiyonel)
  ↓
4. Ana Parsing Fazı (Paralel)
  ↓
5. Son İşleme (Opsiyonel)
  ↓
6. Auto Counter Tanımlama (Opsiyonel)
  ↓
7. İçerik Tarihi Keşfi (Paralel - Opsiyonel)
  ↓
8. Veritabanına Yükleme (Paralel)
  ↓
9. Duplicate Temizleme ve Agregasyon
  ↓
10. Prosedür ve Export İşlemleri
  ↓
End
```

### Aşama Detayları

#### 1️⃣ Motor Başlatma ve Hazırlık

**Aktiviteler:**
- `startEngine(ParseEngineRecord)` - Transfer Module veya Scheduler tarafından tetiklenir
- `preparePaths()` - Dizin yapısı oluşturulur:
  - `/raw/` - Input XML dosyaları
  - `/result/` - Parse edilen CSV çıktılar
  - `/error/` - Hatalı parse'lar

**Input:** flowId, paths, config
**Output:** Hazır çalışma ortamı

---

#### 2️⃣ Metadata Yükleme

**Aktiviteler:**
- `fetchTables()` *(opsiyonel)* - DB schema'dan metadata tabloları üretir
- `getTables()` - Repository'den tablo metadata'sını yükler

**Kontrol Noktası:**
```
isActiveFetchTables? → YES: fetchTables() → getTables()
                    → NO: getTables()
```

**Çıktı:** Tablo yapıları, kolon isimleri, veri tipleri

---

#### 3️⃣ Ön İşleme (Vendor-Specific)

**Aktivite:** `preEngine()`

**Kontrol:**
```
isActivePreParse? → YES: preEngine()
                  → NO: Atla
```

**Amaç:** Vendor-specific hazırlık işlemleri (örn: Huawei, Ericsson, Nokia)

---

## 🚀 Paralel İşleme Mimarisi

### Neden Paralel İşleme?

**Seri İşleme (Eski Yöntem):**
- 150 dosya × 2 dakika/dosya = **300 dakika (5 saat)**

**Paralel İşleme (Yeni Yöntem):**
- 150 dosya ÷ 8 thread = **~38 dakika**

**Hız Artışı:** **7.9x daha hızlı** ⚡

---

### Paralel İşleme Noktaları

Parse Flow'da **3 paralel işleme noktası** var:

#### 1. XML Parsing (Ana Faz)
```
Create Thread Pool (8 threads)
         ↓
    ┌────┴────┬────────┬────────┐
    ↓         ↓        ↓        ↓
Handler 1  Handler 2  ...   Handler N
    ↓         ↓        ↓        ↓
 Parse      Parse    Parse    Parse
 File 1     File 2   File 3   File N
    ↓         ↓        ↓        ↓
    └────┬────┴────────┴────────┘
         ↓
   Synchronization
```

#### 2. Content Date Discovery
```
Read CSV Files
    ↓
┌───┴───┬─────────┬─────────┐
↓       ↓         ↓         ↓
CSV 1   CSV 2     ...      CSV N
Analyze Analyze   Analyze  Analyze
↓       ↓         ↓         ↓
└───┬───┴─────────┴─────────┘
    ↓
Aggregate Dates
```

#### 3. Database Loading
```
Read CSV Files
    ↓
┌───┴───┬─────────┬─────────┐
↓       ↓         ↓         ↓
Loader 1 Loader 2  ...    Loader N
↓       ↓         ↓         ↓
Insert  Insert    Insert   Insert
to DB   to DB     to DB    to DB
↓       ↓         ↓         ↓
└───┬───┴─────────┴─────────┘
    ↓
Synchronization
```

---

## 📊 Ana Fazlar Detayı

### Faz 1: Main Parsing Phase

**Amaç:** XML dosyalarını paralel olarak parse edip CSV'ye dönüştürme

**Adımlar:**

1. **Network Node'ları Yükle**
   ```
   Repository.getNetworkNodesByBranchId()
   → Map<nodeName, nodeId>
   ```
   Örnek: `eNodeB001 → ID: 12345`

2. **XML Dosyalarını Oku**
   ```
   /raw/ dizininden:
   - 150_eNodeB_001.xml (4G PM)
   - 150_gNodeB_002.xml (5G PM)
   - 150_RNC_003.xml (3G PM)
   ```

3. **Thread Pool Oluştur**
   ```
   ExecutorService threadPool = Executors.newFixedThreadPool(8)
   ```

4. **Handler'ları Oluştur ve Submit Et (Loop)**

   **Loop Yapısı:**
   ```
   ┌─────────────────────────────────────┐
   │ <<loop>>                            │
   │ Create & Submit Handlers            │
   │                                     │
   │ [Setup] Initialize XML file list    │
   │                                     │
   │ [Test] next XML file exists? ◇      │
   │        ├─[YES]─┐                    │
   │        │       ↓                    │
   │ [Body]                              │
   │   1. Get next XML file              │
   │   2. Determine parser type          │
   │      (HwEnbPmXmlParseHandler)       │
   │   3. Create ParseHandler(file,      │
   │      nodeIds)                       │
   │   4. Submit to ExecutorService      │
   │      (non-blocking)                 │
   │        │                            │
   │        └──────→ [Test]              │
   │        │                            │
   │        └─[NO]─→ Exit                │
   └─────────────────────────────────────┘
   ```

5. **Paralel XML İşleme**

   Her handler thread'i şu adımları gerçekleştirir:

   ```
   Handler Thread:
   ├─ run() - Parse XML file
   ├─ preHandler()
   │  └─ Filename'den metadata çıkar:
   │     • fragmentDate
   │     • nodeName
   │     • fileId
   ├─ SAX Parser ile XML aç
   ├─ Loop: measInfo section'ları oku
   │  ├─ Loop: measValue record'ları oku
   │  │  ├─ Metrikleri çıkar (RSRP, Throughput)
   │  │  ├─ Tablo kolonlarına map et
   │  │  ├─ CSV buffer'a yaz
   │  │  └─ Auto counter enabled? → counter tanımla
   │  └─ Sonraki measInfo
   └─ postHandler() - Cleanup
   ```

6. **Synchronization ve Cleanup**
   ```
   shutdownExecutorService()
   → Tüm thread'lerin bitmesini bekle

   writer.closeAllStreams()
   → Tüm CSV buffer'ları flush et
   ```

**Çıktı:** `/result/` dizininde tablo bazlı CSV dosyaları
- `cell_metrics-20260201120000.csv`
- `rnc_performance-20260201120000.csv`

---

### Faz 2: Son İşleme (Post Parse)

**Aktivite:** `postEngine()`

**Kontrol:**
```
isActivePostParse? → YES: postEngine()
                   → NO: Atla
```

**Amaç:**
- Vendor-specific son işlemler
- Aggregation'lar
- Validation'lar

---

### Faz 3: Auto Counter Tanımlama

**Aktiviteler:**
- `Save auto counter definitions`
- `autoCounterDefine.clear()`

**Kontrol:**
```
isActiveAutoCounter? → YES: Save definitions
                     → NO: Atla
```

**Amaç:** Parse sırasında keşfedilen yeni metrikleri metadata tablolarına kaydet

---

### Faz 4: Content Date Discovery (Paralel)

**Amaç:** Parse edilen CSV'lerdeki min/max tarih aralıklarını bul

**Akış:**
```
isActiveDiscoverContentDate? → YES
    ↓
Read parsed CSV files from /result/
    ↓
Parallel Discover (Fork)
    ├─ Analyze CSV 1 → Extract min/max dates
    ├─ Analyze CSV 2 → Extract min/max dates
    └─ Analyze CSV N → Extract min/max dates
    ↓
Aggregate date ranges
    ↓
Print discovered dates
```

**Çıktı Örneği:**
```
Discovered Date Range:
  Min: 2026-02-01 00:00:00
  Max: 2026-02-01 23:59:59
```

---

### Faz 5: Data Loading Phase (Paralel)

**Amaç:** CSV dosyalarını paralel olarak veritabanına yükle

**Akış:**

1. **Duplicate Temizleme (Before)**
   ```
   isActiveCleanDuplicateBefore? → YES: cleanDuplicateBeforeLoader()
   ```

2. **Paralel Database Loading**
   ```
   Read CSV files from /result/
       ↓
   Parallel Load (Fork)
       ├─ LoaderFactory.load(csv1)
       ├─ LoaderFactory.load(csv2)
       └─ LoaderFactory.load(csvN)
       ↓
   Synchronization
       ↓
   shutdownExecutorService()
   ```

   **Load Yöntemleri:**
   - PostgreSQL: `COPY` command (bulk insert)
   - Oracle: Prepared statements with batch
   - SQL Server: Bulk insert API

3. **Duplicate Temizleme (After)**
   ```
   isActiveCleanDuplicateAfter? → YES: cleanDuplicateAfterLoader()
   ```

   **Amaç:** DB constraint'lere göre duplicate'leri temizle

---

### Faz 6: Prosedür ve Export İşlemleri

**Final Adımlar:**

1. **Stored Procedure Çağırma**
   ```
   isActiveCallProcedure? → YES: callProcedure()
   ```
   **Amaç:** Veri transformasyonları

2. **Agregasyon**
   ```
   isActiveCallAggregate? → YES: callAggregate()
   ```
   **Amaç:** Saatlik/günlük KPI hesaplamaları

3. **Export**
   ```
   isActiveCallExport? → YES: callExport()
   ```
   **Amaç:** İşlenmiş veriyi external sistemlere gönder

---

## ⚙️ Konfigürasyon ve Esneklik

### Thread Pool Ayarları

**Default Değer:** 8 thread
**Konfigüre Edilebilir:** Flow bazında ayarlanabilir

```json
{
  "flowId": "HW_ENB_PM_FLOW",
  "threadPoolSize": 16,  // Yüksek performans için artırılabilir
  "maxMemory": "4GB"
}
```

**Hesaplama:**
- CPU core sayısı: 8
- Optimal thread sayısı: 8-16 (I/O intensive işlem için)

---

### Conditional İşlemler (Feature Flags)

Activity diagram'daki tüm opsiyonel işlemler flow config'den kontrol edilir:

| Flag | Açıklama | Varsayılan |
|------|----------|-----------|
| `isActiveFetchTables` | Metadata'yı DB'den üret | false |
| `isActivePreParse` | Vendor-specific ön işleme | true |
| `isActiveOnParse` | Ana parsing fazı | true |
| `isActivePostParse` | Son işlemler | true |
| `isActiveAutoCounter` | Otomatik counter tanımlama | false |
| `isActiveDiscoverContentDate` | Tarih aralığı keşfi | false |
| `isActiveCleanDuplicateBefore` | Load öncesi duplicate temizleme | false |
| `isActiveCleanDuplicateAfter` | Load sonrası duplicate temizleme | true |
| `isActiveCallProcedure` | Stored procedure çağırma | false |
| `isActiveCallAggregate` | Agregasyon | true |
| `isActiveCallExport` | Export işlemi | false |

---

### Parser Type Detection

Dosya ismine göre otomatik parser seçimi:

```java
String fileName = "150_eNodeB_001.xml";

if (fileName.contains("eNodeB")) {
    return new HwEnbPmXmlParseHandler();
} else if (fileName.contains("gNodeB")) {
    return new HwGnbPmXmlParseHandler();
} else if (fileName.contains("RNC")) {
    return new HwRncCmXmlParseHandler();
}
```

**Desteklenen Parser'lar:**
- `HwEnbPmXmlParseHandler` - Huawei 4G PM
- `HwGnbPmXmlParseHandler` - Huawei 5G PM
- `HwRncCmXmlParseHandler` - Huawei 3G CM
- `EricEnbPmXmlParseHandler` - Ericsson 4G PM
- ... (vendor-specific)

---

## 📈 Performans ve Ölçeklenebilirlik

### Performans Metrikleri

**Test Senaryosu:**
- Dosya sayısı: 150 XML
- Ortalama dosya boyutu: 50 MB
- Toplam veri: ~7.5 GB

**Sonuçlar:**

| Metrik | Seri İşleme | Paralel İşleme (8 thread) | İyileştirme |
|--------|-------------|---------------------------|-------------|
| **Parse Süresi** | 300 dakika | 38 dakika | **7.9x** |
| **Load Süresi** | 60 dakika | 12 dakika | **5x** |
| **Toplam Süre** | 360 dakika (6 saat) | 50 dakika | **7.2x** |
| **Throughput** | 0.4 dosya/dakika | 3.0 dosya/dakika | **7.5x** |

---

### Memory Yönetimi

**SAX Parser Kullanımı:**
- **Neden SAX?** DOM parser tüm XML'i memory'ye alır (50 MB × 8 thread = 400 MB+)
- **SAX Avantajı:** Stream-based, event-driven → Düşük memory kullanımı
- **Memory Kullanımı:** ~10 MB/thread × 8 thread = **80 MB** (DOM'a göre 5x daha az)

**Event-Driven Parsing:**
```java
@Override
public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if ("measValue".equals(qName)) {
        currentMeasValue = new MeasValue();
    }
}

@Override
public void characters(char[] ch, int start, int length) {
    // Stream data, don't store in memory
    csvWriter.write(ch, start, length);
}
```

---

### Ölçeklenebilirlik

**Horizontal Scaling:**
- Farklı flow'lar farklı sunucularda çalışabilir
- Load balancing ile dağıtık işleme

**Vertical Scaling:**
- Thread pool size artırılabilir (CPU core sayısına göre)
- Memory allocation artırılabilir

**Örnek:**
```
8 core → 8 thread → 38 dakika
16 core → 16 thread → ~20 dakika
32 core → 32 thread → ~12 dakika
```

---

## 🔧 Hata Yönetimi

### Hata Yakalama Noktaları

1. **Parse Hatası**
   - Hatalı XML dosyaları `/error/` dizinine taşınır
   - Log'a kaydedilir, flow devam eder

2. **Database Hatası**
   - Transaction rollback
   - Retry mekanizması (3 deneme)
   - Hata durumunda alert gönderilir

3. **Resource Hatası**
   - OutOfMemory → Graceful shutdown
   - Connection timeout → Retry with backoff

---

## 🎯 Sonuç ve Faydalar

### Temel Başarılar

✅ **Yüksek Performans:** 7.9x hız artışı
✅ **Düşük Memory:** SAX parser ile 5x daha az memory
✅ **Ölçeklenebilir:** Thread pool ile kolayca scale edilebilir
✅ **Esnek:** Feature flag'lerle kontrol edilebilir adımlar
✅ **Güvenilir:** Hata yönetimi ve duplicate kontrolü

---

### Mimari Avantajlar

| Özellik | Açıklama |
|---------|----------|
| **Modüler Yapı** | Her faz bağımsız, kolayca değiştirilebilir |
| **Paralel İşleme** | 3 noktada parallelization: Parse, Discovery, Load |
| **Vendor Agnostic** | Vendor-specific handler'lar ile her vendor desteklenir |
| **Conditional Execution** | Feature flag'lerle runtime kontrol |
| **Transaction Safety** | DB transaction'lar ile veri tutarlılığı |

---

### İş Etkisi

**Operational:**
- 6 saatlik işlem → 50 dakika (günde 5+ saat tasarruf)
- Daha sık veri yenileme (her 1 saatte bir → her 15 dakikada bir)

**Business:**
- Real-time KPI'lar
- Daha hızlı problem tespiti
- Daha iyi customer experience

**Technical:**
- Düşük resource kullanımı
- Yüksek throughput
- Kolay bakım ve geliştirme

---

## 📚 Referanslar

**Diyagram Dosyaları:**
- PlantUML Source: `ParseFlow_ActivityDiagram.puml`
- Enterprise Architect: `sparx/Parser_Activity_Diagram.xml`

**İlgili Dokümanlar:**
- Parser Implementation Guide
- Vendor-Specific Handler Documentation
- Performance Tuning Guide

---

## ❓ Sorular?

**İletişim:**
- Email: parser-team@company.com
- Jira: PARSER-XXX
- Confluence: Parser Documentation

---

**Teşekkürler!** 🙏

*Bu sunum Parse Flow Activity Diagram'ın detaylı açıklamasıdır. Diagram Enterprise Architect'te manuel olarak oluşturulmuş ve PlantUML kaynak diyagramıyla doğrulanmıştır.*
