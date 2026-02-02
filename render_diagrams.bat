@echo off
REM ====================================================
REM ParseFlow PlantUML Diagram Renderer (Windows)
REM ====================================================
REM
REM Kullanim:
REM   1. plantuml.jar dosyasini bu klasore koyun
REM   2. Java yuklu olduguna emin olun
REM   3. Graphviz yuklu olduguna emin olun
REM   4. Bu batch dosyasini cift tiklayarak calistirin
REM
REM ====================================================

echo.
echo ====================================================
echo   ParseFlow Diagram Renderer
echo ====================================================
echo.

REM Java kontrolu
echo [1/5] Java kontrolu yapiliyor...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [HATA] Java bulunamadi!
    echo Lutfen Java yukleyin: https://www.java.com/
    echo.
    pause
    exit /b 1
)
echo       OK - Java bulundu

REM plantuml.jar kontrolu
echo [2/5] plantuml.jar kontrolu yapiliyor...
if not exist plantuml.jar (
    echo.
    echo [HATA] plantuml.jar bulunamadi!
    echo Lutfen plantuml.jar dosyasini bu klasore koyun.
    echo Indir: https://plantuml.com/download
    echo.
    pause
    exit /b 1
)
echo       OK - plantuml.jar bulundu

REM Output klasoru olustur
echo [3/5] Output klasoru olusturuluyor...
if not exist "diagrams_output" mkdir diagrams_output
echo       OK - diagrams_output/ olusturuldu

REM Diagramlari render et
echo [4/5] Diagramlar render ediliyor...
echo.

REM Activity Diagram
if exist ParseFlow_ActivityDiagram.puml (
    echo     - ParseFlow_ActivityDiagram.puml
    java -jar plantuml.jar -tsvg -o diagrams_output ParseFlow_ActivityDiagram.puml
    echo       ^> SVG olusturuldu: diagrams_output/ParseFlow_ActivityDiagram.svg
) else (
    echo     ! ParseFlow_ActivityDiagram.puml bulunamadi (atlanıyor)
)

REM Sequence Diagram
if exist ParseFlow_SequenceDiagram.puml (
    echo     - ParseFlow_SequenceDiagram.puml
    java -jar plantuml.jar -tsvg -o diagrams_output ParseFlow_SequenceDiagram.puml
    echo       ^> SVG olusturuldu: diagrams_output/ParseFlow_SequenceDiagram.svg
) else (
    echo     ! ParseFlow_SequenceDiagram.puml bulunamadi (atlanıyor)
)

REM Use Case Diagram
if exist ParseFlow_UseCaseDiagram.puml (
    echo     - ParseFlow_UseCaseDiagram.puml
    java -jar plantuml.jar -tsvg -o diagrams_output ParseFlow_UseCaseDiagram.puml
    echo       ^> SVG olusturuldu: diagrams_output/ParseFlow_UseCaseDiagram.svg
) else (
    echo     ! ParseFlow_UseCaseDiagram.puml bulunamadi (atlanıyor)
)

REM Transfer Flow diagramlari varsa
if exist TransferFlow_ActivityDiagram_v2.puml (
    echo     - TransferFlow_ActivityDiagram_v2.puml
    java -jar plantuml.jar -tsvg -o diagrams_output TransferFlow_ActivityDiagram_v2.puml
    echo       ^> SVG olusturuldu
)

if exist TransferFlow_SequenceDiagram_v2.puml (
    echo     - TransferFlow_SequenceDiagram_v2.puml
    java -jar plantuml.jar -tsvg -o diagrams_output TransferFlow_SequenceDiagram_v2.puml
    echo       ^> SVG olusturuldu
)

if exist TransferFlow_UseCaseDiagram.puml (
    echo     - TransferFlow_UseCaseDiagram.puml
    java -jar plantuml.jar -tsvg -o diagrams_output TransferFlow_UseCaseDiagram.puml
    echo       ^> SVG olusturuldu
)

echo.
echo [5/5] Islem tamamlandi!

REM Output klasorunu ac
echo.
echo Render edilen SVG dosyalari: diagrams_output\
echo.
echo Output klasorunu acmak ister misiniz? (E/H)
set /p OPEN="Seciminiz: "
if /i "%OPEN%"=="E" (
    start "" "diagrams_output"
)

echo.
echo ====================================================
echo   Basariyla tamamlandi!
echo ====================================================
echo.
pause
