@echo off
chcp 65001 >nul
color 0A
title ساخت اپلیکیشن IQ Test

echo.
echo ========================================
echo    ساخت اپلیکیشن آزمون هوش
echo ========================================
echo.

REM بررسی وجود فایل ورودی وب
if not exist "index.html" if not exist "iq-test.html" (
    color 0C
    echo [خطا] فایل index.html یا iq-test.html پیدا نشد!
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
call npm install @capacitor/core @capacitor/cli
if errorlevel 1 exit /b 1
echo       انجام شد ✓
echo.

REM آماده‌سازی webDir مورد استفاده Capacitor
if not exist "www" mkdir www
if exist "index.html" copy /Y "index.html" "www\index.html" >nul
if not exist "www\index.html" if exist "iq-test.html" copy /Y "iq-test.html" "www\index.html" >nul

echo [3/6] راه‌اندازی Capacitor...
call npx cap init "IQ Test" com.iqtest.app --web-dir=www
if errorlevel 1 exit /b 1
echo       انجام شد ✓
echo.

echo [4/6] نصب پکیج اندروید...
call npm install @capacitor/android
if errorlevel 1 exit /b 1
echo       انجام شد ✓
echo.

echo [5/6] اضافه کردن پلتفرم اندروید...
call npx cap add android
if errorlevel 1 exit /b 1
echo       انجام شد ✓
echo.

echo [6/6] انتقال فایل‌ها...
call npx cap sync android
if errorlevel 1 exit /b 1
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
pause
