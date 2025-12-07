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
      tierSelect,
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
      populateSelect(
        classSelect,
        opts,
        i18n?.classPlaceholder || "Select a class"
      );
    } catch (err) {
      console.error("Errore nel recupero classi:", err);
      populateSelect(
        classSelect,
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
