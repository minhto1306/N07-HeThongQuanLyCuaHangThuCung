@echo off
setlocal
pushd "%~dp0sandbox-app"
javaw -Dfile.encoding=UTF-8 -Xmx1536m -cp "%~dp0;%~dp0..\baseline-compile\classes;%~dp0..\..\lib\*" CashierLauncher
popd
endlocal
