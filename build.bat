@echo off
chcp 65001 >nul
color 0A
title ساخت اپلیکیشن IQ Test

echo.
echo ========================================
echo    ساخت اپلیکیشن آزمون هوش
echo ========================================
echo.

REM بررسی وجود index.html
if not exist "index.html" (
    color 0C
    echo [خطا] فایل index.html پیدا نشد!
    echo فایل HTML خودت رو به index.html تغییر نام بده و بذار توی همین پوشه.
    pause
    exit /b 1
)

echo [1/6] ساخت پروژه npm...
call npm init -y >nul 2>&1
if errorlevel 1 (
    color 0C
    echo [خطا] npm پیدا نشد. Node.js رو نصب کن.
    pause
    exit /b 1
)
echo       انجام شد ✓
echo.

echo [2/6] نصب Capacitor Core و CLI...
call npm install @capacitor/core @capacitor/cli >nul 2>&1
echo       انجام شد ✓
echo.

echo [3/6] راه‌اندازی Capacitor...
call npx cap init "IQ Test" com.iqtest.app --web-dir=. >nul 2>&1
echo       انجام شد ✓
echo.

echo [4/6] نصب پکیج اندروید...
call npm install @capacitor/android >nul 2>&1
echo       انجام شد ✓
echo.

echo [5/6] اضافه کردن پلتفرم اندروید...
call npx cap add android >nul 2>&1
echo       انجام شد ✓
echo.

echo [6/6] انتقال فایل‌ها...
call npx cap sync android >nul 2>&1
echo       انجام شد ✓
echo.

color 0A
echo ========================================
echo    ✓ همه‌چیز آماده شد!
echo ========================================
echo.
echo حالا می‌تونی Android Studio رو باز کنی:
echo.
echo   npx cap open android
echo.
echo یا فایل open-android.bat رو دوبار کلیک کن.
echo.

REM ساخت فایل باز کردن Android Studio
echo @echo off > open-android.bat
echo chcp 65001 ^>nul >> open-android.bat
echo call npx cap open android >> open-android.bat
echo echo Android Studio در حال باز شدن است... >> open-android.bat
echo pause >> open-android.bat

echo یه فایل "open-android.bat" هم ساختم برات.
echo.
pause