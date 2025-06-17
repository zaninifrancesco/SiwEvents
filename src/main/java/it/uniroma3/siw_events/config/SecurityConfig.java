package it.uniroma3.siw_events.config;

import it.uniroma3.siw_events.model.RoleName;
import it.uniroma3.siw_events.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Risorse statiche e pagine pubbliche
                    .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/eventi", "/eventi/{id}").permitAll()
                    .requestMatchers("/oauth2/**", "/login/oauth2/code/google").permitAll() // Permetti accesso agli endpoint OAuth2

                    // Rotte per utenti autenticati (USER o ADMIN)
                    .requestMatchers("/dashboard", "/eventi/{id}/partecipa", "/eventi/nuovo", "/eventi/miei").hasAnyAuthority(RoleName.ROLE_USER.toString(), RoleName.ROLE_ADMIN.toString())

                    // Rotte per amministratori (ADMIN)
                    .requestMatchers("/admin/**").hasAuthority(RoleName.ROLE_ADMIN.toString())

                    .anyRequest().authenticated() // Tutte le altre richieste necessitano di autenticazione
            )
            .oauth2Login(oauth2Login ->
                oauth2Login
                    .loginPage("/login") // Pagina di login personalizzata (opzionale)
                    .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint
                            .userService(customOAuth2UserService) // Servizio personalizzato
                    )
                    .defaultSuccessUrl("/dashboard", true) // Reindirizza al dashboard dopo il login
                    .failureUrl("/login?error=true") // Pagina in caso di errore di login
            )
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/") // Reindirizza alla home page dopo il logout
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            );
        return http.build();
    }
}
