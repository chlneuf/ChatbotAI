
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

    // Gestion des menus des conversations
    document.addEventListener("click", event => {

        const trigger = event.target.closest(
            ".conversation-menu-trigger"
        );

        const menus = document.querySelectorAll(".conversation-menu");

        // Clic sur les trois points
        if (trigger) {
            event.preventDefault();
            event.stopPropagation();

            const currentMenu = trigger.closest(".conversation-menu");
            const wasOpen = currentMenu.classList.contains("open");

            // Fermer les autres menus
            menus.forEach(menu => {
                menu.classList.remove("open");
                menu.querySelector(".conversation-menu-trigger")
                    ?.setAttribute("aria-expanded", "false");
            });

            // Ouvrir ou fermer le menu sélectionné
            if (!wasOpen) {
                currentMenu.classList.add("open");
                trigger.setAttribute("aria-expanded", "true");
            }

            return;
        }

        // Garder le menu ouvert lorsqu'on clique dedans
        if (event.target.closest(".conversation-menu")) {
            return;
        }

        // Fermer lorsqu'on clique à l'extérieur
        menus.forEach(menu => {
            menu.classList.remove("open");
            menu.querySelector(".conversation-menu-trigger")
                ?.setAttribute("aria-expanded", "false");
        });

    });

});
