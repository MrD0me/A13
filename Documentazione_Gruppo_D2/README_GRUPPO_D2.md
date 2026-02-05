## 0) Descrizione del Task svolto e Id del Task
Implementata la funzionalità “suggerimenti” durante le partite: il giocatore può ora richiedere suggerimenti base o avanzati divisi per classe e difficoltà. I suggerimenti avanzati richiedono crediti che vengono assegnati al termine delle partite vinte.

## 1) Implementazione del task
### Sintesi architetturale
- **T1**: aggiunti endpoint REST per richieste di suggerimenti base/avanzati, disponibilità, import e gestione admin; logica di selezione con persistenza dei suggerimenti consegnati per partita; validazioni su classe/difficoltà/tier e costo crediti.
- **T23**: gestione crediti suggerimenti lato profilo utente (recupero e consumo) per l’acquisto dei suggerimenti avanzati.
- **T5**: interfaccia editor aggiornata con pannelli per suggerimenti base e avanzati, contatori disponibili, storico, acquisto e consumo crediti; logica client (JS) per richiesta/availability verso T1 e per mostrare i suggerimenti; assegnazione crediti a fine partita in base alla difficoltà.
- **Gateway**: instradamento delle rotte `/suggerimenti/**` via API Gateway e UI Gateway verso T1.

### Principali modifiche per microservizio

| Microservizio modificato | Tipo di modifica | Nuove tecnologie utilizzate |
| --- | --- | --- |
| API Gateway | Aggiunte nuove regole di instradamento `/suggerimenti/**` verso T1 | - |
| UI Gateway | Proxy Nginx per rotte suggerimenti | - |
| T1 | Effettuata migrazione del DB in T1 da MongoDB a MySQL per semplificare l'implementazione del task. Aggiunti Controller e Service per la gestione dei suggerimenti base/avanzati, supportati da classi DTO e repository. Aggiunto inoltre un template per l'amministratore per aggiungere, modificare o rimuovere suggerimenti. | - |
| T23 | Implementata la gestione dei crediti per i suggerimenti (recupero, spesa, validazioni). | - |
| T5 | Modificata la UI con sezioni suggerimenti, logica JS + chiamate API per richieste/valutazione disponibilità e assegnazione crediti post-vittoria | - |

## 2) Errori/problematiche non risolte nel progetto consegnato
La funzionalità per permettere all'amministratore di eliminare un suggerimento dal database non funziona ancora correttamente.
