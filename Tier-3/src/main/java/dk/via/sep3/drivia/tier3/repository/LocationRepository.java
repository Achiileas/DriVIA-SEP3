package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Integer> {
}
