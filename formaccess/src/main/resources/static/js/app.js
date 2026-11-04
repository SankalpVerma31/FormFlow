/* FormAccess – small helpers used on every page (plain JavaScript, no framework). */
document.addEventListener("DOMContentLoaded", function () {

    // 1. Confirmation dialog for dangerous buttons: <form data-confirm="Are you sure?">
    const modalEl = document.getElementById("confirmModal");
    let pendingForm = null;
    if (modalEl) {
        const modal = new bootstrap.Modal(modalEl);
        document.querySelectorAll("form[data-confirm]").forEach(function (form) {
            form.addEventListener("submit", function (e) {
                if (form.dataset.confirmed === "yes") return;
                e.preventDefault();
                pendingForm = form;
                document.getElementById("confirmModalText").textContent = form.dataset.confirm;
                modal.show();
            });
        });
        document.getElementById("confirmModalOk").addEventListener("click", function () {
            if (pendingForm) {
                pendingForm.dataset.confirmed = "yes";
                modal.hide();
                pendingForm.requestSubmit ? pendingForm.requestSubmit() : pendingForm.submit();
            }
        });
    }

    // 2. Loading state: disable the submit button after the first click (stops double submits).
    document.querySelectorAll("form[data-loading]").forEach(function (form) {
        form.addEventListener("submit", function () {
            const btn = form.querySelector("button[type=submit]");
            if (btn && !btn.disabled) {
                setTimeout(function () {
                    btn.disabled = true;
                    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>' + (form.dataset.loading || "Saving...");
                }, 0);
            }
        });
    });

    // 3. Form builder: show only the settings that belong to the chosen question type.
    document.querySelectorAll("[data-type-select]").forEach(function (select) {
        const container = select.closest("form");
        function refresh() {
            container.querySelectorAll("[data-show-for]").forEach(function (el) {
                const types = el.dataset.showFor.split(" ");
                el.style.display = types.includes(select.value) ? "" : "none";
            });
        }
        select.addEventListener("change", refresh);
        refresh();
    });

    // 4. Rule builder: helpful placeholder for the value box.
    const attr = document.getElementById("ruleAttribute");
    const val = document.getElementById("ruleValue");
    if (attr && val) {
        const hints = {
            DEPARTMENT: "e.g. CSE   (for 'is one of': CSE,IT)",
            YEAR: "e.g. 2",
            ROLE: "PARTICIPANT, CREATOR or ADMIN",
            ACCOUNT_STATUS: "ACTIVE or INACTIVE"
        };
        const update = function () { val.placeholder = hints[attr.value] || ""; };
        attr.addEventListener("change", update);
        update();
    }

    // 5. Copy share link
    document.querySelectorAll("[data-copy]").forEach(function (btn) {
        btn.addEventListener("click", function () {
            const input = document.getElementById(btn.dataset.copy);
            navigator.clipboard.writeText(input.value).then(function () {
                const old = btn.innerHTML;
                btn.innerHTML = '<i class="bi bi-check2"></i> Copied';
                setTimeout(function () { btn.innerHTML = old; }, 1500);
            });
        });
    });

    // 6. Auto-hide success alerts
    document.querySelectorAll(".alert-success[data-autohide]").forEach(function (el) {
        setTimeout(function () { bootstrap.Alert.getOrCreateInstance(el).close(); }, 5000);
    });
});
