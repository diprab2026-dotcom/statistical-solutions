@echo off
setlocal
where javac >nul 2>nul || (
  echo ERROR: Install JDK 17 or newer and ensure javac is on PATH.
  exit /b 1
)
where jpackage >nul 2>nul || (
  echo ERROR: jpackage was not found. Install a full JDK 17 or newer.
  exit /b 1
)
if not exist out mkdir out
if not exist package-input mkdir package-input
if exist "installer\Statistical Solutions" rmdir /s /q "installer\Statistical Solutions"
javac -d out src\com\candyacademia\spsslite\*.java || exit /b 1
if exist out\assets rmdir /s /q out\assets
xcopy /e /i /y assets out\assets >nul || exit /b 1
jar --create --file package-input\StatisticalSolutions.jar --main-class com.candyacademia.spsslite.Main -C out . || exit /b 1
jpackage ^
  --type app-image ^
  --name "Statistical Solutions" ^
  --input package-input ^
  --main-jar StatisticalSolutions.jar ^
  --main-class com.candyacademia.spsslite.Main ^
  --dest installer ^
  --vendor "Candy Academia" ^
  --description "Statistical analysis desktop software" ^
  --app-version 1.0.0 ^
  --icon "assets\Statistical-Solutions.ico" ^
  --win-console false
if errorlevel 1 exit /b 1
echo.
echo Build complete:
echo installer\Statistical Solutions\Statistical Solutions.exe
endlocal
