
package org.ldv.chatbot

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

@SpringBootTest
@AutoConfigureMockMvc
class CsrfSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `une requete POST sans jeton CSRF doit etre refusee`() {
        mockMvc.perform(
            post("/conversations")
                .with(user("test@church.local").roles("USER"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `une requete POST avec jeton CSRF passe le controle CSRF`() {
        mockMvc.perform(
            post("/conversations")
                .with(user("test@church.local").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
            .andExpect { result ->
                assert(result.response.status != 403) {
                    "La requête avec un jeton CSRF valide a été refusée"
                }
            }
    }
}
