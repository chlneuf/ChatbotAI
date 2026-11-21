
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.UtilisateurDAO
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
    private val passwordEncoder: PasswordEncoder
) {

    // Afficher la page d'inscription
    @GetMapping("/inscription")
    fun afficherInscription(): String {
        return "fragments/inscription"
    }

    // Afficher la page de connexion
    @GetMapping("/connexion")
    fun afficherConnexion(): String {
        return "fragments/connexion"
    }

    // Inscrire un nouvel utilisateur
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

        // Vérifier les champs obligatoires
        if (pseudoPropre.isBlank() || emailPropre.isBlank()) {
            model.addAttribute(
                "erreur",
                "Tous les champs sont obligatoires."
            )
            return "fragments/inscription"
        }

        // Vérifier la longueur du mot de passe
        if (motDePasse.length < 8) {
            model.addAttribute(
                "erreur",
                "Le mot de passe doit contenir au moins 8 caractères."
            )
            return "fragments/inscription"
        }

        // Vérifier la confirmation du mot de passe
        if (motDePasse != confirmationMotDePasse) {
            model.addAttribute(
                "erreur",
                "Les mots de passe ne correspondent pas."
            )
            return "fragments/inscription"
        }

        // Vérifier si l'adresse e-mail existe déjà
        if (utilisateurDAO.findByEmail(emailPropre) != null) {
            model.addAttribute(
                "erreur",
                "Cette adresse e-mail est déjà utilisée."
            )
            return "fragments/inscription"
        }

        // Créer le nouvel utilisateur
        val utilisateur = Utilisateur(
            pseudo = pseudoPropre,
            email = emailPropre,
            motDePasse = passwordEncoder.encode(motDePasse)
        )

        // Enregistrer l'utilisateur
        utilisateurDAO.save(utilisateur)

        // Rediriger vers la connexion
        return "redirect:/connexion?inscription"
    }
}
