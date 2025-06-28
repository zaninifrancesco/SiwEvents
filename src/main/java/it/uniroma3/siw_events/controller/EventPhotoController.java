package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.EventPhoto;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/event-photos")
public class EventPhotoController {

    private final CustomOidcUserService customOidcUserService;

    @Autowired
    private EventPhotoService eventPhotoService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private ImageService imageService;

    EventPhotoController(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    // Galleria pubblica delle foto di un evento
    @GetMapping("/event/{eventId}")
    public String viewEventPhotos(@PathVariable Long eventId, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Optional<Event> eventOpt = eventService.findEventById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/?error=event_not_found";
        }
        Event event = eventOpt.get();
        List<EventPhoto> photos = eventPhotoService.getPhotosByEvent(event);
        model.addAttribute("event", event);
        model.addAttribute("photos", photos);
        // Passa currentUser se autenticato
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        currentUserOptional.ifPresent(user -> model.addAttribute("currentUser", user));
        return "event/event_photos_gallery";
    }

    // Form upload foto (solo per utenti autorizzati)
    @GetMapping("/event/{eventId}/upload")
    public String showUploadForm(@PathVariable Long eventId, @AuthenticationPrincipal OAuth2User principal, Model model) {
        Optional<Event> eventOpt = eventService.findEventById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/?error=event_not_found";
        }
        Event event = eventOpt.get();
        Optional<User> currentUserOptional = userService.getCurrentUser(principal);
        if(currentUserOptional.isPresent()){
            User user = currentUserOptional.get();
            if (!eventPhotoService.canUploadPhoto(user, event)) {
                return "redirect:/event-photos/event/" + eventId + "?error=not_authorized";
            }
            model.addAttribute("event", event);
            return "event/upload_event_photo";
        }else{
            return "redirect:/event-photos/event/" + eventId + "?error=error";
        }

    }

    // Upload foto (POST)
    @PostMapping("/event/{eventId}/upload")
    public String uploadPhoto(@PathVariable Long eventId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(value = "description", required = false) String description,
                              @AuthenticationPrincipal OAuth2User principal,
                              Model model) {
        Optional<Event> eventOpt = eventService.findEventById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/?error=event_not_found";
        }
        Event event = eventOpt.get();
        Optional<User> userOpt = userService.getCurrentUser(principal);
        if(userOpt.isPresent()){        
            User user = userOpt.get();    
            if (!eventPhotoService.canUploadPhoto(user, event)) {
                return "redirect:/event-photos/event/" + eventId + "?error=not_authorized";
            }
            String filePath = imageService.savePostedImage(file); // usa la cartella giusta per le foto postate
            EventPhoto photo = new EventPhoto();
            photo.setFileName(file.getOriginalFilename());
            photo.setFilePath(filePath);
            photo.setDescription(description);
            photo.setEvent(event);
            photo.setAuthor(user);
            eventPhotoService.save(photo);
            return "redirect:/event-photos/event/" + eventId;
        }else{
            return "redirect:/event-photos/event/" + eventId + "?error=error";
        }
    }

    // Cancella foto (solo autore o admin)
    @PostMapping("/delete/{photoId}")
    public String deletePhoto(@PathVariable Long photoId, @AuthenticationPrincipal OAuth2User principal) {
        Optional<EventPhoto> photoOpt = eventPhotoService.findById(photoId);
        if (photoOpt.isEmpty()) {
            return "redirect:/?error=photo_not_found";
        }
        EventPhoto photo = photoOpt.get();
        Optional<User> userOpt = userService.getCurrentUser(principal);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            if (!eventPhotoService.canDeletePhoto(user, photo)) {
                return "redirect:/event-photos/event/" + photo.getEvent().getId() + "?error=ns";
            }
            eventPhotoService.delete(photo);
            // Puoi anche eliminare il file fisico se vuoi
            return "redirect:/event-photos/event/" + photo.getEvent().getId();
        }
        else{
            return "redirect:/event-photos/event/" + photo.getEvent().getId() + "?error=error";

        }
    }
}
