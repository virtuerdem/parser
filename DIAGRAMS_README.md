# Transfer Flow - UML Diagrams

Bu klasörde Transfer Flow sisteminin UML diagramları bulunmaktadır.

## 📊 Diagram Dosyaları

### 1. Activity Diagram (v2) - Aktivite Diyagramı
**Dosya:** `TransferFlow_ActivityDiagram_v2.puml`

**İçerik:**
- Engine başlangıcından bitiş aşamasına kadar tüm süreç
- **Loop + Fork/Join kombinasyonu** ile parallel execution gösterimi
- Handler'ların oluşturulması (sequential loop)
- Handler'ların çalıştırılması (parallel fork/join)
- Post-processing aşamaları (decompress, validation)

**Önemli Özellikler:**
- Swimlane/Partition kullanımı (Engine Thread vs Handler Threads)
- Parallel execution noktalarının net gösterimi
- Performance notları ve süre hesaplamaları
- Loop ile dinamik sayıda connection desteği

**Render için:** PlantUML kullanın
```bash
plantuml TransferFlow_ActivityDiagram_v2.puml
```

---

### 2. Use Case Diagram - Kullanım Senaryoları
**Dosya:** `TransferFlow_UseCaseDiagram.puml`

**İçerik:**
- Sistemdeki tüm aktörler (Scheduler, SFTP Server, Ops Team, Parser, Monitor)
- Ana kullanım senaryoları (use cases)
- Aktör-use case ilişkileri
- Include/Extend ilişkileri

**Paketler:**
1. **Engine Management:** Başlangıç, konfigürasyon, thread pool
2. **Connection Handling:** Bağlantı yönetimi, handler oluşturma
3. **File Transfer:** SFTP operasyonları (connect, list, download)
4. **Post-Processing:** Decompress, validation, statistics
5. **Integration & Monitoring:** Archive, notifications, reporting

**Render için:**
```bash
plantuml TransferFlow_UseCaseDiagram.puml
```

---

### 3. Sequence Diagram (v2) - Sıralı Etkileşim Diyagramı
**Dosya:** `TransferFlow_SequenceDiagram_v2.puml`

**İçerik:**
- Tüm bileşenler arası detaylı mesajlaşma
- Database sorgularının tam gösterimi (SQL dahil)
- **Par (parallel) bloklar** ile concurrent execution
- Handler'ların parallel çalışma süreci

**3 Fase:**
1. **Phase 1 - Engine Startup:**
   - Scheduler trigger
   - Configuration loading
   - Thread pool creation
   - Handler submission (loop)

2. **Phase 2 - Parallel Handler Execution:**
   - 3 handler'ın concurrent çalışması (par block)
   - SFTP operations (connect, list, filter, download)
   - Database operations (bulk insert)
   - Connection cleanup

3. **Phase 3 - Post-Processing:**
   - Synchronization (CountDownLatch)
   - Parallel decompression
   - Parallel validation
   - Statistics & notifications

**Render için:**
```bash
plantuml TransferFlow_SequenceDiagram_v2.puml
```

---

## 🎨 PlantUML Kurulumu ve Kullanımı

### Kurulum

**1. PlantUML CLI (Java gerekli):**
```bash
# Java kurulu olmalı
java -version

# PlantUML jar indir
wget https://github.com/plantuml/plantuml/releases/download/v1.2023.13/plantuml.jar

# Render et
java -jar plantuml.jar TransferFlow_ActivityDiagram_v2.puml
```

**2. VS Code Extension:**
- Extension: "PlantUML" by jebbs
- Install: `code --install-extension jebbs.plantuml`
- Preview: `Alt+D` veya `Ctrl+Shift+P` → "PlantUML: Preview"

**3. Online Render:**
- http://www.plantuml.com/plantuml/uml/
- Dosya içeriğini kopyala-yapıştır

**4. IntelliJ IDEA Plugin:**
- Settings → Plugins → "PlantUML Integration"

---

## 📐 Diagram Karşılaştırması

| Diagram Type | Gösterdiği | En İyi Kullanım | Detay Seviyesi |
|--------------|------------|-----------------|----------------|
| **Activity Diagram** | Süreç akışı, karar noktaları, parallelism | İş akışını anlamak, süreç optimizasyonu | Orta |
| **Use Case Diagram** | Aktörler, sistem özellikleri, ilişkiler | Gereksinim analizi, sistem overview | Yüksek Level |
| **Sequence Diagram** | Bileşenler arası mesajlaşma, timing | Detaylı tasarım, debugging, implementation | Çok Detaylı |

---

## 🔍 Önemli Kavramlar

### Activity Diagram'da Loop + Fork/Join

```
Loop (Sequential)          Fork/Join (Parallel)
──────────────             ────────────────────
for i=1 to 3:              ═══╦═══
  create handler[i]            ║
                           Handler 1 ─┐
                           Handler 2  ├─→ PARALLEL
                           Handler 3 ─┘
                               ║
                           ═══╩═══ (await all)
```

**Neden ikisi birlikte?**
- Loop: Handler'ları **oluşturma** (sequential)
- Fork/Join: Handler'ları **çalıştırma** (parallel)

### Sequence Diagram'da Par Block

```
par Handler 1
  H1 -> SFTP: download
else Handler 2
  H2 -> SFTP: download
else Handler 3
  H3 -> SFTP: download
end
```

**Anlamı:** 3 handler **aynı anda** çalışıyor

---

## 📝 Diagram Versiyonları

### v1 (Eski - Silinmiş)
- `TransferFlow_ActivityDiagram.xmi`
- `TransferFlow_SequenceDiagram.puml`

**Sorunlar:**
- Parallel execution net değildi
- Loop gösterimi yoktu
- Sadece 3 sabit connection için çizilmişti

### v2 (Yeni - Bu Dosyalar) ✅
- `TransferFlow_ActivityDiagram_v2.puml`
- `TransferFlow_SequenceDiagram_v2.puml`
- `TransferFlow_UseCaseDiagram.puml`

**İyileştirmeler:**
- ✅ Loop + Fork/Join kombinasyonu
- ✅ Dinamik sayıda connection desteği
- ✅ Par block ile concurrent execution
- ✅ Swimlane/Partition ile thread ayrımı
- ✅ Detaylı SQL query'ler
- ✅ Performance notları

---

## 🚀 Hızlı Başlangıç

Tüm diagramları render etmek için:

```bash
# Tek komutla hepsini render et
plantuml *.puml

# Oluşturulan dosyalar:
# - TransferFlow_ActivityDiagram_v2.png
# - TransferFlow_UseCaseDiagram.png
# - TransferFlow_SequenceDiagram_v2.png
```

SVG formatında (scalable):
```bash
plantuml -tsvg *.puml
```

---

## 📚 Ek Kaynaklar

- **PlantUML Dokümantasyonu:** https://plantuml.com/
- **UML Reference:** https://www.uml-diagrams.org/
- **Activity Diagram Guide:** https://plantuml.com/activity-diagram-beta
- **Sequence Diagram Guide:** https://plantuml.com/sequence-diagram
- **Use Case Diagram Guide:** https://plantuml.com/use-case-diagram

---

## 🔄 Güncellemeler

| Tarih | Version | Değişiklik |
|-------|---------|------------|
| 2024-07-08 | v1 | İlk diagramlar (XMI, temel PlantUML) |
| 2024-12-09 | v2 | Loop+Fork/Join, Use Case eklendi, Sequence güncellendi |

---

## 💡 Notlar

1. **Parallel Execution:** Activity ve Sequence diagram'da **par** ve **fork/join** kullanılarak gösterilmiştir
2. **Dynamic Connections:** Loop yapısı sayesinde 3, 5, 10... farketmeksizin herhangi sayıda connection desteklenir
3. **Thread Pools:** ExecutorService kullanımı tüm diagramlarda vurgulanmıştır
4. **Incremental Transfer:** Filter logic'i tüm diagramlarda gösterilmiştir

---

Son güncelleme: 2024-12-09
Oluşturan: Claude Code
