package pe.edu.upc.bonotech.iam.interfaces.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.bonotech.iam.domain.services.UserCommandService;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.SignInResource;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.SignUpResource;
import pe.edu.upc.bonotech.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import pe.edu.upc.bonotech.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import pe.edu.upc.bonotech.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;

/**
 * AuthenticationController
 * <p>
 * This controller is responsible for handling authentication requests.
 * It exposes two endpoints:
 * <ul>
 * <li>POST /api/v1/auth/sign-in</li>
 * <li>POST /api/v1/auth/sign-up</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the sign-up request.
     * 
     * @param signUpResource the sign-up request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-up")
    public ResponseEntity<AuthenticatedUserResource> signUp(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(signUpCommand);

        if (authenticatedUser.isEmpty())
            return ResponseEntity.badRequest().build();

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(authenticatedUser.get());

        return new ResponseEntity<>(authenticatedUserResource, HttpStatus.CREATED);
    }

    /**
     * Handles the sign-in request.
     * 
     * @param signInResource the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(signInCommand);

        if (authenticatedUser.isEmpty())
            return ResponseEntity.notFound().build();

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(authenticatedUser.get());

        return new ResponseEntity<>(authenticatedUserResource, HttpStatus.OK);
    }

    @ExceptionHandler(RuntimeException.class)
public ResponseEntity<Map<String, String>> handleAuthenticationErrors(RuntimeException ex) {
    Map<String, String> errorResponse = Map.of("error", ex.getMessage());
    
    if (ex.getMessage().contains("already in use")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    } else if (ex.getMessage().contains("not found") || ex.getMessage().contains("Invalid password")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
}
