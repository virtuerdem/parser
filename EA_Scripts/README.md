# Enterprise Architect JavaScript Scripts - ParseFlow Diagramları

Bu klasör, ParseFlow diagramlarını EA 16.1'e import etmek için JavaScript scriptleri içerir.

---

## 📁 Script Dosyaları

| Dosya | Açıklama | Kullanım |
|-------|----------|----------|
| **ImportParseFlowDiagrams.js** | ParseFlow diagramlarını sıfırdan oluşturur | ✅ ÖNERİLEN |
| **ImportXMI.js** | XMI dosyasını EA'ye import eder | XMI varsa kullan |
| **PlantUMLToXMI.js** | PlantUML'den XMI oluşturur | Deneysel |

---

## 🚀 HIZLI BAŞLANGIÇ (ÖNERİLEN YÖNTEM)

### Adım 1: Script'i EA'ye Yükle

1. **Enterprise Architect 16.1**'i açın
2. Menüden: **Tools > Scripting** (veya Ctrl+Alt+S)
3. Scripting penceresinde: **"New Script"** butonuna tıklayın
4. Açılan dialog'da:
   - **Name:** `ImportParseFlowDiagrams`
   - **Group:** `Parse Module`
   - **Language:** `JavaScript`
5. **OK** tıklayın
6. Script editor'de **ImportParseFlowDiagrams.js** dosyasının içeriğini yapıştırın
7. **File > Save** ile kaydedin

### Adım 2: Script'i Çalıştır

1. Scripting penceresinde **ImportParseFlowDiagrams** script'ini seçin
2. **Run** butonuna tıklayın
3. **Script Output** penceresinde ilerlemeyi izleyin

### Adım 3: Sonuçları Kontrol Et

1. **Project Browser**'da (sol panel) şunları göreceksiniz:
   ```
   📦 Parser Module
      └── 📦 Parse Flow Diagrams
           ├── 📊 ParseFlow - Activity Diagram
           ├── 📊 ParseFlow - Sequence Diagram
           └── 📊 ParseFlow - Use Case Diagram
   ```

2. Her diagram'a çift tıklayarak açabilirsiniz

### Sonuç

✅ **Activity Diagram** - Parse flow aktivite diyagramı (boş şablon)
✅ **Sequence Diagram** - Lifeline'lar oluşturuldu (boş şablon)
✅ **Use Case Diagram** - Aktörler ve use case'ler oluşturuldu (boş şablon)

---

## 📊 OLUŞTURULAN DİAGRAMLAR

### 1. Activity Diagram

**Konum:** `Parser Module > Parse Flow Diagrams > ParseFlow - Activity Diagram`

**İçerik:**
- ✅ Initial Node (Start)
- ✅ Activity Nodes:
  - startEngine
  - preparePaths
  - getTables
  - Parse XML Files (Parallel)
  - Auto Counter Discovery
  - Content Date Discovery
  - Bulk Data Loading
  - Post Processing
- ✅ Final Node (End)

**PlantUML Kaynak:** `ParseFlow_ActivityDiagram.puml`

**Manuel Detaylandırma:**
- Fork/Join node'ları ekleyin (parallel execution için)
- Decision node'ları ekleyin (conditional flows için)
- Notes ekleyin (açıklamalar için)

---

### 2. Sequence Diagram

**Konum:** `Parser Module > Parse Flow Diagrams > ParseFlow - Sequence Diagram`

**İçerik:**
- ✅ Lifelines:
  - Transfer Module (Actor)
  - ParseBaseEngine (Object)
  - Repository (Object)
  - Database (Object)
  - ExecutorService (Object)
  - ParseHandler (Object)
  - SAXParser (Object)
  - Writer (Object)
  - LoaderFactory (Object)

**PlantUML Kaynak:** `ParseFlow_SequenceDiagram.puml`

**Manuel Detaylandırma:**
- Message arrow'ları ekleyin (lifeline'lar arası)
- Alt/Opt/Loop fragment'ları ekleyin
- Notes ekleyin
- Database queries ekleyin

**Database Tables Referansı:**
```
Phase 1 - Initialization:
- t_flow
- t_branch
- t_parse_engine
- t_parse_component
- t_parse_table
- t_parse_column

Phase 2 - Parse:
- t_network_node

Phase 3 - Auto Counter:
- t_all_counter

Phase 4 - Content Date:
- t_content_date_result

Phase 5 - Loading:
- t_loader_result
- Data tables (50+)
```

---

### 3. Use Case Diagram

**Konum:** `Parser Module > Parse Flow Diagrams > ParseFlow - Use Case Diagram`

**İçerik:**
- ✅ Actors:
  - Transfer Module
  - Scheduler
  - Database Administrator
  - Data Analyst
  - Monitoring System

- ✅ Use Cases:
  - Initialize Parse Engine
  - Parse XML Files
  - Auto Discover Counters
  - Load Data to Database
  - Generate Reports
  - Monitor Processing
  - Handle Errors

**PlantUML Kaynak:** `ParseFlow_UseCaseDiagram.puml`

**Manuel Detaylandırma:**
- Actor-UseCase association'ları ekleyin
- Include/Extend relationship'leri ekleyin
- System boundary ekleyin
- Notes ekleyin

---

## 🔧 ALTERNATİF YÖNTEM 1: XMI Import (XMI Dosyanız Varsa)

### Adım 1: XMI Dosyasını Hazırlayın

XMI dosyanız yoksa, önce **PlantUMLToXMI.js** ile oluşturabilirsiniz (Alternatif Yöntem 2'ye bakın).

### Adım 2: ImportXMI.js Script'ini Düzenleyin

1. **ImportXMI.js** dosyasını bir metin editöründe açın
2. Satır 17'deki dosya yolunu düzenleyin:
   ```javascript
   var XMI_FILE_PATH = "C:\\Users\\YourName\\Documents\\ParseFlow_Diagrams.xmi";
   ```

### Adım 3: Script'i EA'ye Yükle ve Çalıştır

1. EA'de: **Tools > Scripting**
2. **New Script** → Name: `ImportXMI`
3. ImportXMI.js içeriğini yapıştırın
4. **Save** ve **Run**

### Adım 4: Manuel Import (Script Çalışmazsa)

1. EA menüden: **Project > Import/Export > Import Package from XMI...**
2. XMI dosyasını seçin
3. Target package: **Parser Module** seçin
4. **Import** butonuna tıklayın

---

## 🔬 ALTERNATİF YÖNTEM 2: PlantUML'den XMI Oluşturma (Deneysel)

### Not
Bu yöntem basitleştirilmiş bir converter kullanır. Karmaşık PlantUML syntax'ları desteklenmeyebilir.

### Adım 1: PlantUMLToXMI.js Script'ini Düzenleyin

1. **PlantUMLToXMI.js** dosyasını bir metin editöründe açın
2. Satır 19-23'teki dosya yollarını düzenleyin:
   ```javascript
   var PLANTUML_FILES = [
       "C:\\path\\to\\ParseFlow_ActivityDiagram.puml",
       "C:\\path\\to\\ParseFlow_SequenceDiagram.puml",
       "C:\\path\\to\\ParseFlow_UseCaseDiagram.puml"
   ];
   ```

3. Satır 26'daki output yolunu düzenleyin:
   ```javascript
   var OUTPUT_XMI = "C:\\path\\to\\ParseFlow_Diagrams.xmi";
   ```

### Adım 2: Script'i EA'ye Yükle ve Çalıştır

1. EA'de: **Tools > Scripting**
2. **New Script** → Name: `PlantUMLToXMI`
3. PlantUMLToXMI.js içeriğini yapıştırın
4. **Save** ve **Run**

### Adım 3: XMI'yi Import Edin

Oluşturulan XMI dosyasını import etmek için **Alternatif Yöntem 1**'i kullanın.

---

## 📖 SCRIPT OUTPUT ÖRNEĞİ

### ImportParseFlowDiagrams.js Çıktısı:

```
===== ParseFlow Diagrams Import Başlatılıyor =====

✓ 'Parser Module' package bulundu

===== Diagramlar Oluşturuluyor =====

Creating Activity Diagram...
  ✓ Activity Diagram oluşturuldu
Creating Sequence Diagram...
  ✓ Sequence Diagram oluşturuldu
Creating Use Case Diagram...
  ✓ Use Case Diagram oluşturuldu

===== Import Tamamlandı =====

Diagramlar 'Parser Module > Parse Flow Diagrams' altında oluşturuldu.

NOT: Diagramlar boş şablonlar olarak oluşturuldu.
PlantUML dosyalarındaki içeriği manuel olarak EA'ye aktarabilirsiniz.
```

---

## 🎨 DİAGRAMLARI DETAYLANDIRMA

Oluşturulan diagramlar **temel şablonlar**dır. Detaylandırmak için:

### Activity Diagram

1. Diagram'ı açın
2. **Toolbox > Activity** sekmesinden element'ler sürükleyin:
   - **Decision** (◇) - Conditional branches için
   - **Fork/Join** (━) - Parallel execution için
   - **Activity Partition** - Swimlane'ler için
3. Element'leri birbirine bağlayın (**Control Flow** ile)
4. Notes ekleyin (**Note** element)

### Sequence Diagram

1. Diagram'ı açın
2. Lifeline'lar arası **Message** ekleyin:
   - Sol panelden **Message** seçin
   - Bir lifeline'dan diğerine çizgi çekin
3. **Combined Fragment** ekleyin:
   - **Toolbox > Sequence** > **Combined Fragment**
   - Type seçin: alt, opt, loop, par
4. Notes ve database queries ekleyin

### Use Case Diagram

1. Diagram'ı açın
2. **Association** ekleyin:
   - Actor'dan Use Case'e çizgi çekin
3. **Include/Extend** ilişkileri ekleyin:
   - **Toolbox > Use Case** > **Include** veya **Extend**
4. **System Boundary** ekleyin:
   - **Toolbox > Use Case** > **Boundary**

---

## 🔍 SORUN GİDERME

### Script Çalışmıyor

**Problem:** Script çalıştırıldığında hata veriyor

**Çözüm 1:** JavaScript engine kontrolü
```
1. EA menüden: Tools > Options > Automation
2. JavaScript engine enabled olmalı
```

**Çözüm 2:** Manuel import
```
1. Project Browser'da 'Parser Module' package'ine sağ tıklayın
2. "Add > Add Package" ile "Parse Flow Diagrams" package'i oluşturun
3. Her diagram için:
   - Package'e sağ tıklayın > "Add Diagram"
   - Type ve name girin
   - Element'leri manuel ekleyin
```

---

### Script Output Görünmüyor

**Problem:** Script çalıştığında output penceresi görünmüyor

**Çözüm:**
```
1. EA menüden: View > Script Output
2. Veya: Tools > Scripting > Output sekmesi
```

---

### "Repository" Undefined Hatası

**Problem:** Script'te "Repository is undefined" hatası

**Çözüm:**
```
Bu EA'nin JavaScript context problemi.

1. Script'i EA Scripting window'dan çalıştırdığınızdan emin olun
2. EA'yi restart edin
3. Manuel import yöntemini kullanın
```

---

### XMI Import Başarısız

**Problem:** XMI import "Failed to import" hatası

**Çözüm 1:** XMI version kontrolü
```
EA 16.1, XMI 1.1, 2.1, 2.4 versiyonlarını destekler.
XMI dosyanızın header'ını kontrol edin.
```

**Çözüm 2:** Manuel XMI import
```
1. Project > Import/Export > Import Package from XMI...
2. XMI dosyasını seçin
3. Import options'ları inceleyin
4. Import butonuna tıklayın
```

---

## 📚 REFERANSLAR

### PlantUML Kaynak Dosyalar

```
ParseFlow_ActivityDiagram.puml   - Activity diagram source
ParseFlow_SequenceDiagram.puml   - Sequence diagram source
ParseFlow_UseCaseDiagram.puml    - Use case diagram source
```

### Dökümanlar

```
PARSE_DIAGRAMS_README.md         - Diagram açıklamaları
PARSER_DATABASE_INTEGRATION.md   - Database entegrasyonu
EA_IMPORT_GUIDE.md               - Detaylı EA import rehberi
```

### EA Documentation

- [EA JavaScript API Reference](https://sparxsystems.com/enterprise_architect_user_guide/16.1/automation/)
- [EA Scripting Guide](https://sparxsystems.com/enterprise_architect_user_guide/16.1/automation/scripting.html)

---

## ✅ SONUÇ

### Başarıyla Oluşturuldu

✅ **3 JavaScript scripti** EA'de kullanıma hazır
✅ **3 Diagram şablonu** oluşturulacak
✅ **Manuel detaylandırma** ile tam özellikli diagramlar

### Sonraki Adımlar

1. ✅ Script'i EA'de çalıştırın
2. ⚙️ Diagramları detaylandırın (element'ler, relationship'ler, notes)
3. 💾 EA projesini kaydedin
4. 📤 Takımla paylaşın

---

**Son Güncelleme:** 2026-01-13
**EA Versiyonu:** 16.1
**Script Language:** JavaScript (JScript)
