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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    public String showHomePage(@RequestParam(value = "date", required = false) String date,
                               Model model) {
        List<Event> events = eventService.findAllEvents();
        // Ordina per data dell'evento (dal più vicino al più lontano)
        events.sort((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()));
        // Eventi in evidenza: primi 3 con più partecipanti
        List<Event> featuredEvents = events.stream()
            .sorted((e1, e2) -> Integer.compare(
                e2.getParticipants() != null ? e2.getParticipants().size() : 0,
                e1.getParticipants() != null ? e1.getParticipants().size() : 0))
            .limit(3)
            .toList();
        model.addAttribute("events", events);
        model.addAttribute("featuredEvents", featuredEvents);
        return "home";
    }

    @GetMapping("/eventi/{id}")
    public String showEventDetail(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Optional<Event> eventOptional = eventService.findEventById(id);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            model.addAttribute("event", event);
            model.addAttribute("currentParticipants", participationService.countByEvent(event));

            boolean isUserParticipating = false;
            if (principal != null) {
                Optional<User> currentUserOptional = userService.getCurrentUser(principal);
                if (currentUserOptional.isPresent()) {
                    User currentUser = currentUserOptional.get();
                    model.addAttribute("currentUser", currentUser);
                    isUserParticipating = participationService.isUserParticipating(currentUser, event);
                }
            }
            model.addAttribute("isUserParticipating", isUserParticipating);
            // Google Calendar URL solo se partecipa
            if (isUserParticipating) {
                String title = URLEncoder.encode(event.getTitle(), StandardCharsets.UTF_8);
                String description = URLEncoder.encode(event.getDescription(), StandardCharsets.UTF_8);
                String location = URLEncoder.encode(event.getLocation(), StandardCharsets.UTF_8);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
                String start = event.getDateTime().format(fmt);
                String end = event.getDateTime().plusHours(4).format(fmt);
                String gcalUrl = "https://www.google.com/calendar/render?action=TEMPLATE"
                        + "&text=" + title
                        + "&dates=" + start + "/" + end
                        + "&details=" + description
                        + "&location=" + location;
                model.addAttribute("googleCalendarUrl", gcalUrl);
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
                event.setDateTime(LocalDateTime.now()); 
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
    
    @GetMapping("/eventi/{id}/modifica")
    public String showEditEventForm(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Optional<Event> eventOptional = eventService.findEventById(id);
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);

        if (eventOptional.isPresent() && currentUserOptional.isPresent()) {
            Event event = eventOptional.get();
            User currentUser = currentUserOptional.get();

            if (event.getCreatedBy().getId().equals(currentUser.getId())) {
                model.addAttribute("event", event);
                return "event_form_edit";
            } else {
                return "redirect:/eventi/" + id;
            }
        }
        return "redirect:/";
    }

    @PostMapping("/eventi/{id}/modifica")
    public String updateEvent(@PathVariable("id") Long id, @ModelAttribute("event") Event eventDetails,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {

        Optional<Event> eventOptional = eventService.findEventById(id);
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);

        if (eventOptional.isPresent() && currentUserOptional.isPresent()) {
            Event existingEvent = eventOptional.get();
            User currentUser = currentUserOptional.get();

            if (existingEvent.getCreatedBy().getId().equals(currentUser.getId())) {
                try {
                    if (imageFile != null && !imageFile.isEmpty()) {
                        String imageUrl = imageService.saveImage(imageFile);
                        eventDetails.setEventImageUrl(imageUrl);
                    } else {
                        eventDetails.setEventImageUrl(existingEvent.getEventImageUrl());
                    }
                    eventService.updateEvent(id, eventDetails);
                    redirectAttributes.addFlashAttribute("successMessage", "Evento aggiornato con successo!");
                    return "redirect:/eventi/" + id;
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Errore durante l'aggiornamento: " + e.getMessage());
                    return "redirect:/eventi/" + id + "/modifica";
                }
            } else {
                return "redirect:/eventi/" + id;
            }
        }
        return "redirect:/";
    }

    @PostMapping("/eventi/{id}/elimina")
    public String deleteEvent(@PathVariable("id") Long id, @AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOptional = eventService.findEventById(id);
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);

        if (eventOptional.isPresent() && currentUserOptional.isPresent()) {
            Event event = eventOptional.get();
            User currentUser = currentUserOptional.get();

            if (event.getCreatedBy().getId().equals(currentUser.getId())) {
                eventService.deleteEvent(id);
                redirectAttributes.addFlashAttribute("successMessage", "Evento eliminato con successo!");
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Non sei autorizzato ad eliminare questo evento.");
                return "redirect:/eventi/" + id;
            }
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Evento non trovato.");
        return "redirect:/";
    }

    @PostMapping("/eventi/{eventId}/rimuovi-partecipante/{userId}")
    public String removeParticipant(@PathVariable("eventId") Long eventId,
                                    @PathVariable("userId") Long userId,
                                    @AuthenticationPrincipal OAuth2User principal,
                                    RedirectAttributes redirectAttributes) {
        Optional<Event> eventOptional = eventService.findEventById(eventId);
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        if (eventOptional.isPresent() && currentUserOptional.isPresent()) {
            Event event = eventOptional.get();
            User currentUser = currentUserOptional.get();
            if (event.getCreatedBy().getId().equals(currentUser.getId())) {
                participationService.removeParticipantFromEvent(userId, eventId);
                redirectAttributes.addFlashAttribute("successMessage", "Partecipante rimosso con successo.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Non sei autorizzato a rimuovere partecipanti da questo evento.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Evento o utente non trovato.");
        }
        return "redirect:/eventi/" + eventId;
    }
}
