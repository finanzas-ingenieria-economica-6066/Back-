package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateBondResource {
    private String name;
    private String currency;
    private BigDecimal loanAmount;
    private BigDecimal bbpAmount;
    private BigDecimal initialFeePercentage;
    private BigDecimal initialFeeAmount;
    private BigDecimal exchangeRate;
    private String interestType;
    private BigDecimal annualRate;
    private String capitalizationPeriod;
    private String paymentFrequency;
    private Integer totalMonths;
    private String gracePeriodType;
    private Integer gracePeriodMonths;
    private BigDecimal insurancePercentage;
    private BigDecimal fixedInsurance;
    private BigDecimal initialCommission;
    private BigDecimal periodicCommission;
    private BigDecimal finalCommission;
    private LocalDate disbursementDate;
    private BigDecimal discountRate;
}