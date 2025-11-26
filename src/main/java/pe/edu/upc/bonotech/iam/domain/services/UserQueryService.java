package pe.edu.upc.bonotech.iam.domain.services;

import java.util.Optional;

import pe.edu.upc.bonotech.iam.domain.model.aggregates.User;
import pe.edu.upc.bonotech.iam.domain.model.queries.GetUserByEmailQuery;

public interface UserQueryService {
    Optional<User> handle(GetUserByEmailQuery query);
}
