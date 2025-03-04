@echo off
setlocal EnableDelayedExpansion

set JAVA_HOME=%JAVA_HOME%
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Java\jdk-21

echo Build process started

set ROOT_DIR=%CD%

rem Build commons
echo Building commons
cd /d "%ROOT_DIR%\commons"
mvn install
if %ERRORLEVEL% neq 0 (
    echo Error in commons build during mvn install. Check JAVA_HOME.
    exit /b 1
)
cd /d "%ROOT_DIR%"

rem Build T1-G11
echo Building T1-G11
cd /d "%ROOT_DIR%\T1-G11\applicazione\manvsclass"
mvn clean package -Dspring.profiles.active=prod
if %ERRORLEVEL% neq 0 (
    echo Error in T1-G11 build during mvn clean package
    exit /b 1
)
docker build -t mick0974/a13:t1-g11 .
cd /d "%ROOT_DIR%"

rem Build T23-G1
echo Building T23-G1
cd /d "%ROOT_DIR%\T23-G1"
mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Error in T23-G1 build during mvn clean package
    exit /b 1
)
docker build -t mick0974/a13:t23-g1 .
cd /d "%ROOT_DIR%"

rem Build T4-G18
echo Building T4-G18
cd /d "%ROOT_DIR%\T4-G18"
docker build -t mick0974/a13:t4-g18 .
cd /d "%ROOT_DIR%"

rem Build T5-G2
echo Building T5-G2
cd /d "%ROOT_DIR%\T5-G2\t5"
mvn clean package -DskipTests=true -Dspring.profiles.active=prod
if %ERRORLEVEL% neq 0 (
    echo Error in T5-G2 build during mvn clean package
    exit /b 1
)
docker build -t mick0974/a13:t5-g2 .
cd /d "%ROOT_DIR%"

rem Build T6-G12
echo Building T6-G12
cd /d "%ROOT_DIR%\T6-G12\T6"
mvn clean package -DskipTests=true
if %ERRORLEVEL% neq 0 (
    echo Error in T6-G12 build during mvn clean package
    exit /b 1
)
cd ..
docker build -t mick0974/a13:t6-g12 .
cd /d "%ROOT_DIR%"

rem Build T7-G31
echo Building T7-G31
cd /d "%ROOT_DIR%\T7-G31\RemoteCCC"
mvn clean package -DskipTests=true
if %ERRORLEVEL% neq 0 (
    echo Error in T7-G31 build during mvn clean package
    exit /b 1
)
docker build -t mick0974/a13:t7-g31 .
cd /d "%ROOT_DIR%"

rem Build T8-G21
echo Building T8-G21
cd /d "%ROOT_DIR%\T8-G21\Progetto_SAD_GRUPPO21_TASK8\Progetto_def\opt_livelli\Prototipo2.0\Serv"
mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Error in T8-G21 build during mvn clean package
    exit /b 1
)
cd ..
docker build -t mick0974/a13:t8-g21 .
cd /d "%ROOT_DIR%"

rem Build T9-G19
echo Building T9-G19
cd /d "%ROOT_DIR%\T9-G19\Progetto-SAD-G19-master"
mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Error in T9-G19 build during mvn clean package
    exit /b 1
)
docker build -t mick0974/a13:t9-g19 .
cd /d "%ROOT_DIR%"

rem Build ui_gateway
echo Building ui_gateway
cd /d "%ROOT_DIR%\ui_gateway"
docker build -t mick0974/a13:ui-gateway .
cd /d "%ROOT_DIR%"

rem Build api_gateway
echo Building api_gateway
cd /d "%ROOT_DIR%\api_gateway"
mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Error in api_gateway build during mvn clean package
    exit /b 1
)
docker build -t mick0974/a13:api-gateway .
cd /d "%ROOT_DIR%"

echo Build process completed
