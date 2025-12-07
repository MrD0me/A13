@echo off
REM Utility per reinserire i suggerimenti nel database MySQL di T1

docker exec -it t1-mysql_db mysql -umanvsclass_user -pmanvsclass_pass manvsclass -e "DELETE FROM suggestions;"
docker cp "%~dp0mysql_init\insert_suggestions.sql" t1-mysql_db:/tmp/insert_suggestions.sql
docker exec -i t1-mysql_db sh -c "mysql -umanvsclass_user -pmanvsclass_pass manvsclass < /tmp/insert_suggestions.sql"
docker exec -it t1-mysql_db mysql -umanvsclass_user -pmanvsclass_pass manvsclass -e "SELECT COUNT(*) as total FROM suggestions;"
