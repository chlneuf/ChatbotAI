
package org.ldv.chatbot.model.dao

import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.AIModel
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationDAO : JpaRepository<Conversation, Long> {

    fun findAllByUtilisateur_IdOrderByDateDerniereModificationDesc(
        utilisateurId: Long
    ): List<Conversation>

    fun findByIdAndUtilisateur_Id(
        id: Long,
        utilisateurId: Long
    ): Conversation?

    fun existsByModel(model: AIModel): Boolean
}
