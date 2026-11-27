
document.addEventListener("DOMContentLoaded", function () {

    // Vérifier la disponibilité de la synthèse vocale
    if (
        !("speechSynthesis" in window) ||
        !("SpeechSynthesisUtterance" in window)
    ) {
        return;
    }

    const synthese = window.speechSynthesis;
    const selectVoix = document.getElementById("choixVoix");

    // Récupérer la voix enregistrée
    const voixEnregistree = localStorage.getItem("voixChatbot");

    // Charger les voix françaises dans la liste
    function chargerVoix() {

        if (!selectVoix) return;

        const voix = synthese.getVoices();

        selectVoix.innerHTML =
            '<option value="">Voix par défaut</option>';

        voix
            .filter(voix => voix.lang.toLowerCase().startsWith("fr"))
            .forEach(voix => {

                const option = document.createElement("option");

                option.value = voix.voiceURI;
                option.textContent = `${voix.name} (${voix.lang})`;

                selectVoix.appendChild(option);
            });

        // Restaurer la voix précédemment choisie
        if (voixEnregistree) {
            const voixExiste = voix.some(
                voix => voix.voiceURI === voixEnregistree
            );

            if (voixExiste) {
                selectVoix.value = voixEnregistree;
            }
        }
    }

    // Charger les voix immédiatement et lorsqu'elles deviennent disponibles
    if (selectVoix) {
        chargerVoix();
        synthese.addEventListener("voiceschanged", chargerVoix);

        // Enregistrer le choix de l'utilisateur
        selectVoix.addEventListener("change", function () {

            localStorage.setItem(
                "voixChatbot",
                selectVoix.value
            );

        });
    }

    // Lecture automatique uniquement après une nouvelle réponse IA
    const pageChat = document.querySelector(".church-chat");

    if (!pageChat || pageChat.dataset.lectureAuto !== "true") {
        return;
    }

    const messages = document.querySelectorAll(".message-assistant");

    if (messages.length === 0) return;

    const dernierMessage = messages[messages.length - 1];
    const texte = dernierMessage.innerText.trim();

    if (!texte) return;

    // Créer la lecture vocale
    const lecture = new SpeechSynthesisUtterance(texte);

    lecture.lang = "fr-FR";
    lecture.rate = 1;

    // Rechercher la voix sélectionnée
    const voixChoisie = synthese.getVoices().find(
        voix => voix.voiceURI === localStorage.getItem("voixChatbot")
    );

    // Utiliser la voix choisie si elle existe
    if (voixChoisie) {
        lecture.voice = voixChoisie;
        lecture.lang = voixChoisie.lang;
    }

    // Lancer la lecture
    synthese.cancel();
    synthese.speak(lecture);

});
