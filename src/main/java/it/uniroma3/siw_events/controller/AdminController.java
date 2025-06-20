package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.service.EventService;
import it.uniroma3.siw_events.service.ParticipationService;
import it.uniroma3.siw_events.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService; // Aggiunto per la gestione eventi da admin

    @Autowired
    private ParticipationService participationService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null || userService.getCurrentUser(principal).isEmpty() ||
            userService.getCurrentUser(principal).get().getRuolo() == null ||
            !userService.getCurrentUser(principal).get().getRuolo().name().equals("ADMIN")) {
            return "redirect:/";
        }
        model.addAttribute("pageTitle", "Dashboard Amministrazione");
        model.addAttribute("eventCount", eventService.findAllEvents().size());
        model.addAttribute("userCount", userService.findAllUsers().size());
        return "admin/admin_dashboard";
    }

    @GetMapping("/utenti")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("pageTitle", "Gestione Utenti");
        return "admin/manage_users";
    }

    @GetMapping("/eventi")
    public String manageEvents(Model model) {
        model.addAttribute("events", eventService.findAllEvents());
        model.addAttribute("pageTitle", "Gestione Eventi");
        return "admin/manage_events"; // Dovrai creare questo template
    }

    @PostMapping("/eventi/elimina/{id}")
    public String deleteEventAsAdmin(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Evento eliminato con successo!");
        return "redirect:/admin/eventi";
    }

    @PostMapping("/utenti/elimina/{id}")
    public String deleteUserAsAdmin(@PathVariable("id") Long id, @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        // Impedisci l'auto-eliminazione
        if (principal != null && userService.getCurrentUser(principal).isPresent()) {
            Long currentUserId = userService.getCurrentUser(principal).get().getId();
            if (currentUserId.equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Non puoi eliminare te stesso!");
                return "redirect:/admin/utenti";
            }
        }
        userService.deleteUserById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Utente eliminato con successo!");
        return "redirect:/admin/utenti";
    }

    @PostMapping("/eventi/{eventId}/rimuovi-partecipazione/{userId}")
    public String removeUserParticipation(@PathVariable("eventId") Long eventId, @PathVariable("userId") Long userId, RedirectAttributes redirectAttributes) {
        participationService.removeParticipantFromEvent(userId, eventId);
        redirectAttributes.addFlashAttribute("successMessage", "Partecipazione rimossa con successo!");
        return "redirect:/admin/eventi";
    }

    // TODO: Aggiungere metodi per creare/modificare/eliminare eventi da admin
    // TODO: Aggiungere metodi per modificare ruoli utenti
}
