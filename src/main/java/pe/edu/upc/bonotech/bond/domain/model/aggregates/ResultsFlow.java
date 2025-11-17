package pe.edu.upc.bonotech.bond.domain.model.aggregates;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import pe.edu.upc.bonotech.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultsFlow extends AuditableAbstractAggregateRoot<ResultsFlow> {
    private static final int SCALE = 10;

    @OneToOne(fetch = FetchType.LAZY)
    private Bond bond;

    // === INDICADORES DE TRANSPARENCIA FINANCIERA (Requeridos por SBS) ===
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal van;                    // VALOR ACTUAL NETO
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal tir;                    // TASA INTERNA DE RETORNO
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal tcea;                   // TASA COSTO EFECTIVO ANUAL

    // === RESUMEN DE PAGOS TOTALES ===
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalBasePayments;      // Suma de todas las cuotas base
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalInterest;          // Intereses totales pagados
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalAmortization;      // Amortización total (debe ser = capital financiado)
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalInsurance;         // Total seguros desgravamen
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalFixedInsurance;    // Total seguro multirriesgo
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalCommissions;       // Total comisiones periódicas
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalCosts;             // Costos totales (seguros + comisiones)

    // === TOTAL GLOBAL ===
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal totalPaid;              // Total pagado por el cliente

    // === INDICADORES ADICIONALES ===
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal averageMonthlyPayment;  // Cuota promedio mensual
    
    @Column(precision = 20, scale = SCALE)
    private BigDecimal costOfCredit;           // Costo total del crédito (intereses + costos)

    // === MÉTODOS DE CÁLCULO ===
    
    /**
     * Calcula todos los totales basados en los flujos del crédito
     */
    public void calculateTotals() {
        if (bond == null || bond.getFlows() == null) return;
        
        this.totalBasePayments = BigDecimal.ZERO;
        this.totalInterest = BigDecimal.ZERO;
        this.totalAmortization = BigDecimal.ZERO;
        this.totalInsurance = BigDecimal.ZERO;
        this.totalFixedInsurance = BigDecimal.ZERO;
        this.totalCommissions = BigDecimal.ZERO;
        
        for (Flow flow : bond.getFlows()) {
            if (flow.isAmortizationPeriod()) {
                this.totalBasePayments = this.totalBasePayments.add(flow.getBasePayment());
                this.totalInterest = this.totalInterest.add(flow.getInterest());
                this.totalAmortization = this.totalAmortization.add(flow.getAmortization());
            }
            
            this.totalInsurance = this.totalInsurance.add(flow.getInsuranceAmount());
            this.totalFixedInsurance = this.totalFixedInsurance.add(flow.getFixedInsuranceAmount());
            this.totalCommissions = this.totalCommissions.add(flow.getCommissionAmount());
        }
        
        // Calcular costos totales
        this.totalCosts = this.totalInsurance
            .add(this.totalFixedInsurance)
            .add(this.totalCommissions)
            .add(bond.getInitialCommission())
            .add(bond.getFinalCommission());
        
        // Calcular total pagado
        this.totalPaid = this.totalBasePayments
            .add(this.totalCosts);
        
        // Calcular costo del crédito
        this.costOfCredit = this.totalInterest
            .add(this.totalCosts);
        
        // Calcular cuota promedio
        if (bond.getTotalMonths() > 0) {
            this.averageMonthlyPayment = this.totalPaid
                .divide(BigDecimal.valueOf(bond.getTotalMonths()), SCALE, BigDecimal.ROUND_HALF_UP);
        }
        
        // Validar que la amortización total sea igual al capital financiado
        validateAmortization();
    }
    
    /**
     * Valida que la amortización total coincida con el capital financiado
     */
    private void validateAmortization() {
        if (bond != null && totalAmortization != null) {
            BigDecimal financedCapital = bond.getFinancedCapital();
            BigDecimal difference = totalAmortization.subtract(financedCapital).abs();
            
            if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) {
                // Log warning - pequeña diferencia por redondeo
                System.err.println("Advertencia: Diferencia en amortización: " + difference);
            }
        }
    }
    
    /**
     * Calcula el VAN basado en los flujos y tasa de descuento
     */
    public void calculateVAN(BigDecimal discountRate) {
        if (bond == null || bond.getFlows() == null || discountRate == null) return;
        
        BigDecimal van = BigDecimal.ZERO;
        BigDecimal periodicDiscountRate = calculatePeriodicDiscountRate(discountRate);
        
        // Flujo inicial (período 0) - desembolso neto para el cliente
        BigDecimal initialFlow = bond.getFinancedCapital().subtract(bond.getInitialCommission());
        van = van.add(initialFlow);
        
        // Flujos periódicos (períodos 1..n)
        for (Flow flow : bond.getFlows()) {
            if (flow.getPeriodNumber() > 0) {
                BigDecimal discountFactor = BigDecimal.ONE.add(periodicDiscountRate)
                    .pow(flow.getPeriodNumber(), java.math.MathContext.DECIMAL128);
                BigDecimal presentValue = flow.getTotalPayment().negate() // Negativo porque es pago
                    .divide(discountFactor, SCALE, BigDecimal.ROUND_HALF_UP);
                van = van.add(presentValue);
            }
        }
        
        this.van = van;
    }
    
    /**
     * Calcula la TIR usando método de aproximación numérica
     */
    public void calculateTIR() {
        if (bond == null || bond.getFlows() == null) return;
        
        // Método simplificado - en producción usar algoritmo más robusto
        BigDecimal lowRate = BigDecimal.valueOf(0.0001);   // 0.01%
        BigDecimal highRate = BigDecimal.valueOf(2.0);     // 200%
        BigDecimal precision = BigDecimal.valueOf(0.0001); // 0.01%
        
        this.tir = calculateTIRBinarySearch(lowRate, highRate, precision);
        this.tcea = calculateTCEAFromTIR(this.tir);
    }
    
    /**
     * Búsqueda binaria para encontrar TIR
     */
    private BigDecimal calculateTIRBinarySearch(BigDecimal low, BigDecimal high, BigDecimal precision) {
        BigDecimal mid = BigDecimal.ZERO;
        
        for (int i = 0; i < 100; i++) { // Límite de iteraciones
            mid = low.add(high).divide(BigDecimal.valueOf(2), SCALE, BigDecimal.ROUND_HALF_UP);
            
            BigDecimal vanMid = calculateVANForRate(mid);
            
            if (vanMid.abs().compareTo(precision) <= 0) {
                break; // Encontrado
            } else if (vanMid.compareTo(BigDecimal.ZERO) > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }
        
        return mid;
    }
    
    /**
     * Calcula VAN para una tasa dada
     */
    private BigDecimal calculateVANForRate(BigDecimal rate) {
        BigDecimal van = BigDecimal.ZERO;
        BigDecimal periodicRate = calculatePeriodicDiscountRate(rate);
        
        // Flujo inicial
        BigDecimal initialFlow = bond.getFinancedCapital().subtract(bond.getInitialCommission());
        van = van.add(initialFlow);
        
        // Flujos periódicos
        for (Flow flow : bond.getFlows()) {
            if (flow.getPeriodNumber() > 0) {
                BigDecimal discountFactor = BigDecimal.ONE.add(periodicRate)
                    .pow(flow.getPeriodNumber(), java.math.MathContext.DECIMAL128);
                BigDecimal presentValue = flow.getTotalPayment().negate()
                    .divide(discountFactor, SCALE, BigDecimal.ROUND_HALF_UP);
                van = van.add(presentValue);
            }
        }
        
        return van;
    }
    
    /**
     * Convierte tasa anual a tasa periódica según frecuencia de pago
     */
    private BigDecimal calculatePeriodicDiscountRate(BigDecimal annualRate) {
        int periodsPerYear = bond.getPeriodsPerYear();
        return BigDecimal.valueOf(Math.pow(annualRate.doubleValue() + 1, 1.0 / periodsPerYear))
            .subtract(BigDecimal.ONE)
            .setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calcula TCEA a partir de TIR periódica
     */
    private BigDecimal calculateTCEAFromTIR(BigDecimal periodicTIR) {
        int periodsPerYear = bond.getPeriodsPerYear();
        return BigDecimal.valueOf(Math.pow(periodicTIR.doubleValue() + 1, periodsPerYear))
            .subtract(BigDecimal.ONE)
            .setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    }
    
    // === MÉTODOS DE VALIDACIÓN ===
    
    /**
     * Verifica si los resultados son consistentes
     */
    public boolean isValid() {
        return this.van != null &&
               this.tir != null &&
               this.tcea != null &&
               this.totalPaid != null &&
               this.totalPaid.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Obtiene resumen en formato legible
     */
    public String getSummary() {
        return String.format(
            "VAN: %s, TIR: %.2f%%, TCEA: %.2f%%, Total Pagado: %s",
            van != null ? van.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "N/A",
            tir != null ? tir.multiply(BigDecimal.valueOf(100)).doubleValue() : 0,
            tcea != null ? tcea.multiply(BigDecimal.valueOf(100)).doubleValue() : 0,
            totalPaid != null ? totalPaid.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "N/A"
        );
    }
}