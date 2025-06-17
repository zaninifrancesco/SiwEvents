package it.uniroma3.siw_events.repository;

import it.uniroma3.siw_events.model.Role;
import it.uniroma3.siw_events.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

