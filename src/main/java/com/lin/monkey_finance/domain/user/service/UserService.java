package com.lin.monkey_finance.domain.user.service;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import com.lin.monkey_finance.domain.user.dto.AuthResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserLoginDto;
import com.lin.monkey_finance.domain.user.dto.UserRegisterDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LedgerService ledgerService;
    private final JwtEncoder jwtEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, LedgerService ledgerService, JwtEncoder jwtEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ledgerService = ledgerService;
        this.jwtEncoder = jwtEncoder;
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

    @Transactional
    public AuthResponseDto login(UserLoginDto userLoginDto){
        User user = userRepository.findByEmail(userLoginDto.email())
                .orElseThrow(() -> new IllegalArgumentException("Wrong email or password"));

        if(!passwordEncoder.matches(userLoginDto.password(), user.getPassword())){
            throw new IllegalArgumentException("Wrong email or password");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("monkey-finance")
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        UserResponseDto userResponseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getDateOfBirth(),
                user.getCreatedAt());
        return new AuthResponseDto(token, userResponseDto);
    }
}
