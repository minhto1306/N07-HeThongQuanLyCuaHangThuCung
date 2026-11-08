@echo off
setlocal
cd /d "C:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio"
echo Dang nhap bang tai khoan Google dang dung Antigravity, khong chon API key.
echo Sau khi dang nhap thanh cong, thoat agy de quay lai day.
"C:\Users\lekha\AppData\Local\agy\bin\agy.exe"
echo.
echo Kiem tra trang thai cau noi:
node tools/ai-flow/src/cli.mjs doctor
echo.
echo Bao lai cho Codex khi da thay danh sach model. Khong gui token hay mat khau.
pause
endlocal
