package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.model.Event;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("events", eventService.findAllEvents());
        return "home"; // Nome del template Thymeleaf (home.html)
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
            return "event_detail"; // Nome del template Thymeleaf (event_detail.html)
        } else {
            return "redirect:/"; // Evento non trovato, reindirizza alla home
        }
    }

    @PostMapping("/eventi/{id}/partecipa")
    public String joinEvent(@PathVariable("id") Long eventId, @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/oauth2/authorization/google"; // Se non autenticato, rimanda al login
        }
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
        if (principal == null) {
            // Dovrebbe essere già gestito da Spring Security, ma è una doppia sicurezza
            return "redirect:/";
        }
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
        model.addAttribute("eventForm", new Event()); // Usa l'entità Event come form-backing object
        return "event_form";
    }

    @PostMapping("/eventi/nuovo")
    public String createEvent(@ModelAttribute("eventForm") Event event,
                              @AuthenticationPrincipal OAuth2User principal,
                              RedirectAttributes redirectAttributes, Model model) {
        if (principal == null) {
            return "redirect:/oauth2/authorization/google";
        }

        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        if (currentUserOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utente non trovato. Impossibile creare l'evento.");
            return "redirect:/eventi/nuovo";
        }

        // Validazione di base (può essere espansa con @Valid e Bean Validation)
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Il titolo è obbligatorio.");
            // Ritorna l'oggetto eventForm per preservare i dati inseriti
            model.addAttribute("eventForm", event);
            return "event_form";
        }
        if (event.getDateTime() == null) {
            model.addAttribute("errorMessage", "La data e l'ora sono obbligatorie.");
            model.addAttribute("eventForm", event);
            return "event_form";
        }
        if (event.getDateTime().isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMessage", "La data dell'evento non può essere nel passato.");
            model.addAttribute("eventForm", event);
            return "event_form";
        }

        try {
            eventService.createEvent(event, currentUserOptional.get());
            redirectAttributes.addFlashAttribute("successMessage", "Evento creato con successo!");
            return "redirect:/eventi/" + event.getId(); // Reindirizza alla pagina del nuovo evento
        } catch (Exception e) {
            // Log dell'eccezione e messaggio di errore più generico
            // logger.error("Errore durante la creazione dell'evento", e);
            model.addAttribute("errorMessage", "Errore durante la creazione dell'evento: " + e.getMessage());
            model.addAttribute("eventForm", event);
            return "event_form";
        }
    }

    @GetMapping("/eventi/miei")
    public String showMyEvents(@AuthenticationPrincipal OAuth2User principal, Model model, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            // L'utente non è autenticato, dovrebbe essere gestito da Spring Security,
            // ma per sicurezza reindirizziamo al login.
            return "redirect:/oauth2/authorization/google";
        }

        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        if (currentUserOptional.isEmpty()) {
            // Questo caso non dovrebbe accadere se l'utente è autenticato tramite OAuth2
            // e CustomOAuth2UserService funziona correttamente.
            redirectAttributes.addFlashAttribute("errorMessage", "Utente non trovato.");
            return "redirect:/dashboard";
        }

        User currentUser = currentUserOptional.get();
        model.addAttribute("events", eventService.findEventsCreatedBy(currentUser));
        model.addAttribute("pageTitle", "I Miei Eventi Creati");
        return "my_events"; // Nome del template Thymeleaf (my_events.html)
    }

    // Altri metodi per la gestione degli eventi (creazione, ecc.) verranno aggiunti qui
}
