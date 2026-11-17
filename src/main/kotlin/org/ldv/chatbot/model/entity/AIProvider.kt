package org.ldv.chatbot.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class AIProvider (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,
    var name: String,

    @OneToMany(mappedBy = "provider", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var models: MutableList<AIModel> = mutableListOf(),


)