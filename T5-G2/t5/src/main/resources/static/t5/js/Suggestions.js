
// Gestione dei suggerimenti standard e avanzati
const ADVANCED_SUGGESTION_COST = 2;
const DIFFICULTY_CAPS = { EASY: 10, MEDIUM: 5, HARD: 2 };

function parseIntOr(value, fallback) {
	const parsed = parseInt(value, 10);
	return isNaN(parsed) ? fallback : parsed;
}

function storedDifficulty() {
	return localStorage.getItem("difficulty") || "EASY";
}

function storedClassName() {
	return localStorage.getItem("underTestClassName") || "";
}

function storedGameId() {
	return localStorage.getItem("roundId") || 0;
}

document.addEventListener("DOMContentLoaded", function () {
	var newButton = document.getElementById("suggerimento");
	if (newButton) {
		newButton.addEventListener("click", function () {
			richiediSuggerimento();
		});
	}
	var historyButton = document.getElementById("showSuggestionHistory");
	if (historyButton) {
		historyButton.addEventListener("click", function () {
			mostraStoricoSuggerimenti();
		});
	}
	initSuggestionCounters();
	renderSuggestionHistory();
    
    setupAdvancedSuggestions();
});

function suggestionsMaxForDifficulty(difficulty){
	if(!difficulty) return 0;
	return DIFFICULTY_CAPS[(difficulty + "").toUpperCase()] || 0;
}

function initSuggestionCounters(){
	var difficulty = storedDifficulty();
	var className = storedClassName();
	var max = parseIntOr(localStorage.getItem("suggestionsMax"), null);
	var history = getSuggestionHistory();
	var historyEmpty = !history || history.length === 0;

	//Fallback front-end (utilizzando suggestionsMaxForDifficulty) nel caso di errore lato backend.
	// All'avvio usiamo il limite per difficolta, ma appena possibile lo sostituiamo col cap reale da backend.
	if(max === null || max <= 0){
		max = suggestionsMaxForDifficulty(difficulty);
	}
	localStorage.setItem("suggestionsMax", max);

	var available = parseIntOr(localStorage.getItem("suggestionsAvailable"), null);
	if(available === null || available < 0){
		available = max;
	} else {
		available = Math.min(available, max);
	}
	localStorage.setItem("suggestionsAvailable", available);

	// Nel caso di ripresa di una partita già in corso, garantiamo che il contatore dei suggerimenti rimanenti
	// non sia sballato.
	var existingAvailable = historyEmpty ? null : available;

	// Se abbiamo la classe, chiediamo subito al backend il cap reale senza consumare suggerimenti.
	if(className){
		fetchAvailability(className, difficulty)
		.then(data => {
			({ max, available } = applyAvailabilityUpdate({
				data,
				existingAvailable,
				storageMaxKey: "suggestionsMax",
				storageAvailableKey: "suggestionsAvailable",
				currentMax: max,
				currentAvailable: available
			}));
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
	updateCounterElement("suggestion-counter", "suggestionsAvailable", "suggestionsMax");
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

function fetchAvailability(className, difficulty, tier) {
	var payload = { className: className, difficulty: difficulty };
	if (tier) payload.tier = tier;
	return fetch("/api/suggerimenti/disponibilita", {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(payload)
	})
		.then(resp => {
			if(!resp.ok) throw new Error("Errore disponibilita suggerimenti (" + resp.status + ")");
			return resp.json();
		});
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
	var difficulty = storedDifficulty();
	var className = storedClassName();
	var max = parseIntOr(localStorage.getItem("advancedSuggestionsMax"), 0);
	var available = parseIntOr(localStorage.getItem("advancedSuggestionsAvailable"), 0);
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
		fetchAvailability(className, difficulty, "ADVANCED")
			.then(data => {
				({ max, available } = applyAvailabilityUpdate({
					data,
					existingAvailable,
					storageMaxKey: "advancedSuggestionsMax",
					storageAvailableKey: "advancedSuggestionsAvailable",
					currentMax: max,
					currentAvailable: available
				}));
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
	updateCounterElement("advanced-suggestion-counter", "advancedSuggestionsAvailable", "advancedSuggestionsMax");
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
		emptyItem.textContent = suggerimenti_nessuno_avanzato;
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
			var available = parseIntOr(availableRaw, 0);
			var maxFromServer = parseIntOr((data.suggestionsMax || data.totalAvailableSuggestions), suggestionsMaxForDifficulty(difficulty));
			localStorage.setItem('suggestionsAvailable', available);
			localStorage.setItem('suggestionsMax', maxFromServer);
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
	var difficulty = storedDifficulty();
	var remainingSuggestions = parseIntOr(localStorage.getItem("advancedSuggestionsAvailable"), 0);
	var gameId = storedGameId();
	var className = storedClassName();
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
			var available = parseIntOr(availableRaw, 0);
			var maxFromServer = parseIntOr((data.suggestionsMax || data.totalAvailableSuggestions), 0);
			localStorage.setItem("advancedSuggestionsAvailable", available);
			localStorage.setItem("advancedSuggestionsMax", maxFromServer);
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

function updateCounterElement(counterId, availableKey, maxKey) {
	var counter = document.getElementById(counterId);
	if(!counter) return;
	var available = parseInt(localStorage.getItem(availableKey), 10) || 0;
	var max = parseInt(localStorage.getItem(maxKey), 10) || 0;
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

function applyAvailabilityUpdate(params) {
	var data = params.data;
	var maxFromServer = parseIntOr((data.suggestionsMax || data.totalAvailableSuggestions), params.currentMax);
	var availableFromServer = parseIntOr((data.availableSuggestions || data.totalAvailableSuggestions), null);
	var max = params.currentMax;
	var available = params.currentAvailable;

	if(maxFromServer && maxFromServer > 0){
		max = maxFromServer;
		localStorage.setItem(params.storageMaxKey, max);
		available = Math.min(available, max);
	}
	if(availableFromServer !== null && availableFromServer >= 0){
		if(params.existingAvailable === null){
			available = availableFromServer;
		} else {
			available = Math.min(params.existingAvailable, availableFromServer);
		}
		available = Math.min(available, max);
		localStorage.setItem(params.storageAvailableKey, available);
	}
	return { max, available };
}
