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
* 	Qui c'è tutta la logica dei bottoni e della GUI
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
    let fontSize = parseInt(fontSizeInput.value, 10) || 16; // Valore predefinito se non è impostato

    // Funzione per aggiornare la dimensione del font per tutti gli editor
    function aggiornaFontSize() {
        editors.forEach(editor => {
            const wrapper = editor.getWrapperElement();
            wrapper.style.fontSize = fontSize + "px"; // Imposta la dimensione del font
            wrapper.style.lineHeight = (fontSize * 1.5) + "px"; // Imposta l'altezza della linea (1.5 è un esempio di fattore)
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
document.addEventListener("DOMContentLoaded", function () {
    var newButton = document.getElementById("newButton");
    newButton.addEventListener("click", function () {
        // Richiedi un suggerimento a T1
        richiediSuggerimento();
    });
});

// Inizializza il contatore dei suggerimenti e mostra nell'interfaccia
document.addEventListener("DOMContentLoaded", function() {
    initSuggestionCounters();
});

function suggestionsMaxForDifficulty(difficulty){
    if(!difficulty) return 0;
    switch((difficulty + "").toUpperCase()){
        case 'HARD': return 0;
        case 'MEDIUM': return 3;
        case 'EASY': return 5;
        default: return 0;
    }
}

function initSuggestionCounters(){
    var difficulty = localStorage.getItem("difficulty") || "EASY";
    var max = parseInt(localStorage.getItem("suggestionsMax"), 10);
    if(isNaN(max)){
        max = suggestionsMaxForDifficulty(difficulty);
        localStorage.setItem("suggestionsMax", max);
    }

    var available = parseInt(localStorage.getItem("suggestionsAvailable"), 10);
    if(isNaN(available)){
        available = max;
        localStorage.setItem("suggestionsAvailable", available);
    }

    updateSuggestionCounter();
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

function richiediSuggerimento() {
    // Recupera i dati dalla sessione/localStorage
    var difficulty = localStorage.getItem("difficulty") || "EASY";
    var remainingSuggestions = parseInt(localStorage.getItem("suggestionsAvailable")) || 0;
    var gameId = localStorage.getItem("roundId") || 0;

    // Prepara il payload per la richiesta
    var requestBody = {
        gameId: gameId,
        difficulty: difficulty,
        remainingSuggestions: remainingSuggestions
    };

    // Effettua la richiesta a T1
    fetch("/api/suggerimenti/richiedi", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    })
        .then(response => response.json())
        .then(data => {
            // Aggiorna il numero di suggerimenti nel localStorage
            localStorage.setItem("suggestionsAvailable", data.remainingSuggestions);

            // Assicura che suggestionsMax sia impostato
            var max = localStorage.getItem("suggestionsMax");
            if(!max){
                localStorage.setItem("suggestionsMax", suggestionsMaxForDifficulty(difficulty));
            }

            updateSuggestionCounter();

            // Mostra i suggerimenti in una modale
            mostraSuggerimenti(data);

            // Se è l'ultimo suggerimento o sono finiti, mostra un avviso
            if (data.noMoreSuggestions) {
                // Mostra un alert se sono finiti
                if (data.remainingSuggestions === 0) {
                    alert("Non hai più suggerimenti disponibili per questa partita!");
                } else if (data.message) {
                    // Mostra il messaggio di avviso se è l'ultimo
                    console.warn(data.message);
                }
            }
        })
        .catch(error => {
            console.error("Errore nella richiesta dei suggerimenti:", error);
            alert("Errore nel caricamento dei suggerimenti");
        });
}

function mostraSuggerimenti(data) {
    // Se non ci sono suggerimenti, mostra un messaggio di errore
    if (!data.suggestions || data.suggestions.length === 0) {
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
						<h1 class="modal-title fs-5" id="suggerimentiLabel">Suggerimenti</h1>
						<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
					</div>
					<div class="modal-body">
						<div class="alert alert-warning" role="alert">
							<strong>Attenzione!</strong> Non hai più suggerimenti disponibili per questa partita.
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
    var htmlContent = "<ul class='list-group'>";
    data.suggestions.forEach(function(suggerimento) {
        htmlContent += "<li class='list-group-item'>" + suggerimento + "</li>";
    });
    htmlContent += "</ul>";

    // Aggiungi il contatore e il messaggio se necessario
    var messageHtml = "";
    if (data.noMoreSuggestions && data.message) {
        messageHtml = `<div class="alert alert-info mt-3" role="alert"><strong>Avviso:</strong> ${data.message}</div>`;
    }

    modal.innerHTML = `
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<h1 class="modal-title fs-5" id="suggerimentiLabel">Suggerimenti</h1>
						<small class="text-muted">Suggerimenti rimasti: ${data.remainingSuggestions}/${localStorage.getItem('suggestionsMax') || 0}</small>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body">
					${htmlContent}
					${messageHtml}
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
}