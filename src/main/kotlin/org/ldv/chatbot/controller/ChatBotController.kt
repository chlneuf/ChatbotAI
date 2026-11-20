
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.AIModelDAO
import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.dao.MessageDAO
import org.ldv.chatbot.model.entity.Message
import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.TypeMessage
import org.ldv.chatbot.service.IAServiceFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Controller
class ChatController(
    private val iaServiceFactory: IAServiceFactory,
    private val conversationDAO: ConversationDAO,
    private val messageDAO: MessageDAO,
    private val modeleIADAO: AIModelDAO
) {

    // Historique : conversations épinglées en premier
    private fun chargerHistorique(): List<Conversation> {
        return conversationDAO
            .findAllByOrderByDateDerniereModificationDesc()
            .sortedWith(
                compareByDescending<Conversation> { it.pinned }
                    .thenByDescending { it.dateDerniereModification }
            )
    }

    // Accueil
    @GetMapping("/")
    fun accueil(model: Model): String {
        model.addAttribute("conversation", null)
        model.addAttribute("modeles", modeleIADAO.findAll())
        model.addAttribute("messages", emptyList<Message>())
        model.addAttribute("conversations", chargerHistorique())

        return "chat"
    }

    // Création d'une nouvelle conversation
    @PostMapping("/conversations")
    fun creerConversation(
        @RequestParam prompt: String,
        @RequestParam idModele: Long
    ): String {

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
            model = modele
        )

        val conversationEnregistree = conversationDAO.save(conversation)

        val messageUtilisateur = Message(
            contenu = promptNettoye,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversationEnregistree
        )

        messageDAO.save(messageUtilisateur)

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

        } catch (e: Exception) {
            println("Erreur lors de l'appel au modèle ${modele.name} : ${e.message}")
        }

        conversationEnregistree.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversationEnregistree)

        return "redirect:/conversations/${conversationEnregistree.id}"
    }

    // Affichage d'une conversation
    @GetMapping("/conversations/{id}")
    fun afficherConversation(
        @PathVariable id: Long,
        model: Model
    ): String {

        val conversation = conversationDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }

        model.addAttribute("conversations", chargerHistorique())
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
        @RequestParam prompt: String
    ): String {

        val promptNettoye = prompt.trim()

        if (promptNettoye.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le message ne peut pas être vide"
            )
        }

        val conversation = conversationDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }

        val messageUtilisateur = Message(
            contenu = promptNettoye,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversation
        )

        messageDAO.save(messageUtilisateur)

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

        } catch (e: Exception) {
            println("Erreur lors de l'appel à ${conversation.model.name} : ${e.message}")
        }

        conversation.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversation)

        return "redirect:/conversations/$id"
    }

    // Suppression d'une conversation
    @PostMapping("/conversations/{id}/delete")
    @Transactional
    fun supprimerConversation(
        @PathVariable id: Long
    ): String {

        val conversation = conversationDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }

        conversationDAO.delete(conversation)

        return "redirect:/"
    }

    // Renommer une conversation
    @PostMapping("/conversations/{id}/rename")
    fun renommerConversation(
        @PathVariable id: Long,
        @RequestParam title: String
    ): String {

        val conversation = conversationDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }

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
        @PathVariable id: Long
    ): String {

        val conversation = conversationDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND)
            }

        conversation.pinned = !conversation.pinned
        conversationDAO.save(conversation)

        return "redirect:/"
    }
    @GetMapping("/parametres")
    fun afficherParametres(): String {
        return "fragements/parametres"
    }
}
