/*
 *   Copyright (c) 2024 Stefano Marano https://github.com/StefanoMarano80017
 *   All rights reserved.

 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


/*
* 	Qui c'Ã¨ tutta la logica dei bottoni e della GUI
*/

//TASTO CERCA
var searchButton = document.getElementById("searchButton");
searchButton.addEventListener("click", function () {
    var searchTerm = document.getElementById("searchTerm").value.trim();
    var resultCount = 0; // Variabile per contare le occorrenze
    var resultSpan = document.getElementById("SearchItems"); // Span dove mostrare il risultato

    if (searchTerm) {
        clearSearchHighlights(); // Rimuovi eventuali evidenziazioni precedenti
        var cursor = editor_utente.getSearchCursor(searchTerm);
        // Evidenzia tutte le occorrenze del termine cercato
        while (cursor.findNext()) {
            editor_utente.markText(cursor.from(), cursor.to(), {
                className: "highlight",
            });
            resultCount++;
        }
        // Aggiorna lo span con il numero di occorrenze trovate
        resultSpan.textContent = resultCount;
        resultSpan.style.display = "inline"; // Mostra lo span
    } else {
        // Nessun termine inserito, mostra un messaggio di errore e rendi visibile lo span
        resultSpan.style.display = "inline"; // Mostra lo span
    }
});

// Funzione per sostituire il termine cercato con il nuovo termine
var startReplaceButton = document.getElementById("startReplace");
startReplaceButton.addEventListener("click", function () {
    var searchTerm = document.getElementById("searchTerm").value.trim();
    var replaceTerm = document.getElementById("replaceTerm").value.trim();

    if (searchTerm && replaceTerm) {
        var cursor = editor_utente.getSearchCursor(searchTerm);
        // Sostituisci ogni occorrenza del termine cercato con il nuovo testo
        while (cursor.findNext()) {
            editor_utente.replaceRange(replaceTerm, cursor.from(), cursor.to());
        }
        clearSearchHighlights(); // Rimuovi le evidenziazioni dopo la sostituzione
    }
});

// Funzione per rimuovere tutte le evidenziazioni precedenti
function clearSearchHighlights() {
    var marks = editor_utente.getAllMarks();
    marks.forEach(function (mark) {
        mark.clear();
    });
    var resultSpan = document.getElementById("SearchItems");
    resultSpan.style.display = "none"; // Nasconde lo span
}
// Aggiungi evento input per rimuovere le vecchie evidenziazioni quando si inserisce un nuovo termine di ricerca
document.getElementById("searchTerm").addEventListener("input", clearSearchHighlights);


// INPUT FILE
var fileInput = document.getElementById("fileInput");
// Aggiungi un event listener per l'input file
fileInput.addEventListener("change", function (event) {
    var file = event.target.files[0]; // Prendi il primo file selezionato
    if (file) {
        var reader = new FileReader();
        // Leggi il contenuto del file
        reader.onload = function (e) {
            var fileContent = e.target.result; // Contenuto del file
            editor_utente.setValue(fileContent); // Inserisci il contenuto nel CodeMirror
        };
        reader.readAsText(file); // Leggi il file come testo
    }
});

// DOWNLOAD FILE
var DownloadButton = document.getElementById("DownloadButton");
// Aggiungi un event listener per il pulsante di salvataggio
DownloadButton.addEventListener("click", function () {
    var fileContent = editor_utente.getValue(); // Ottieni il contenuto dall'editor
    var blob = new Blob([fileContent], { type: "text/plain" }); // Crea un oggetto Blob con il contenuto
    // Crea un link temporaneo per il download del file
    var link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "codice.java"; // Nome del file salvato
    // Simula un clic sul link per avviare il download
    link.click();
    // Rilascia l'oggetto URL per evitare perdite di memoria
    URL.revokeObjectURL(link.href);
});

//REDO E UNDO
document.querySelector("#undo-button").addEventListener("click", function () {
    editor_utente.undo();
});
document.querySelector("#redo-button").addEventListener("click", function () {
    editor_utente.redo();
});

//AGGIUNTA A STORICO
function viewStorico() {
    var list = document.getElementById("storico_list"); // Seleziona la lista
    list.innerHTML = ""; // Pulisce la lista esistente

    // Recupera lo storico dal localStorage
    const storico = leggiStorico(); // Usa la funzione leggiStorico() per ottenere i dati

    // Verifica se ci sono elementi nello storico
    if (storico.length === 0) {
        return;
    }

    // Itera attraverso l'array storico e crea gli <li>
    storico.forEach(item => {
        var newItem = document.createElement("li");
        newItem.className = "list-group-item d-flex justify-content-between align-items-start";
        // Imposta il contenuto HTML del nuovo elemento
        newItem.innerHTML = `
            <div class="ms-2 me-auto">
                <div class="fw-bold">Punteggio</div>
                <small>%COV: ${item.covValue}</small>
            </div>
            <span class="badge text-bg-primary rounded-pill">${item.punteggio}</span>
        `;

        // Aggiunge il nuovo elemento alla lista
        list.appendChild(newItem);
    });
}

// Funzione per scrivere nel localStorage
function addStorico(turnoID, punteggio, covValue) {
    // Ottieni l'attuale storico dal localStorage
    const storicoJSON = localStorage.getItem("storico");

    // Converti la stringa JSON in un array, o crea un nuovo array se non esiste
    const storico = storicoJSON ? JSON.parse(storicoJSON) : [];

    // Crea un nuovo oggetto da aggiungere
    const nuovoElemento = {
        turnoID: turnoID,
        punteggio: punteggio,
        covValue: covValue * 100
    };

    // Aggiungi il nuovo elemento all'array
    storico.push(nuovoElemento);

    // Salva l'array aggiornato nel localStorage
    localStorage.setItem("storico", JSON.stringify(storico));
}

// Funzione per leggere dal localStorage
function leggiStorico() {
    // Ottieni l'attuale storico dal localStorage
    const storicoJSON = localStorage.getItem("storico");
    // Se esiste, convertilo in un array di oggetti, altrimenti restituisci un array vuoto
    return storicoJSON ? JSON.parse(storicoJSON) : [];
}

function updateStorico(turnoID, punteggio, covValue){
    addStorico(turnoID, punteggio, covValue);
    viewStorico();
}


//TASTO INFO
document.addEventListener("DOMContentLoaded", function () {
    // Seleziona il bottone popover
    var popoverButton = document.getElementById("popover_info");
    var messaggio =
        ClasseUT  + 	localStorage.getItem("underTestClassName") + "<br>" +
        Difficolta + 	localStorage.getItem("difficulty") + "<br>" +
        "Robot: " 	   +	localStorage.getItem("robot");

    var popover = new bootstrap.Popover(popoverButton, {
        content: messaggio,  	  // Usa il contenuto dal localStorage
        trigger: 'click',         // Mostra il popover al passaggio del mouse (puoi cambiare con 'click' o 'focus')
        html: true                // Se vuoi abilitare contenuti HTML nel popover
    });
});

//Tasto font size
document.addEventListener("DOMContentLoaded", function() {
    const incrementButton = document.getElementById("increment-button");
    const decrementButton = document.getElementById("decrement-button");
    const fontSizeInput = document.getElementById("font-size-input");

    // Recupera gli editor CodeMirror esistenti
    const editors = [
        editor_utente,
        editor_robot,
        console_utente,
        console_robot
    ];

    // Imposta la dimensione del font iniziale
    let fontSize = parseInt(fontSizeInput.value, 10) || 16; // Valore predefinito se non Ã¨ impostato

    // Funzione per aggiornare la dimensione del font per tutti gli editor
    function aggiornaFontSize() {
        editors.forEach(editor => {
            const wrapper = editor.getWrapperElement();
            wrapper.style.fontSize = fontSize + "px"; // Imposta la dimensione del font
            wrapper.style.lineHeight = (fontSize * 1.5) + "px"; // Imposta l'altezza della linea (1.5 Ã¨ un esempio di fattore)
            editor.refresh(); // Ricarica l'editor per applicare le modifiche
        });
    }

    // Gestione click per il bottone di incremento
    incrementButton.addEventListener("click", function() {
        fontSize++;
        fontSizeInput.value = fontSize; // Aggiorna il campo input
        aggiornaFontSize(); // Applica la nuova dimensione del font
    });

    // Gestione click per il bottone di decremento
    decrementButton.addEventListener("click", function() {
        if (fontSize > 1) { // Impedisce di scendere sotto 1px
            fontSize--;
            fontSizeInput.value = fontSize; // Aggiorna il campo input
            aggiornaFontSize(); // Applica la nuova dimensione del font
        }
    });

    // Aggiungi un listener all'input per il cambiamento del valore
    document.getElementById('font-size-input').addEventListener('input', function() {
        const newFontSize = parseInt(this.value);
        if (!isNaN(newFontSize) && newFontSize > 0) { // Assicurati che il valore sia un numero valido
            fontSize = newFontSize;
            aggiornaFontSize();
        }
    });

    // Imposta la dimensione del font iniziale al caricamento della pagina
    aggiornaFontSize();
});

// Funzione per salvare il contenuto dell'editor nel localStorage
/*
function saveToLocalStorage() {
	const content = editor_utente.getValue();
	localStorage.setItem('codeMirrorContent', content);
}
// Aggiungi un listener per l'evento change
editor_utente.on('change', saveToLocalStorage);

 */

// BOTTONE SUGGERIMENTI
const ADVANCED_SUGGESTION_COST = 2;
document.addEventListener("DOMContentLoaded", function () {
    var newButton = document.getElementById("suggerimento");
    newButton.addEventListener("click", function () {
        // Richiedi un suggerimento a T1
        richiediSuggerimento();
    });
    var historyButton = document.getElementById("showSuggestionHistory");
    if (historyButton) {
        historyButton.addEventListener("click", function () {
            mostraStoricoSuggerimenti();
        });
    }
});

// Inizializza il contatore dei suggerimenti e mostra nell'interfaccia
document.addEventListener("DOMContentLoaded", function() {
    initSuggestionCounters();
    renderSuggestionHistory();
});

document.addEventListener("DOMContentLoaded", function () {
    setupAdvancedSuggestions();
});

function suggestionsMaxForDifficulty(difficulty){
    if(!difficulty) return 0;
    switch((difficulty + "").toUpperCase()){
        case 'EASY': return 10;
        case 'MEDIUM': return 5;
        case 'HARD': return 2;
        default: return 0;
    }
}

function initSuggestionCounters(){
    var difficulty = localStorage.getItem("difficulty") || "EASY";
    var className = localStorage.getItem("underTestClassName") || "";
    var max = parseInt(localStorage.getItem("suggestionsMax"), 10);

    //Fallback front-end (utilizzando suggestionsMaxForDifficulty) nel caso di errore lato backend.
    // All'avvio usiamo il limite per difficoltà, ma appena possibile lo sostituiamo col cap reale da backend.
    if(isNaN(max) || max <= 0){
        max = suggestionsMaxForDifficulty(difficulty);
        localStorage.setItem("suggestionsMax", max);
    }

    var available = parseInt(localStorage.getItem("suggestionsAvailable"), 10);
    if(isNaN(available) || available < 0 || available > max){
        available = max;
        localStorage.setItem("suggestionsAvailable", available);
    }

    // Se abbiamo la classe, chiediamo subito al backend il cap reale senza consumare suggerimenti.
    if(className){
        fetch("/api/suggerimenti/disponibilita", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                className: className,
                difficulty: difficulty
            })
        })
        .then(resp => {
            if(!resp.ok) throw new Error("Errore disponibilita suggerimenti (" + resp.status + ")");
            return resp.json();
        })
        .then(data => {
            var maxFromServer = parseInt(data.suggestionsMax || data.totalAvailableSuggestions, 10);
            var availableFromServer = parseInt(data.availableSuggestions || data.totalAvailableSuggestions, 10);
            if(maxFromServer && maxFromServer > 0){
                localStorage.setItem("suggestionsMax", maxFromServer);
            }
            if(!isNaN(availableFromServer) && availableFromServer >= 0){
                localStorage.setItem("suggestionsAvailable", availableFromServer);
            }
            updateSuggestionCounter();
        })
        .catch(err => {
            console.warn("Impossibile recuperare disponibilita suggerimenti, uso fallback locale:", err);
            try {
                document.getElementById("suggerimento").disabled = true;
            } catch (e) { /* ignore */ }
            updateSuggestionCounter();
        });
    } else {
        updateSuggestionCounter();
    }
}

function updateSuggestionCounter(){
    var counter = document.getElementById("suggestion-counter");
    if(!counter) return;
    var available = parseInt(localStorage.getItem("suggestionsAvailable"), 10) || 0;
    var max = parseInt(localStorage.getItem("suggestionsMax"), 10) || 0;
    counter.textContent = available + "/" + max;
    // Cambia colore se esauriti
    if(available <= 0) {
        counter.classList.remove('bg-light');
        counter.classList.add('bg-danger', 'text-white');
    } else {
        counter.classList.remove('bg-danger', 'text-white');
        counter.classList.add('bg-light', 'text-dark');
    }
}

function getSuggestionHistory() {
    try {
        var stored = localStorage.getItem("suggestionHistory");
        return stored ? JSON.parse(stored) : [];
    } catch (e) {
        console.warn("Impossibile leggere la cronologia suggerimenti:", e);
        return [];
    }
}

function saveSuggestionHistory(history) {
    localStorage.setItem("suggestionHistory", JSON.stringify(history));
}

function addSuggestionsToHistory(suggestions) {
    if (!suggestions || suggestions.length === 0) return;
    var history = getSuggestionHistory();
    suggestions.forEach(function (suggerimento) {
        history.push(suggerimento);
    });
    saveSuggestionHistory(history);
    renderSuggestionHistory();
}

function renderSuggestionHistory() {
    var list = document.getElementById("suggestion-history-list");
    if (!list) return;
    var history = getSuggestionHistory();
    list.innerHTML = "";

    if (history.length === 0) {
        var emptyItem = document.createElement("li");
        emptyItem.className = "list-group-item";
        emptyItem.textContent = "Nessun suggerimento ricevuto.";
        list.appendChild(emptyItem);
        return;
    }

    history.forEach(function (text, index) {
        var item = document.createElement("li");
        item.className = "list-group-item d-flex justify-content-between align-items-start";

        var content = document.createElement("div");
        content.className = "ms-2 me-auto";

        var title = document.createElement("div");
        title.className = "fw-bold";
        title.textContent = "Suggerimento " + (index + 1);

        var body = document.createElement("small");
        body.textContent = text;

        content.appendChild(title);
        content.appendChild(body);
        item.appendChild(content);
        list.appendChild(item);
    });
}

function setupAdvancedSuggestions() {
    var costBadge = document.getElementById("advanced-suggestion-cost");
    if (costBadge) {
        costBadge.textContent = ADVANCED_SUGGESTION_COST;
    }
    var storedCredits = parseInt(localStorage.getItem("hintCredits"), 10);
    if (!isNaN(storedCredits)) {
        updateAdvancedCreditsBadge(storedCredits);
    }
    var advButton = document.getElementById("advancedSuggestionBtn");
    if (advButton) {
        advButton.addEventListener("click", function () {
            richiediSuggerimentoAvanzato();
        });
    }
    var advancedTab = document.getElementById("advanced-tab");
    if (advancedTab) {
        advancedTab.addEventListener("shown.bs.tab", function () {
            refreshCreditsFromServer();
            initAdvancedSuggestionCounters();
            renderAdvancedSuggestionHistory();
        });
    }
    refreshCreditsFromServer();
    initAdvancedSuggestionCounters();
    renderAdvancedSuggestionHistory();
}

function refreshCreditsFromServer() {
    try {
        var playerId = jwtData?.userId;
        if (!playerId) {
            updateAdvancedCreditsBadge(0);
            updateAdvancedControls();
            return Promise.resolve(0);
        }
        return fetch(`/api/userService/players/${playerId}/progression/credits`)
            .then(resp => {
                if (!resp.ok) throw new Error("Errore recupero crediti (" + resp.status + ")");
                return resp.json();
            })
            .then(data => {
                var credits = parseInt((data && (data.credits ?? data.Credits)), 10);
                if (isNaN(credits)) {
                    credits = 0;
                }
                localStorage.setItem("hintCredits", credits);
                updateAdvancedCreditsBadge(credits);
                updateAdvancedControls();
                return credits;
            })
            .catch(err => {
                console.warn("Impossibile aggiornare i crediti dal server:", err);
                updateAdvancedControls();
                return 0;
            });
    } catch (e) {
        console.warn("refreshCreditsFromServer error", e);
        updateAdvancedControls();
        return Promise.resolve(0);
    }
}

function updateAdvancedCreditsBadge(value) {
    var badge = document.getElementById("advanced-credits-badge");
    if (!badge) return;
    badge.textContent = value;
    if (value <= 0) {
        badge.classList.remove("bg-success");
        badge.classList.add("bg-danger");
    } else {
        badge.classList.remove("bg-danger");
        badge.classList.add("bg-success");
    }
}

function initAdvancedSuggestionCounters() {
    var difficulty = localStorage.getItem("difficulty") || "EASY";
    var className = localStorage.getItem("underTestClassName") || "";
    var max = parseInt(localStorage.getItem("advancedSuggestionsMax"), 10);
    var available = parseInt(localStorage.getItem("advancedSuggestionsAvailable"), 10);
    var history = [];
    try { history = (typeof getAdvancedSuggestionHistory === "function") ? getAdvancedSuggestionHistory() : []; } catch (e) { history = []; }
    var historyEmpty = !history || history.length === 0;
    var existingAvailable = (historyEmpty || isNaN(available)) ? null : available;

    if (isNaN(max) || max < 0) {
        max = 0;
        localStorage.setItem("advancedSuggestionsMax", max);
    }
    if (isNaN(available) || available < 0 || available > max) {
        available = max;
        localStorage.setItem("advancedSuggestionsAvailable", available);
    }

    if (className) {
        fetch("/api/suggerimenti/disponibilita", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                className: className,
                difficulty: difficulty,
                tier: "ADVANCED"
            })
        })
            .then(resp => {
                if (!resp.ok) throw new Error("Errore disponibilita suggerimenti avanzati (" + resp.status + ")");
                return resp.json();
            })
            .then(data => {
                var maxFromServer = parseInt(data.suggestionsMax || data.totalAvailableSuggestions, 10);
                var availableFromServer = parseInt(data.availableSuggestions || data.totalAvailableSuggestions, 10);
                if (!isNaN(maxFromServer)) {
                    max = maxFromServer;
                    localStorage.setItem("advancedSuggestionsMax", max);
                }
                if (!isNaN(availableFromServer)) {
                    if (existingAvailable === null) {
                        available = availableFromServer;
                    } else {
                        available = Math.min(existingAvailable, availableFromServer);
                    }
                    available = Math.min(available, max);
                    localStorage.setItem("advancedSuggestionsAvailable", available);
                }
                updateAdvancedSuggestionCounter();
            })
            .catch(err => {
                console.warn("Impossibile recuperare disponibilita suggerimenti avanzati, uso fallback locale:", err);
                updateAdvancedSuggestionCounter();
            });
    } else {
        updateAdvancedSuggestionCounter();
    }
}

function updateAdvancedSuggestionCounter() {
    var counter = document.getElementById("advanced-suggestion-counter");
    if (!counter) return;
    var available = parseInt(localStorage.getItem("advancedSuggestionsAvailable"), 10) || 0;
    var max = parseInt(localStorage.getItem("advancedSuggestionsMax"), 10) || 0;
    counter.textContent = available + "/" + max;
    if (available <= 0) {
        counter.classList.remove('bg-light');
        counter.classList.add('bg-danger', 'text-white');
    } else {
        counter.classList.remove('bg-danger', 'text-white');
        counter.classList.add('bg-light', 'text-dark');
    }
    updateAdvancedControls();
}

function updateAdvancedControls() {
    var button = document.getElementById("advancedSuggestionBtn");
    if (!button) return;
    var credits = parseInt(localStorage.getItem("hintCredits"), 10) || 0;
    var available = parseInt(localStorage.getItem("advancedSuggestionsAvailable"), 10) || 0;
    button.disabled = credits < ADVANCED_SUGGESTION_COST || available <= 0;
}

function getAdvancedSuggestionHistory() {
    try {
        var stored = localStorage.getItem("advancedSuggestionHistory");
        return stored ? JSON.parse(stored) : [];
    } catch (e) {
        console.warn("Impossibile leggere la cronologia suggerimenti avanzati:", e);
        return [];
    }
}

function saveAdvancedSuggestionHistory(history) {
    localStorage.setItem("advancedSuggestionHistory", JSON.stringify(history));
}

function addAdvancedSuggestionsToHistory(suggestions) {
    if (!suggestions || suggestions.length === 0) return;
    var history = getAdvancedSuggestionHistory();
    suggestions.forEach(function (suggerimento) {
        history.push(suggerimento);
    });
    saveAdvancedSuggestionHistory(history);
    renderAdvancedSuggestionHistory();
}

function renderAdvancedSuggestionHistory() {
    var list = document.getElementById("advanced-suggestion-history-list");
    if (!list) return;
    var history = getAdvancedSuggestionHistory();
    list.innerHTML = "";

    if (history.length === 0) {
        var emptyItem = document.createElement("li");
        emptyItem.className = "list-group-item";
        emptyItem.textContent = "Nessun suggerimento avanzato ricevuto.";
        list.appendChild(emptyItem);
        return;
    }

    history.forEach(function (text, index) {
        var item = document.createElement("li");
        item.className = "list-group-item d-flex justify-content-between align-items-start";

        var content = document.createElement("div");
        content.className = "ms-2 me-auto";

        var title = document.createElement("div");
        title.className = "fw-bold";
        title.textContent = "Suggerimento avanzato " + (index + 1);

        var body = document.createElement("small");
        body.textContent = text;

        content.appendChild(title);
        content.appendChild(body);
        item.appendChild(content);
        list.appendChild(item);
    });
}

function mostraStoricoSuggerimenti() {
    var history = getSuggestionHistory();
    var modal = document.createElement("div");
    modal.className = "modal fade";
    modal.id = "storicoSuggerimentiModal";
    modal.setAttribute("tabindex", "-1");
    modal.setAttribute("aria-labelledby", "storicoSuggerimentiLabel");
    modal.setAttribute("aria-hidden", "true");

    var bodyContent = "";
    if (history.length === 0) {
        bodyContent = "<p class='mb-0'>Nessun suggerimento ricevuto.</p>";
    } else {
        bodyContent = "<ul class='list-group'>";
        history.forEach(function (suggerimento, index) {
            bodyContent += "<li class='list-group-item'><strong>#"+ (index+1) +":</strong> " + suggerimento + "</li>";
        });
        bodyContent += "</ul>";
    }

    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="storicoSuggerimentiLabel">Cronologia suggerimenti</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    ${bodyContent}
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    var bsModal = new bootstrap.Modal(modal);
    bsModal.show();
    modal.addEventListener("hidden.bs.modal", function () {
        modal.remove();
    });
}

function mostraAlertSuggerimenti(message) {
    var modal = document.createElement("div");
    modal.className = "modal fade";
    modal.id = "alertSuggerimentiModal";
    modal.setAttribute("tabindex", "-1");
    modal.setAttribute("aria-labelledby", "alertSuggerimentiLabel");
    modal.setAttribute("aria-hidden", "true");

    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="alertSuggerimentiLabel">Suggerimenti</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="alert alert-warning" role="alert">
                        <strong>Attenzione!</strong> ${message}
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    var bsModal = new bootstrap.Modal(modal);
    bsModal.show();
    modal.addEventListener("hidden.bs.modal", function () {
        modal.remove();
    });
}

function richiediSuggerimento() {
    // Recupera i dati dalla sessione/localStorage
    var difficulty = localStorage.getItem("difficulty") || "EASY";
    var remainingSuggestions = parseInt(localStorage.getItem("suggestionsAvailable")) || 0;
    var gameId = localStorage.getItem("roundId") || 0;
    var className = localStorage.getItem("underTestClassName") || "";

    if (remainingSuggestions <= 0) {
        mostraAlertSuggerimenti("Non sono piu disponibili suggerimenti per questa partita.");
        return;
    }

    if(!className){
        alert("Impossibile determinare la classe in gioco. Ricarica la pagina o avvia nuovamente la partita.");
        return;
    }

    // Prepara il payload per la richiesta
    var requestBody = {
        gameId: gameId,
        difficulty: difficulty,
        remainingSuggestions: remainingSuggestions,
        className: className
    };

    // Effettua la richiesta ai servizi T23 passando per l'API Gateway
    fetch("/api/suggerimenti/richiedi", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Risposta non valida dal server (" + response.status + ")");
            }
            return response.json();
        })
        .then(data => {
            // Aggiorna il numero di suggerimenti nel localStorage usando i valori reali restituiti dal backend.
            var availableRaw = (typeof data.suggestionsAvailable === "number") ? data.suggestionsAvailable : data.remainingSuggestions;
            var available = parseInt(availableRaw, 10);
            var maxFromServer = parseInt(data.suggestionsMax || data.totalAvailableSuggestions, 10);
            localStorage.setItem("suggestionsAvailable", isNaN(available) ? 0 : available);
            if(maxFromServer && maxFromServer > 0){
                localStorage.setItem("suggestionsMax", maxFromServer);
            } else {
                // Fallback per compatibilità se il backend non fornisse il cap.
                var fallbackMax = suggestionsMaxForDifficulty(difficulty);
                localStorage.setItem("suggestionsMax", fallbackMax);
            }

            updateSuggestionCounter();
            addSuggestionsToHistory(data.suggestions);

            // Mostra i suggerimenti in una modale
            mostraSuggerimenti(data);
        })
        .catch(error => {
            console.error("Errore nella richiesta dei suggerimenti:", error);
            mostraAlertSuggerimenti("Non sono disponibili suggerimenti");
        });
}

function richiediSuggerimentoAvanzato() {
    var difficulty = localStorage.getItem("difficulty") || "EASY";
    var remainingSuggestions = parseInt(localStorage.getItem("advancedSuggestionsAvailable")) || 0;
    var gameId = localStorage.getItem("roundId") || 0;
    var className = localStorage.getItem("underTestClassName") || "";
    var playerId = jwtData?.userId;
    var credits = parseInt(localStorage.getItem("hintCredits"), 10) || 0;

    if (!playerId) {
        mostraAlertSuggerimenti("Impossibile identificare il giocatore. Effettua nuovamente l'accesso.");
        return;
    }

    if (remainingSuggestions <= 0) {
        mostraAlertSuggerimenti("Non sono disponibili suggerimenti avanzati per questa partita.");
        updateAdvancedSuggestionCounter();
        return;
    }

    if (!className) {
        alert("Impossibile determinare la classe in gioco. Ricarica la pagina o avvia nuovamente la partita.");
        return;
    }

    if (credits < ADVANCED_SUGGESTION_COST) {
        refreshCreditsFromServer();
        mostraAlertSuggerimenti("Crediti insufficienti per acquistare un suggerimento avanzato.");
        return;
    }

    var requestBody = {
        gameId: gameId,
        difficulty: difficulty,
        remainingSuggestions: remainingSuggestions,
        className: className,
        playerId: playerId,
        cost: ADVANCED_SUGGESTION_COST
    };

    fetch("/api/suggerimenti/avanzati/richiedi", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    })
        .then(response => {
            if (response.status === 402) {
                return response.json().then(err => {
                    throw new Error(err.message || "Crediti insufficienti per i suggerimenti avanzati");
                });
            }
            if (!response.ok) {
                throw new Error("Risposta non valida dal server (" + response.status + ")");
            }
            return response.json();
        })
        .then(data => {
            var availableRaw = (typeof data.suggestionsAvailable === "number") ? data.suggestionsAvailable : data.remainingSuggestions;
            var available = parseInt(availableRaw, 10);
            var maxFromServer = parseInt(data.suggestionsMax || data.totalAvailableSuggestions, 10);
            if (!isNaN(available)) {
                localStorage.setItem("advancedSuggestionsAvailable", available);
            }
            if (!isNaN(maxFromServer)) {
                localStorage.setItem("advancedSuggestionsMax", maxFromServer);
            }
            if (typeof data.creditsLeft === "number") {
                localStorage.setItem("hintCredits", data.creditsLeft);
                updateAdvancedCreditsBadge(data.creditsLeft);
            }

            updateAdvancedSuggestionCounter();
            addAdvancedSuggestionsToHistory(data.suggestions);

            var subtitle = "";
            if (typeof data.creditsLeft === "number") {
                subtitle = `Crediti rimasti: <span class="badge bg-secondary">${data.creditsLeft}</span>`;
            }

            mostraSuggerimenti(data, {
                title: "Suggerimenti avanzati",
                subtitleHtml: subtitle,
                noMoreMessage: "Non sono piu disponibili suggerimenti avanzati per questa partita."
            });
        })
        .catch(error => {
            console.error("Errore nella richiesta dei suggerimenti avanzati:", error);
            mostraAlertSuggerimenti(error.message || "Impossibile ottenere suggerimenti avanzati");
        });
}

function mostraSuggerimenti(data, options) {
    options = options || {};
    var titleLabel = options.title || "Suggerimenti";
    var subtitleHtml = options.subtitleHtml || "";
    var suggestions = data.suggestions || [];
    var noMoreMessage = (options.noMoreMessage || data.message || "Non sono piu disponibili suggerimenti per questa partita.");
    // Usa i valori forniti dal backend per mostrare un contatore coerente (es. 3/3 anziche 10/10).
    var remaining = (typeof data.suggestionsAvailable === "number") ? data.suggestionsAvailable : data.remainingSuggestions;
    var max = data.suggestionsMax || data.totalAvailableSuggestions || localStorage.getItem('suggestionsMax') || 0;

    // Se non ci sono suggerimenti, mostra un messaggio di errore
    if (suggestions.length === 0) {
        var modal = document.createElement("div");
        modal.className = "modal fade";
        modal.id = "suggerimentiModal";
        modal.setAttribute("tabindex", "-1");
        modal.setAttribute("aria-labelledby", "suggerimentiLabel");
        modal.setAttribute("aria-hidden", "true");

        modal.innerHTML = `
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5" id="suggerimentiLabel">${titleLabel}</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    
                    <div class="modal-body">
                        <div class="alert alert-warning" role="alert">
                            ${noMoreMessage}
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(modal);
        var bsModal = new bootstrap.Modal(modal);
        bsModal.show();

        modal.addEventListener("hidden.bs.modal", function () {
            modal.remove();
        });
        return;
    }

    // Crea una finestra modale con i suggerimenti
    var modal = document.createElement("div");
    modal.className = "modal fade";
    modal.id = "suggerimentiModal";
    modal.setAttribute("tabindex", "-1");
    modal.setAttribute("aria-labelledby", "suggerimentiLabel");
    modal.setAttribute("aria-hidden", "true");

    // Crea il contenuto HTML con i suggerimenti
    var htmlContent = "<ul class='list-group' id='suggerimentiList'>";
    suggestions.forEach(function(suggerimento) {
        var encoded = encodeURIComponent(suggerimento);
        htmlContent += `<li class='list-group-item d-flex justify-content-between align-items-center'>
                    <span>${suggerimento}</span>
                    <button class='btn btn-sm btn-primary insertaSuggerimentoBtn' data-suggerimento='${encoded}'>
                        Inserisci
                    </button>
                </li>`;
    });
    htmlContent += "</ul>";
            
    // Aggiungi event listener ai bottoni dopo che sono nel DOM
    setTimeout(function() {
        var buttons = document.querySelectorAll('.insertaSuggerimentoBtn');
        buttons.forEach(function(button) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                var testo = this.getAttribute('data-suggerimento');
                try {
                    testo = decodeURIComponent(testo);
                } catch (err) {
                    console.warn('Impossibile decodificare il suggerimento:', err);
                }
                inserisciSuggerimentoNelCodice(testo);
                var modalEl = document.getElementById('suggerimentiModal');
                if (modalEl) {
                    var instance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                    instance.hide();
                }
            });
        });
    }, 100);

    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="suggerimentiLabel">${titleLabel} rimasti: ${remaining}/${max}</h1>
                    ${subtitleHtml ? `<div class="small text-muted ms-2">${subtitleHtml}</div>` : ""}
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    ${htmlContent}
                    ${data.noMoreSuggestions ? `<div class="alert alert-warning mt-3" role="alert">${noMoreMessage}</div>` : ""}
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Chiudi</button>
                </div>
            </div>
        </div>
    `;

    // Aggiungi la modale al documento
    document.body.appendChild(modal);

    // Crea e mostra la modale Bootstrap
    var bsModal = new bootstrap.Modal(modal);
    bsModal.show();

    // Rimuovi la modale dopo che viene chiusa
    modal.addEventListener("hidden.bs.modal", function () {
        modal.remove();
    });
}
function inserisciSuggerimentoNelCodice(suggerimento) {
    // Inserisce il suggerimento alla fine del codice, dentro le parentesi graffe
    var currentCode = editor_utente.getValue();
    var commentLines = suggerimento.split("\n").map(function(line) {
        return " * " + line;
    }).join("\n");
    var suggestionComment = "\n/* SUGGERIMENTO:\n" + commentLines + "\n */\n";

    // Trova l'ultima parentesi graffa chiusa
    var lastBraceIndex = currentCode.lastIndexOf("}");

    if (lastBraceIndex !== -1) {
        // Inserisci il commento prima dell'ultima parentesi graffa
        var newCode = currentCode.slice(0, lastBraceIndex) + suggestionComment + "\n" + currentCode.slice(lastBraceIndex);
        editor_utente.setValue(newCode);
    } else {
        // Se non trova parentesi graffe, aggiunge in fondo
        var newCode = currentCode + suggestionComment;
        editor_utente.setValue(newCode);
    }

    // Mostra notifica di successo
    alert("Suggerimento inserito nel codice!");
}
//pulizia local storage a fine partita
function flush_localStorage(){
    //Pulisco i dati locali
    pulisciLocalStorage("difficulty");
    pulisciLocalStorage("modalita");
    pulisciLocalStorage("robot");
    pulisciLocalStorage("roundId");
    pulisciLocalStorage("turnId");
    pulisciLocalStorage("underTestClassName");
    pulisciLocalStorage("username");
    pulisciLocalStorage("storico");
    pulisciLocalStorage("codeMirrorContent");
    pulisciLocalStorage("suggestionsAvailable");
    pulisciLocalStorage("suggestionsMax");
    pulisciLocalStorage("suggestionHistory");
    pulisciLocalStorage("advancedSuggestionHistory");
    pulisciLocalStorage("advancedSuggestionsAvailable");
    pulisciLocalStorage("advancedSuggestionsMax");
}














