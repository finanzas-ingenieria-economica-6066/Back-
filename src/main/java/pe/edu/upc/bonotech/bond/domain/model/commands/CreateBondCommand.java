package pe.edu.upc.bonotech.bond.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBondCommand(
    // === DATOS DEL USUARIO Y IDENTIFICACIÓN ===
    Long userId,
    String name,
    
    // === DATOS BÁSICOS DEL CRÉDITO MIVIVIENDA ===
    String currency,
    BigDecimal loanAmount,                    // Monto del crédito solicitado
    BigDecimal bbpAmount,                     // Bono del Buen Pagador
    BigDecimal initialFeePercentage,          // % de cuota inicial
    BigDecimal initialFeeAmount,              // Monto de cuota inicial
    
    // === TIPO DE CAMBIO ===
    BigDecimal exchangeRate,                  // TC PEN/USD
    
    // === TASAS E INTERESES ===
    String interestType,
    BigDecimal annualRate,                    // Tasa anual (TEA o TNA)
    String capitalizationPeriod,
    String paymentFrequency,
    
    // === PLAZOS Y PERÍODOS DE GRACIA ===
    Integer totalMonths,                      // Plazo total en meses
    String gracePeriodType,                   // NINGUNO, PARCIAL, TOTAL
    Integer gracePeriodMonths,                // Meses de gracia
    
    // === SEGUROS Y COMISIONES ===
    BigDecimal insurancePercentage,           // % seguro desgravamen mensual
    BigDecimal fixedInsurance,                // Seguro multirriesgo mensual
    BigDecimal initialCommission,             // Comisión inicial
    BigDecimal periodicCommission,            // Comisión periódica
    BigDecimal finalCommission,               // Comisión final
    
    // === FECHAS ===
    LocalDate disbursementDate,               // Fecha de desembolso
    
    // === TASA PARA CÁLCULOS DE TRANSPARENCIA ===
    BigDecimal discountRate                   // Tasa de descuento para VAN
) {
    
    // === VALORES POR DEFECTO Y VALIDACIONES ===
    public CreateBondCommand {
        // Validaciones básicas
        if (userId == null) throw new IllegalArgumentException("UserId no puede ser nulo");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nombre no puede estar vacío");
        if (currency == null) throw new IllegalArgumentException("Moneda no puede ser nula");
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) 
            throw new IllegalArgumentException("Monto del crédito debe ser positivo");
        
        // Valores por defecto para campos opcionales
        if (bbpAmount == null) bbpAmount = BigDecimal.ZERO;
        if (initialFeePercentage == null) initialFeePercentage = BigDecimal.ZERO;
        if (initialFeeAmount == null) initialFeeAmount = BigDecimal.ZERO;
        if (exchangeRate == null) exchangeRate = BigDecimal.ONE; // Default 1:1
        if (annualRate == null) annualRate = BigDecimal.ZERO;
        if (totalMonths == null) totalMonths = 240; // 20 años por defecto
        if (gracePeriodMonths == null) gracePeriodMonths = 0;
        if (insurancePercentage == null) insurancePercentage = BigDecimal.ZERO;
        if (fixedInsurance == null) fixedInsurance = BigDecimal.ZERO;
        if (initialCommission == null) initialCommission = BigDecimal.ZERO;
        if (periodicCommission == null) periodicCommission = BigDecimal.ZERO;
        if (finalCommission == null) finalCommission = BigDecimal.ZERO;
        if (disbursementDate == null) disbursementDate = LocalDate.now();
        if (discountRate == null) discountRate = BigDecimal.ZERO;
        
        // Validar que montos no sean negativos
        validateNonNegative(bbpAmount, "Bono del Buen Pagador");
        validateNonNegative(initialFeeAmount, "Cuota inicial");
        validateNonNegative(exchangeRate, "Tipo de cambio");
        validateNonNegative(annualRate, "Tasa anual");
        validateNonNegative(insurancePercentage, "Seguro desgravamen");
        validateNonNegative(fixedInsurance, "Seguro multirriesgo");
        validateNonNegative(initialCommission, "Comisión inicial");
        validateNonNegative(periodicCommission, "Comisión periódica");
        validateNonNegative(finalCommission, "Comisión final");
        validateNonNegative(discountRate, "Tasa de descuento");
    }
    
    private static void validateNonNegative(BigDecimal value, String fieldName) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " no puede ser negativo");
        }
    }
    
    // === MÉTODOS DE CONVENIENCIA ===
    
    /**
     * Calcula el capital a financiar basado en los montos proporcionados
     */
    public BigDecimal calculateFinancedCapital() {
        BigDecimal capital = this.loanAmount
            .subtract(this.bbpAmount)
            .subtract(this.initialFeeAmount);
        return capital.max(BigDecimal.ZERO);
    }
    
    /**
     * Verifica si hay conversión de moneda
     */
    public boolean requiresCurrencyConversion() {
        return this.exchangeRate != null && 
               this.exchangeRate.compareTo(BigDecimal.ONE) != 0;
    }
    
    /**
     * Verifica si tiene período de gracia
     */
    public boolean hasGracePeriod() {
        return gracePeriodType != null && 
               !gracePeriodType.equals("NONE") && 
               gracePeriodMonths != null && 
               gracePeriodMonths > 0;
    }
}