
package org.ldv.chatbot.config

import org.ldv.chatbot.service.UtilisateurDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    // Encodage des mots de passe avec BCrypt
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // Vérification des identifiants à partir de la base de données
    @Bean
    fun authenticationProvider(
        utilisateurDetailsService: UtilisateurDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        val provider = DaoAuthenticationProvider(utilisateurDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    // Configuration générale de la sécurité
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationProvider: AuthenticationProvider
    ): SecurityFilterChain {

        http
            .authenticationProvider(authenticationProvider)

            // Gestion des autorisations
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/inscription",
                    "/connexion",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/vendor/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()

                // Accès réservé aux administrateurs
                it.requestMatchers("/admin/**").hasRole("ADMIN")

                // Toutes les autres pages nécessitent une authentification
                it.anyRequest().authenticated()
            }

            // Gestion de la connexion
            .formLogin {
                it.loginPage("/connexion")
                    .loginProcessingUrl("/connexion")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/connexion?erreur")
                    .permitAll()
            }

            // Gestion de la déconnexion
            .logout {
                it.logoutUrl("/logout")
                    .logoutSuccessUrl("/connexion?deconnexion")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            }

        return http.build()
    }
}
