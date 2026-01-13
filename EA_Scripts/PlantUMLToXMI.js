/**
 * Enterprise Architect JavaScript - PlantUML to XMI Converter
 *
 * Bu script, PlantUML dosyalarını okuyup basit XMI formatına çevirir.
 *
 * NOT: Bu basitleştirilmiş bir converter'dır.
 * Karmaşık PlantUML syntax'ları desteklenmeyebilir.
 *
 * Kullanım:
 * 1. EA'de: Tools > Scripting
 * 2. "New Script" tıklayın
 * 3. Bu dosyanın içeriğini yapıştırın
 * 4. PLANTUML_FILES dizisini düzenleyin
 * 5. Script adını "PlantUMLToXMI" olarak kaydedin
 * 6. "Run" ile çalıştırın
 */

!INC Local Scripts.EAConstants-JScript

// PlantUML dosya yolları
var PLANTUML_FILES = [
    "C:\\path\\to\\ParseFlow_ActivityDiagram.puml",
    "C:\\path\\to\\ParseFlow_SequenceDiagram.puml",
    "C:\\path\\to\\ParseFlow_UseCaseDiagram.puml"
];

var OUTPUT_XMI = "C:\\path\\to\\ParseFlow_Diagrams.xmi";

function main() {
    Session.Output("===== PlantUML to XMI Converter =====");
    Session.Output("");

    var fso = new ActiveXObject("Scripting.FileSystemObject");

    // XMI dosyası oluştur
    var xmiContent = generateXMIHeader();

    // Her PlantUML dosyasını işle
    for (var i = 0; i < PLANTUML_FILES.length; i++) {
        var filePath = PLANTUML_FILES[i];

        if (!fso.FileExists(filePath)) {
            Session.Output("✗ Dosya bulunamadı: " + filePath);
            continue;
        }

        Session.Output("Processing: " + filePath);

        var file = fso.OpenTextFile(filePath, 1); // 1 = ForReading
        var content = file.ReadAll();
        file.Close();

        // PlantUML type'ını belirle
        var diagramType = detectDiagramType(content);
        Session.Output("  Diagram Type: " + diagramType);

        // XMI'ye ekle
        xmiContent += convertToXMI(content, diagramType, fso.GetFileName(filePath));

        Session.Output("  ✓ Converted");
        Session.Output("");
    }

    // XMI footer
    xmiContent += generateXMIFooter();

    // XMI dosyasını yaz
    try {
        var outputFile = fso.CreateTextFile(OUTPUT_XMI, true); // true = overwrite
        outputFile.Write(xmiContent);
        outputFile.Close();

        Session.Output("✓ XMI dosyası oluşturuldu: " + OUTPUT_XMI);
        Session.Output("");
        Session.Output("Bu dosyayı EA'ye import edebilirsiniz:");
        Session.Output("  Project > Import/Export > Import Package from XMI...");
    } catch (e) {
        Session.Output("✗ XMI dosyası yazma hatası: " + e.message);
    }

    Session.Output("");
    Session.Output("===== Conversion Tamamlandı =====");
}

/**
 * Diagram type'ını PlantUML içeriğinden belirle
 */
function detectDiagramType(content) {
    if (content.indexOf("@startuml") == -1) {
        return "Unknown";
    }

    if (content.indexOf("Activity") != -1 || content.indexOf("partition") != -1) {
        return "Activity";
    }

    if (content.indexOf("Sequence") != -1 || content.indexOf("participant") != -1) {
        return "Sequence";
    }

    if (content.indexOf("UseCase") != -1 || content.indexOf("actor") != -1 || content.indexOf("usecase") != -1) {
        return "UseCase";
    }

    return "Unknown";
}

/**
 * XMI header oluştur
 */
function generateXMIHeader() {
    var header = '<?xml version="1.0" encoding="UTF-8"?>\n';
    header += '<XMI xmi.version="1.1" xmlns:UML="omg.org/UML1.3">\n';
    header += '  <XMI.header>\n';
    header += '    <XMI.documentation>\n';
    header += '      <XMI.exporter>PlantUML to XMI Converter</XMI.exporter>\n';
    header += '      <XMI.exporterVersion>1.0</XMI.exporterVersion>\n';
    header += '    </XMI.documentation>\n';
    header += '  </XMI.header>\n';
    header += '  <XMI.content>\n';
    header += '    <UML:Model name="ParseFlow Diagrams" xmi.id="model-001">\n';
    header += '      <UML:Namespace.ownedElement>\n';
    return header;
}

/**
 * XMI footer oluştur
 */
function generateXMIFooter() {
    var footer = '      </UML:Namespace.ownedElement>\n';
    footer += '    </UML:Model>\n';
    footer += '  </XMI.content>\n';
    footer += '</XMI>\n';
    return footer;
}

/**
 * PlantUML içeriğini XMI'ye çevir (basitleştirilmiş)
 */
function convertToXMI(content, diagramType, fileName) {
    var xmi = "";

    // Package oluştur
    var packageId = "pkg-" + fileName.replace(/\./g, "-");
    var packageName = fileName.replace(".puml", "");

    xmi += '        <UML:Package name="' + packageName + '" xmi.id="' + packageId + '">\n';
    xmi += '          <UML:ModelElement.taggedValue>\n';
    xmi += '            <UML:TaggedValue tag="diagram_type" value="' + diagramType + '"/>\n';
    xmi += '            <UML:TaggedValue tag="source" value="PlantUML"/>\n';
    xmi += '          </UML:ModelElement.taggedValue>\n';
    xmi += '          <UML:Namespace.ownedElement>\n';

    // Basit elementler ekle (parsing yapmadan, placeholder)
    xmi += '            <!-- Elements will be created in EA -->\n';
    xmi += '            <!-- Use ImportParseFlowDiagrams.js to create diagram structure -->\n';

    xmi += '          </UML:Namespace.ownedElement>\n';
    xmi += '        </UML:Package>\n';

    return xmi;
}

// Script'i çalıştır
main();
