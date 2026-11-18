package org.ldv.chatbot.service

import org.ldv.chatbot.model.entity.AIModel

interface IAService {

    // Identifie le fournisseur
    fun getFournisseur(): String

    // Envoie un prompt au modèle choisi
    fun envoyerPrompt(
        prompt: String,
        modele: AIModel
    ): String
}


