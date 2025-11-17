package pe.edu.upc.bonotech.iam.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.bonotech.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.bonotech.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.bonotech.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class User extends AuditableAbstractAggregateRoot<User> {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    public User(SignUpCommand command, String hashedPassword) {
        this.fullName = command.fullName();
        this.email = command.email();
        this.password = hashedPassword;
    }

    public User(SignInCommand command) {
        this.email = command.email();
        this.password = command.password();
    }
}