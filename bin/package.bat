@echo off
echo [INFO] Package the war in target dir.

cd %~dp0
cd ..
call mvn clean compile package -Dmaven.test.skip=true -X
cd bin
pause