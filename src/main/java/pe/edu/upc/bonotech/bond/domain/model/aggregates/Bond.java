package pe.edu.upc.bonotech.bond.domain.model.aggregates;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.bonotech.bond.domain.model.commands.CreateBondCommand;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECapitalizationPeriod;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.EInterestType;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.EPaymentFrequency;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.EGracePeriodType;
import pe.edu.upc.bonotech.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bond extends AuditableAbstractAggregateRoot<Bond> {

    private static final int SCALE = 10;

    // === DATOS DEL USUARIO Y IDENTIFICACIÓN ===
    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    // === DATOS BÁSICOS DEL CRÉDITO MIVIVIENDA ===
    @NotNull
    @Enumerated(EnumType.STRING)
    private ECurrency currency;

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal loanAmount;                    // Monto del crédito solicitado

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal bbpAmount;                     // Bono del Buen Pagador

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal initialFeePercentage;          // % de cuota inicial

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal initialFeeAmount;              // Monto de cuota inicial

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal financedCapital;               // Capital a financiar (calculado)

    // === TIPO DE CAMBIO (para conversiones) ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal exchangeRate;                  // TC PEN/USD

    // === TASAS E INTERESES ===
    @NotNull
    @Enumerated(EnumType.STRING)
    private EInterestType interestType;

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal annualRate;                    // Tasa anual (TEA o TNA)

    @NotNull
    @Enumerated(EnumType.STRING)
    private ECapitalizationPeriod capitalizationPeriod;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EPaymentFrequency paymentFrequency;

    // === PLAZOS Y PERÍODOS DE GRACIA ===
    @NotNull
    private Integer totalMonths;                      // Plazo total en meses

    @NotNull
    @Enumerated(EnumType.STRING)
    private EGracePeriodType gracePeriodType;         // NINGUNO, PARCIAL, TOTAL

    @NotNull
    private Integer gracePeriodMonths;                // Meses de gracia

    // === SEGUROS Y COMISIONES ===
    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal insurancePercentage;           // % seguro desgravamen mensual

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal fixedInsurance;                // Seguro multirriesgo mensual

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal initialCommission;             // Comisión inicial

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal periodicCommission;            // Comisión periódica

    @NotNull
    @Column(precision = 20, scale = SCALE)
    private BigDecimal finalCommission;               // Comisión final

    // === FECHAS ===
    @NotNull
    private LocalDate disbursementDate;               // Fecha de desembolso

    // === TASA PARA CÁLCULOS DE TRANSPARENCIA ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal discountRate;                  // Tasa de descuento para VAN

    // === RELACIONES ===
    @OneToMany(mappedBy = "bond", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flow> flows = new ArrayList<>();

    @OneToOne(mappedBy = "bond", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ResultsFlow resultsFlow;

    // === CONSTRUCTOR PRINCIPAL ===
    public Bond(CreateBondCommand command) {
        this.userId = command.userId();
        this.name = command.name();
        this.currency = ECurrency.valueOf(command.currency());
        
        // Datos básicos del crédito
        this.loanAmount = command.loanAmount();
        this.bbpAmount = command.bbpAmount();
        this.initialFeePercentage = command.initialFeePercentage();
        this.initialFeeAmount = command.initialFeeAmount();
        this.financedCapital = calculateFinancedCapital();
        
        // Tipo de cambio
        this.exchangeRate = command.exchangeRate();
        
        // Tasas e intereses
        this.interestType = EInterestType.valueOf(command.interestType());
        this.annualRate = command.annualRate();
        
        // Configuración de capitalización según tipo de tasa
        if (this.interestType == EInterestType.EFFECTIVE) {
            this.capitalizationPeriod = ECapitalizationPeriod.ANNUALLY;
        } else {
            this.capitalizationPeriod = ECapitalizationPeriod.valueOf(command.capitalizationPeriod());
        }
        
        this.paymentFrequency = EPaymentFrequency.valueOf(command.paymentFrequency());
        
        // Plazos y gracia
        this.totalMonths = command.totalMonths();
        this.gracePeriodType = EGracePeriodType.valueOf(command.gracePeriodType());
        this.gracePeriodMonths = command.gracePeriodMonths();
        
        // Seguros y comisiones
        this.insurancePercentage = command.insurancePercentage();
        this.fixedInsurance = command.fixedInsurance();
        this.initialCommission = command.initialCommission();
        this.periodicCommission = command.periodicCommission();
        this.finalCommission = command.finalCommission();
        
        // Fechas
        this.disbursementDate = command.disbursementDate();
        
        // Tasa de descuento
        this.discountRate = command.discountRate();
        
        // Si es tasa nominal, convertir a efectiva
        convertNominalToEffectiveIfNeeded();
    }

    // === MÉTODOS DE CÁLCULO ===
    
    /**
     * Calcula el capital a financiar después de aplicar BBP y cuota inicial
     */
    private BigDecimal calculateFinancedCapital() {
        BigDecimal capital = this.loanAmount
                .subtract(this.bbpAmount)
                .subtract(this.initialFeeAmount);
        return capital.max(BigDecimal.ZERO); // No puede ser negativo
    }
    
    /**
     * Convierte tasa nominal a efectiva anual si es necesario
     */
    private void convertNominalToEffectiveIfNeeded() {
        if (this.interestType == EInterestType.NOMINAL) {
            int daysOfCap = getDaysFromCapitalizationPeriod(this.capitalizationPeriod);
            
            BigDecimal m = BigDecimal.valueOf(360).divide(BigDecimal.valueOf(daysOfCap), SCALE, BigDecimal.ROUND_HALF_UP);
            BigDecimal rateDivM = this.annualRate.divide(m, SCALE, BigDecimal.ROUND_HALF_UP);
            BigDecimal base = BigDecimal.ONE.add(rateDivM);
            
            this.annualRate = BigDecimal.valueOf(Math.pow(base.doubleValue(), m.doubleValue()))
                    .subtract(BigDecimal.ONE)
                    .setScale(SCALE, BigDecimal.ROUND_HALF_UP);
                    
            this.interestType = EInterestType.EFFECTIVE; // Ahora es efectiva
        }
    }
    
    /**
     * Obtiene días según período de capitalización
     */
    private int getDaysFromCapitalizationPeriod(ECapitalizationPeriod period) {
        return switch (period) {
            case MONTHLY -> 30;
            case QUARTERLY -> 90;
            case SEMI_ANNUALLY -> 180;
            case ANNUALLY -> 360;
        };
    }
    
    /**
     * Obtiene períodos por año según frecuencia de pago
     */
    public int getPeriodsPerYear() {
        return switch (this.paymentFrequency) {
            case MONTHLY -> 12;
            case QUARTERLY -> 4;
            case SEMI_ANNUALLY -> 2;
            case ANNUALLY -> 1;
        };
    }
    
    /**
     * Calcula el número total de períodos de amortización (excluyendo gracia)
     */
    public int getAmortizationPeriods() {
        int graceMonths = this.gracePeriodType != EGracePeriodType.NONE ? this.gracePeriodMonths : 0;
        return this.totalMonths - graceMonths;
    }
    
    // === MÉTODOS DE VALIDACIÓN ===
    
    /**
     * Valida que los datos sean consistentes para el cálculo
     */
    public boolean isValidForCalculation() {
        return this.loanAmount.compareTo(BigDecimal.ZERO) > 0 &&
               this.financedCapital.compareTo(BigDecimal.ZERO) > 0 &&
               this.totalMonths > 0 &&
               this.annualRate.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Actualiza el capital financiado (útil cuando cambian montos)
     */
    public void updateFinancedCapital() {
        this.financedCapital = calculateFinancedCapital();
    }

    // === MÉTODOS MANUALES PARA EVITAR ERRORES DE SÍMBOLO NO ENCONTRADO ===
    public List<Flow> getFlows() { return flows; }
    public void setFlows(List<Flow> flows) { this.flows = flows; }
    public ResultsFlow getResultsFlow() { return resultsFlow; }
    public void setResultsFlow(ResultsFlow resultsFlow) { this.resultsFlow = resultsFlow; }
    public BigDecimal getFinancedCapital() { return financedCapital; }
    public void setFinancedCapital(BigDecimal financedCapital) { this.financedCapital = financedCapital; }
    public Integer getTotalMonths() { return totalMonths; }
    public void setTotalMonths(Integer totalMonths) { this.totalMonths = totalMonths; }
    public Integer getGracePeriodMonths() { return gracePeriodMonths; }
    public void setGracePeriodMonths(Integer gracePeriodMonths) { this.gracePeriodMonths = gracePeriodMonths; }
    public EGracePeriodType getGracePeriodType() { return gracePeriodType; }
    public void setGracePeriodType(EGracePeriodType gracePeriodType) { this.gracePeriodType = gracePeriodType; }
    public BigDecimal getFixedInsurance() { return fixedInsurance; }
    public void setFixedInsurance(BigDecimal fixedInsurance) { this.fixedInsurance = fixedInsurance; }
    public BigDecimal getPeriodicCommission() { return periodicCommission; }
    public void setPeriodicCommission(BigDecimal periodicCommission) { this.periodicCommission = periodicCommission; }
    public EPaymentFrequency getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(EPaymentFrequency paymentFrequency) { this.paymentFrequency = paymentFrequency; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal annualRate) { this.annualRate = annualRate; }
    public BigDecimal getInsurancePercentage() { return insurancePercentage; }
    public void setInsurancePercentage(BigDecimal insurancePercentage) { this.insurancePercentage = insurancePercentage; }
    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public BigDecimal getBbpAmount() { return bbpAmount; }
    public void setBbpAmount(BigDecimal bbpAmount) { this.bbpAmount = bbpAmount; }
    public BigDecimal getInitialFeePercentage() { return initialFeePercentage; }
    public void setInitialFeePercentage(BigDecimal initialFeePercentage) { this.initialFeePercentage = initialFeePercentage; }
    public BigDecimal getInitialFeeAmount() { return initialFeeAmount; }
    public void setInitialFeeAmount(BigDecimal initialFeeAmount) { this.initialFeeAmount = initialFeeAmount; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public EInterestType getInterestType() { return interestType; }
    public void setInterestType(EInterestType interestType) { this.interestType = interestType; }
    public ECapitalizationPeriod getCapitalizationPeriod() { return capitalizationPeriod; }
    public void setCapitalizationPeriod(ECapitalizationPeriod capitalizationPeriod) { this.capitalizationPeriod = capitalizationPeriod; }
    public BigDecimal getInitialCommission() { return initialCommission; }
    public void setInitialCommission(BigDecimal initialCommission) { this.initialCommission = initialCommission; }
    public BigDecimal getFinalCommission() { return finalCommission; }
    public void setFinalCommission(BigDecimal finalCommission) { this.finalCommission = finalCommission; }
    public LocalDate getDisbursementDate() { return disbursementDate; }
    public void setDisbursementDate(LocalDate disbursementDate) { this.disbursementDate = disbursementDate; }
}