
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.dao.RoleDAO
import org.ldv.chatbot.model.entity.Utilisateur
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class InscriptionController(
    private val utilisateurDAO: UtilisateurDAO,
    private val roleDAO: RoleDAO,
    private val passwordEncoder: PasswordEncoder
) {

    @GetMapping("/inscription")
    fun afficherInscription(): String {
        return "fragments/inscription"
    }

    @GetMapping("/connexion")
    fun afficherConnexion(): String {
        return "fragments/connexion"
    }

    @PostMapping("/inscription")
    fun inscrireUtilisateur(
        @RequestParam pseudo: String,
        @RequestParam email: String,
        @RequestParam motDePasse: String,
        @RequestParam confirmationMotDePasse: String,
        model: Model
    ): String {

        val pseudoPropre = pseudo.trim()
        val emailPropre = email.trim().lowercase()

        if (pseudoPropre.isBlank() || emailPropre.isBlank()) {
            model.addAttribute("erreur", "Tous les champs sont obligatoires.")
            return "fragments/inscription"
        }

        if (motDePasse.length < 8) {
            model.addAttribute("erreur", "Le mot de passe doit contenir au moins 8 caractères.")
            return "fragments/inscription"
        }

        if (motDePasse != confirmationMotDePasse) {
            model.addAttribute("erreur", "Les mots de passe ne correspondent pas.")
            return "fragments/inscription"
        }

        if (utilisateurDAO.findByEmail(emailPropre) != null) {
            model.addAttribute("erreur", "Cette adresse e-mail est déjà utilisée.")
            return "fragments/inscription"
        }

        // Récupérer le rôle utilisateur
        val roleUser = roleDAO.findAll()
            .firstOrNull { it.nomLogique == "ROLE_USER" }
            ?: throw IllegalStateException("Le rôle ROLE_USER n'existe pas.")

        // Créer le compte utilisateur
        val utilisateur = Utilisateur(
            pseudo = pseudoPropre,
            email = emailPropre,
            motDePasse = passwordEncoder.encode(motDePasse)!!,
            role = roleUser
        )

        utilisateurDAO.save(utilisateur)

        return "redirect:/connexion?inscription"
    }
}
