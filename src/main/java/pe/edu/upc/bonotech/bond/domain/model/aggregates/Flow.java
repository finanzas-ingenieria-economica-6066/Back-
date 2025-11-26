package pe.edu.upc.bonotech.bond.domain.model.aggregates;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.bonotech.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Flow extends AuditableAbstractAggregateRoot<Flow> {
    // Constructor simple requerido por BondCommandService (4 parámetros)
    public Flow(Bond bond, int periodNumber, String periodType, BigDecimal periodicRate) {
        this.bond = bond;
        this.periodNumber = periodNumber;
        this.periodType = periodType;
        this.periodicRate = periodicRate;
    }

    // Constructor completo requerido por tests y BondCommandService (12 parámetros)
    public Flow(Bond bond, int periodNumber, String periodType,
                BigDecimal initialBalance, BigDecimal finalBalance,
                BigDecimal basePayment, BigDecimal interest, BigDecimal amortization,
                BigDecimal insuranceAmount, BigDecimal fixedInsuranceAmount, BigDecimal commissionAmount,
                BigDecimal totalPayment, BigDecimal periodicRate) {
        this.bond = bond;
        this.periodNumber = periodNumber;
        this.periodType = periodType;
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.basePayment = basePayment;
        this.interest = interest;
        this.amortization = amortization;
        this.insuranceAmount = insuranceAmount;
        this.fixedInsuranceAmount = fixedInsuranceAmount;
        this.commissionAmount = commissionAmount;
        this.totalPayment = totalPayment;
        this.periodicRate = periodicRate;
    }

    private static final int SCALE = 10;

    @ManyToOne(fetch = FetchType.EAGER)
    private Bond bond;

    // === IDENTIFICACIÓN DEL PERÍODO ===
    private Integer periodNumber;              // Número del período (1, 2, 3...)
    private String periodType;                 // "GRACE_TOTAL", "GRACE_PARTIAL", "AMORTIZATION"

    // === SALDOS ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal initialBalance;         // Saldo inicial del período

    @Column(precision = 20, scale = SCALE)
    private BigDecimal finalBalance;           // Saldo final del período

    // === COMPOSICIÓN DE LA CUOTA BASE (MÉTODO FRANCÉS) ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal basePayment;            // Cuota base (interés + amortización)

    @Column(precision = 20, scale = SCALE)
    private BigDecimal interest;               // Interés del período

    @Column(precision = 20, scale = SCALE)
    private BigDecimal amortization;           // Amortización de capital

    // === SEGUROS Y COMISIONES ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal insuranceAmount;        // Seguro desgravamen

    @Column(precision = 20, scale = SCALE)
    private BigDecimal fixedInsuranceAmount;   // Seguro multirriesgo

    @Column(precision = 20, scale = SCALE)
    private BigDecimal commissionAmount;       // Comisión periódica

    // === TOTALES ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalPayment;           // Pago total del período

    // === TASAS ===
    @Column(precision = 20, scale = SCALE)
    private BigDecimal periodicRate;           // Tasa periódica efectiva

    // === MÉTODOS DE CONVENIENCIA ===
    
    /**
     * Calcula el pago total sumando todos los componentes
     */
    public void calculateTotalPayment() {
        BigDecimal base = this.basePayment != null ? this.basePayment : BigDecimal.ZERO;
        BigDecimal insurance = this.insuranceAmount != null ? this.insuranceAmount : BigDecimal.ZERO;
        BigDecimal fixedInsurance = this.fixedInsuranceAmount != null ? this.fixedInsuranceAmount : BigDecimal.ZERO;
        BigDecimal commission = this.commissionAmount != null ? this.commissionAmount : BigDecimal.ZERO;
        
        this.totalPayment = base
            .add(insurance)
            .add(fixedInsurance)
            .add(commission);
    }
    
    /**
     * Verifica si es un período de gracia
     */
    public boolean isGracePeriod() {
        return "GRACE_TOTAL".equals(this.periodType) || 
               "GRACE_PARTIAL".equals(this.periodType);
    }
    
    /**
     * Verifica si es período de amortización
     */
    public boolean isAmortizationPeriod() {
        return "AMORTIZATION".equals(this.periodType);
    }
    
    /**
     * Obtiene el flujo de caja para cálculo de VAN/TIR
     * Positivo para entradas (desembolso), negativo para salidas (pagos)
     */
    public BigDecimal getCashFlow() {
        if (periodNumber == null || periodNumber == 0) {
            // Período 0: desembolso (entrada positiva para el cliente)
            if (bond != null) {
                return bond.getFinancedCapital().subtract(bond.getInitialCommission());
            }
            return BigDecimal.ZERO;
        } else {
            // Períodos 1..n: pagos (salida negativa para el cliente)
            BigDecimal payment = this.totalPayment != null ? this.totalPayment : BigDecimal.ZERO;
            return payment.negate();
        }
    }
}