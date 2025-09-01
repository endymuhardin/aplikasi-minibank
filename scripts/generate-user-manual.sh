#!/bin/bash

# Indonesian User Manual Generator Script
# Aplikasi Minibank - Sistem Perbankan Syariah
#
# This script is a complete one-stop solution that:
# 1. Runs the documentation tests to generate screenshots and videos
# 2. Generates comprehensive Indonesian user manual automatically
#
# Usage:
#    ./scripts/generate-user-manual.sh [options]
#    or
#    bash scripts/generate-user-manual.sh [options]
#
# Options:
#    --fast          Run tests in headless mode (faster, no visual feedback)
#    --visible       Run tests with visible browser (slower, shows execution)
#    --help          Show this help message

set -e

# Default options
HEADLESS="false"
SLOWMO="2000"
SHOW_HELP=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --fast)
            HEADLESS="true"
            SLOWMO="500"
            shift
            ;;
        --visible)
            HEADLESS="false"
            SLOWMO="2000"
            shift
            ;;
        --help)
            SHOW_HELP=true
            shift
            ;;
        *)
            echo "❌ Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Show help if requested
if [ "$SHOW_HELP" = true ]; then
    echo "Indonesian User Manual Generator - Aplikasi Minibank"
    echo ""
    echo "This script automatically:"
    echo "1. Runs documentation tests to capture screenshots and videos"
    echo "2. Generates comprehensive Indonesian user manual"
    echo ""
    echo "Usage:"
    echo "  bash scripts/generate-user-manual.sh [options]"
    echo ""
    echo "Options:"
    echo "  --fast      Run tests in headless mode (faster, no visual feedback)"
    echo "  --visible   Run tests with visible browser (slower, shows execution) [default]"
    echo "  --help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  bash scripts/generate-user-manual.sh              # Default: visible browser"
    echo "  bash scripts/generate-user-manual.sh --fast       # Fast headless mode"
    echo "  bash scripts/generate-user-manual.sh --visible    # Explicit visible mode"
    exit 0
fi

echo "🚀 Memulai pembuatan panduan pengguna Aplikasi Minibank..."
echo ""

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: Script harus dijalankan dari root directory project (yang mengandung pom.xml)"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java tidak ditemukan. Pastikan Java sudah terinstall."
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven tidak ditemukan. Pastikan Maven sudah terinstall."
    exit 1
fi

# Check if Docker is running for database tests
if ! docker info &> /dev/null; then
    echo "❌ Error: Docker tidak berjalan. Pastikan Docker sudah dijalankan untuk database testing."
    exit 1
fi

echo "✅ Prasyarat sistem terpenuhi"
echo ""

# Step 1: Clean previous test artifacts
echo "🧹 Membersihkan file test sebelumnya..."
rm -rf target/playwright-screenshots target/playwright-recordings 2>/dev/null || true

# Step 2: Compile the project first
echo "🔧 Kompilasi project..."
mvn compile -q

if [ $? -ne 0 ]; then
    echo "❌ Error: Gagal mengkompilasi project."
    exit 1
fi

echo "✅ Kompilasi berhasil"
echo ""

# Step 3: Run documentation tests to generate screenshots and videos
echo "🎬 Menjalankan test dokumentasi untuk menggenerate screenshot dan video..."
echo "   Mode: $([ "$HEADLESS" = "true" ] && echo "Headless (cepat)" || echo "Visible browser (lambat)")"
echo "   Slow motion: ${SLOWMO}ms delay"
echo ""

TEST_START_TIME=$(date +%s)

# Run the documentation test with appropriate settings
mvn test \
    -Dtest=PersonalCustomerAccountOpeningTutorialTest \
    -Dplaywright.headless="$HEADLESS" \
    -Dplaywright.slowmo="$SLOWMO" \
    -Dplaywright.record=true \
    -q

TEST_EXIT_CODE=$?
TEST_END_TIME=$(date +%s)
TEST_DURATION=$((TEST_END_TIME - TEST_START_TIME))

if [ $TEST_EXIT_CODE -ne 0 ]; then
    echo ""
    echo "❌ Error: Test dokumentasi gagal."
    echo "   Periksa log error di atas untuk detail masalah."
    echo "   Pastikan database Docker sudah berjalan."
    exit 1
fi

echo ""
echo "✅ Test dokumentasi berhasil ($(($TEST_DURATION / 60))m $(($TEST_DURATION % 60))s)"

# Check if test generated the expected files
SCREENSHOT_COUNT=0
VIDEO_COUNT=0

if [ -d "target/playwright-screenshots" ]; then
    SCREENSHOT_COUNT=$(find target/playwright-screenshots -name "*.png" | wc -l)
fi

if [ -d "target/playwright-recordings" ]; then
    VIDEO_COUNT=$(find target/playwright-recordings -name "*.webm" | wc -l)
fi

echo "📊 File yang dihasilkan:"
echo "   📷 Screenshot: $SCREENSHOT_COUNT file"
echo "   🎥 Video: $VIDEO_COUNT file"
echo ""

if [ $SCREENSHOT_COUNT -eq 0 ] && [ $VIDEO_COUNT -eq 0 ]; then
    echo "⚠️  Peringatan: Tidak ada screenshot atau video yang dihasilkan."
    echo "   Panduan akan dibuat tanpa media visual."
fi

# Step 4: Generate the Indonesian user manual
echo "📚 Menggenerate panduan pengguna Indonesia..."
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.UserManualGenerator" -q

MANUAL_EXIT_CODE=$?

if [ $MANUAL_EXIT_CODE -eq 0 ]; then
    # Step 5: Copy generated documentation to docs/user-manual
    echo "📂 Menyalin dokumentasi ke direktori docs/user-manual..."
    
    # Create docs/user-manual directory if it doesn't exist
    mkdir -p docs/user-manual
    
    # Copy all generated content from target to docs folder
    if [ -d "target/playwright-documentation" ]; then
        cp -r target/playwright-documentation/* docs/user-manual/
        echo "✅ Dokumentasi berhasil disalin ke docs/user-manual/"
    else
        echo "❌ Error: Direktori target/playwright-documentation tidak ditemukan"
        exit 1
    fi
    
    echo ""
    echo "✅ Panduan pengguna berhasil dibuat!"
    echo ""
    
    # Show generated files
    OUTPUT_DIR="docs/user-manual"
    if [ -d "$OUTPUT_DIR" ]; then
        echo "📁 File yang dibuat:"
        find "$OUTPUT_DIR" -type f -name "*.md" | while read -r file; do
            echo "   📄 $(realpath --relative-to=. "$file" 2>/dev/null || echo "$file")"
        done
        echo ""
        
        # Show file sizes
        echo "📊 Detail file:"
        find "$OUTPUT_DIR" -type f -name "*.md" -exec ls -lh {} \; | awk '{print "   " $9 " (" $5 ")"}'
        echo ""
    fi
    
    # Show final summary
    echo "📊 Ringkasan lengkap:"
    echo "   📷 Screenshot digunakan: $SCREENSHOT_COUNT file"
    echo "   🎥 Video digunakan: $VIDEO_COUNT file"
    echo "   📄 Dokumen dibuat: 2 file (README + panduan utama)"
    echo ""
    echo "🎯 Panduan dapat diakses di: docs/user-manual/README.md"
    echo ""
    echo "💡 Tips penggunaan:"
    echo "   • Buka file markdown dengan editor yang mendukung preview"
    echo "   • Convert ke HTML/PDF untuk distribusi yang lebih luas"
    echo "   • Screenshot dan video tersimpan untuk referensi langsung"
    echo ""
    
    # Show total execution time
    TOTAL_END_TIME=$(date +%s)
    TOTAL_DURATION=$((TOTAL_END_TIME - TEST_START_TIME))
    echo "⏱️  Total waktu eksekusi: $(($TOTAL_DURATION / 60))m $(($TOTAL_DURATION % 60))s"
    
else
    echo ""
    echo "❌ Error: Gagal membuat panduan pengguna."
    echo "   Test berhasil tapi generator panduan gagal."
    echo "   Periksa log error di atas untuk detail masalah."
    exit 1
fi

echo ""
echo "🎉 Selesai! Panduan pengguna lengkap siap digunakan."
echo ""
echo "📂 Langkah selanjutnya:"
echo "   1. Buka docs/user-manual/README.md untuk overview"
echo "   2. Buka docs/user-manual/panduan-pembukaan-rekening-nasabah-personal.md untuk tutorial lengkap"
echo "   3. Bagikan dokumentasi ke tim Customer Service"
echo ""