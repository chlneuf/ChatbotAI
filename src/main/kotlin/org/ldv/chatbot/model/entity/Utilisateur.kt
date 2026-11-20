package org.ldv.chatbot.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Utilisateur(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 100)
    var pseudo: String,

    @Column(nullable = false, unique = true, length = 150)
    var email: String,

    @Column(length = 500)
    var photoProfil: String? = null
)
