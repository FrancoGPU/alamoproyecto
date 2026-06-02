@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "APP_JAR=%~dp0target\asistencia-alamo-0.0.1-SNAPSHOT.jar"

if not exist "%JAVA_EXE%" (
  echo No se encontro Java 21 en "%JAVA_EXE%"
  exit /b 1
)

if not exist "%APP_JAR%" (
  echo No se encontro el JAR en "%APP_JAR%".
  echo Ejecuta primero: C:\tools\apache-maven-3.9.6\bin\mvn.cmd package -DskipTests
  exit /b 1
)

"%JAVA_EXE%" -jar "%APP_JAR%"
