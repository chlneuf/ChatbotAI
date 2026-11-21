
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

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationProvider(
        utilisateurDetailsService: UtilisateurDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        val provider = DaoAuthenticationProvider(utilisateurDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationProvider: AuthenticationProvider
    ): SecurityFilterChain {
        http
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/inscription",
                    "/connexion",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/vendor/**"
                ).permitAll()

                it.anyRequest().authenticated()
            }
            .formLogin {
                it.loginPage("/connexion")
                    .loginProcessingUrl("/connexion")
                    .usernameParameter("email")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            }
            .logout {
                it.logoutUrl("/logout")
                    .logoutSuccessUrl("/connexion?deconnexion")
                    .permitAll()
            }

        return http.build()
    }
}
