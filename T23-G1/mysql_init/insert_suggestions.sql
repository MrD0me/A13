-- Inserimento suggerimenti per le classi disponibili

-- Suggerimenti per Calcolatrice
INSERT INTO suggestions (text, class_name, difficulty, language, created_at, updated_at) VALUES
('Testa la moltiplicazione con uno e con zero', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Assicurati che la divisione tra pari produca interi senza resto', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Verifica che la radice quadrata di numeri perfetti sia corretta', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Controlla la potenza con esponente 0 e 1', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Testa la gestione di spazi o input vuoti come numeri', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Verifica che il cambio di segno funzioni per positivi e negativi', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Testa l''inserimento di numeri con virgola e il loro parsing', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Controlla che la percentuale calcoli correttamente frazioni semplici', 'Calcolatrice', 'EASY', 'it', NOW(), NOW()),
('Testa le operazioni di moltiplicazione con numeri positivi e negativi', 'Calcolatrice', 'MEDIUM', 'it', NOW(), NOW()),
('Controlla il comportamento con numeri decimali molto piccoli', 'Calcolatrice', 'MEDIUM', 'it', NOW(), NOW()),
('Verifica la precisione dei calcoli con numeri molto grandi', 'Calcolatrice', 'HARD', 'it', NOW(), NOW()),
('Testa casi limite come overflow e underflow numerici', 'Calcolatrice', 'HARD', 'it', NOW(), NOW());

-- Suggerimenti per FTPFile
INSERT INTO suggestions (text, class_name, difficulty, language, created_at, updated_at) VALUES
('Inizia testando i metodi per impostare e ottenere il nome del file', 'FTPFile', 'EASY', 'it', NOW(), NOW()),
('Verifica i metodi per la dimensione del file: getSize(), setSize()', 'FTPFile', 'EASY', 'it', NOW(), NOW()),
('Testa i metodi relativi ai permessi: isFile(), isDirectory()', 'FTPFile', 'MEDIUM', 'it', NOW(), NOW()),
('Controlla la gestione delle date: timestamp, lastModified', 'FTPFile', 'MEDIUM', 'it', NOW(), NOW()),
('Verifica il parsing corretto di stringhe di permessi Unix', 'FTPFile', 'HARD', 'it', NOW(), NOW()),
('Testa casi complessi con link simbolici e permessi speciali', 'FTPFile', 'HARD', 'it', NOW(), NOW());

-- Suggerimenti per OutputFormat
INSERT INTO suggestions (text, class_name, difficulty, language, created_at, updated_at) VALUES
('Testa la formattazione di stringhe semplici senza parametri', 'OutputFormat', 'EASY', 'it', NOW(), NOW()),
('Verifica la gestione di valori null nel formato', 'OutputFormat', 'EASY', 'it', NOW(), NOW()),
('Controlla la formattazione di numeri con diverse precisioni', 'OutputFormat', 'MEDIUM', 'it', NOW(), NOW()),
('Testa la formattazione di date con diversi pattern', 'OutputFormat', 'MEDIUM', 'it', NOW(), NOW()),
('Verifica il comportamento con formati non validi o malformati', 'OutputFormat', 'HARD', 'it', NOW(), NOW()),
('Testa casi complessi con formattazione annidata e caratteri speciali', 'OutputFormat', 'HARD', 'it', NOW(), NOW());

-- Suggerimenti generici (senza classe specifica)
INSERT INTO suggestions (text, class_name, difficulty, language, created_at, updated_at) VALUES
('Ricorda di testare i casi limite e i valori di confine', NULL, 'EASY', 'it', NOW(), NOW()),
('Verifica il comportamento con input null quando possibile', NULL, 'EASY', 'it', NOW(), NOW()),
('Pensa a scenari di utilizzo reali della classe', NULL, 'MEDIUM', 'it', NOW(), NOW()),
('Controlla la coerenza tra metodi correlati', NULL, 'MEDIUM', 'it', NOW(), NOW()),
('Verifica che le eccezioni siano lanciate nei casi appropriati', NULL, 'HARD', 'it', NOW(), NOW()),
('Considera test per la concorrenza se la classe è thread-safe', NULL, 'HARD', 'it', NOW(), NOW());
