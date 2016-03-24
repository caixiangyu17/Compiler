@echo  off
setlocal enabledelayedexpansion

set CLASSPATH=%CLASSPATH%;%cd%
cd /d ..

set /a count=count+1
set /p temp=input the file name
echo.
set temp=!temp:~0,-3!
echo ----------------------------!temp!----------------------------------
java VC.vc test/!temp!.vc
echo !temp!.j
java jasmin.Main test/!temp!.j
java test/!temp!
echo.
echo.
echo.
cd /d test
)   
pause
