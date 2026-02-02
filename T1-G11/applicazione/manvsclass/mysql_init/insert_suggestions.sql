-- Creazione tabella dei suggerimenti nel db MySQL
USE manvsclass;

CREATE TABLE IF NOT EXISTS suggestions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(1024) NOT NULL,
    class_name VARCHAR(255),
    difficulty VARCHAR(16) NOT NULL,
    language VARCHAR(8),
    tier VARCHAR(16) NOT NULL DEFAULT 'BASE',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB;
