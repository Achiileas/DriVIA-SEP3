package dk.via.sep3.drivia.tier3.location.repository;

import dk.via.sep3.drivia.tier3.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
