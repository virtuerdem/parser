#!/bin/bash
# ====================================================
# ParseFlow PlantUML Diagram Renderer (Linux/macOS)
# ====================================================
#
# Kullanim:
#   1. plantuml.jar dosyasini bu klasore koyun
#   2. Java yuklu olduguna emin olun
#   3. Graphviz yuklu olduguna emin olun
#   4. chmod +x render_diagrams.sh
#   5. ./render_diagrams.sh
#
# ====================================================

# Renkler
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "===================================================="
echo "   ParseFlow Diagram Renderer"
echo "===================================================="
echo ""

# Java kontrolu
echo -e "${BLUE}[1/5]${NC} Java kontrolu yapiliyor..."
if ! command -v java &> /dev/null; then
    echo ""
    echo -e "${RED}[HATA]${NC} Java bulunamadi!"
    echo "Lutfen Java yukleyin:"
    echo "  Ubuntu/Debian: sudo apt-get install default-jre"
    echo "  macOS: brew install openjdk"
    echo ""
    exit 1
fi
echo -e "      ${GREEN}OK${NC} - Java bulundu"

# plantuml.jar kontrolu
echo -e "${BLUE}[2/5]${NC} plantuml.jar kontrolu yapiliyor..."
if [ ! -f "plantuml.jar" ]; then
    echo ""
    echo -e "${RED}[HATA]${NC} plantuml.jar bulunamadi!"
    echo "Lutfen plantuml.jar dosyasini bu klasore koyun."
    echo ""
    echo "Indir:"
    echo "  wget https://github.com/plantuml/plantuml/releases/download/v1.2024.3/plantuml-1.2024.3.jar -O plantuml.jar"
    echo ""
    exit 1
fi
echo -e "      ${GREEN}OK${NC} - plantuml.jar bulundu"

# Graphviz kontrolu (opsiyonel ama onerilen)
if ! command -v dot &> /dev/null; then
    echo -e "      ${YELLOW}UYARI${NC} - Graphviz bulunamadi (bazi diagramlar render edilmeyebilir)"
    echo "      Graphviz yuklemek icin:"
    echo "        Ubuntu/Debian: sudo apt-get install graphviz"
    echo "        macOS: brew install graphviz"
fi

# Output klasoru olustur
echo -e "${BLUE}[3/5]${NC} Output klasoru olusturuluyor..."
mkdir -p diagrams_output
echo -e "      ${GREEN}OK${NC} - diagrams_output/ olusturuldu"

# Diagramlari render et
echo -e "${BLUE}[4/5]${NC} Diagramlar render ediliyor..."
echo ""

SUCCESS_COUNT=0
FAIL_COUNT=0

# Render fonksiyonu
render_diagram() {
    local file=$1
    if [ -f "$file" ]; then
        echo -n "    - $file ... "
        if java -jar plantuml.jar -tsvg -o diagrams_output "$file" 2>/dev/null; then
            echo -e "${GREEN}OK${NC}"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        else
            echo -e "${RED}FAIL${NC}"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        echo -e "    ${YELLOW}!${NC} $file bulunamadi (atlanıyor)"
    fi
}

# ParseFlow diagramlari
render_diagram "ParseFlow_ActivityDiagram.puml"
render_diagram "ParseFlow_SequenceDiagram.puml"
render_diagram "ParseFlow_UseCaseDiagram.puml"

# TransferFlow diagramlari (varsa)
render_diagram "TransferFlow_ActivityDiagram_v2.puml"
render_diagram "TransferFlow_SequenceDiagram_v2.puml"
render_diagram "TransferFlow_UseCaseDiagram.puml"

echo ""
echo -e "${BLUE}[5/5]${NC} Islem tamamlandi!"
echo ""

# Sonuc ozeti
echo "===================================================="
echo -e "   ${GREEN}Basarili:${NC} $SUCCESS_COUNT diagram"
if [ $FAIL_COUNT -gt 0 ]; then
    echo -e "   ${RED}Basarisiz:${NC} $FAIL_COUNT diagram"
fi
echo "===================================================="
echo ""
echo "Render edilen SVG dosyalari: diagrams_output/"
echo ""

# Output klasorunu listele
if [ $SUCCESS_COUNT -gt 0 ]; then
    echo "Olusturulan dosyalar:"
    ls -lh diagrams_output/*.svg 2>/dev/null | awk '{print "  - " $9 " (" $5 ")"}'
    echo ""
fi

# macOS'ta Finder'da ac
if [[ "$OSTYPE" == "darwin"* ]]; then
    read -p "Output klasorunu acmak ister misiniz? (e/h): " OPEN
    if [[ "$OPEN" == "e" || "$OPEN" == "E" ]]; then
        open diagrams_output
    fi
fi

# Linux'ta file manager ile ac
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    read -p "Output klasorunu acmak ister misiniz? (e/h): " OPEN
    if [[ "$OPEN" == "e" || "$OPEN" == "E" ]]; then
        if command -v xdg-open &> /dev/null; then
            xdg-open diagrams_output
        elif command -v nautilus &> /dev/null; then
            nautilus diagrams_output
        elif command -v dolphin &> /dev/null; then
            dolphin diagrams_output
        else
            echo "File manager bulunamadi. Manuel olarak acin: diagrams_output/"
        fi
    fi
fi

echo ""
echo -e "${GREEN}Basariyla tamamlandi!${NC}"
echo ""
