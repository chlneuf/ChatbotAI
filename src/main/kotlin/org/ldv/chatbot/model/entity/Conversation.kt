
package org.ldv.chatbot.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class Conversation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    var title: String,

    // Permet d'épingler une conversation
    @Column(nullable = false)
    var pinned: Boolean = false,

    var createdAt: LocalDateTime = LocalDateTime.now(),

    var dateDerniereModification: LocalDateTime = LocalDateTime.now(),

    @ManyToOne
    @JoinColumn(name = "aimodel_id")
    var model: AIModel,

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    var utilisateur: Utilisateur,

    @OneToMany(
        mappedBy = "conversation",
        cascade = [CascadeType.REMOVE],
        orphanRemoval = true
    )
    var messages: MutableList<Message> = mutableListOf()
)
