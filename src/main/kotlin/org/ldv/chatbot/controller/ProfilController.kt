
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Controller
class ProfilController(
    private val utilisateurDAO: UtilisateurDAO
) {

    @GetMapping("/profil")
    fun afficherProfil(
        authentication: Authentication,
        model: Model
    ): String {

        val utilisateur = utilisateurDAO.findByEmail(authentication.name)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        model.addAttribute("utilisateur", utilisateur)

        return "fragments/profil"
    }

    @PostMapping("/profil")
    fun modifierProfil(
        authentication: Authentication,
        @RequestParam pseudo: String,
        @RequestParam email: String,
        @RequestParam(required = false) photoProfil: String?,
        redirectAttributes: RedirectAttributes
    ): String {

        val utilisateur = utilisateurDAO.findByEmail(authentication.name)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val nouveauPseudo = pseudo.trim()
        val nouvelEmail = email.trim().lowercase()

        if (nouveauPseudo.isBlank() || nouvelEmail.isBlank()) {
            redirectAttributes.addFlashAttribute(
                "erreur",
                "Le pseudo et l'adresse e-mail sont obligatoires."
            )
            return "redirect:/profil"
        }

        val autreUtilisateur = utilisateurDAO.findByEmail(nouvelEmail)

        if (autreUtilisateur != null && autreUtilisateur.id != utilisateur.id) {
            redirectAttributes.addFlashAttribute(
                "erreur",
                "Cette adresse e-mail est déjà utilisée."
            )
            return "redirect:/profil"
        }

        utilisateur.pseudo = nouveauPseudo
        utilisateur.email = nouvelEmail
        utilisateur.photoProfil = photoProfil?.trim()?.takeIf { it.isNotEmpty() }

        utilisateurDAO.save(utilisateur)

        redirectAttributes.addFlashAttribute(
            "message",
            "Profil modifié avec succès !"
        )

        return "redirect:/profil"
    }
}
