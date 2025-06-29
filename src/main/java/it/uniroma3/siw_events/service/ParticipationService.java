package it.uniroma3.siw_events.service;

import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.Participation;
import it.uniroma3.siw_events.model.User;
import it.uniroma3.siw_events.repository.EventRepository;
import it.uniroma3.siw_events.repository.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ParticipationService {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Transactional(readOnly = true)
    public boolean isUserParticipating(User user, Event event) {
        return participationRepository.findByUserAndEvent(user, event).isPresent();
    }

    @Transactional
    public Participation joinEvent(User user, Event event) throws IllegalStateException {
        if (isUserParticipating(user, event)) {
            throw new IllegalStateException("L'utente sta già partecipando a questo evento.");
        }

        if (event.getMaxParticipants() != null) {
            long currentParticipants = participationRepository.countByEvent(event);
            if (currentParticipants >= event.getMaxParticipants()) {
                throw new IllegalStateException("L'evento ha raggiunto il numero massimo di partecipanti.");
            }
        }

        Participation participation = new Participation();
        participation.setUser(user);
        participation.setEvent(event);
        participation.setJoinedAt(LocalDateTime.now());
        return participationRepository.save(participation);
    }

    @Transactional
    public void leaveEvent(User user, Event event) throws IllegalStateException {
        Participation participation = participationRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new IllegalStateException("L'utente non sta partecipando a questo evento."));
        participationRepository.delete(participation);
    }

    @Transactional(readOnly = true)
    public List<Participation> getParticipationsByUser(User user) {
        return participationRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Participation> getParticipationsByEvent(Event event) {
        return participationRepository.findByEvent(event);
    }

    @Transactional(readOnly = true)
    public long countByEvent(Event event){
        return participationRepository.countByEvent(event);
    }

    @Transactional
    public void removeParticipantFromEvent(Long userId, Long eventId) {
        Optional<Participation> participationOpt = participationRepository.findByUserAndEvent(
                new User() {{ setId(userId); }},
                new Event() {{ setId(eventId); }}
        );
        participationOpt.ifPresent(participationRepository::delete);
    }
}

