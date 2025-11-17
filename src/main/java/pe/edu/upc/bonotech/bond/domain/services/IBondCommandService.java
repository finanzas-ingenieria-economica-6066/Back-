package pe.edu.upc.bonotech.bond.domain.services;

import java.util.Optional;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;
import pe.edu.upc.bonotech.bond.domain.model.commands.CreateBondCommand;
import pe.edu.upc.bonotech.bond.interfaces.REST.resources.UpdateBondResource;

public interface IBondCommandService {
    
    /**
     * Crea una nueva simulación de crédito MIVivienda
     */
    Optional<Bond> handle(CreateBondCommand command);

    /**
     * Actualiza y recalcula completamente una simulación existente
     */
    Optional<Bond> updateAndRecalculateLoan(Long loanId, CreateBondCommand command);

    /**
     * Actualización parcial y recálculo de una simulación existente
     */
    Optional<Bond> partialUpdateAndRecalculateLoan(Long loanId, UpdateBondResource resource);
    
    /**
     * Elimina una simulación de crédito
     */
    boolean deleteLoan(Long loanId);
}