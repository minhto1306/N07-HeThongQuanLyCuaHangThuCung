@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist bin mkdir bin
powershell -NoProfile -Command "$f = (Get-ChildItem -Path 'src' -Filter '*.java' -Recurse | ForEach-Object { '\"' + $_.FullName.Replace('\', '/') + '\"' }); [System.IO.File]::WriteAllLines('sources.txt', $f, (New-Object System.Text.UTF8Encoding($false)))"
javac -encoding UTF-8 -cp "lib/*;src" -d bin @sources.txt
set BUILD_ERR=%ERRORLEVEL%
if exist sources.txt del sources.txt
if %BUILD_ERR% EQU 0 (
    echo [THANH CONG] Toan bo ma nguon Java da duoc bien dich thanh cong vao thu muc bin!
) else (
    echo [THAT BAI] Co loi trong qua trinh bien dich ma nguon!
)
exit /b %BUILD_ERR%
