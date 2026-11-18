package org.ldv.chatbot.service

import org.ldv.chatbot.model.entity.AIModel

import org.springframework.stereotype.Component

@Component
class IAServiceFactory(
    private val services: List<IAService>
) {

    fun getService(modele: AIModel): IAService {

        val fournisseur = modele.provider?.name
            ?: throw IllegalArgumentException(
                "Aucun fournisseur associé au modèle"
            )

        return services.firstOrNull {
            it.getFournisseur().equals(
                fournisseur,
                ignoreCase = true
            )
        } ?: throw IllegalArgumentException(
            "Aucun service disponible pour $fournisseur"
        )
    }
}
