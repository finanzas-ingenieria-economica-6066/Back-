package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import java.math.BigDecimal;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Flow;

public record FlowResource(
    Integer periodNumber,
    String periodType,
    BigDecimal initialBalance,
    BigDecimal finalBalance,
    BigDecimal basePayment,
    BigDecimal interest,
    BigDecimal amortization,
    BigDecimal insuranceAmount,
    BigDecimal fixedInsuranceAmount,
    BigDecimal commissionAmount,
    BigDecimal totalPayment,
    BigDecimal periodicRate
) {
    public static FlowResource fromFlow(Flow flow) {
        return new FlowResource(
            flow.getPeriodNumber(),
            flow.getPeriodType(),
            flow.getInitialBalance(),
            flow.getFinalBalance(),
            flow.getBasePayment(),
            flow.getInterest(),
            flow.getAmortization(),
            flow.getInsuranceAmount(),
            flow.getFixedInsuranceAmount(),
            flow.getCommissionAmount(),
            flow.getTotalPayment(),
            flow.getPeriodicRate()
        );
    }
}