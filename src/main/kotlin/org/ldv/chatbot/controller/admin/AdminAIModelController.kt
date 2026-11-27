
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

    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("modelesIA", aiModelDAO.findAll())
        return "admin/modeleIA/index"
    }

    @GetMapping("/create")
    fun create(model: Model): String {
        model.addAttribute("fournisseursIA", aiProviderDAO.findAll())
        return "admin/modeleIA/create"
    }

    @PostMapping
    fun store(
        @RequestParam name: String,
        @RequestParam urlApi: String,
        @RequestParam(defaultValue = "") descriptor: String,
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

        aiModelDAO.save(
            AIModel(
                name = name.trim(),
                urlApi = urlApi.trim(),
                descriptor = descriptor.trim(),
                provider = provider
            )
        )

        redirectAttributes.addFlashAttribute(
            "succes",
            "Le modèle IA a été créé avec succès."
        )

        return "redirect:/admin/modeles-ia"
    }

    @GetMapping("/{id}")
    fun show(@PathVariable id: Long, model: Model): String {
        val modele = trouverModele(id)
        model.addAttribute("modeleIA", modele)
        return "admin/modeleIA/show"
    }

    @GetMapping("/{id}/edit")
    fun edit(@PathVariable id: Long, model: Model): String {
        val modele = trouverModele(id)

        model.addAttribute("modeleIA", modele)
        model.addAttribute("fournisseursIA", aiProviderDAO.findAll())

        return "admin/modeleIA/edit"
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam urlApi: String,
        @RequestParam(defaultValue = "") descriptor: String,
        @RequestParam providerId: Long,
        redirectAttributes: RedirectAttributes
    ): String {

        if (name.isBlank() || urlApi.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le nom et l'URL sont obligatoires"
            )
        }

        val modele = trouverModele(id)

        val provider = aiProviderDAO.findById(providerId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fournisseur IA introuvable"
                )
            }

        if (
            conversationDAO.existsByModel(modele) &&
            (modele.provider.id != provider.id ||
                    modele.urlApi != urlApi.trim())
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

    @DeleteMapping("/{id}")
    fun destroy(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {

        println(">>> SUPPRESSION DEMANDEE POUR LE MODELE ID : $id")

        val modele = trouverModele(id)

        if (conversationDAO.existsByModel(modele)) {
            redirectAttributes.addFlashAttribute(
                "erreur",
                "Ce modèle IA est utilisé dans une conversation et ne peut pas être supprimé."
            )
            return "redirect:/admin/modeles-ia"
        }

        return try {
            aiModelDAO.delete(modele)
            aiModelDAO.flush()

            redirectAttributes.addFlashAttribute(
                "succes",
                "Le modèle IA a été supprimé avec succès."
            )

            "redirect:/admin/modeles-ia"

        } catch (exception: DataIntegrityViolationException) {
            redirectAttributes.addFlashAttribute(
                "erreur",
                "Impossible de supprimer ce modèle IA : il est encore utilisé."
            )

            "redirect:/admin/modeles-ia"
        }
    }

    private fun trouverModele(id: Long): AIModel {
        return aiModelDAO.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Modèle IA introuvable"
                )
            }
    }
}
