package it.uniroma3.siw_events.config;

import it.uniroma3.siw_events.service.CustomOidcUserService;
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
    private CustomOidcUserService customOidcUserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/eventi", "/eventi/{id}").permitAll()
                    .requestMatchers("/oauth2/**", "/login/oauth2/code/google").permitAll()
                    .requestMatchers("/login", "/login?error=true").permitAll()
                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/dashboard", "/eventi/{id}/partecipa", "/eventi/nuovo")
                        .hasAnyAuthority("USER", "ADMIN")
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2Login ->
                oauth2Login
                    .loginPage("/login")
                    .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint
                            .oidcUserService(customOidcUserService)
                    )
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error=true")
            )
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            );
        return http.build();
    }
}
