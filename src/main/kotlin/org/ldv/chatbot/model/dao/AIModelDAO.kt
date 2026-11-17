package org.ldv.chatbot.model.dao

import org.ldv.chatbot.model.entity.AIModel
import org.springframework.data.jpa.repository.JpaRepository

interface AIModelDAO : JpaRepository<AIModel, Long> {
}