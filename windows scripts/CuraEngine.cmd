@echo off
SET DIR=%~dp0
rem echo "%DIR%..\..\AutoMaker\java\bin\java.exe"
"%DIR%..\..\AutoMaker\java\bin\java.exe" -jar "%DIR%..\robox-slicer-flow-1.0-SNAPSHOT.jar" %*
rem pause