package pe.edu.upc.bonotech.iam.application.internal.queryservices;

import java.util.Optional;

import org.springframework.stereotype.Service;

import pe.edu.upc.bonotech.iam.domain.model.aggregates.User;
import pe.edu.upc.bonotech.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.bonotech.iam.domain.services.UserQueryService;
import pe.edu.upc.bonotech.iam.infrastructure.persistence.jpa.repositories.UserRepository;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }

}
