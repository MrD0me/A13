-- Popola la tabella dei suggerimenti al bootstrap del container MySQL di T1.
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

-- Suggerimenti per Calcolatrice
INSERT INTO suggestions (text, class_name, difficulty, language, tier, created_at, updated_at) VALUES
('Testa la moltiplicazione con uno e con zero', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Assicurati che la divisione tra pari produca interi senza resto', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Verifica che la radice quadrata di numeri perfetti sia corretta', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Controlla la potenza con esponente 0 e 1', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Testa la gestione di spazi o input vuoti come numeri', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Verifica che il cambio di segno funzioni per positivi e negativi', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Testa l''inserimento di numeri con virgola e il loro parsing', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Controlla che la percentuale calcoli correttamente frazioni semplici', 'Calcolatrice', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Testa le operazioni di moltiplicazione con numeri positivi e negativi', 'Calcolatrice', 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Controlla il comportamento con numeri decimali molto piccoli', 'Calcolatrice', 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Verifica la precisione dei calcoli con numeri molto grandi', 'Calcolatrice', 'HARD', 'it', 'BASE', NOW(), NOW()),
('Testa casi limite come overflow e underflow numerici', 'Calcolatrice', 'HARD', 'it', 'BASE', NOW(), NOW()),
('//SUGGESTIONS\n@Test\npublic void testDivision() {\n    Calcolatrice calc = new Calcolatrice();\n    int result = calc.divide(10, 2);\n    System.out.println("Result: " + result);\n}', 'Calcolatrice', 'EASY', 'EN', 'BASE', NOW(), NOW()),
('Valuta i casi di arrotondamento e precisione usando BigDecimal', 'Calcolatrice', 'HARD', 'it', 'ADVANCED', NOW(), NOW()),
('Imposta test parametrizzati con numeri random per scoprire edge-case inattesi', 'Calcolatrice', 'MEDIUM', 'it', 'ADVANCED', NOW(), NOW()),
('Costruisci property-based test per somma/sottrazione con numeri generati casualmente', 'Calcolatrice', 'EASY', 'it', 'ADVANCED', NOW(), NOW()),
('Verifica combinazioni edge (minimi/massimi) su tutte le operazioni base', 'Calcolatrice', 'EASY', 'it', 'ADVANCED', NOW(), NOW());

-- Suggerimenti per FTPFile
INSERT INTO suggestions (text, class_name, difficulty, language, tier, created_at, updated_at) VALUES
('Inizia testando i metodi per impostare e ottenere il nome del file', 'FTPFile', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Verifica i metodi per la dimensione del file: getSize(), setSize()', 'FTPFile', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Testa i metodi relativi ai permessi: isFile(), isDirectory()', 'FTPFile', 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Controlla la gestione delle date: timestamp, lastModified', 'FTPFile', 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Verifica il parsing corretto di stringhe di permessi Unix', 'FTPFile', 'HARD', 'it', 'BASE', NOW(), NOW()),
('Testa casi complessi con link simbolici e permessi speciali', 'FTPFile', 'HARD', 'it', 'BASE', NOW(), NOW()),
('Simula permessi inconsistenti su sistemi diversi (Windows vs Unix)', 'FTPFile', 'HARD', 'it', 'ADVANCED', NOW(), NOW()),
('Verifica la resilienza a path traversal e nomi file malformati in input', 'FTPFile', 'MEDIUM', 'it', 'ADVANCED', NOW(), NOW());

-- Suggerimenti per OutputFormat
INSERT INTO suggestions (text, class_name, difficulty, language, tier, created_at, updated_at) VALUES
('Testa la formattazione di stringhe semplici senza parametri', 'OutputFormat', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Verifica la gestione di valori null nel formato', 'OutputFormat', 'EASY', 'it', 'BASE', NOW(), NOW()),
('Controlla la formattazione di numeri con diverse precisioni', 'OutputFormat', 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Testa la formattazione di date con diversi pattern', 'OutputFormat', 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Verifica il comportamento con formati non validi o malformati', 'OutputFormat', 'HARD', 'it', 'BASE', NOW(), NOW()),
('Testa casi complessi con formattazione annidata e caratteri speciali', 'OutputFormat', 'HARD', 'it', 'BASE', NOW(), NOW()),
('Confronta l''output con locale diversi per valuta e data per evitare regressioni', 'OutputFormat', 'MEDIUM', 'it', 'ADVANCED', NOW(), NOW()),
('Genera input fuzz (stringhe lunghe, unicode) per verificare la robustezza della formattazione', 'OutputFormat', 'HARD', 'it', 'ADVANCED', NOW(), NOW());

-- Suggerimenti generici (senza classe specifica)
INSERT INTO suggestions (text, class_name, difficulty, language, tier, created_at, updated_at) VALUES
('Ricorda di testare i casi limite e i valori di confine', NULL, 'EASY', 'it', 'BASE', NOW(), NOW()),
('Verifica il comportamento con input null quando possibile', NULL, 'EASY', 'it', 'BASE', NOW(), NOW()),
('Pensa a scenari di utilizzo reali della classe', NULL, 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Controlla la coerenza tra metodi correlati', NULL, 'MEDIUM', 'it', 'BASE', NOW(), NOW()),
('Verifica che le eccezioni siano lanciate nei casi appropriati', NULL, 'HARD', 'it', 'BASE', NOW(), NOW()),
('Considera test per la concorrenza se la classe e thread-safe', NULL, 'HARD', 'it', 'BASE', NOW(), NOW()),
('Disegna test di proprieta (property-based) per validare invarianti di alto livello', NULL, 'MEDIUM', 'it', 'ADVANCED', NOW(), NOW()),
('Valuta performance e memoria con input estremi per individuare colli di bottiglia', NULL, 'HARD', 'it', 'ADVANCED', NOW(), NOW());
