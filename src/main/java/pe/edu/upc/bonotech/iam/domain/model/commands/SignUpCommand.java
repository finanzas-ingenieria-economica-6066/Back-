package pe.edu.upc.bonotech.iam.domain.model.commands;

public record SignUpCommand(
        String email,
        String password,
        String fullName) {

}
