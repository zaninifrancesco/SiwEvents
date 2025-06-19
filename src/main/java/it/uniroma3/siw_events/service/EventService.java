package it.uniroma3.siw_events.service;

import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ImageService imageService; // Assicurati di avere un servizio per gestire le immagini

    @Transactional(readOnly = true)
    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event createEvent(Event event, User createdBy) {
        event.setCreatedBy(createdBy);
        event.setCreatedAt(LocalDateTime.now());
        // Eventuali altre logiche di business prima del salvataggio
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> findEventsCreatedBy(User user) {
        return eventRepository.findByCreatedBy(user);
    }

    @Transactional
    public void updateEvent(Long eventId, Event eventDetails) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));
        
        // Se l'immagine viene aggiornata, elimina la vecchia immagine dal server
        if (eventDetails.getEventImageUrl() != null && !eventDetails.getEventImageUrl().isEmpty()
                && (event.getEventImageUrl() == null || !event.getEventImageUrl().equals(eventDetails.getEventImageUrl()))) {
            if (event.getEventImageUrl() != null && !event.getEventImageUrl().isEmpty()) {
                imageService.deleteImage(event.getEventImageUrl());
            }
            event.setEventImageUrl(eventDetails.getEventImageUrl());
        }
        
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setDateTime(eventDetails.getDateTime());
        event.setLocation(eventDetails.getLocation());
        event.setMaxParticipants(eventDetails.getMaxParticipants());
        
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            // Elimina l'immagine associata se esiste
            if (event.getEventImageUrl() != null && !event.getEventImageUrl().isEmpty()) {
                imageService.deleteImage(event.getEventImageUrl());
            }
            eventRepository.deleteById(eventId);
        }
    }

    // Metodi per aggiornare, eliminare eventi verranno aggiunti in seguito
}
