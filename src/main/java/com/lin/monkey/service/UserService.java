package com.lin.monkey.service;

import com.lin.monkey.dto.UserRequestDto;
import com.lin.monkey.dto.UserResponseDto;
import com.lin.monkey.exception.UserNotFoundException;
import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.User;
import com.lin.monkey.model.UsersLedgers;
import com.lin.monkey.repository.UsersLedgersRepository;
import com.lin.monkey.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.lin.monkey.repository.UserRepository;
import com.lin.monkey.repository.LedgerRepository;

import java.util.Optional;
import java.util.UUID;

@Service
/*
 * All db operations inside this class will be executed
 * in one transaction. If there's error, rollback will occur
 * Connection and disconnection occur automatically
 * */
@Transactional
public class UserService {

    // final means fields don't change after object creation (immutable)
    private final UserRepository userRepository; // I give it to the user repository cause it gives it to the db
    private final PasswordEncoder passwordEncoder;
    private final LedgerRepository ledgerRepository;
    private final UsersLedgersRepository usersLedgersRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, LedgerRepository ledgerRepository, UsersLedgersRepository usersLedgersRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ledgerRepository = ledgerRepository;
        this.usersLedgersRepository = usersLedgersRepository;
    }

    /*
     * Registers user, returns UserResponseDto
     * Accepts UserRequestDto from controller
     * Gives data to repository, and it gives it to the db
     * Returns safe dto as an answer
     * */
    public UserResponseDto register(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email \"" + dto.getEmail() + "\" is already in use");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username \"" + dto.getUsername() + "\" already exists");
        }

        /*
         * Equals compares content of the strings, "!=" compares links (memory)
         * */
        if (!dto.getPassword().equals(dto.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        //Creating user entity and filling it with data from dto
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setBirthDate(dto.getBirthDate());
        user.setPhoneNumber(dto.getPhoneNumber());

        User savedUser = userRepository.save(user);

        Ledger startingLedger = new Ledger();
        startingLedger.setName("My ledger");
        startingLedger.setDescription("Main ledger for income and expenses");
        startingLedger.setOwnerId(savedUser.getId());
        startingLedger = ledgerRepository.save(startingLedger);

        UsersLedgers entry = new UsersLedgers();
        entry.setUserId(savedUser.getId());
        entry.setLedgerId(startingLedger.getId());
        entry.setRole("ADMIN");
        usersLedgersRepository.save(entry);

        savedUser.setDefaultLedgerId(startingLedger.getId());

        User userWithLedger = userRepository.save(savedUser);

        // Returns saved object as an answer
        // and turns it into safe dto obj unsing fromUser()
        return UserResponseDto.fromUser(userWithLedger);
    }

    public UserResponseDto changeDefaultLedger(UUID userId, UUID ledgerId) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found"));

        User user = findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String userRole = usersLedgersRepository.findUserRoleInLedger(userId, ledgerId)
                .orElseThrow(() -> new AccessDeniedException("User doesn't have access to this ledger"));

        user.setDefaultLedgerId(ledgerId);
        User updatedUser = userRepository.save(user);
        return UserResponseDto.fromUser(updatedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }
}
