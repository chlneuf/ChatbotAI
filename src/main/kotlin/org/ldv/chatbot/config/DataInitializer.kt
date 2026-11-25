
package org.ldv.chatbot.config

import org.ldv.chatbot.model.dao.AIProviderDAO
import org.ldv.chatbot.model.dao.AIModelDAO
import org.ldv.chatbot.model.dao.RoleDAO
import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.AIModel
import org.ldv.chatbot.model.entity.AIProvider
import org.ldv.chatbot.model.entity.Role
import org.ldv.chatbot.model.entity.Utilisateur
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    private val fournisseurIADAO: AIProviderDAO,
    private val modeleIADAO: AIModelDAO,
    private val roleDAO: RoleDAO,
    private val utilisateurDAO: UtilisateurDAO,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun initData(): CommandLineRunner = CommandLineRunner {

        // FOURNISSEURS IA
        val mistral = fournisseurIADAO.findAll()
            .firstOrNull { it.name == "Mistral" }
            ?: fournisseurIADAO.save(AIProvider(name = "Mistral"))

        val gemini = fournisseurIADAO.findAll()
            .firstOrNull { it.name == "Gemini" }
            ?: fournisseurIADAO.save(AIProvider(name = "Gemini"))

        // MODÈLES IA
        val modeles = listOf(
            AIModel(
                name = "gemini-3.8-flash",
                urlApi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent",
                descriptor = "Modèle rapide et polyvalent.",
                provider = gemini
            ),
            AIModel(
                name = "gemini-3.5-flash-lite",
                urlApi = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent",
                descriptor = "Modèle rapide pour les tâches simples.",
                provider = gemini
            ),
            AIModel(
                name = "mistral-small-latest",
                urlApi = "https://api.mistral.ai/v1/chat/completions",
                descriptor = "Modèle léger pour les conversations et les tâches courantes.",
                provider = mistral
            ),
            AIModel(
                name = "mistral-medium-latest",
                urlApi = "https://api.mistral.ai/v1/chat/completions",
                descriptor = "Modèle plus avancé pour les tâches complexes.",
                provider = mistral
            )
        )

        for (modele in modeles) {
            val existe = modeleIADAO.findAll()
                .any { it.name == modele.name }

            if (!existe) {
                modeleIADAO.save(modele)
            }
        }

        // RÔLES
        val roleUser = roleDAO.findAll()
            .firstOrNull { it.nomLogique == "ROLE_USER" }
            ?: roleDAO.save(
                Role(
                    nom = "Utilisateur",
                    nomLogique = "ROLE_USER"
                )
            )

        val roleAdmin = roleDAO.findAll()
            .firstOrNull { it.nomLogique == "ROLE_ADMIN" }
            ?: roleDAO.save(
                Role(
                    nom = "Administrateur",
                    nomLogique = "ROLE_ADMIN"
                )
            )

        // COMPTE ADMINISTRATEUR
        val adminEmail = "admin@church.local"

        val adminExistant = utilisateurDAO.findByEmail(adminEmail)

        if (adminExistant == null) {
            utilisateurDAO.save(
                Utilisateur(
                    pseudo = "Administrateur",
                    email = adminEmail,
                    motDePasse = passwordEncoder.encode("AdminChurch123!")!!,
                    role = roleAdmin
                )
            )

            println("Compte administrateur créé.")
        } else {
            adminExistant.motDePasse =
                passwordEncoder.encode("AdminChurch123!")!!

            adminExistant.role = roleAdmin

            utilisateurDAO.save(adminExistant)

            println("Compte administrateur mis à jour.")
        }
    }
}

