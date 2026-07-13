package com.lin.monkey_finance.domain.user.service;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import com.lin.monkey_finance.domain.user.dto.UserRegisterDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LedgerService ledgerService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, LedgerService ledgerService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public UserResponseDto register(UserRegisterDto userRegisterDto){
        if (userRepository.existsByEmail(userRegisterDto.email())){
            throw new IllegalArgumentException("User with such email already exists" );
        }

        if (userRepository.existsByUsername(userRegisterDto.username())){
            throw new IllegalArgumentException("This username is already taken" );
        }

        String encodedPass = passwordEncoder.encode(userRegisterDto.password());

        User user = new User(
                userRegisterDto.username(),
                userRegisterDto.email(),
                encodedPass,
                userRegisterDto.name(),
                userRegisterDto.dateOfBirth()
        );

        User savedUser = userRepository.save(user);

        ledgerService.createDefaultLedger(savedUser.getId(), savedUser.getUsername());

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getDateOfBirth(),
                savedUser.getCreatedAt()
        );
    }
}
