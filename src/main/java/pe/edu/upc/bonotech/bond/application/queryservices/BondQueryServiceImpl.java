package pe.edu.upc.bonotech.bond.application.queryservices;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;
import pe.edu.upc.bonotech.bond.domain.services.IBondQueryService;
import pe.edu.upc.bonotech.bond.infrastructure.persistence.jpa.repositories.BondRepository;

@Service
public class BondQueryServiceImpl implements IBondQueryService {

    @Autowired
    private BondRepository bondRepository;

    @Override
    public List<Bond> getLoansByUserId(Long userId) {
        return bondRepository.findByUserId(userId);
    }
    
    @Override
    public Optional<Bond> getLoanById(Long loanId) {
        return bondRepository.findById(loanId);
    }
    
    @Override
    public List<Bond> getLoansByCurrency(String currency) {
        try {
            ECurrency currencyEnum = ECurrency.valueOf(currency.toUpperCase());
            return bondRepository.findByCurrency(currencyEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Moneda no válida: " + currency);
        }
    }
    
    @Override
    public List<Bond> getLoansWithBBP() {
        return bondRepository.findLoansWithBBP();
    }
    
    @Override
    public List<Bond> getRecentLoans(int limit) {
        // Para límites diferentes a 10, necesitarías un método personalizado
        return bondRepository.findTop10ByOrderByCreatedAtDesc();
    }
}