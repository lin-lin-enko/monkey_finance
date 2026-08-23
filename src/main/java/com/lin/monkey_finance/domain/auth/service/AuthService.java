package com.lin.monkey_finance.domain.auth.service;
import com.lin.monkey_finance.common.exception.AccountStatusException;
import com.lin.monkey_finance.common.exception.InvalidTokenException;
import com.lin.monkey_finance.common.exception.ResourceAlreadyExistsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import com.lin.monkey_finance.domain.user.dto.AuthResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserLoginDto;
import com.lin.monkey_finance.domain.user.dto.UserRegisterDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.mapper.UserMapper;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.model.UserStatus;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import com.lin.monkey_finance.domain.user.service.EmailService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LedgerService ledgerService;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final EntityManager entityManager;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LedgerService ledgerService,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            EntityManager entityManager,
            EmailService emailService,
            UserMapper userMapper){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ledgerService = ledgerService;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.entityManager = entityManager;
        this.emailService = emailService;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDto register(UserRegisterDto userRegisterDto){
        if (userRepository.existsByEmail(userRegisterDto.email())){
            throw new ResourceAlreadyExistsException("User with such email already exists" );
        }

        if (userRepository.existsByUsername(userRegisterDto.username())){
            throw new ResourceAlreadyExistsException("This username is already taken" );
        }

        String encodedPass = passwordEncoder.encode(userRegisterDto.password());

        User user = new User(
                userRegisterDto.username(),
                userRegisterDto.email(),
                encodedPass,
                userRegisterDto.name(),
                userRegisterDto.dateOfBirth(),
                UserStatus.PENDING
        );

        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.refresh(savedUser);

        ledgerService.createDefaultLedger(savedUser.getId());

        String token = generateEmailConfirmationToken(savedUser.getId());
        emailService.sendConfirmationEmail(savedUser.getEmail(), token);

        return userMapper.toResponseDto(savedUser);
    }

    public String generateEmailConfirmationToken(UUID userId){
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("monkey-finance")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(userId.toString())
                .claim("purpose", "EMAIL_CONFIRMATION")
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    @Transactional
    public void confirmEmailAddress(String token) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException e){
            throw new InvalidTokenException("Confirmation link has expired or is invalid");
        }

        if (!"EMAIL_CONFIRMATION".equals(jwt.getClaimAsString("purpose"))){
            throw new InvalidTokenException("Invalid token type");
        }

        UUID userId = UUID.fromString(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponseDto login(UserLoginDto userLoginDto){
        User user = userRepository.findByEmail(userLoginDto.email())
                .orElseThrow(() -> new BadCredentialsException("Wrong email or password"));

        if(!passwordEncoder.matches(userLoginDto.password(), user.getPassword())){
            throw new BadCredentialsException("Wrong email or password");
        }

        switch (user.getStatus()){
            case BLOCKED -> throw new AccountStatusException("Your account has been blocked");
            case PENDING -> {
                String token = generateEmailConfirmationToken(user.getId());
                emailService.sendConfirmationEmail(user.getEmail(), token);
                throw new AccountStatusException("Your email wasn't yet confirmed. The confirmation link was sent to you again");
            }
        }


        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("monkey-finance")
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("purpose", "LOGIN")
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return new AuthResponseDto(token, userMapper.toResponseDto(user));
    }
}
