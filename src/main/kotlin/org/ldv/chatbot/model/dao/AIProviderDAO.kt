package org.ldv.chatbot.model.dao

import org.ldv.chatbot.model.entity.AIProvider

import org.springframework.data.jpa.repository.JpaRepository

interface AIProviderDAO : JpaRepository<AIProvider, Long> {
}