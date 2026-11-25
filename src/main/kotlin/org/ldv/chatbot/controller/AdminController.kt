
package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AdminController(
    private val utilisateurDAO: UtilisateurDAO
) {

    @GetMapping
    fun afficherDashboard(model: Model): String {

        val utilisateurs = utilisateurDAO.findAll()

        model.addAttribute("utilisateurs", utilisateurs)
        model.addAttribute("nombreUtilisateurs", utilisateurs.size)

        return "fragments/admin"
    }
}
