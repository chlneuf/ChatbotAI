
document.addEventListener("DOMContentLoaded", () => {

    // ==========================================
    // THÈME CLAIR / SOMBRE
    // ==========================================

    const themeToggle = document.getElementById("themeToggle");
    const html = document.documentElement;

    function appliquerTheme(theme) {
        html.setAttribute("data-theme", theme);
        localStorage.setItem("churchTheme", theme);

        if (themeToggle) {
            const estSombre = theme === "dark";

            themeToggle.innerHTML = estSombre
                ? '<i class="bi bi-sun-fill" aria-hidden="true"></i><span>Mode clair</span>'
                : '<i class="bi bi-moon-stars-fill" aria-hidden="true"></i><span>Mode sombre</span>';

            themeToggle.setAttribute(
                "aria-label",
                estSombre ? "Activer le thème clair" : "Activer le thème sombre"
            );

            themeToggle.setAttribute("aria-pressed", String(estSombre));
        }
    }

    const themeEnregistre = localStorage.getItem("churchTheme");
    appliquerTheme(themeEnregistre === "dark" ? "dark" : "light");

    if (themeToggle) {
        themeToggle.addEventListener("click", () => {
            const themeActuel = html.getAttribute("data-theme");
            appliquerTheme(themeActuel === "dark" ? "light" : "dark");
        });
    }


    // ==========================================
    // CHAMP DE SAISIE DU CHAT
    // ==========================================

    document.querySelectorAll(".chat-textarea").forEach(textarea => {

        const ajusterHauteur = () => {
            textarea.style.height = "auto";
            textarea.style.height = textarea.scrollHeight + "px";
        };

        textarea.addEventListener("input", ajusterHauteur);

        textarea.addEventListener("keydown", event => {
            if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();

                const form = textarea.closest("form");

                if (textarea.value.trim() !== "" && form) {
                    form.requestSubmit();
                }
            }
        });

    });


    // ==========================================
    // INDICATEUR DE CHARGEMENT
    // ==========================================

    document.querySelectorAll(".chat-form").forEach(form => {

        form.addEventListener("submit", () => {

            const textarea = form.querySelector(".chat-textarea");
            const button = form.querySelector(".send-button");
            const loading = form.querySelector(".chat-loading");

            if (!textarea || textarea.value.trim() === "") {
                return;
            }

            if (button) {
                button.disabled = true;
                button.textContent = "…";
            }

            if (loading) {
                loading.hidden = false;
            }

        });

    });


    // ==========================================
    // COPIER UNE RÉPONSE
    // ==========================================

    document.querySelectorAll(".copy-message-button").forEach(button => {

        button.addEventListener("click", async () => {

            const message = button
                .closest(".message-bubble")
                ?.querySelector(".message-content");

            if (!message) return;

            try {
                await navigator.clipboard.writeText(message.innerText);

                button.innerHTML = '<i class="bi bi-check2"></i> Copié !';
                button.classList.add("copied");

                setTimeout(() => {
                    button.innerHTML = '<i class="bi bi-copy"></i> Copier';
                    button.classList.remove("copied");
                }, 2000);

            } catch (error) {
                console.error("Impossible de copier le message :", error);
                button.textContent = "Copie impossible";
            }

        });

    });


    // ==========================================
    // MENUS DES CONVERSATIONS
    // ==========================================

    document.addEventListener("click", event => {

        const menus = document.querySelectorAll(".conversation-menu");

        const trigger = event.target.closest(
            ".conversation-menu-trigger"
        );

        if (trigger) {
            event.preventDefault();
            event.stopPropagation();

            const currentMenu = trigger.closest(".conversation-menu");
            const wasOpen = currentMenu.classList.contains("open");

            menus.forEach(menu => {
                menu.classList.remove("open");
                menu.querySelector(".conversation-menu-trigger")
                    ?.setAttribute("aria-expanded", "false");
            });

            if (!wasOpen) {
                currentMenu.classList.add("open");
                trigger.setAttribute("aria-expanded", "true");
            }

            return;
        }

        if (event.target.closest(".conversation-menu")) {
            return;
        }

        menus.forEach(menu => {
            menu.classList.remove("open");
            menu.querySelector(".conversation-menu-trigger")
                ?.setAttribute("aria-expanded", "false");
        });

    });


    // ==========================================
    // PARTAGER UNE CONVERSATION
    // ==========================================

    document.querySelectorAll(".share-conversation-button").forEach(button => {

        button.addEventListener("click", async () => {

            const relativeUrl = button.dataset.url;

            if (!relativeUrl) return;

            const shareUrl = new URL(
                relativeUrl,
                window.location.origin
            ).href;

            try {
                await navigator.clipboard.writeText(shareUrl);

                const originalContent = button.innerHTML;

                button.innerHTML =
                    '<i class="bi bi-check2"></i> Lien copié !';

                setTimeout(() => {
                    button.innerHTML = originalContent;
                }, 2000);

            } catch (error) {
                console.error("Erreur de partage :", error);
                window.prompt("Copie ce lien :", shareUrl);
            }

        });

    });

});
