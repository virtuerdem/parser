# Enterprise Architect 16.1 İçin İçe Aktarma Kılavuzu

Bu kılavuz, PlantUML diagramlarının Enterprise Architect 16.1'e nasıl aktarılacağını gösterir.

## 🎯 Sorun

PlantUML (.puml) dosyaları **text-based** formattadır ve doğrudan EA tarafından açılamaz.
Enterprise Architect kendi formatını kullanır (.eap, .eapx, .qea).

## ✅ Çözümler

### YÖNTEM 1: XMI Dosyalarını İçe Aktarma (ÖNERİLEN)

EA, XMI 2.1 formatını destekler. Mevcut XMI dosyalarınızı kullanabilirsiniz.

#### Adımlar:

1. **Enterprise Architect'i Açın**
   - EA 16.1'i başlatın
   - Yeni bir proje oluşturun veya mevcut projeyi açın

2. **Model Oluşturun**
   - Project Browser'da sağ tık → Add → Add Package
   - İsim: "Transfer Flow System"

3. **XMI İçe Aktarın**
   ```
   Menü: Project → Import/Export → Import Package from XMI
   ```

4. **Dosya Seçimi**
   - `TransferFlow_SequenceDiagram.xmi` dosyasını seçin
   - `TransferFlow_ActivityDiagram.xmi` dosyasını seçin (varsa)

5. **Import Seçenekleri**
   - Format: XMI 2.1
   - Strip GUIDs: Hayır (unchecked)
   - Import Diagrams: Evet (checked)
   - OK'e tıklayın

6. **Sonuç**
   - Diagramlar Project Browser'da görünecektir
   - Çift tıklayarak açabilirsiniz

---

### YÖNTEM 2: Manuel Olarak Yeniden Oluşturma

PlantUML dosyalarını referans alarak EA'da manuel çizim:

#### Activity Diagram İçin:

1. **Yeni Diagram Oluştur**
   ```
   Package'a sağ tık → Add Diagram → Activity
   İsim: "Transfer Flow - Activity Diagram"
   Type: Activity (UML 2.5)
   ```

2. **Toolbox'tan Elemanlar Ekle**
   - **Initial Node** (başlangıç noktası - siyah nokta)
   - **Actions** (dikdörtgen kutular)
     - Örn: "startEngine(flowId)"
     - "Load connections from DB"
   - **Decision Nodes** (elmas - karar noktaları)
   - **Fork Node** (kalın yatay çizgi - parallelization başlangıcı)
   - **Join Node** (kalın yatay çizgi - parallelization bitişi)
   - **Activity Final** (bitiş noktası - çift daire)

3. **Swimlanes (Partitions) Ekle**
   ```
   Toolbox → ActivityPartition
   Diagram'a sürükle
   ```
   - İlk partition: "Engine Thread"
   - İkinci partition: "Handler Thread 1"
   - Üçüncü partition: "Handler Thread 2"
   - Dördüncü partition: "Handler Thread 3"

4. **Loop Gösterimi**
   ```
   Toolbox → StructuredActivity → Loop Node
   ```
   - Loop node ekle
   - İçine "Create Handler" action'ını koy
   - Properties → Loop Type: "Loop"

5. **Fork/Join Parallelism**
   - Fork node ekle (Handler creation'dan sonra)
   - 3 ayrı handler flow çiz (parallel lanes)
   - Join node ekle (tüm handler'lar bittikten sonra)

6. **Bağlantılar**
   - Control Flow (ok) ile elemanları bağla
   - Decision'ların guard conditions'larını ekle

#### Sequence Diagram İçin:

1. **Yeni Diagram Oluştur**
   ```
   Package'a sağ tık → Add Diagram → Sequence
   İsim: "Transfer Flow - Sequence Diagram"
   Type: Sequence (UML 2.5)
   ```

2. **Lifelines Ekle** (Toolbox → Lifeline)
   - Scheduler (Actor)
   - TransferEngine
   - Repository
   - Database
   - ExecutorService
   - Handler1, Handler2, Handler3
   - ConnectionFactory
   - SFTP Server
   - ValidationService
   - Parser

3. **Messages Ekle**
   - Synchronous Message (dolu ok): `→`
   - Return Message (kesik ok): `⇢`
   - Asynchronous Message (açık ok): `⇨`

4. **Combined Fragments Ekle**
   - **Loop Fragment**:
     ```
     Toolbox → Combined Fragment → Loop
     Operand: for each connection [1..3]
     ```

   - **Par Fragment** (Parallel):
     ```
     Toolbox → Combined Fragment → Par
     3 operand ekle (Handler 1, 2, 3 için)
     ```

   - **Alt Fragment** (Alternative):
     ```
     Toolbox → Combined Fragment → Alt
     Validation için: Valid XML / Invalid XML
     ```

5. **Execution Specifications**
   - Her lifeline üzerinde mesaj aldığında
   - Lifeline'a sağ tık → Insert → Execution Specification
   - Message başlangıç ve bitiş noktalarına yerleştir

#### Use Case Diagram İçin:

1. **Yeni Diagram Oluştur**
   ```
   Package'a sağ tık → Add Diagram → Use Case
   İsim: "Transfer Flow - Use Case Diagram"
   ```

2. **Actors Ekle** (Toolbox → Actor)
   - Scheduler
   - SFTP Server
   - Operations Team
   - Parser Module
   - Monitoring System

3. **Use Cases Ekle** (Toolbox → Use Case)
   - Oval şekiller
   - Start Transfer Engine
   - Load Configuration
   - Download Files
   - Validate XML Files
   - vb.

4. **İlişkiler**
   - **Association** (düz çizgi): Actor → Use Case
   - **Include** (kesikli ok): `<<include>>`
   - **Extend** (kesikli ok): `<<extend>>`

5. **Package Grupları**
   ```
   Toolbox → Package
   ```
   - Engine Management
   - Connection Handling
   - File Transfer
   - Post-Processing
   - Integration & Monitoring

---

### YÖNTEM 3: PlantUML Plugin Kullanma

EA için PlantUML desteği sağlayan 3. parti eklentiler:

#### A) MDG Technology for PlantUML

**Not:** EA 16.1 için resmi bir PlantUML plugin'i yoktur. Bu yöntem çalışmayabilir.

#### B) Image Import (Geçici Çözüm)

1. **PlantUML'den Görsel Oluştur**
   ```bash
   # PNG oluştur
   java -jar plantuml.jar -tpng TransferFlow_ActivityDiagram_v2.puml
   ```

2. **EA'ya Görsel Ekle**
   ```
   Diagram'da sağ tık → Insert → Image
   PNG dosyasını seç
   ```

   **Dezavantajı:** Sadece görsel, düzenlenemez

---

### YÖNTEM 4: XMI Dosyası Manuel Oluşturma

EA için optimize edilmiş XMI dosyası oluşturalım.

#### EA XMI 2.1 Format Şablonu:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xmi:XMI xmi:version="2.1"
         xmlns:uml="http://schema.omg.org/spec/UML/2.1"
         xmlns:xmi="http://schema.omg.org/spec/XMI/2.1">
  <xmi:Documentation exporter="PlantUML" exporterVersion="1.0"/>

  <uml:Model xmi:type="uml:Model" xmi:id="model1" name="Transfer Flow System">

    <!-- Package -->
    <packagedElement xmi:type="uml:Package" xmi:id="pkg1" name="Transfer Flow">

      <!-- Activity Diagram Elements -->
      <packagedElement xmi:type="uml:Activity" xmi:id="act1" name="Transfer Flow Process">

        <!-- Initial Node -->
        <node xmi:type="uml:InitialNode" xmi:id="initial1"/>

        <!-- Actions -->
        <node xmi:type="uml:Action" xmi:id="action1" name="startEngine(flowId)"/>
        <node xmi:type="uml:Action" xmi:id="action2" name="Load Connections"/>

        <!-- Control Flows -->
        <edge xmi:type="uml:ControlFlow" xmi:id="flow1"
              source="initial1" target="action1"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="flow2"
              source="action1" target="action2"/>

        <!-- Fork Node (Parallel) -->
        <node xmi:type="uml:ForkNode" xmi:id="fork1"/>

        <!-- Join Node -->
        <node xmi:type="uml:JoinNode" xmi:id="join1"/>

        <!-- Final Node -->
        <node xmi:type="uml:ActivityFinalNode" xmi:id="final1"/>

      </packagedElement>

    </packagedElement>
  </uml:Model>
</xmi:XMI>
```

---

## 📋 Adım Adım: XMI İçe Aktarma

### Hazırlık:

Repoda zaten XMI dosyaları var:
- `TransferFlow_SequenceDiagram.xmi`
- `TransferFlow_ActivityDiagram.xmi` (varsa)

### EA 16.1'de İçe Aktarma:

1. **EA'yı Başlat**
   ```
   Enterprise Architect 16.1 → Yeni Proje Oluştur
   File → New Project
   Template: Blank
   Konum: C:\EA_Projects\TransferFlow.eapx
   ```

2. **Package Oluştur**
   ```
   Project Browser'da "Model" üzerine sağ tık
   → Add → Add Package
   Name: "Transfer Flow System"
   OK
   ```

3. **XMI Import**
   ```
   Menü: Project → Import/Export → Import Package from XMI...

   Ayarlar:
   ✓ Import Diagrams
   ✓ Import All
   Format: XMI 2.1

   Dosya Seç:
   → TransferFlow_SequenceDiagram.xmi

   OK
   ```

4. **Sonucu Kontrol Et**
   ```
   Project Browser'da:
   Model → Transfer Flow System → Sequence Diagrams

   Diagram'ı çift tıklayarak aç
   ```

5. **Diğer Diagramları İçe Aktar**
   - Aynı adımları diğer XMI dosyaları için tekrarla

---

## 🎨 EA'da Düzenleme

### Layout Düzenleme:
```
Diagram üzerinde:
- Layout → Auto Layout
- Layout → Align Elements
```

### Görünüm Düzenleme:
```
Diagram Properties (sağ tık → Properties):
- Show Stereotypes: Evet
- Show Notes: Evet
- Show Parameters: Evet
```

### Export (PDF/PNG):
```
Menü: Publish → Publish Diagram
Format: PDF veya PNG
```

---

## ❌ Yaygın Sorunlar ve Çözümler

### Sorun 1: "XMI Import Failed"
**Çözüm:**
- XMI versiyonunu kontrol et (EA 16.1 → XMI 2.1 veya 2.5)
- XML syntax hatası olabilir → XML validator kullan

### Sorun 2: "Diagrams Not Showing"
**Çözüm:**
- Import sırasında "Import Diagrams" seçeneği işaretli mi?
- Project Browser → Refresh (F5)

### Sorun 3: "Layout Bozuk"
**Çözüm:**
- Diagram → Layout → Auto Layout (All)
- Manuel düzenleme gerekebilir

---

## 📦 Alternatif: EA Proje Dosyası Sağlama

Eğer isterseniz, doğrudan EA formatında (.eapx) proje dosyası da oluşturabilirim.
Ancak bu, EA'nın kurulu olmasını ve API erişimini gerektirir.

---

## 🚀 Önerilen Yaklaşım

**En İyi Yöntem:**
1. ✅ Mevcut XMI dosyalarını EA'ya import et (Yöntem 1)
2. ✅ Layout'u manuel düzenle
3. ✅ EA'da kaydet (.eapx formatında)

**Alternatif:**
- PlantUML dosyalarını referans alarak EA'da manuel çiz (Yöntem 2)
- Bu daha fazla zaman alır ama tam kontrol sağlar

---

## 📞 Yardım

Hangi yöntemi tercih edersiniz?
- XMI import için yardım mı?
- EA formatında proje dosyası mı?
- Manuel çizim için detaylı adımlar mı?

Söyleyin, ona göre devam edelim!
