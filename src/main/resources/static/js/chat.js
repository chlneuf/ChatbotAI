
document.addEventListener("DOMContentLoaded", () => {

    // Agrandissement automatique du champ de saisie
    document.querySelectorAll(".chat-textarea").forEach(textarea => {

        const ajusterHauteur = () => {
            textarea.style.height = "auto";
            textarea.style.height = textarea.scrollHeight + "px";
        };

        textarea.addEventListener("input", ajusterHauteur);

        // Entrée pour envoyer, Maj + Entrée pour revenir à la ligne
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

    // Indicateur de chargement lors de l'envoi
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

    // Copier une réponse de l'assistant
    document.querySelectorAll(".copy-message-button").forEach(button => {

        button.addEventListener("click", async () => {

            const message = button
                .closest(".message-bubble")
                .querySelector(".message-content");

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

});

document.addEventListener("DOMContentLoaded", () => {

    const menus = document.querySelectorAll(".conversation-menu");

    menus.forEach(menu => {
        const trigger = menu.querySelector(".conversation-menu-trigger");

        trigger.addEventListener("click", event => {
            event.preventDefault();
            event.stopPropagation();

            menus.forEach(otherMenu => {
                if (otherMenu !== menu) {
                    otherMenu.classList.remove("open");
                    otherMenu.querySelector(".conversation-menu-trigger")
                        .setAttribute("aria-expanded", "false");
                }
            });

            menu.classList.toggle("open");

            trigger.setAttribute(
                "aria-expanded",
                menu.classList.contains("open") ? "true" : "false"
            );
        });
    });

    document.addEventListener("click", () => {
        menus.forEach(menu => {
            menu.classList.remove("open");
            menu.querySelector(".conversation-menu-trigger")
                .setAttribute("aria-expanded", "false");
        });
    });

});

// Partager une conversation
document.querySelectorAll(".share-conversation-button").forEach(button => {

    button.addEventListener("click", async () => {

        const relativeUrl = button.dataset.url;
        const shareUrl = new URL(relativeUrl, window.location.origin).href;

        try {
            await navigator.clipboard.writeText(shareUrl);

            const originalContent = button.innerHTML;

            button.innerHTML = '<i class="bi bi-check2"></i> Lien copié !';

            setTimeout(() => {
                button.innerHTML = originalContent;
            }, 2000);

        } catch (error) {
            console.error("Erreur de partage :", error);
            window.prompt("Copie ce lien :", shareUrl);
        }
    });

});
// Paramètres Church
document.addEventListener("DOMContentLoaded", () => {

    const theme = document.getElementById("themeSelect");
    const color = document.getElementById("primaryColor");
    const times = document.getElementById("showTimes");
    const notifications = document.getElementById("notifications");
    const save = document.getElementById("saveSettings");
    const message = document.getElementById("settingsMessage");

    if (!save) return;

    const saved = JSON.parse(
        localStorage.getItem("churchSettings") || "{}"
    );

    theme.value = saved.theme || "light";
    color.value = saved.color || "#713BFF";
    times.checked = saved.times ?? true;
    notifications.checked = saved.notifications ?? false;

    save.addEventListener("click", () => {

        const settings = {
            theme: theme.value,
            color: color.value,
            times: times.checked,
            notifications: notifications.checked
        };

        localStorage.setItem(
            "churchSettings",
            JSON.stringify(settings)
        );

        document.documentElement.style.setProperty(
            "--primary",
            settings.color
        );

        document.body.classList.toggle(
            "church-dark-mode",
            settings.theme === "dark"
        );

        message.textContent = "Préférences enregistrées !";

    });

});