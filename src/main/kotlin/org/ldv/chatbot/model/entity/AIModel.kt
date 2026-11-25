
package org.ldv.chatbot.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class AIModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    var name: String,
    var urlApi: String,
    var descriptor: String,

    @ManyToOne
    @JoinColumn(name = "provider_id")
    var provider: AIProvider,

    @OneToMany(mappedBy = "model")
    var conversations: MutableList<Conversation> = mutableListOf()
)
