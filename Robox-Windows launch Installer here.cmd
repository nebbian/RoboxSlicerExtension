@echo off
:: BatchGotAdmin (Run as Admin code starts)
REM --> Check for permissions
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
echo Requesting administrative privileges...
goto UACPrompt
) else ( goto gotAdmin )
:UACPrompt
echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
echo UAC.ShellExecute "%~s0", "", "", "runas", 1 >> "%temp%\getadmin.vbs"
"%temp%\getadmin.vbs"
exit /B
:gotAdmin
if exist "%temp%\getadmin.vbs" ( del "%temp%\getadmin.vbs" )
pushd "%CD%"
CD /D "%~dp0"
:: BatchGotAdmin (Run as Admin code ends)
:: Your codes should start from the following line

SET NONE="none"
SET CELJAVA=%NONE%

@ECHO OFF &SETLOCAL
FOR /f %%a IN ('java -fullversion 2^>^&1^|awk "{print $NF}"') DO SET "javaversion=%%a"
IF DEFINED javaversion (
	ECHO java version: %javaversion%
	SET CELJAVA=java
) ELSE (
	ECHO java NOT found
)

ECHO 1 %CELJAVA%

IF EXIST "C:\Program Files\CEL\AutoMaker\java\bin\java.exe" (
	@echo Found Automaker java launching installer ...
	SET "CELJAVA=C:\Program Files\CEL\AutoMaker\java\bin\java.exe"
)
IF EXIST "C:\Program Files (x86)\CEL\AutoMaker\java\bin\java.exe" (
	@echo Found Automaker java launching installer ...
	SET "CELJAVA=C:\Program Files (x86)\CEL\AutoMaker\java\bin\java.exe"
) 

IF "%CELJAVA%" == "none" (
	@echo default automaker folder was not found launching powershell
	Powershell.exe -executionpolicy remotesigned -File  Robox-WindowsChooseAutomakerFolder.ps1
) ELSE (
	"%CELJAVA%" -jar robox-extensions-installer.jar
)


