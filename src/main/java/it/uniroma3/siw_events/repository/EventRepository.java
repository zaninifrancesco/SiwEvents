package it.uniroma3.siw_events.repository;

import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCreatedBy(User createdBy);
}

