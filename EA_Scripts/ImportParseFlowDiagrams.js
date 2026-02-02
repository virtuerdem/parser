/**
 * Enterprise Architect JavaScript - ParseFlow Diagrams Import Script
 *
 * Bu script, ParseFlow diagramlarını EA'ye import etmek için kullanılır.
 *
 * Kullanım:
 * 1. EA'de: Tools > Scripting
 * 2. "New Script" tıklayın
 * 3. Bu dosyanın içeriğini yapıştırın
 * 4. Script adını "ImportParseFlowDiagrams" olarak kaydedin
 * 5. "Run" ile çalıştırın
 *
 * NOT: XMI dosyaları repository'de olmalı
 */

!INC Local Scripts.EAConstants-JScript

function main() {
    // EA Repository nesnesini al
    var repository = Repository;

    if (repository == null) {
        Session.Output("ERROR: Repository bulunamadı!");
        return;
    }

    Session.Output("===== ParseFlow Diagrams Import Başlatılıyor =====");
    Session.Output("");

    // Root package'i al veya oluştur
    var models = repository.Models;
    var rootPackage = null;

    // "Parser Module" package'ini ara
    for (var i = 0; i < models.Count; i++) {
        var model = models.GetAt(i);
        if (model.Name == "Parser Module") {
            rootPackage = model;
            break;
        }
    }

    // Yoksa oluştur
    if (rootPackage == null) {
        rootPackage = models.AddNew("Parser Module", "Package");
        rootPackage.Update();
        models.Refresh();
        Session.Output("✓ 'Parser Module' package oluşturuldu");
    } else {
        Session.Output("✓ 'Parser Module' package bulundu");
    }

    // Parse Flow package'ini oluştur
    var parseFlowPackage = getOrCreatePackage(rootPackage, "Parse Flow Diagrams");

    Session.Output("");
    Session.Output("===== Diagramlar Oluşturuluyor =====");
    Session.Output("");

    // Activity Diagram
    createActivityDiagram(parseFlowPackage);

    // Sequence Diagram
    createSequenceDiagram(parseFlowPackage);

    // Use Case Diagram
    createUseCaseDiagram(parseFlowPackage);

    Session.Output("");
    Session.Output("===== Import Tamamlandı =====");
    Session.Output("");
    Session.Output("Diagramlar 'Parser Module > Parse Flow Diagrams' altında oluşturuldu.");
    Session.Output("");
    Session.Output("NOT: Diagramlar boş şablonlar olarak oluşturuldu.");
    Session.Output("PlantUML dosyalarındaki içeriği manuel olarak EA'ye aktarabilirsiniz.");
}

/**
 * Package al veya oluştur
 */
function getOrCreatePackage(parentPackage, packageName) {
    var packages = parentPackage.Packages;

    // Var mı kontrol et
    for (var i = 0; i < packages.Count; i++) {
        var pkg = packages.GetAt(i);
        if (pkg.Name == packageName) {
            Session.Output("✓ Package bulundu: " + packageName);
            return pkg;
        }
    }

    // Yoksa oluştur
    var newPackage = packages.AddNew(packageName, "Package");
    newPackage.Update();
    packages.Refresh();
    Session.Output("✓ Package oluşturuldu: " + packageName);
    return newPackage;
}

/**
 * Activity Diagram oluştur
 */
function createActivityDiagram(parentPackage) {
    Session.Output("Creating Activity Diagram...");

    var diagrams = parentPackage.Diagrams;
    var diagram = diagrams.AddNew("ParseFlow - Activity Diagram", "Activity");
    diagram.Notes = "Parse Flow aktivite diyagramı.\n\n" +
                    "Ana akış:\n" +
                    "1. Engine Initialization\n" +
                    "2. Parse Phase (Parallel XML parsing)\n" +
                    "3. Content Date Discovery\n" +
                    "4. Data Loading\n" +
                    "5. Post-Processing\n\n" +
                    "PlantUML: ParseFlow_ActivityDiagram.puml";
    diagram.Update();
    diagrams.Refresh();

    // Ana swim lanes ekle
    addActivityElements(diagram, parentPackage);

    Session.Output("  ✓ Activity Diagram oluşturuldu");
}

/**
 * Activity Diagram elementleri ekle
 */
function addActivityElements(diagram, parentPackage) {
    var elements = parentPackage.Elements;

    // Activities
    var activities = [
        "Start",
        "startEngine",
        "preparePaths",
        "getTables",
        "Parse XML Files (Parallel)",
        "Auto Counter Discovery",
        "Content Date Discovery",
        "Bulk Data Loading",
        "Post Processing",
        "End"
    ];

    // Her activity'i oluştur ve diyagrama ekle (dikey düzen)
    var startX = 200;  // Sol marjin
    var startY = 50;   // Üst marjin
    var nodeWidth = 180;
    var nodeHeight = 60;
    var verticalGap = 120;  // Node'lar arası dikey boşluk

    for (var i = 0; i < activities.length; i++) {
        var activity = elements.AddNew(activities[i], "Action");
        activity.Update();

        // Diyagrama ekle - dikey sıralama
        var yPos = startY + (i * verticalGap);
        var positionString = "l=" + startX + ";t=" + yPos + ";r=" + (startX + nodeWidth) + ";b=" + (yPos + nodeHeight) + ";";

        var diagramObject = diagram.DiagramObjects.AddNew(positionString, "");
        diagramObject.ElementID = activity.ElementID;
        diagramObject.Update();
    }

    elements.Refresh();
}

/**
 * Sequence Diagram oluştur
 */
function createSequenceDiagram(parentPackage) {
    Session.Output("Creating Sequence Diagram...");

    var diagrams = parentPackage.Diagrams;
    var diagram = diagrams.AddNew("ParseFlow - Sequence Diagram", "Sequence");
    diagram.Notes = "Parse Flow detaylı sequence diyagramı.\n\n" +
                    "5 Ana Fase:\n" +
                    "1. Engine Initialization (DB config reads)\n" +
                    "2. Parse Phase (Parallel XML parsing)\n" +
                    "3. Content Date Discovery\n" +
                    "4. Data Loading\n" +
                    "5. Post-Loading Operations\n\n" +
                    "Database Tables:\n" +
                    "- t_flow, t_branch, t_parse_engine, t_parse_component\n" +
                    "- t_parse_table, t_parse_column\n" +
                    "- t_network_node\n" +
                    "- t_all_counter\n" +
                    "- t_content_date_result\n" +
                    "- t_loader_result\n\n" +
                    "PlantUML: ParseFlow_SequenceDiagram.puml";
    diagram.Update();
    diagrams.Refresh();

    // Lifeline'lar ekle
    addSequenceElements(diagram, parentPackage);

    Session.Output("  ✓ Sequence Diagram oluşturuldu");
}

/**
 * Sequence Diagram elementleri ekle
 */
function addSequenceElements(diagram, parentPackage) {
    var elements = parentPackage.Elements;

    // Lifelines
    var lifelines = [
        {name: "Transfer Module", type: "Actor"},
        {name: "ParseBaseEngine", type: "Object"},
        {name: "Repository", type: "Object"},
        {name: "Database", type: "Object"},
        {name: "ExecutorService", type: "Object"},
        {name: "ParseHandler", type: "Object"},
        {name: "SAXParser", type: "Object"},
        {name: "Writer", type: "Object"},
        {name: "LoaderFactory", type: "Object"}
    ];

    // Lifeline'ları yatay sırada yerleştir
    var startX = 50;
    var startY = 50;
    var lifelineWidth = 120;
    var lifelineHeight = 80;
    var horizontalGap = 150;  // Lifeline'lar arası yatay boşluk

    for (var i = 0; i < lifelines.length; i++) {
        var lifeline = elements.AddNew(lifelines[i].name, lifelines[i].type);
        lifeline.Update();

        // Diyagrama ekle - yatay sıralama
        var xPos = startX + (i * horizontalGap);
        var positionString = "l=" + xPos + ";t=" + startY + ";r=" + (xPos + lifelineWidth) + ";b=" + (startY + lifelineHeight) + ";";

        var diagramObject = diagram.DiagramObjects.AddNew(positionString, "");
        diagramObject.ElementID = lifeline.ElementID;
        diagramObject.Update();
    }

    elements.Refresh();
}

/**
 * Use Case Diagram oluştur
 */
function createUseCaseDiagram(parentPackage) {
    Session.Output("Creating Use Case Diagram...");

    var diagrams = parentPackage.Diagrams;
    var diagram = diagrams.AddNew("ParseFlow - Use Case Diagram", "Use Case");
    diagram.Notes = "Parse Flow kullanım senaryoları.\n\n" +
                    "Aktörler:\n" +
                    "1. Transfer Module\n" +
                    "2. Scheduler\n" +
                    "3. Database Administrator\n" +
                    "4. Data Analyst\n" +
                    "5. Monitoring System\n\n" +
                    "Ana Paketler:\n" +
                    "1. Engine Management\n" +
                    "2. Metadata Management\n" +
                    "3. XML Parsing\n" +
                    "4. Content Analysis\n" +
                    "5. Data Loading\n" +
                    "6. Post-Processing\n" +
                    "7. Monitoring & Logging\n\n" +
                    "PlantUML: ParseFlow_UseCaseDiagram.puml";
    diagram.Update();
    diagrams.Refresh();

    // Use case elementleri ekle
    addUseCaseElements(diagram, parentPackage);

    Session.Output("  ✓ Use Case Diagram oluşturuldu");
}

/**
 * Use Case Diagram elementleri ekle
 */
function addUseCaseElements(diagram, parentPackage) {
    var elements = parentPackage.Elements;

    // Actors - sol tarafta dikey sıralama
    var actors = [
        "Transfer Module",
        "Scheduler",
        "Database Administrator",
        "Data Analyst",
        "Monitoring System"
    ];

    var actorX = 50;
    var actorStartY = 100;
    var actorWidth = 100;
    var actorHeight = 80;
    var actorGap = 120;

    for (var i = 0; i < actors.length; i++) {
        var actor = elements.AddNew(actors[i], "Actor");
        actor.Update();

        // Diyagrama ekle (sol tarafta)
        var yPos = actorStartY + (i * actorGap);
        var positionString = "l=" + actorX + ";t=" + yPos + ";r=" + (actorX + actorWidth) + ";b=" + (yPos + actorHeight) + ";";

        var diagramObject = diagram.DiagramObjects.AddNew(positionString, "");
        diagramObject.ElementID = actor.ElementID;
        diagramObject.Update();
    }

    // Use Cases - sağ tarafta dikey sıralama
    var useCases = [
        "Initialize Parse Engine",
        "Parse XML Files",
        "Auto Discover Counters",
        "Load Data to Database",
        "Generate Reports",
        "Monitor Processing",
        "Handle Errors"
    ];

    var useCaseX = 400;
    var useCaseStartY = 80;
    var useCaseWidth = 180;
    var useCaseHeight = 70;
    var useCaseGap = 100;

    for (var i = 0; i < useCases.length; i++) {
        var useCase = elements.AddNew(useCases[i], "UseCase");
        useCase.Update();

        // Diyagrama ekle (sağ tarafta)
        var yPos = useCaseStartY + (i * useCaseGap);
        var positionString = "l=" + useCaseX + ";t=" + yPos + ";r=" + (useCaseX + useCaseWidth) + ";b=" + (yPos + useCaseHeight) + ";";

        var diagramObject = diagram.DiagramObjects.AddNew(positionString, "");
        diagramObject.ElementID = useCase.ElementID;
        diagramObject.Update();
    }

    elements.Refresh();
}

// Script'i çalıştır
main();
