package it.uniroma3.siw_events.repository;

import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.Participation;
import it.uniroma3.siw_events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByUserAndEvent(User user, Event event);
    List<Participation> findByUser(User user);
    List<Participation> findByEvent(Event event);
}

