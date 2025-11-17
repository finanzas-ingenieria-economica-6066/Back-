package pe.edu.upc.bonotech.iam.domain.model.commands;

public record SignInCommand(
        String email,
        String password) {

}
