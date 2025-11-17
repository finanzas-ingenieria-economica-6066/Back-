package pe.edu.upc.bonotech.iam.domain.services;

import java.util.Optional;

import pe.edu.upc.bonotech.iam.domain.model.aggregates.AuthenticatedUser;
import pe.edu.upc.bonotech.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.bonotech.iam.domain.model.commands.SignUpCommand;

public interface UserCommandService {
    Optional<AuthenticatedUser> handle(SignUpCommand command);

    Optional<AuthenticatedUser> handle(SignInCommand command);
}
