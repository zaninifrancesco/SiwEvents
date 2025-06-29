package it.uniroma3.siw_events.service;

import it.uniroma3.siw_events.model.EventPhoto;
import it.uniroma3.siw_events.model.RoleName;
import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.repository.EventPhotoRepository;
import it.uniroma3.siw_events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventPhotoService {

    private final ImageService imageService;

    @Autowired
    private EventPhotoRepository eventPhotoRepository;

    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ParticipationService participationService;

    EventPhotoService(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Verifica se l'utente può caricare foto per l'evento specificato.
     */
    public boolean canUploadPhoto(User user, Event event) {
        if (user == null || event == null) {
            System.out.println("prima condizione");
            return false;
        }
        boolean isParticipant = participationService.isUserParticipating(user, event);
        if (!isParticipant) {
            System.out.println("Seconda condizione");
            return false;
        }
        
        return true;
    }

    /**
     * Verifica se l'utente può cancellare la foto (autore o admin)
     */
    public boolean canDeletePhoto(User user, EventPhoto photo) {
        if (user == null || photo == null || photo.getAuthor() == null || user.getId() == null) {
            return false;
        }
        if (photo.getAuthor().getId().equals(user.getId())) {
            return true;
        }

        return user.getRuolo().equals(RoleName.ADMIN);
    }

    public List<EventPhoto> getPhotosByEvent(Event event) {
        return eventPhotoRepository.findByEvent(event);
    }

    public List<EventPhoto> getPhotosByAuthor(User user) {
        return eventPhotoRepository.findByAuthor(user);
    }

    public Optional<EventPhoto> findById(Long id){
        return eventPhotoRepository.findById(id);
    }

    public EventPhoto save(EventPhoto photo) {
        return eventPhotoRepository.save(photo);
    }

    public void delete(EventPhoto photo) {
        imageService.deleteImage(photo.getFilePath());
        eventPhotoRepository.delete(photo);
    }
}
