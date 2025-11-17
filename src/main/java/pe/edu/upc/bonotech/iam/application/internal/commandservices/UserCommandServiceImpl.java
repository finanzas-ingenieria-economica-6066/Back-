package pe.edu.upc.bonotech.iam.application.internal.commandservices;

import java.util.Optional;

import org.springframework.stereotype.Service;

import pe.edu.upc.bonotech.iam.application.internal.outboundservices.hashing.HashingService;
import pe.edu.upc.bonotech.iam.application.internal.outboundservices.tokens.TokenService;
import pe.edu.upc.bonotech.iam.domain.model.aggregates.User;
import pe.edu.upc.bonotech.iam.domain.model.aggregates.AuthenticatedUser;
import pe.edu.upc.bonotech.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.bonotech.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.bonotech.iam.domain.services.UserCommandService;
import pe.edu.upc.bonotech.iam.infrastructure.persistence.jpa.repositories.UserRepository;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final HashingService hashingService;

    public UserCommandServiceImpl(UserRepository userRepository, TokenService tokenService,
            HashingService hashingService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
    }

    @Override
    public Optional<AuthenticatedUser> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already in use");

        var user = new User(command, hashingService.encode(command.password()));
        var createdUser = userRepository.save(user);
        var token = tokenService.generateToken(createdUser.getEmail());

        return Optional.of(new AuthenticatedUser(createdUser, token));
    }

    @Override
    public Optional<AuthenticatedUser> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email());

        if (user.isEmpty())
            throw new RuntimeException("User not found");

        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");

        var token = tokenService.generateToken(user.get().getEmail());

        return Optional.of(new AuthenticatedUser(user.get(), token));
    }
}
