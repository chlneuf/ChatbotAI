
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.AIModelDAO
import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.dao.MessageDAO
import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.Message
import org.ldv.chatbot.model.entity.TypeMessage
import org.ldv.chatbot.service.IAServiceFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDateTime

@Controller
class ChatController(
    private val iaServiceFactory: IAServiceFactory,
    private val conversationDAO: ConversationDAO,
    private val messageDAO: MessageDAO,
    private val modeleIADAO: AIModelDAO,
    private val utilisateurDAO: UtilisateurDAO
) {

    // Récupérer l'utilisateur connecté
    private fun utilisateurConnecte(authentication: Authentication) =
        utilisateurDAO.findByEmail(authentication.name)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

    // Récupérer uniquement les conversations de l'utilisateur
    private fun chargerHistorique(utilisateurId: Long): List<Conversation> {
        return conversationDAO
            .findAllByUtilisateur_IdOrderByDateDerniereModificationDesc(utilisateurId)
            .sortedWith(
                compareByDescending<Conversation> { it.pinned }
                    .thenByDescending { it.dateDerniereModification }
            )
    }

    // Vérifier que la conversation appartient à l'utilisateur
    private fun chargerConversation(
        id: Long,
        utilisateurId: Long
    ): Conversation {
        return conversationDAO.findByIdAndUtilisateur_Id(id, utilisateurId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    // Accueil
    @GetMapping("/")
    fun accueil(
        authentication: Authentication,
        model: Model
    ): String {

        val utilisateur = utilisateurConnecte(authentication)

        model.addAttribute("utilisateur", utilisateur)
        model.addAttribute("conversation", null)
        model.addAttribute("modeles", modeleIADAO.findAll())
        model.addAttribute("messages", emptyList<Message>())
        model.addAttribute("conversations", chargerHistorique(utilisateur.id!!))

        return "chat"
    }

    // Création d'une nouvelle conversation
    @PostMapping("/conversations")
    fun creerConversation(
        @RequestParam prompt: String,
        @RequestParam idModele: Long,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes
    ): String {

        val utilisateur = utilisateurConnecte(authentication)
        val promptNettoye = prompt.trim()

        if (promptNettoye.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le message ne peut pas être vide"
            )
        }

        val modele = modeleIADAO.findById(idModele)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modèle IA introuvable"
                )
            }

        val conversation = Conversation(
            title = promptNettoye.take(50),
            createdAt = LocalDateTime.now(),
            model = modele,
            utilisateur = utilisateur
        )

        val conversationEnregistree = conversationDAO.save(conversation)

        val messageUtilisateur = Message(
            contenu = promptNettoye,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversationEnregistree
        )

        messageDAO.save(messageUtilisateur)

        var lectureAuto = false

        try {
            val iaService = iaServiceFactory.getService(modele)
            val reponse = iaService.envoyerPrompt(promptNettoye, modele)

            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversationEnregistree
            )

            messageDAO.save(messageAssistant)

            lectureAuto = true

        } catch (e: Exception) {
            println("Erreur lors de l'appel au modèle ${modele.name} : ${e.message}")
        }

        conversationEnregistree.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversationEnregistree)

        if (lectureAuto) {
            redirectAttributes.addFlashAttribute("lectureAuto", true)
        }

        return "redirect:/conversations/${conversationEnregistree.id}"
    }

    // Affichage d'une conversation
    @GetMapping("/conversations/{id}")
    fun afficherConversation(
        @PathVariable id: Long,
        authentication: Authentication,
        model: Model
    ): String {

        val utilisateur = utilisateurConnecte(authentication)
        val conversation = chargerConversation(id, utilisateur.id!!)

        model.addAttribute("utilisateur", utilisateur)
        model.addAttribute("conversations", chargerHistorique(utilisateur.id!!))
        model.addAttribute("modeles", modeleIADAO.findAll())
        model.addAttribute("conversation", conversation)
        model.addAttribute(
            "messages",
            conversation.messages.sortedBy { it.dateCreation }
        )

        return "chat"
    }

    // Envoi d'un nouveau message
    @PostMapping("/conversations/{id}/messages")
    fun envoyerMessage(
        @PathVariable id: Long,
        @RequestParam prompt: String,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes
    ): String {

        val utilisateur = utilisateurConnecte(authentication)
        val conversation = chargerConversation(id, utilisateur.id!!)
        val promptNettoye = prompt.trim()

        if (promptNettoye.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le message ne peut pas être vide"
            )
        }

        val messageUtilisateur = Message(
            contenu = promptNettoye,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversation
        )

        messageDAO.save(messageUtilisateur)

        var lectureAuto = false

        try {
            val modele = conversation.model
            val iaService = iaServiceFactory.getService(modele)
            val reponse = iaService.envoyerPrompt(promptNettoye, modele)

            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversation
            )

            messageDAO.save(messageAssistant)

            lectureAuto = true

        } catch (e: Exception) {
            println("Erreur lors de l'appel à ${conversation.model.name} : ${e.message}")
        }

        conversation.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversation)

        if (lectureAuto) {
            redirectAttributes.addFlashAttribute("lectureAuto", true)
        }

        return "redirect:/conversations/$id"
    }

    // Suppression d'une conversation
    @PostMapping("/conversations/{id}/delete")
    @Transactional
    fun supprimerConversation(
        @PathVariable id: Long,
        authentication: Authentication
    ): String {

        val utilisateur = utilisateurConnecte(authentication)
        val conversation = chargerConversation(id, utilisateur.id!!)

        conversationDAO.delete(conversation)

        return "redirect:/"
    }

    // Renommer une conversation
    @PostMapping("/conversations/{id}/rename")
    fun renommerConversation(
        @PathVariable id: Long,
        @RequestParam title: String,
        authentication: Authentication
    ): String {

        val utilisateur = utilisateurConnecte(authentication)
        val conversation = chargerConversation(id, utilisateur.id!!)
        val nouveauTitre = title.trim()

        if (nouveauTitre.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le titre ne peut pas être vide"
            )
        }

        conversation.title = nouveauTitre.take(100)
        conversation.dateDerniereModification = LocalDateTime.now()

        conversationDAO.save(conversation)

        return "redirect:/conversations/$id"
    }

    // Épingler ou désépingler une conversation
    @PostMapping("/conversations/{id}/pin")
    fun epinglerConversation(
        @PathVariable id: Long,
        authentication: Authentication
    ): String {

        val utilisateur = utilisateurConnecte(authentication)
        val conversation = chargerConversation(id, utilisateur.id!!)

        conversation.pinned = !conversation.pinned
        conversation.dateDerniereModification = LocalDateTime.now()

        conversationDAO.save(conversation)

        return "redirect:/"
    }

    // Paramètres
    @GetMapping("/parametres")
    fun afficherParametres(): String {
        return "fragments/parametres"
    }
}
