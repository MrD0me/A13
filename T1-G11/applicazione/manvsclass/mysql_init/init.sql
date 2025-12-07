-- Keep values in sync with .env (MYSQL_USER / MYSQL_PASSWORD / MYSQL_DATABASE)
CREATE DATABASE IF NOT EXISTS manvsclass;
CREATE USER IF NOT EXISTS 'manvsclass_user'@'%' IDENTIFIED BY 'manvsclass_pass';
ALTER USER 'manvsclass_user'@'%' IDENTIFIED BY 'manvsclass_pass';
GRANT ALL PRIVILEGES ON manvsclass.* TO 'manvsclass_user'@'%';
FLUSH PRIVILEGES;
