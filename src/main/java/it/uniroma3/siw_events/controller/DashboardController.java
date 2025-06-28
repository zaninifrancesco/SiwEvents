package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.service.EventService;
import it.uniroma3.siw_events.service.ImageService;
import it.uniroma3.siw_events.service.ParticipationService;
import it.uniroma3.siw_events.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            Optional<User> currentUserOptional = userService.getCurrentUser(principal);
            if (currentUserOptional.isPresent()) {
                User currentUser = currentUserOptional.get();
                model.addAttribute("user", currentUser);
                model.addAttribute("createdEvents", eventService.findEventsCreatedBy(currentUser));
                model.addAttribute("participations", participationService.getParticipationsByUser(currentUser));
            }
        }
        return "dashboard";
    }

    @PostMapping("/dashboard/profile-image")
    public String uploadProfileImage(@RequestParam("profileImage") MultipartFile profileImage,
                                     @AuthenticationPrincipal OAuth2User principal,
                                     RedirectAttributes redirectAttributes) {
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        if (currentUserOptional.isPresent() && profileImage != null && !profileImage.isEmpty()) {
            User user = currentUserOptional.get();
            // Elimina la vecchia immagine se esiste
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                imageService.deleteImage(user.getProfileImageUrl());
            }
            String imageUrl = imageService.saveProfileImage(profileImage);
            user.setProfileImageUrl(imageUrl);
            userService.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Foto profilo aggiornata!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nel caricamento della foto profilo.");
        }
        return "redirect:/dashboard";
    }
}
