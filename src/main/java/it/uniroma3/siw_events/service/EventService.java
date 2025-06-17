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

    // Metodi per aggiornare, eliminare eventi verranno aggiunti in seguito
}
