package org.ldv.chatbot.config

import org.ldv.chatbot.model.dao.AIModelDAO
import org.ldv.chatbot.model.dao.AIProviderDAO
import org.ldv.chatbot.model.entity.AIModel
import org.ldv.chatbot.model.entity.AIProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer(
    private val fourniseurIADAO: AIProviderDAO,
    private val modeleIADAO: AIModelDAO
) {

    @Bean
    fun initData() = CommandLineRunner {

        // Vérifier si le fournisseur Mistral existe déjà
        val mistral = fourniseurIADAO.findAll()
            .firstOrNull { it.name == "Mistral" }
            ?: fourniseurIADAO.save(
                AIProvider(name = "Mistral"),

                )

        val gemini = fourniseurIADAO.findAll()
            .firstOrNull { it.name == "Gemini" }
            ?: fourniseurIADAO.save(
                AIProvider(name = "Gemini"),

                )


        // Modèles à initialiser
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
            ),

            )
        // Éviter de recréer les modèles à chaque démarrage
        for (modele in modeles) {
            val existe = modeleIADAO.findAll()
                .any { it.name == modele.name }

            if (!existe) {
                modeleIADAO.save(modele)
            }
        }
    }
}
