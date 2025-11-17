package pe.edu.upc.bonotech.shared.infrastructure.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;
import pe.edu.upc.bonotech.shared.domain.services.CurrencyConversionService;

@Service
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private static final BigDecimal PEN_TO_USD_RATE = new BigDecimal("0.26");  // Ejemplo: 1 PEN = 0.26 USD
    private static final BigDecimal USD_TO_PEN_RATE = new BigDecimal("3.85");  // Ejemplo: 1 USD = 3.85 PEN
    private static final int SCALE = 10;

    @Override
    public BigDecimal convert(BigDecimal amount, ECurrency fromCurrency, ECurrency toCurrency) {
        if (amount == null || fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Amount y currencies no pueden ser nulos");
        }

        // Si es la misma moneda, no hay conversión
        if (fromCurrency == toCurrency) {
            return amount.setScale(SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal exchangeRate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(exchangeRate).setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getExchangeRate(ECurrency fromCurrency, ECurrency toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currencies no pueden ser nulos");
        }

        // Misma moneda
        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE;
        }

        // Conversiones PEN ↔ USD
        if (fromCurrency == ECurrency.PEN && toCurrency == ECurrency.USD) {
            return PEN_TO_USD_RATE;
        } else if (fromCurrency == ECurrency.USD && toCurrency == ECurrency.PEN) {
            return USD_TO_PEN_RATE;
        }

        // Para otras conversiones (podrías expandir esto)
        throw new UnsupportedOperationException(
            "Conversión no soportada: " + fromCurrency + " a " + toCurrency
        );
    }

    /**
     * Actualiza las tasas de cambio (útil para integración con APIs externas)
     */
    public void updateExchangeRates(BigDecimal penToUsdRate, BigDecimal usdToPenRate) {
        // En una implementación real, esto vendría de una API externa como SUNAT, BCRP, etc.
        // PEN_TO_USD_RATE = penToUsdRate;
        // USD_TO_PEN_RATE = usdToPenRate;
    }

    /**
     * Obtiene el tipo de cambio actual para logging/monitoreo
     */
    public String getCurrentRatesInfo() {
        return String.format(
            "Tasas actuales - PEN→USD: %s, USD→PEN: %s",
            PEN_TO_USD_RATE.setScale(4, RoundingMode.HALF_UP),
            USD_TO_PEN_RATE.setScale(4, RoundingMode.HALF_UP)
        );
    }
}