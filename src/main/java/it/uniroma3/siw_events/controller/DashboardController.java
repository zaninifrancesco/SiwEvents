package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.service.EventService;
import it.uniroma3.siw_events.service.ParticipationService;
import it.uniroma3.siw_events.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ParticipationService participationService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));

            Optional<User> currentUserOptional = userService.getCurrentUser(principal);
            if (currentUserOptional.isPresent()) {
                User currentUser = currentUserOptional.get();
                model.addAttribute("createdEvents", eventService.findEventsCreatedBy(currentUser));
                model.addAttribute("participations", participationService.getParticipationsByUser(currentUser));
            }
        }
        return "dashboard";
    }
}
