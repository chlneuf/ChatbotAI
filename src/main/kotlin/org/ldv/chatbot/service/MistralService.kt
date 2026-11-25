
package org.ldv.chatbot.service

import org.ldv.chatbot.dto.mistral.MistralChatRequest
import org.ldv.chatbot.dto.mistral.MistralChatResponse
import org.ldv.chatbot.dto.mistral.MistralMessage
import org.ldv.chatbot.model.entity.AIModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class MistralService(
    private val restClientBuilder: RestClient.Builder,

    @Value("\${mistral.api.key}")
    private val apiKey: String

) : IAService {

    override fun getFournisseur(): String = "Mistral"

    override fun envoyerPrompt(prompt: String, modele: AIModel): String {

        // Date actuelle en France
        val dateActuelle = LocalDate.now(ZoneId.of("Europe/Paris"))
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        // Contexte temporel transmis à Mistral
        val promptAvecContexte = """
            CONTEXTE TEMPOREL IMPORTANT :
            Nous sommes actuellement le $dateActuelle.
            Utilise cette date comme référence pour toutes les questions
            concernant la date actuelle, l'année et les événements récents.
            Ne prétends pas être à une date antérieure.
            Si tu ne connais pas une information récente, précise-le
            au lieu de l'inventer.

            MESSAGE DE L'UTILISATEUR :
            $prompt
        """.trimIndent()

        val message = MistralMessage(
            role = "user",
            content = promptAvecContexte
        )

        val request = MistralChatRequest(
            model = modele.name,
            messages = listOf(message)
        )

        return try {
            val response = restClientBuilder.build()
                .post()
                .uri(modele.urlApi)
                .header("Authorization", "Bearer $apiKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MistralChatResponse::class.java)

            response?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?: "Aucune réponse reçue de Mistral."

        } catch (e: Exception) {
            "Erreur lors de l'appel à Mistral : ${e.message}"
        }
    }
}
