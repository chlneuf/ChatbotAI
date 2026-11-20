
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.Utilisateur
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ProfilController(
    private val utilisateurDAO: UtilisateurDAO
) {

    @GetMapping("/profil")
    fun afficherProfil(model: Model): String {
        val utilisateur = utilisateurDAO.findAll().firstOrNull()
            ?: Utilisateur(
                pseudo = "Mon profil",
                email = "utilisateur@church.local"
            ).let { utilisateurDAO.save(it) }

        model.addAttribute("utilisateur", utilisateur)
        return "fragments/profil"
    }

    @PostMapping("/profil")
    fun modifierProfil(
        @RequestParam pseudo: String,
        @RequestParam email: String,
        @RequestParam(required = false) photoProfil: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        val utilisateur = utilisateurDAO.findAll().firstOrNull()
            ?: Utilisateur(
                pseudo = "Mon profil",
                email = "utilisateur@church.local"
            )

        utilisateur.pseudo = pseudo.trim()
        utilisateur.email = email.trim()
        utilisateur.photoProfil = photoProfil?.trim()?.takeIf { it.isNotEmpty() }

        utilisateurDAO.save(utilisateur)
        redirectAttributes.addFlashAttribute("message", "Profil modifié avec succès !")

        return "redirect:/profil"
    }
}
