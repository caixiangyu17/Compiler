@echo  off
setlocal enabledelayedexpansion

for %%i in (*.vc) do (
set CLASSPATH=%CLASSPATH%;%cd%
cd /d ..
set /a count=count+1
set temp=%%i
set temp=!temp:~0,-3!
echo ----------------------------!temp!----------------------------------
java VC.vc test/!temp!.vc > test/output.txt
echo !temp!.j
java jasmin.Main test/!temp!.j
java test/!temp!
echo.
cd /d test
)   
pause
