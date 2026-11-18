package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Works for both customers and employees, because they are both Users
public interface UserRepository extends JpaRepository<User, Integer>{

    Optional<User> findByEmail( String email );

    boolean existsByEmail( String email );
}
