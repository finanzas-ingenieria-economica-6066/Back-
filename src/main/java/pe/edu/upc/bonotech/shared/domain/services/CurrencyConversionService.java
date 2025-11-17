package pe.edu.upc.bonotech.shared.domain.services;

import java.math.BigDecimal;

import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;

public interface CurrencyConversionService {

    /**
     * Convierte un valor monetario de una moneda a otra
     * 
     * @param amount       El monto a convertir
     * @param fromCurrency La moneda origen
     * @param toCurrency   La moneda destino
     * @return El monto convertido
     */
    BigDecimal convert(BigDecimal amount, ECurrency fromCurrency, ECurrency toCurrency);

    /**
     * Obtiene la tasa de cambio entre dos monedas
     * 
     * @param fromCurrency La moneda origen
     * @param toCurrency   La moneda destino
     * @return La tasa de cambio
     */
    BigDecimal getExchangeRate(ECurrency fromCurrency, ECurrency toCurrency);
}
