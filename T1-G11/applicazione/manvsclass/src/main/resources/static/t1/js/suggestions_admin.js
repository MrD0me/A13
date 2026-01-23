document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("suggestion-form");
  const resetBtn = document.getElementById("reset-form");
  const backBtn = document.getElementById("back-dashboard");
  const successAlert = document.getElementById("alert-success");
  const errorAlert = document.getElementById("alert-error");
  const difficultySelect = document.getElementById("difficulty");
  const tierSelect = document.getElementById("tier");
  const classSelect = document.getElementById("className");
  const i18n = window.suggestionI18n || {};
  const listClassSelect = document.getElementById("list-class");
  const listDifficultySelect = document.getElementById("list-difficulty");
  const listTierSelect = document.getElementById("list-tier");
  const listContainer = document.getElementById("suggestion-list");
  const loadSuggestionsBtn = document.getElementById("load-suggestions");

  const difficultyOptions = [
    { value: "EASY", label: i18n?.difficulties?.EASY || "EASY" },
    { value: "MEDIUM", label: i18n?.difficulties?.MEDIUM || "MEDIUM" },
    { value: "HARD", label: i18n?.difficulties?.HARD || "HARD" }
  ];

  const tierOptions = [
    { value: "BASE", label: i18n?.tiers?.BASE || "BASE" },
    { value: "ADVANCED", label: i18n?.tiers?.ADVANCED || "ADVANCED" }
  ];

  const populateSelect = (selectEl, options, placeholder) => {
    selectEl.innerHTML = "";
    const ph = document.createElement("option");
    ph.value = "";
    ph.disabled = true;
    ph.selected = true;
    ph.textContent = placeholder;
    selectEl.appendChild(ph);
    options.forEach(opt => {
      const o = document.createElement("option");
      o.value = opt.value;
      o.textContent = opt.label;
      selectEl.appendChild(o);
    });
  };

  const loadDifficultiesAndTiers = () => {
    populateSelect(
      difficultySelect,
      difficultyOptions,
      i18n?.difficultyPlaceholder || "Select difficulty"
    );
    populateSelect(
      listDifficultySelect,
      difficultyOptions,
      i18n?.difficultyPlaceholder || "Select difficulty"
    );
    populateSelect(
      tierSelect,
      tierOptions,
      i18n?.tierPlaceholder || "Select type"
    );
    populateSelect(
      listTierSelect,
      tierOptions,
      i18n?.tierPlaceholder || "Select type"
    );
  };

  const loadClasses = async () => {
    try {
      const resp = await fetch(APIS.OPPONENTS.CLASSES_SUMMARY);
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const data = await resp.json();
      const opts = (data || []).map(name => ({ value: name, label: name }));
      populateSelect(classSelect, opts, i18n?.classPlaceholder || "Select a class");
      populateSelect(listClassSelect, opts, i18n?.classPlaceholder || "Select a class");
    } catch (err) {
      console.error("Errore nel recupero classi:", err);
      populateSelect(
        classSelect,
        [],
        i18n?.noClasses || "No classes available"
      );
      populateSelect(
        listClassSelect,
        [],
        i18n?.noClasses || "No classes available"
      );
    }
  };

  const showAlert = (el) => {
    successAlert.style.display = "none";
    errorAlert.style.display = "none";
    el.style.display = "block";
  };

  loadDifficultiesAndTiers();
  loadClasses();

  resetBtn.addEventListener("click", () => {
    form.reset();
    successAlert.style.display = "none";
    errorAlert.style.display = "none";
    loadDifficultiesAndTiers();
  });

  const renderSuggestions = (items) => {
    listContainer.innerHTML = "";
    if (!items || items.length === 0) {
      const empty = document.createElement("div");
      empty.textContent = i18n?.list?.empty || "No suggestions found";
      listContainer.appendChild(empty);
      return;
    }
    items.forEach(item => {
      const card = document.createElement("div");
      card.style.border = "1px solid #1f2937";
      card.style.borderRadius = "10px";
      card.style.padding = "10px";
      card.style.marginBottom = "8px";
      const header = document.createElement("div");
      header.style.display = "flex";
      header.style.justifyContent = "space-between";
      header.style.alignItems = "center";
      header.innerHTML = `<strong>${item.tier}</strong> | ${item.language || ''}`;
      const text = document.createElement("div");
      text.textContent = item.text;
      text.style.margin = "6px 0";
      const delBtn = document.createElement("button");
      delBtn.className = "btn btn-secondary";
      delBtn.textContent = i18n?.list?.delete || "Delete";
      delBtn.addEventListener("click", async () => {
        try {
          const resp = await fetch(APIS.SUGGESTIONS.DELETE(item.id), { method: "DELETE" });
          if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
          loadSuggestions();
        } catch (err) {
          console.error("Errore eliminazione suggerimento:", err);
          showAlert(errorAlert);
        }
      });
      card.appendChild(header);
      card.appendChild(text);
      card.appendChild(delBtn);
      listContainer.appendChild(card);
    });
  };

  const loadSuggestions = async () => {
    if (!listClassSelect.value || !listDifficultySelect.value) {
      renderSuggestions([]);
      return;
    }
    const params = new URLSearchParams({
      className: listClassSelect.value,
      difficulty: listDifficultySelect.value
    });
    if (listTierSelect.value) {
      params.append("tier", listTierSelect.value);
    }
    try {
      const resp = await fetch(`${APIS.SUGGESTIONS.LIST}?${params.toString()}`);
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const data = await resp.json();
      renderSuggestions(data);
    } catch (err) {
      console.error("Errore caricamento suggerimenti:", err);
      renderSuggestions([]);
    }
  };

  if (loadSuggestionsBtn) {
    loadSuggestionsBtn.addEventListener("click", loadSuggestions);
  }

  [listClassSelect, listDifficultySelect, listTierSelect].forEach(sel => {
    sel?.addEventListener("change", () => {
      // Optional: auto-load on change
    });
  });

  backBtn.addEventListener("click", () => {
    window.location.href = VIEWS.DASHBOARD_ADMIN;
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    successAlert.style.display = "none";
    errorAlert.style.display = "none";

    const payload = {
      className: form.className.value.trim(),
      difficulty: form.difficulty.value,
      tier: form.tier.value,
      language: form.language.value.trim(),
      text: form.text.value.trim()
    };

    try {
      const res = await fetch(APIS.SUGGESTIONS.CREATE, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }

      showAlert(successAlert);
      form.reset();
      form.language.value = "it";
      loadDifficultiesAndTiers();
      loadClasses();
    } catch (err) {
      console.error("Errore inserimento suggerimento:", err);
      showAlert(errorAlert);
    }
  });
});
