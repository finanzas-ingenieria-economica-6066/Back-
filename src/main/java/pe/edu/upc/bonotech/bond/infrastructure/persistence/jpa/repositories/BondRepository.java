package pe.edu.upc.bonotech.bond.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;

@Repository
public interface BondRepository extends JpaRepository<Bond, Long> {
    
    /**
     * Obtiene todas las simulaciones de un usuario
     */
    List<Bond> findByUserId(Long userId);
    
    /**
     * Obtiene simulaciones por tipo de moneda
     */
    List<Bond> findByCurrency(ECurrency currency);
    
    /**
     * Obtiene simulaciones que tienen Bono del Buen Pagador (BBP > 0)
     */
    @Query("SELECT b FROM Bond b WHERE b.bbpAmount > 0")
    List<Bond> findLoansWithBBP();
    
    /**
     * Obtiene simulaciones por rango de monto
     */
    @Query("SELECT b FROM Bond b WHERE b.loanAmount BETWEEN :minAmount AND :maxAmount")
    List<Bond> findByLoanAmountRange(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount);
    
    /**
     * Obtiene las últimas N simulaciones ordenadas por fecha de creación
     */
    List<Bond> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * Obtiene simulaciones por tipo de período de gracia
     */
    List<Bond> findByGracePeriodType(String gracePeriodType);
    
    /**
     * Cuenta las simulaciones por usuario
     */
    Long countByUserId(Long userId);
    
    /**
     * Verifica si existe una simulación con el mismo nombre para el usuario
     */
    Boolean existsByUserIdAndName(Long userId, String name);
}