package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.UserDTO;
import dk.via.sep3.drivia.tier3.entity.Customer;
import dk.via.sep3.drivia.tier3.entity.Employee;
import dk.via.sep3.drivia.tier3.entity.User;
import dk.via.sep3.drivia.tier3.entity.UserRole;
import dk.via.sep3.drivia.tier3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// White-box tests for UserServiceImpl, grouped by ZOMBIES
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;
    private Customer anna;
    private Employee bob;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);

        anna = new Customer("Anna", "anna@mail.dk", "secret1", "12345678");
        ReflectionTestUtils.setField(anna, "id", 1);
        bob = new Employee("Bob", "bob@drivia.dk", "staff123", "E-01");
        ReflectionTestUtils.setField(bob, "id", 2);
    }

    private void registeringIsPossible() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    // ---------- Z: Zero ----------

    @Test
    void loginWithoutEmailOrPasswordIsRefused() {
        assertThrows(SecurityException.class, () -> userService.login(null, "secret1"));
        assertThrows(SecurityException.class, () -> userService.login("anna@mail.dk", null));
        verifyNoInteractions(userRepository);
    }

    @Test
    void unknownUserIdThrowsNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUser(99));
    }

    // ---------- O: One ----------

    @Test
    void registerCreatesACustomer() {
        registeringIsPossible();

        UserDTO result = userService.registerCustomer("Anna", "anna@mail.dk", "secret1", "12345678");

        assertEquals(UserRole.CUSTOMER, result.role());
        assertEquals("Anna", result.name());
        assertEquals("anna@mail.dk", result.email());
    }

    // ---------- M: Many ----------

    @Test
    void customersAndEmployeesCanBothLogIn() {
        when(userRepository.findByEmail("anna@mail.dk")).thenReturn(Optional.of(anna));
        when(userRepository.findByEmail("bob@drivia.dk")).thenReturn(Optional.of(bob));

        assertEquals(UserRole.CUSTOMER, userService.login("anna@mail.dk", "secret1").role());
        assertEquals(UserRole.EMPLOYEE, userService.login("bob@drivia.dk", "staff123").role());
    }

    // ---------- B: Boundaries ----------

    @Test
    void passwordWithFiveCharactersIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerCustomer("Anna", "anna@mail.dk", "12345", "12345678"));
        verifyNoInteractions(userRepository);
    }

    @Test
    void passwordWithSixCharactersIsAccepted() {
        registeringIsPossible();

        assertEquals(UserRole.CUSTOMER,
                userService.registerCustomer("Anna", "anna@mail.dk", "123456", "12345678").role());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "anna.mail.dk"})
    void missingOrInvalidEmailIsRefused(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerCustomer("Anna", email, "secret1", "12345678"));
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void missingOrBlankNameIsRefused(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerCustomer(name, "anna@mail.dk", "secret1", "12345678"));
        verifyNoInteractions(userRepository);
    }

    // ---------- I: Interfaces ----------

    @Test
    void registerSavesACustomerWithItsPhoneNumber() {
        registeringIsPossible();

        userService.registerCustomer("Anna", "anna@mail.dk", "secret1", "12345678");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        Customer customer = assertInstanceOf(Customer.class, saved.getValue());
        assertEquals("12345678", customer.getPhoneNumber());
    }

    @Test
    void emailIsTrimmedAndLowerCasedBeforeItIsUsed() {
        registeringIsPossible();

        UserDTO result = userService.registerCustomer("Anna", "  Anna@Mail.DK ", "secret1", "12345678");

        verify(userRepository).existsByEmail("anna@mail.dk");
        assertEquals("anna@mail.dk", result.email());
    }

    @Test
    void loginLooksUpTheEmailInLowerCase() {
        when(userRepository.findByEmail("anna@mail.dk")).thenReturn(Optional.of(anna));

        userService.login("ANNA@mail.dk", "secret1");

        verify(userRepository).findByEmail("anna@mail.dk");
    }

    // ---------- E: Exceptions ----------

    @Test
    void emailThatIsAlreadyRegisteredIsRefused() {
        when(userRepository.existsByEmail("anna@mail.dk")).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> userService.registerCustomer("Anna", "anna@mail.dk", "secret1", "12345678"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameError() {
        when(userRepository.findByEmail("anna@mail.dk")).thenReturn(Optional.of(anna));
        when(userRepository.findByEmail("nobody@mail.dk")).thenReturn(Optional.empty());

        SecurityException wrongPassword = assertThrows(SecurityException.class,
                () -> userService.login("anna@mail.dk", "wrong12"));
        SecurityException unknownEmail = assertThrows(SecurityException.class,
                () -> userService.login("nobody@mail.dk", "secret1"));

        assertEquals(wrongPassword.getMessage(), unknownEmail.getMessage());
    }

    // ---------- S: Simple scenarios ----------

    @Test
    void registeredCustomerCanLogIn() {
        when(userRepository.existsByEmail("anna@mail.dk")).thenReturn(false);
        Customer[] saved = new Customer[1];
        when(userRepository.save(any(User.class))).thenAnswer(call -> {
            saved[0] = call.getArgument(0);
            return saved[0];
        });
        userService.registerCustomer("Anna", "anna@mail.dk", "secret1", "12345678");

        when(userRepository.findByEmail("anna@mail.dk")).thenReturn(Optional.of(saved[0]));
        UserDTO loggedIn = userService.login("anna@mail.dk", "secret1");

        assertEquals("Anna", loggedIn.name());
        assertEquals(UserRole.CUSTOMER, loggedIn.role());
    }
}
