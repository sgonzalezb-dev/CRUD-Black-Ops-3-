@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo [1/2] Compilando Black Ops III Zombies Arsenal...
if exist out rmdir /s /q out
mkdir out

javac -encoding UTF-8 -d out ^
    src\com\bo3zombies\arsenal\*.java ^
    src\com\bo3zombies\arsenal\model\*.java ^
    src\com\bo3zombies\arsenal\persistence\*.java ^
    src\com\bo3zombies\arsenal\service\*.java ^
    src\com\bo3zombies\arsenal\ui\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Fallo la compilacion. Revisa los mensajes anteriores.
    pause
    exit /b 1
)

echo [2/2] Iniciando aplicacion...
java -Dfile.encoding=UTF-8 -cp "out;resources" com.bo3zombies.arsenal.Main
