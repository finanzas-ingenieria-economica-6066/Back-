package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import java.math.BigDecimal;

public record CurrencyConversionInfo(
    String currency,
    BigDecimal exchangeRate,
    BigDecimal convertedLoanAmount,           // Monto del crédito convertido
    BigDecimal convertedFinancedCapital,      // Capital financiado convertido
    BigDecimal convertedTotalPaid             // Total pagado convertido (opcional)
) {
    
    // Constructor simplificado para conversiones básicas
    public CurrencyConversionInfo(String currency, BigDecimal exchangeRate, BigDecimal convertedLoanAmount) {
        this(currency, exchangeRate, convertedLoanAmount, null, null);
    }
    
    // Constructor para conversiones completas
    public CurrencyConversionInfo(String currency, BigDecimal exchangeRate, 
                                BigDecimal convertedLoanAmount, BigDecimal convertedFinancedCapital) {
        this(currency, exchangeRate, convertedLoanAmount, convertedFinancedCapital, null);
    }
}