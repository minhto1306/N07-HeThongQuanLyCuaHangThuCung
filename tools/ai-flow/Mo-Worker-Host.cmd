@echo off
setlocal
cd /d "C:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio"
echo Giu cua so nay mo trong khi Codex giao viec cho Gemini.
echo Ctrl+C de dung. Khong dong cua so khi worker dang lam viec.
node tools/ai-flow/src/cli.mjs host
pause
endlocal
