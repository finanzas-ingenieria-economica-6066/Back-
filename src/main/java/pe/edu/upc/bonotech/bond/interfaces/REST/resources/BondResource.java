package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;

public record BondResource(
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
    ResultsFlowResource resultsFlow
) {
    public static BondResource fromBond(Bond bond) {
        var flows = bond.getFlows().stream()
            .map(FlowResource::fromFlow)
            .toList();

        var results = bond.getResultsFlow() != null
            ? ResultsFlowResource.fromResultsFlow(bond.getResultsFlow())
            : null;

        return new BondResource(
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
            results
        );
    }
}