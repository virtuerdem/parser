# 🚀 Enterprise Architect 16.1 - Hızlı Başlangıç

## ✅ Hazır Dosyalar

Repoda zaten EA uyumlu XMI dosyaları var:

```
✓ TransferFlow_SequenceDiagram.xmi      (22 KB - UML 2.1)
✓ TransferFlow_ActivityDiagram.xmi      (9 KB - UML 2.1)
```

Bu dosyalar **doğrudan** EA 16.1'e import edilebilir!

---

## 📥 5 Adımda İçe Aktarma

### 1️⃣ EA'yı Açın ve Proje Oluşturun

```
Enterprise Architect 16.1 başlat
↓
File → New Project
↓
Project Type: Blank
File Name: TransferFlow.eapx
Konum: İstediğiniz klasör
↓
Create
```

### 2️⃣ Package Oluşturun

```
Project Browser'da (sol panel):
"Model" üzerine sağ tık
↓
Add → Add Package
↓
Name: Transfer Flow System
Type: Package
↓
OK
```

### 3️⃣ Sequence Diagram'ı İçe Aktarın

```
Menü çubuğu: Project → Import/Export → Import Package from XMI...
↓
Ayarlar:
  ☑ Import Diagrams
  ☑ Import All
  Format: XMI 2.1
↓
Filename: [Browse] → TransferFlow_SequenceDiagram.xmi seçin
↓
Import
↓
Target Package: Transfer Flow System seçin
↓
OK
```

**⏱ Bekleme Süresi:** ~5-10 saniye

### 4️⃣ Activity Diagram'ı İçe Aktarın

```
Aynı adımları tekrarlayın:
Project → Import/Export → Import Package from XMI...
↓
Filename: TransferFlow_ActivityDiagram.xmi
↓
Import → OK
```

### 5️⃣ Diagramları Görüntüleyin

```
Project Browser'da:
Model → Transfer Flow System

Göreceksiniz:
├─ Interactions
│  └─ Transfer Engine Complete Flow [Sequence Diagram]
└─ Activities
   └─ Transfer Flow Process [Activity Diagram]

Çift tıklayarak açın! ✅
```

---

## 🎨 İlk Açılışta Yapılacaklar

### Diagram Layout Düzenleme

```
Diagram açıkken:
Menü: Layout → Auto Layout → All
```

Veya manuel:
- Elemanları sürükle-bırak
- Hizala: Layout → Align → ...
- Grid snap: View → Show Grid

### Görünüm Ayarları

```
Diagram üzerinde sağ tık → Properties
↓
Diagram tab:
  ☑ Show Notes
  ☑ Show Stereotypes
  ☑ Show Parameter Details
  ☑ Show Message Numbers (Sequence için)
```

### Zoom Ayarı

```
Diagram araç çubuğu:
Zoom: 100% → İsterseniz değiştirin

Veya:
Ctrl + Mouse Wheel (zoom in/out)
```

---

## 📋 PlantUML Diagramları için

PlantUML (.puml) dosyaları **EA'da doğrudan açılamaz**.

### Seçenekleriniz:

**A) Görsel Olarak Ekleyin:**
1. PlantUML'den PNG oluşturun:
   ```bash
   java -jar plantuml.jar TransferFlow_ActivityDiagram_v2.puml
   ```

2. EA'da görsel ekleyin:
   ```
   Diagram'da sağ tık → Insert → Image
   PNG dosyasını seçin
   ```

**B) Manuel Yeniden Çizin:**
- PlantUML dosyasını referans alın
- EA'da yeni diagram oluşturun
- Elemanları manuel ekleyin

**Önerim:** XMI dosyalarını kullanın, çok daha kolay! ✅

---

## ❓ Sorun Giderme

### ❌ "Could not import XMI file"

**Çözüm 1:** Dosya yolu kontrolü
```
- Dosya adında Türkçe karakter var mı?
- Dosya yolu çok uzun mu?
- Dosya başka bir programda açık mı?
```

**Çözüm 2:** XMI versiyonu
```
EA 16.1 destekler: XMI 2.1, 2.4.1, 2.5
Mevcut dosyalar: XMI 2.1 ✅
```

### ❌ "Package not found after import"

**Çözüm:**
```
Project Browser → Refresh (F5)

Veya:

Project Browser'da Model üzerine sağ tık
→ Find in Project Browser
```

### ❌ "Diagram elements overlapping"

**Çözüm:**
```
Diagram'da:
Ctrl + A (hepsini seç)
↓
Layout → Auto Layout → Default (or Custom)
↓
Layout → Align → Distribute Vertically/Horizontally
```

### ❌ "Missing connections between elements"

**Çözüm:**
```
XMI import bazen connector'ları kaybedebilir.
Manuel bağlantıları yeniden çizin:

Toolbox → Connector
Eleman 1'den Eleman 2'ye sürükle
```

---

## 🎯 Sonuç

### ✅ Başarılı Import Sonrası:

```
Project Browser görünümü:

Model
└─ Transfer Flow System
   ├─ Interactions
   │  └─ 📊 Transfer Engine Complete Flow
   │     • Lifelines: 8 adet
   │     • Messages: 50+ adet
   │     • Fragments: Loop, Alt, Par
   │
   └─ Activities
      └─ 📊 Transfer Flow Process
         • Initial Node
         • Actions: 15+ adet
         • Decision Nodes
         • Fork/Join Nodes
         • Final Node
```

### 🎨 Diagram'ları Düzenleyin:

- Layout'u düzeltin
- Renkleri değiştirin (Properties → Appearance)
- Notlar ekleyin (Right-click → Insert → Note)
- Stereotypes ekleyin

### 💾 Kaydedin:

```
File → Save Project
(Otomatik kaydedilir .eapx dosyasına)
```

### 📤 Export Edin (Opsiyonel):

```
Publish → Publish as HTML
Publish → Generate RTF Documentation
Publish → Publish Diagram (PDF/PNG)
```

---

## 📞 Daha Fazla Yardım

**Detaylı Kılavuz:** `EA_IMPORT_GUIDE.md`

**EA Dokümantasyon:**
- Help → User Guide (F1)
- https://sparxsystems.com/enterprise_architect_user_guide/16.1/

**XMI Hakkında:**
- Format: OMG XMI 2.1 Standard
- Spec: https://www.omg.org/spec/XMI/2.1

---

## ⚡ Bonus: Klavye Kısayolları

```
Ctrl + N       Yeni diagram
Ctrl + D       Duplicate element
Ctrl + Z       Undo
Ctrl + Y       Redo
F5             Refresh browser
F11            Full screen
Space + Drag   Pan diagram (el aracı)
Ctrl + Wheel   Zoom in/out
Alt + 1/2/3    Switch perspectives
```

---

Başarılar! 🎉

Herhangi bir sorun yaşarsanız, lütfen bildirin.
