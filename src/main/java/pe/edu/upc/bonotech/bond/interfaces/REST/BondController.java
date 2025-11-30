package pe.edu.upc.bonotech.bond.interfaces.REST;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.bonotech.bond.domain.services.IBondCommandService;
import pe.edu.upc.bonotech.bond.domain.services.IBondQueryService;
import pe.edu.upc.bonotech.bond.interfaces.REST.resources.BondResource;
import pe.edu.upc.bonotech.bond.interfaces.REST.resources.CreateBondResource;
import pe.edu.upc.bonotech.bond.interfaces.REST.resources.UpdateBondResource;
import pe.edu.upc.bonotech.iam.domain.services.UserQueryService;

@RestController
@RequestMapping(value = "/v1/loans", produces = MediaType.APPLICATION_JSON_VALUE) // Cambiado a /loans
@Tag(name = "Loans", description = "MIVivienda Loan Management Endpoints") // Cambiado a Loans
public class BondController {
    
    @Autowired
    private IBondCommandService bondCommandService;

    @Autowired
    private IBondQueryService bondQueryService;

    @Autowired
    private UserQueryService userQueryService;

    @PostMapping("/simulate")
    @Operation(summary = "Simulate MIVivienda loan", description = "Create a new loan simulation with Bono del Buen Pagador")
    public ResponseEntity<?> simulateLoan(@RequestBody CreateBondResource resource) {
        try {
            var command = resource.toCommand();
            var loan = bondCommandService.handle(command);

            if (loan.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo crear la simulación"));
            }
            
            var loanResource = BondResource.fromBond(loan.get());
            return ResponseEntity.ok(loanResource);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update loan simulation", description = "Update and recalculate an existing loan simulation")
    public ResponseEntity<?> updateLoan(@PathVariable Long id, @RequestBody UpdateBondResource resource) {
        try {
            var updatedLoan = bondCommandService.partialUpdateAndRecalculateLoan(id, resource);

            if (updatedLoan.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var loanResource = BondResource.fromBond(updatedLoan.get());
            return ResponseEntity.ok(loanResource);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/my-loans")
    @Operation(summary = "Get user's loan simulations", description = "Retrieve all loan simulations belonging to the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getMyLoans() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            var userOpt = userQueryService
                .handle(new pe.edu.upc.bonotech.iam.domain.model.queries.GetUserByEmailQuery(userEmail));

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
            }

            var loans = bondQueryService.getLoansByUserId(userOpt.get().getId());

            List<BondResource> loanResources = loans.stream()
                .map(BondResource::fromBond)
                .toList();

            return ResponseEntity.ok(loanResources);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener las simulaciones: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan details", description = "Get detailed information of a specific loan simulation")
    public ResponseEntity<?> getLoanById(@PathVariable Long id) {
        try {
            var bondOpt = bondQueryService.getBondById(id);

            if (bondOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró la simulación con ID: " + id));
            }

            var bondResource = BondResource.fromBond(bondOpt.get());

            return ResponseEntity.ok(bondResource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener el detalle: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete loan simulation", description = "Delete an existing loan simulation by ID")
    public ResponseEntity<?> deleteLoan(@PathVariable Long id) {
        try {
            // Se asume que tu servicio tiene un método para eliminar
            // Si retorna un booleano indicando éxito:
            boolean deleted = bondCommandService.deleteBond(id);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró la simulación con ID: " + id));
            }

            // Retornamos 200 OK con mensaje (o podría ser 204 No Content)
            return ResponseEntity.ok(Map.of("message", "Simulación eliminada exitosamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al eliminar: " + e.getMessage()));
        }
    }


    @GetMapping("/{id}/amortization-table")
    @Operation(summary = "Get amortization table", description = "Get the complete amortization table for a loan")
    public ResponseEntity<?> getAmortizationTable(@PathVariable Long id) {
        try {
            // Implementar para obtener solo la tabla de amortización
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Endpoint en desarrollo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{id}/with-conversion")
@Operation(summary = "Get loan with currency conversion", description = "Get loan details with currency conversion information")
public ResponseEntity<?> getLoanWithConversion(@PathVariable Long id) {
    try {
        // Obtener el loan (necesitarás implementar este método en el query service)
        // var loanOpt = bondQueryService.getBondById(id);
        
        // if (loanOpt.isEmpty()) {
        //     return ResponseEntity.notFound().build();
        // }
        
        // var loanWithConversion = BondWithConversionResource.fromBond(loanOpt.get(), currencyConversionService);
        // return ResponseEntity.ok(loanWithConversion);
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "Endpoint en desarrollo"));
            
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}
}