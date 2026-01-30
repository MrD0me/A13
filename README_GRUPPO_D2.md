## 0) Descrizione del Task svolto e Id del Task
Implementata la funzionalità “suggerimenti” durante le partite: il giocatore può ora richiedere suggerimenti base o avanzati divisi per classe e difficoltà. I suggerimenti avanzati richiedono crediti che vengono assegnati al termine delle partite vinte.

## 1) Implementazione del task
### Sintesi architetturale
- **T1-G11**: aggiunti endpoint REST per richieste di suggerimenti base/avanzati, disponibilità, import e gestione admin; logica di selezione con persistenza dei suggerimenti consegnati per partita; validazioni su classe/difficoltà/tier e costo crediti.
- **T23-G1**: gestione crediti suggerimenti lato profilo utente (recupero e consumo) per l’acquisto dei suggerimenti avanzati.
- **T5-G2**: interfaccia editor aggiornata con pannelli per suggerimenti base e avanzati, contatori disponibili, storico, acquisto e consumo crediti; logica client (JS) per richiesta/availability verso T1 e per mostrare i suggerimenti; assegnazione crediti a fine partita in base alla difficoltà.
- **Gateway**: instradamento delle rotte `/suggerimenti/**` via apiGateway e ui_gateway verso T1.

### Principali modifiche per microservizio

| Microservizio | Tipo di modifica | Nuove tecnologie |
| --- | --- | --- |
| apiGateway | Nuove regole di instradamento `/suggerimenti/**` verso T1 | - |
| ui_gateway | Proxy Nginx per rotte suggerimenti | - |
| T1-G11 | Controller e service per suggerimenti base/avanzati, DTO, repository, sicurezza (bypass token remoto per ridurre latenza), template admin e static JS per CRUD, script SQL di seed | - |
| T23-G1 | Gestione crediti suggerimenti (recupero, spesa, validazioni) nel servizio PlayerProgress | - |
| T5-G2 | UI editor con sezioni suggerimenti, logica JS per richieste/availability/advanced purchase, assegnazione crediti post-vittoria | - |

## 3) Errori/problematiche non risolte nel progetto consegnato
DA COMPLETARE