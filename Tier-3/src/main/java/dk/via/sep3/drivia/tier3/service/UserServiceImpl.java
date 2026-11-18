package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.UserDTO;
import dk.via.sep3.drivia.tier3.entity.Customer;
import dk.via.sep3.drivia.tier3.entity.User;
import dk.via.sep3.drivia.tier3.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService{

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;

    public UserServiceImpl( UserRepository userRepository ){
        this.userRepository = userRepository;
    }

    // Visitors can only register as customers. Employees are added to the database directly.
    @Override
    @Transactional
    public UserDTO registerCustomer( String name, String email, String password, String phoneNumber ){
        validate(name, email, password);
        String cleanEmail = email.trim().toLowerCase();
        if ( userRepository.existsByEmail(cleanEmail) ){
            throw new IllegalStateException("Email " + cleanEmail + " is already registered");
        }
        Customer customer = new Customer(name.trim(), cleanEmail, password, phoneNumber);
        return toDTO(userRepository.save(customer));
    }

    // Same message for an unknown email and a wrong password, so nobody can find out which emails exist
    @Override
    @Transactional( readOnly = true )
    public UserDTO login( String email, String password ){
        if ( email == null || password == null ){
            throw new SecurityException("Wrong email or password");
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> user.getPassword().equals(password))
                .map(this::toDTO)
                .orElseThrow(() -> new SecurityException("Wrong email or password"));
    }

    @Override
    @Transactional( readOnly = true )
    public UserDTO getUser( int id ){
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("User " + id + " not found"));
    }

    private void validate( String name, String email, String password ){
        if ( name == null || name.isBlank() ){
            throw new IllegalArgumentException("Name is required");
        }
        if ( email == null || email.isBlank() || !email.contains("@") ){
            throw new IllegalArgumentException("A valid email is required");
        }
        if ( password == null || password.length() < MIN_PASSWORD_LENGTH ){
            throw new IllegalArgumentException("Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    private UserDTO toDTO( User user ){
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
