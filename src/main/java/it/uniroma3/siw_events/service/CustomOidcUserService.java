package it.uniroma3.siw_events.service;

import it.uniroma3.siw_events.model.RoleName;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        
        Map<String, Object> attributes = oidcUser.getAttributes();
        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Cerca l'utente esistente o creane uno nuovo
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleId(googleId);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRuolo(RoleName.USER); // Ruolo di default
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // Crea le authorities basate sul ruolo dell'utente
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRuolo().name());
        
        return new DefaultOidcUser(
                Collections.singleton(authority),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub" // Nome dell'attributo che identifica l'utente
        );
    }
}
