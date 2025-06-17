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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

                // Assegna il ruolo ROLE_USER di default
                Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                        .orElseGet(() -> {
                            Role newRole = new Role(RoleName.ROLE_USER);
                            return roleRepository.save(newRole);
                        });
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);
            }
        }
        userRepository.save(user);

        // Crea un set di GrantedAuthority dai ruoli dell'utente
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
                .collect(Collectors.toSet());

        // Ottieni la chiave dell'attributo usata per il nome/ID principale dell'utente
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // Ritorna un nuovo DefaultOAuth2User con le autorità corrette e gli attributi originali
        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), userNameAttributeName);
    }
}
