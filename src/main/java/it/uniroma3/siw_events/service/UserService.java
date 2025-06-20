package it.uniroma3.siw_events.service;

import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> getUserByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser(OAuth2User principal) {
        if (principal == null) {
            return Optional.empty();
        }
        String googleId = principal.getName(); // Per Google, getName() di solito restituisce l'ID 'sub'
        return userRepository.findByGoogleId(googleId);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    // Altri metodi relativi all'utente possono essere aggiunti qui
}
