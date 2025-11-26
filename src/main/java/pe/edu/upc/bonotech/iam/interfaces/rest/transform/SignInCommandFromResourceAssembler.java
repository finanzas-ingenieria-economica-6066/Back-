package pe.edu.upc.bonotech.iam.interfaces.rest.transform;

import pe.edu.upc.bonotech.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }

}
