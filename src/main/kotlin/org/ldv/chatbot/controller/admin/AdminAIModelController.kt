
package org.ldv.chatbot.controller.admin

import org.ldv.chatbot.model.dao.AIModelDAO
import org.ldv.chatbot.model.dao.AIProviderDAO
import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.entity.AIModel
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin/modeles-ia")
class AdminAIModelController(
    private val aiModelDAO: AIModelDAO,
    private val aiProviderDAO: AIProviderDAO,
    private val conversationDAO: ConversationDAO
) {

    // Liste des modèles
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("modelesIA", aiModelDAO.findAll())
        return "admin/modeleIA/index"
    }

    // Formulaire de création
    @GetMapping("/create")
    fun create(model: Model): String {
        model.addAttribute("fournisseursIA", aiProviderDAO.findAll())
        return "admin/modeleIA/create"
    }

    // Enregistrement d'un modèle
    @PostMapping
    fun store(
        @RequestParam name: String,
        @RequestParam urlApi: String,
        @RequestParam descriptor: String,
        @RequestParam providerId: Long,
        redirectAttributes: RedirectAttributes
    ): String {

        if (name.isBlank() || urlApi.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le nom et l'URL sont obligatoires"
            )
        }

        val provider = aiProviderDAO.findById(providerId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fournisseur IA introuvable"
                )
            }

        val modele = AIModel(
            name = name.trim(),
            urlApi = urlApi.trim(),
            descriptor = descriptor.trim(),
            provider = provider
        )

        aiModelDAO.save(modele)

        redirectAttributes.addFlashAttribute(
            "succes",
            "Le modèle IA a été créé avec succès."
        )

        return "redirect:/admin/modeles-ia"
    }

    // Détails d'un modèle
    @GetMapping("/{id}")
    fun show(
        @PathVariable id: Long,
        model: Model
    ): String {

        val modele = aiModelDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Modèle IA introuvable"
                )
            }

        model.addAttribute("modeleIA", modele)
        return "admin/modeleIA/show"
    }

    // Formulaire de modification
    @GetMapping("/{id}/edit")
    fun edit(
        @PathVariable id: Long,
        model: Model
    ): String {

        val modele = aiModelDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Modèle IA introuvable"
                )
            }

        model.addAttribute("modeleIA", modele)
        model.addAttribute("fournisseursIA", aiProviderDAO.findAll())

        return "admin/modeleIA/edit"
    }

    // Enregistrement des modifications
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam urlApi: String,
        @RequestParam descriptor: String,
        @RequestParam providerId: Long,
        redirectAttributes: RedirectAttributes
    ): String {

        if (name.isBlank() || urlApi.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le nom et l'URL sont obligatoires"
            )
        }

        val modele = aiModelDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Modèle IA introuvable"
                )
            }

        val provider = aiProviderDAO.findById(providerId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fournisseur IA introuvable"
                )
            }

        // Ne pas modifier un modèle utilisé par des conversations
        if (conversationDAO.existsByModel(modele) &&
            (modele.provider.id != provider.id || modele.urlApi != urlApi.trim())
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Impossible de modifier le fournisseur ou l'URL d'un modèle utilisé."
            )
        }

        modele.name = name.trim()
        modele.urlApi = urlApi.trim()
        modele.descriptor = descriptor.trim()
        modele.provider = provider

        aiModelDAO.save(modele)

        redirectAttributes.addFlashAttribute(
            "succes",
            "Le modèle IA a été modifié avec succès."
        )

        return "redirect:/admin/modeles-ia"
    }

    // Suppression d'un modèle
    @DeleteMapping("/{id}")
    fun destroy(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {

        val modele = aiModelDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Modèle IA introuvable"
                )
            }

        if (conversationDAO.existsByModel(modele)) {
            redirectAttributes.addFlashAttribute(
                "erreur",
                "Ce modèle IA est utilisé dans une conversation."
            )
        } else {
            try {
                aiModelDAO.delete(modele)

                redirectAttributes.addFlashAttribute(
                    "succes",
                    "Le modèle IA a été supprimé avec succès."
                )
            } catch (exception: DataIntegrityViolationException) {
                redirectAttributes.addFlashAttribute(
                    "erreur",
                    "Ce modèle IA ne peut pas être supprimé."
                )
            }
        }

        return "redirect:/admin/modeles-ia"
    }
}
