package pe.edu.upc.bonotech.bond.domain.services;

import java.util.List;
import java.util.Optional;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;

public interface IBondQueryService {
    
    /**
     * Obtiene todas las simulaciones de un usuario
     */
    List<Bond> getLoansByUserId(Long userId);
    
    /**
     * Obtiene una simulación específica por ID
     */
    Optional<Bond> getLoanById(Long loanId);
    
    /**
     * Obtiene simulaciones por tipo de moneda
     */
    List<Bond> getLoansByCurrency(String currency);
    
    /**
     * Obtiene simulaciones que tienen Bono del Buen Pagador
     */
    List<Bond> getLoansWithBBP();
    
    /**
     * Obtiene el historial de simulaciones recientes
     */
    List<Bond> getRecentLoans(int limit);

    Optional<Bond> getBondById(Long id);
}