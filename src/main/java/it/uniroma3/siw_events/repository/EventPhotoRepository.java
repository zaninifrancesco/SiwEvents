package it.uniroma3.siw_events.repository;

import it.uniroma3.siw_events.model.EventPhoto;
import it.uniroma3.siw_events.model.Event;
import it.uniroma3.siw_events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
    List<EventPhoto> findByEvent(Event event);
    List<EventPhoto> findByAuthor(User author);
    List<EventPhoto> findByEventAndAuthor(Event event, User author);
}
