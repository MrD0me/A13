@echo off
echo ========================================
echo Caricamento suggerimenti nel database
echo ========================================
echo.

echo [1/4] Eliminazione vecchi suggerimenti...
docker exec -it t23-mysql_db mysql -uuser -ppassword studentsrepo -e "DELETE FROM suggestions;"
if %errorlevel% neq 0 (
    echo ERRORE: Impossibile eliminare i vecchi suggerimenti
    pause
    exit /b 1
)
echo OK - Vecchi suggerimenti eliminati
echo.

echo [2/4] Copia del file SQL nel container...
docker cp "%~dp0mysql_init\insert_suggestions.sql" t23-mysql_db:/tmp/insert_suggestions.sql
if %errorlevel% neq 0 (
    echo ERRORE: Impossibile copiare il file nel container
    pause
    exit /b 1
)
echo OK - File copiato con successo
echo.

echo [3/4] Esecuzione dello script SQL...
docker exec -i t23-mysql_db sh -c "mysql -uuser -ppassword studentsrepo < /tmp/insert_suggestions.sql"
if %errorlevel% neq 0 (
    echo ERRORE: Impossibile eseguire lo script SQL
    pause
    exit /b 1
)
echo OK - Script eseguito con successo
echo.

echo [4/4] Verifica dei suggerimenti inseriti...
docker exec -it t23-mysql_db mysql -uuser -ppassword studentsrepo -e "SELECT COUNT(*) as total FROM suggestions;"
echo.

echo ========================================
echo Operazione completata con successo!
echo ========================================
pause
