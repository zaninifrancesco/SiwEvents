package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.model.Event;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("events", eventService.findAllEvents());
        return "home";
    }

    @GetMapping("/eventi/{id}")
    public String showEventDetail(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Optional<Event> eventOptional = eventService.findEventById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            model.addAttribute("event", event);
            model.addAttribute("currentParticipants", participationService.countByEvent(event));

            if (principal != null) {
                Optional<User> currentUserOptional = userService.getCurrentUser(principal);
                if (currentUserOptional.isPresent()) {
                    model.addAttribute("isUserParticipating", participationService.isUserParticipating(currentUserOptional.get(), event));
                }
            }
            return "event_detail";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/eventi/{id}/partecipa")
    public String joinEvent(@PathVariable("id") Long eventId, @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        Optional<Event> eventOptional = eventService.findEventById(eventId);

        if (currentUserOptional.isPresent() && eventOptional.isPresent()) {
            User user = currentUserOptional.get();
            Event event = eventOptional.get();
            try {
                participationService.joinEvent(user, event);
                redirectAttributes.addFlashAttribute("successMessage", "Partecipazione confermata!");
            } catch (IllegalStateException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Utente o evento non trovato.");
        }
        return "redirect:/eventi/" + eventId;
    }

    @PostMapping("/eventi/{id}/annulla-partecipazione")
    public String leaveEvent(@PathVariable("id") Long eventId, @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        Optional<Event> eventOptional = eventService.findEventById(eventId);

        if (currentUserOptional.isPresent() && eventOptional.isPresent()) {
            User user = currentUserOptional.get();
            Event event = eventOptional.get();
            try {
                participationService.leaveEvent(user, event);
                redirectAttributes.addFlashAttribute("successMessage", "Partecipazione annullata.");
            } catch (IllegalStateException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Utente o evento non trovato.");
        }
        return "redirect:/eventi/" + eventId;
    }

    @GetMapping("/eventi/nuovo")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "event_form";
    }

    @PostMapping("/eventi/nuovo")
    public String createEvent(@ModelAttribute Event event, @RequestParam("imageFile") MultipartFile imageFile, @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        if (currentUserOptional.isPresent()) {
            User currentUser = currentUserOptional.get();
            // Imposta la data e l'ora correnti se non sono state fornite dal form
            if (event.getDateTime() == null) {
                event.setDateTime(LocalDateTime.now()); // O gestisci come errore se deve essere impostato dall'utente
            }
            if (!imageFile.isEmpty()) {
                String imageUrl = imageService.saveImage(imageFile);
                event.setEventImageUrl(imageUrl);
            }
            eventService.createEvent(event, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Evento creato con successo!");
            return "redirect:/eventi/" + event.getId();
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi essere loggato per creare un evento.");
            return "redirect:/login";
        }
    }
    
    // TODO: Aggiungere metodi per modificare ed eliminare eventi con controlli di autorizzazione
}
