package pe.edu.upc.bonotech.bond.domain.model.aggregates;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.bonotech.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Flow extends AuditableAbstractAggregateRoot<Flow> {

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
        this.totalPayment = this.basePayment
            .add(this.insuranceAmount)
            .add(this.fixedInsuranceAmount)
            .add(this.commissionAmount);
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
        if (periodNumber == 0) {
            // Período 0: desembolso (entrada positiva para el cliente)
            return bond.getFinancedCapital().subtract(bond.getInitialCommission());
        } else {
            // Períodos 1..n: pagos (salida negativa para el cliente)
            return this.totalPayment.negate();
        }
    }
}