package pe.edu.upc.bonotech.iam.domain.model.aggregates;

public record AuthenticatedUser(User user, String token) {

}
