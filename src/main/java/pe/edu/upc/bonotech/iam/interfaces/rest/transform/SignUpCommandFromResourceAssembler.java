package pe.edu.upc.bonotech.iam.interfaces.rest.transform;

import pe.edu.upc.bonotech.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
                resource.email(),
                resource.password(),
                resource.fullName());
    }
}
