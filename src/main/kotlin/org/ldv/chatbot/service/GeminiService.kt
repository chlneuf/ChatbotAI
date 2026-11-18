
package org.ldv.chatbot.service

import org.ldv.chatbot.dto.gemini.GeminiChatRequest
import org.ldv.chatbot.dto.gemini.GeminiChatResponse
import org.ldv.chatbot.dto.gemini.GeminiContent
import org.ldv.chatbot.dto.gemini.GeminiPart
import org.ldv.chatbot.model.entity.AIModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class GeminiService(
    private val restClientBuilder: RestClient.Builder,
    @Value("\${gemini.api.key}") private val apiKey: String
) : IAService {

    override fun getFournisseur(): String = "Gemini"

    override fun envoyerPrompt(prompt: String, modele: AIModel): String {

        val request = GeminiChatRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        return try {
            val response = restClientBuilder.build()
                .post()
                .uri(modele.urlApi)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GeminiChatResponse::class.java)

            response?.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: "Aucune réponse reçue de Gemini."

        } catch (e: Exception) {
            "Erreur lors de l'appel à Gemini : ${e.message}"
        }
    }
}
