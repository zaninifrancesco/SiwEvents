package it.uniroma3.siw_events.service;

import it.uniroma3.siw_events.model.Role;
import it.uniroma3.siw_events.model.RoleName;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.repository.RoleRepository;
import it.uniroma3.siw_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// Aggiungi queste importazioni per il logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    // Aggiungi il logger
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getName(); // L'ID univoco fornito da Google (es. 'sub')

        Optional<User> userOptional = userRepository.findByGoogleId(googleId);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Aggiorna il nome se è cambiato
            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
            }
            // Assicurati che l'email sia aggiornata se necessario
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
            }
            // Aggiungi log per l'aggiornamento dell'utente
            logger.info("Updating existing user: {}", email);
        } else {
            // Prova a cercare per email, nel caso l'utente esista già ma non abbia ancora un googleId
            Optional<User> userByEmailOptional = userRepository.findByEmail(email);
            if (userByEmailOptional.isPresent()) {
                user = userByEmailOptional.get();
                user.setGoogleId(googleId); // Collega l'account esistente a Google
                if (name != null && !name.equals(user.getName())) {
                    user.setName(name);
                }
            } else {
                // Crea un nuovo utente
                user = new User();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setName(name);
                user.setCreatedAt(LocalDateTime.now());
                logger.info("Creating new user: Email={}, Name={}, GoogleId={}", email, name, googleId);

                // Assegna il ruolo ROLE_USER di default
                Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                        .orElseGet(() -> {
                            logger.info("Role ROLE_USER not found, creating it.");
                            Role newRole = new Role(RoleName.ROLE_USER);
                            return roleRepository.save(newRole);
                        });
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);
                logger.info("Assigned role {} to new user {}", userRole.getName(), email);
            }
        }

        // Log prima del salvataggio
        logger.info("Attempting to save user: Email={}, Name={}, GoogleId={}", user.getEmail(), user.getName(), user.getGoogleId());
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> logger.info("User role before save: {}", role.getName()));
        } else {
            logger.warn("User roles collection is null before save for user: {}", user.getEmail());
        }

        userRepository.save(user);
        logger.info("User saved (or attempted to save). Checking roles post-save for user: {}", user.getEmail());

        // È importante recuperare l'utente dal database per assicurarsi che i ruoli siano caricati correttamente,
        // specialmente se la sessione di Hibernate è stata chiusa o se ci sono problemi con il refresh dell'entità.
        // Tuttavia, con @Transactional, l'entità 'user' dovrebbe essere gestita e aggiornata.
        // Aggiungiamo un log per verificare i ruoli direttamente dall'oggetto 'user' dopo il save.
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            logger.warn("User roles collection is null or empty AFTER save for user: {}", user.getEmail());
        } else {
            user.getRoles().forEach(role -> logger.info("User role after save (from user object): {}", role.getName()));
        }


        // Crea un set di GrantedAuthority dai ruoli dell'utente
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    logger.info("Mapping role {} to authority {} for user {}", role.getName(), role.getName().toString(), user.getEmail());
                    return new SimpleGrantedAuthority(role.getName().toString());
                })
                .collect(Collectors.toSet());
        logger.info("Created authorities for user {}: {}", user.getEmail(), authorities);

        if (authorities.isEmpty()) {
            logger.warn("CRITICAL: No authorities generated for user {}!", user.getEmail());
        } else if (authorities.stream().noneMatch(auth -> auth.getAuthority().equals(RoleName.ROLE_USER.toString()))) {
            logger.warn("CRITICAL: ROLE_USER authority is MISSING for user {}! Authorities: {}", user.getEmail(), authorities);
        }


        // Ottieni la chiave dell'attributo usata per il nome/ID principale dell'utente
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // Ritorna un nuovo DefaultOAuth2User con le autorità corrette e gli attributi originali
        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), userNameAttributeName);
    }
}