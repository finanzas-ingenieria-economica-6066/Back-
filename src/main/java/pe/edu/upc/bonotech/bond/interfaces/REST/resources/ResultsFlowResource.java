package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import java.math.BigDecimal;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.ResultsFlow;

public record ResultsFlowResource(
    // === INDICADORES DE TRANSPARENCIA ===
    BigDecimal van,
    BigDecimal tir,
    BigDecimal tcea,
    
    // === RESUMEN DE PAGOS ===
    BigDecimal totalBasePayments,
    BigDecimal totalInterest,
    BigDecimal totalAmortization,
    BigDecimal totalInsurance,
    BigDecimal totalFixedInsurance,
    BigDecimal totalCommissions,
    BigDecimal totalCosts,
    
    // === TOTALES GLOBALES ===
    BigDecimal totalPaid,
    BigDecimal averageMonthlyPayment,
    BigDecimal costOfCredit
) {
    public static ResultsFlowResource fromResultsFlow(ResultsFlow results) {
        return new ResultsFlowResource(
            results.getVan(),
            results.getTir(),
            results.getTcea(),
            results.getTotalBasePayments(),
            results.getTotalInterest(),
            results.getTotalAmortization(),
            results.getTotalInsurance(),
            results.getTotalFixedInsurance(),
            results.getTotalCommissions(),
            results.getTotalCosts(),
            results.getTotalPaid(),
            results.getAverageMonthlyPayment(),
            results.getCostOfCredit()
        );
    }
}