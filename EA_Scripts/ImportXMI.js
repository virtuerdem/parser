/**
 * Enterprise Architect JavaScript - XMI Import Script
 *
 * Bu script, XMI dosyalarını EA'ye import eder.
 *
 * Kullanım:
 * 1. EA'de: Tools > Scripting
 * 2. "New Script" tıklayın
 * 3. Bu dosyanın içeriğini yapıştırın
 * 4. Aşağıdaki XMI_FILE_PATH değişkenini düzenleyin
 * 5. Script adını "ImportXMI" olarak kaydedin
 * 6. "Run" ile çalıştırın
 */

!INC Local Scripts.EAConstants-JScript

// XMI dosya yolunu buradan düzenleyin
var XMI_FILE_PATH = "C:\\path\\to\\ParseFlow_Diagrams.xmi";

function main() {
    var repository = Repository;

    if (repository == null) {
        Session.Output("ERROR: Repository bulunamadı!");
        return;
    }

    Session.Output("===== XMI Import Script =====");
    Session.Output("");

    // Dosya var mı kontrol et
    var fso = new ActiveXObject("Scripting.FileSystemObject");
    if (!fso.FileExists(XMI_FILE_PATH)) {
        Session.Output("ERROR: XMI dosyası bulunamadı!");
        Session.Output("Path: " + XMI_FILE_PATH);
        Session.Output("");
        Session.Output("Lütfen XMI_FILE_PATH değişkenini düzenleyin.");
        return;
    }

    Session.Output("XMI dosyası bulundu: " + XMI_FILE_PATH);
    Session.Output("");

    // Import package'ini seç
    var models = repository.Models;
    var targetPackage = null;

    // "Parser Module" package'ini ara
    for (var i = 0; i < models.Count; i++) {
        var model = models.GetAt(i);
        if (model.Name == "Parser Module") {
            targetPackage = model;
            break;
        }
    }

    // Yoksa oluştur
    if (targetPackage == null) {
        targetPackage = models.AddNew("Parser Module", "Package");
        targetPackage.Update();
        models.Refresh();
        Session.Output("✓ 'Parser Module' package oluşturuldu");
    } else {
        Session.Output("✓ 'Parser Module' package bulundu (ID: " + targetPackage.PackageID + ")");
    }

    Session.Output("");
    Session.Output("XMI import başlatılıyor...");
    Session.Output("");

    try {
        // XMI import
        // Not: EA JavaScript API'de XMI import için ProjectInterface kullanılır
        var projectInterface = repository.GetProjectInterface();

        // Import XMI
        var result = projectInterface.ImportPackageXMI(
            targetPackage.PackageGUID,
            XMI_FILE_PATH,
            1,  // Import type: 1 = XMI
            0   // Import option: 0 = default
        );

        if (result) {
            Session.Output("✓ XMI import başarılı!");
            Session.Output("");
            Session.Output("Import edilen içerik 'Parser Module' altında görünecektir.");

            // Package'i refresh et
            targetPackage.Packages.Refresh();
            repository.RefreshModelView(targetPackage.PackageID);
        } else {
            Session.Output("✗ XMI import başarısız!");
            Session.Output("");
            Session.Output("Alternatif: Manuel import");
            Session.Output("1. Project Browser'da 'Parser Module' package'ine sağ tıklayın");
            Session.Output("2. 'Import/Export' > 'Import Package from XMI...' seçin");
            Session.Output("3. XMI dosyasını seçin: " + XMI_FILE_PATH);
        }
    } catch (e) {
        Session.Output("✗ Import hatası: " + e.message);
        Session.Output("");
        Session.Output("ÇÖZÜM 1: Manuel Import");
        Session.Output("1. Project Browser'da 'Parser Module' package'ine sağ tıklayın");
        Session.Output("2. 'Import/Export' > 'Import Package from XMI...' seçin");
        Session.Output("3. XMI dosyasını seçin: " + XMI_FILE_PATH);
        Session.Output("");
        Session.Output("ÇÖZÜM 2: EA Menu'den Import");
        Session.Output("1. EA menüden: Project > Import/Export > Import Package from XMI...");
        Session.Output("2. XMI dosyasını seçin: " + XMI_FILE_PATH);
        Session.Output("3. Target Package: 'Parser Module'");
    }

    Session.Output("");
    Session.Output("===== Import İşlemi Tamamlandı =====");
}

// Script'i çalıştır
main();
