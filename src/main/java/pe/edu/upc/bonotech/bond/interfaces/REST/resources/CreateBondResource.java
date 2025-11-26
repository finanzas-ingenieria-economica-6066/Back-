package pe.edu.upc.bonotech.bond.interfaces.REST.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

import pe.edu.upc.bonotech.bond.domain.model.commands.CreateBondCommand;

public record CreateBondResource(
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
    public CreateBondCommand toCommand() {
        return new CreateBondCommand(
            userId,
            name,
            currency,
            loanAmount,
            bbpAmount,
            initialFeePercentage,
            initialFeeAmount,
            exchangeRate,
            interestType,
            annualRate,
            capitalizationPeriod,
            paymentFrequency,
            totalMonths,
            gracePeriodType,
            gracePeriodMonths,
            insurancePercentage,
            fixedInsurance,
            initialCommission,
            periodicCommission,
            finalCommission,
            disbursementDate,
            discountRate
        );
    }
}