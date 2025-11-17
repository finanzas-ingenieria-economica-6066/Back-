package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;
import pe.edu.upc.bonotech.shared.domain.services.CurrencyConversionService;

public record BondWithConversionResource(
    // === INFORMACIÓN BASE DEL CRÉDITO ===
    Long id,
    String name,
    String currency,
    BigDecimal loanAmount,
    BigDecimal bbpAmount,
    BigDecimal initialFeePercentage,
    BigDecimal initialFeeAmount,
    BigDecimal financedCapital,
    BigDecimal exchangeRate,
    String interestType,
    BigDecimal annualRate,
    String capitalizationPeriod,
    String paymentFrequency,
    Integer totalMonths,
    String gracePeriodType,
    Integer gracePeriodMonths,
    BigDecimal insurancePercentage,
    BigDecimal fixedInsurance,
    BigDecimal initialCommission,
    BigDecimal periodicCommission,
    BigDecimal finalCommission,
    LocalDate disbursementDate,
    BigDecimal discountRate,
    List<FlowResource> flows,
    ResultsFlowResource resultsFlow,
    
    // === CONVERSIONES A OTRAS MONEDAS ===
    CurrencyConversionInfo usdConversion,
    CurrencyConversionInfo penConversion
) {

    public static BondWithConversionResource fromBond(Bond bond, CurrencyConversionService conversionService) {
        var flows = bond.getFlows().stream()
            .map(FlowResource::fromFlow)
            .toList();

        var results = bond.getResultsFlow() != null
            ? ResultsFlowResource.fromResultsFlow(bond.getResultsFlow())
            : null;

        ECurrency bondCurrency = bond.getCurrency();

        // === CONVERSIÓN A USD ===
        CurrencyConversionInfo usdConversion = null;
        if (bondCurrency != ECurrency.USD) {
            ECurrency usdCurrency = ECurrency.USD;
            BigDecimal usdRate = conversionService.getExchangeRate(bondCurrency, usdCurrency);
            BigDecimal usdLoanAmount = conversionService.convert(bond.getLoanAmount(), bondCurrency, usdCurrency);
            BigDecimal usdFinancedCapital = conversionService.convert(bond.getFinancedCapital(), bondCurrency, usdCurrency);
            
            // Calcular total pagado en USD si hay resultados
            BigDecimal usdTotalPaid = null;
            if (results != null && results.totalPaid() != null) {
                usdTotalPaid = conversionService.convert(results.totalPaid(), bondCurrency, usdCurrency);
            }
            
            usdConversion = new CurrencyConversionInfo(
                usdCurrency.toString(),
                usdRate,
                usdLoanAmount,
                usdFinancedCapital,
                usdTotalPaid
            );
        }

        // === CONVERSIÓN A PEN ===
        CurrencyConversionInfo penConversion = null;
        if (bondCurrency != ECurrency.PEN) {
            ECurrency penCurrency = ECurrency.PEN;
            BigDecimal penRate = conversionService.getExchangeRate(bondCurrency, penCurrency);
            BigDecimal penLoanAmount = conversionService.convert(bond.getLoanAmount(), bondCurrency, penCurrency);
            BigDecimal penFinancedCapital = conversionService.convert(bond.getFinancedCapital(), bondCurrency, penCurrency);
            
            // Calcular total pagado en PEN si hay resultados
            BigDecimal penTotalPaid = null;
            if (results != null && results.totalPaid() != null) {
                penTotalPaid = conversionService.convert(results.totalPaid(), bondCurrency, penCurrency);
            }
            
            penConversion = new CurrencyConversionInfo(
                penCurrency.toString(),
                penRate,
                penLoanAmount,
                penFinancedCapital,
                penTotalPaid
            );
        }

        return new BondWithConversionResource(
            bond.getId(),
            bond.getName(),
            bond.getCurrency().toString(),
            bond.getLoanAmount(),
            bond.getBbpAmount(),
            bond.getInitialFeePercentage(),
            bond.getInitialFeeAmount(),
            bond.getFinancedCapital(),
            bond.getExchangeRate(),
            bond.getInterestType().toString(),
            bond.getAnnualRate(),
            bond.getCapitalizationPeriod().toString(),
            bond.getPaymentFrequency().toString(),
            bond.getTotalMonths(),
            bond.getGracePeriodType().toString(),
            bond.getGracePeriodMonths(),
            bond.getInsurancePercentage(),
            bond.getFixedInsurance(),
            bond.getInitialCommission(),
            bond.getPeriodicCommission(),
            bond.getFinalCommission(),
            bond.getDisbursementDate(),
            bond.getDiscountRate(),
            flows,
            results,
            usdConversion,
            penConversion
        );
    }

    // === MÉTODOS DE CONVENIENCIA ===
    
    /**
     * Obtiene el resumen de conversiones disponibles
     */
    public String getConversionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Conversiones disponibles: ");
        
        if (usdConversion != null) {
            summary.append("USD ");
        }
        if (penConversion != null) {
            summary.append("PEN ");
        }
        
        return summary.toString().trim();
    }
    
    /**
     * Verifica si tiene conversiones
     */
    public boolean hasConversions() {
        return usdConversion != null || penConversion != null;
    }
}