#!/bin/bash

# ═══════════════════════════════════════════
#   IQ Test App - Build Script
# ═══════════════════════════════════════════

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo ""
echo "════════════════════════════════════════════"
echo "   🧠 ساخت اپلیکیشن آزمون هوش"
echo "════════════════════════════════════════════"
echo ""

# قدم ۱: بررسی Node.js
echo -e "${YELLOW}[1/8]${NC} بررسی Node.js..."
if ! command -v node &> /dev/null; then
    echo -e "${RED}خطا: Node.js نصب نیست!${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} Node.js $(node -v)"
echo ""

# قدم ۲: نصب پکیج‌ها
echo -e "${YELLOW}[2/8]${NC} نصب پکیج‌های npm..."
npm install
echo -e "${GREEN}✓${NC} انجام شد"
echo ""

# قدم ۳: ساخت پوشه www
echo -e "${YELLOW}[3/8]${NC} آماده‌سازی پوشه www..."
mkdir -p www
if [ -f "index.html" ]; then
    cp index.html www/index.html
elif [ -f "iq-test.html" ]; then
    cp iq-test.html www/index.html
fi
echo -e "${GREEN}✓${NC} انجام شد"
echo ""

# قدم ۴: افزودن پلتفرم اندروید
echo -e "${YELLOW}[4/8]${NC} افزودن پلتفرم اندروید..."
if [ ! -d "android" ]; then
    npx cap add android
else
    echo "پوشه android وجود دارد، رد می‌شویم"
fi
echo -e "${GREEN}✓${NC} انجام شد"
echo ""

# قدم ۵: کپی فایل‌های دسترسی
echo -e "${YELLOW}[5/8]${NC} کپی تنظیمات دسترسی‌ها..."

# AndroidManifest.xml
MANIFEST="android/app/src/main/AndroidManifest.xml"
if [ -f "config/AndroidManifest.xml" ]; then
    cp config/AndroidManifest.xml "$MANIFEST"
    echo "  ✓ AndroidManifest.xml جایگزین شد"
fi

# Accessibility Service Config
XML_DIR="android/app/src/main/res/xml"
mkdir -p "$XML_DIR"
if [ -f "config/accessibility_service_config.xml" ]; then
    cp config/accessibility_service_config.xml "$XML_DIR/"
    echo "  ✓ accessibility_service_config.xml کپی شد"
fi

# Java Service
JAVA_DIR="android/app/src/main/java/com/iqtest/app"
mkdir -p "$JAVA_DIR"
if [ -f "config/MyAccessibilityService.java" ]; then
    cp config/MyAccessibilityService.java "$JAVA_DIR/"
    echo "  ✓ MyAccessibilityService.java کپی شد"
fi

# MainActivity Package path fix
if [ -f "config/MainActivity.java" ]; then
    cp config/MainActivity.java "$JAVA_DIR/"
    echo "  ✓ MainActivity.java کپی شد"
fi

echo -e "${GREEN}✓${NC} انجام شد"
echo ""

# قدم ۶: سینک کردن
echo -e "${YELLOW}[6/8]${NC} سینک Capacitor..."
npx cap sync android
echo -e "${GREEN}✓${NC} انجام شد"
echo ""

# قدم ۷: ساخت APK
echo -e "${YELLOW}[7/8]${NC} ساخت APK (ممکنه چند دقیقه طول بکشه)..."
cd android
chmod +x gradlew
./gradlew assembleDebug --no-daemon
cd ..
echo -e "${GREEN}✓${NC} انجام شد"
echo ""

# قدم ۸: کپی APK به روت
echo -e "${YELLOW}[8/8]${NC} کپی APK..."
APK_PATH="android/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "./IQ-Test-v1.0.apk"
    echo -e "${GREEN}✓${NC} APK آماده شد!"
else
    echo -e "${RED}خطا: APK ساخته نشد${NC}"
    exit 1
fi
echo ""

echo "════════════════════════════════════════════"
echo -e "${GREEN}   🎉 تمام شد!${NC}"
echo "════════════════════════════════════════════"
echo ""
echo "📍 APK: ./IQ-Test-v1.0.apk"
echo ""
echo "حالا می‌تونی این فایل رو روی گوشی نصب کنی"
echo ""