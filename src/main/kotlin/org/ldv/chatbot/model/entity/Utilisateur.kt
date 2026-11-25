
package org.ldv.chatbot.model.entity

import jakarta.persistence.*

@Entity
class Utilisateur(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 100)
    var pseudo: String,

    @Column(nullable = false, unique = true, length = 150)
    var email: String,

    @Column(nullable = false, length = 100)
    var motDePasse: String,

    @Column(length = 500)
    var photoProfil: String? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    var role: Role,

    @OneToMany(
        mappedBy = "utilisateur",
        cascade = [CascadeType.REMOVE],
        orphanRemoval = true
    )
    var conversations: MutableList<Conversation> = mutableListOf()
)
