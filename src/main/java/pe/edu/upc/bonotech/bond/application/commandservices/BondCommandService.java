package pe.edu.upc.bonotech.bond.application.commandservices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;
import pe.edu.upc.bonotech.bond.domain.model.aggregates.Flow;
import pe.edu.upc.bonotech.bond.domain.model.aggregates.ResultsFlow;
import pe.edu.upc.bonotech.bond.domain.model.commands.CreateBondCommand;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.EGracePeriodType;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.EPaymentFrequency;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECurrency;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.EInterestType;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.ECapitalizationPeriod;
import pe.edu.upc.bonotech.bond.domain.services.IBondCommandService;
import pe.edu.upc.bonotech.bond.infrastructure.persistence.jpa.repositories.BondRepository;
import pe.edu.upc.bonotech.bond.interfaces.REST.resources.UpdateBondResource;
import pe.edu.upc.bonotech.shared.domain.services.CurrencyConversionService;

@Service
@Transactional
public class BondCommandService implements IBondCommandService {
    @Override
    public Optional<Bond> updateAndRecalculateLoan(Long loanId, CreateBondCommand command) {
        // Implementación real pendiente
        return Optional.empty();
    }

    @Override
    public Optional<Bond> partialUpdateAndRecalculateLoan(Long loanId, UpdateBondResource resource) {
        // Implementación real pendiente
        return Optional.empty();
    }

    @Override
    public boolean deleteLoan(Long loanId) {
        // Implementación real pendiente
        return false;
    }

    private static final int SCALE = 10;
    private static final int DAYS_PER_YEAR = 360;
    private static final int DAYS_PER_MONTH = 30;

    @Autowired
    private BondRepository bondRepository;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Override
    public Optional<Bond> handle(CreateBondCommand command) {
        try {
            var bond = new Bond(command);
            
            if (!bond.isValidForCalculation()) {
                throw new IllegalArgumentException("Datos del crédito no válidos para cálculo");
            }
            
            var createdBond = bondRepository.save(bond);

            // Calcular cronograma completo
            generatePaymentSchedule(createdBond);
            
            // Calcular resultados financieros
            calculateFinancialResults(createdBond);

            return Optional.of(bondRepository.save(createdBond));
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el crédito: " + e.getMessage(), e);
        }
    }

    /**
     * Genera el cronograma de pagos completo (períodos de gracia + amortización)
     */
    private void generatePaymentSchedule(Bond bond) {
        bond.getFlows().clear();
        
        BigDecimal saldo = bond.getFinancedCapital();
        BigDecimal tep = calculateTEP(bond);
        int totalPeriods = bond.getTotalMonths();
        int gracePeriods = bond.getGracePeriodMonths();
        EGracePeriodType graceType = bond.getGracePeriodType();
        
        // 1. Períodos de gracia
        for (int period = 1; period <= gracePeriods; period++) {
            Flow flow = createGracePeriodFlow(bond, period, saldo, tep, graceType);
            bond.getFlows().add(flow);
            
            // Actualizar saldo según tipo de gracia
            if (graceType == EGracePeriodType.TOTAL) {
                // Gracia total: intereses se capitalizan
                BigDecimal interest = saldo.multiply(tep);
                saldo = saldo.add(interest);
            }
            // Gracia parcial: saldo se mantiene igual
        }
        
        // 2. Períodos de amortización
        BigDecimal cuota = calculateFrenchMethodPayment(saldo, tep, totalPeriods - gracePeriods);
        
        for (int period = gracePeriods + 1; period <= totalPeriods; period++) {
            Flow flow = createAmortizationFlow(bond, period, saldo, cuota, tep);
            bond.getFlows().add(flow);
            
            // Actualizar saldo
            BigDecimal interest = saldo.multiply(tep);
            BigDecimal amortization = cuota.subtract(interest);
            saldo = saldo.subtract(amortization);
        }
        
        // Asegurar que el último saldo sea cero (ajuste por redondeo)
        adjustFinalBalance(bond);
    }

    /**
     * Crea un flujo para período de gracia
     */
    private Flow createGracePeriodFlow(Bond bond, int period, BigDecimal saldo, BigDecimal tep, EGracePeriodType graceType) {
        BigDecimal interest = saldo.multiply(tep);
        BigDecimal insurance = calculateInsurance(bond, saldo);
        
        BigDecimal basePayment = BigDecimal.ZERO;
        BigDecimal totalPayment = BigDecimal.ZERO;
        
        if (graceType == EGracePeriodType.PARTIAL) {
            // Gracia parcial: solo se pagan intereses
            basePayment = interest;
            totalPayment = basePayment.add(insurance).add(bond.getFixedInsurance()).add(bond.getPeriodicCommission());
        } else {
            // Gracia total: no se paga nada, intereses se capitalizan
            totalPayment = insurance.add(bond.getFixedInsurance()).add(bond.getPeriodicCommission());
        }
        
        return new Flow(
            bond,
            period,
            graceType == EGracePeriodType.TOTAL ? "GRACE_TOTAL" : "GRACE_PARTIAL",
            saldo,
            graceType == EGracePeriodType.TOTAL ? saldo.add(interest) : saldo, // Saldo final
            basePayment,
            interest,
            BigDecimal.ZERO, // Amortización cero en gracia
            insurance,
            bond.getFixedInsurance(),
            bond.getPeriodicCommission(),
            totalPayment,
            tep
        );
    }

    /**
     * Crea un flujo para período de amortización
     */
    private Flow createAmortizationFlow(Bond bond, int period, BigDecimal saldo, BigDecimal cuota, BigDecimal tep) {
        BigDecimal interest = saldo.multiply(tep);
        BigDecimal amortization = cuota.subtract(interest);
        BigDecimal insurance = calculateInsurance(bond, saldo);
        BigDecimal finalBalance = saldo.subtract(amortization).max(BigDecimal.ZERO);
        
        BigDecimal totalPayment = cuota.add(insurance).add(bond.getFixedInsurance()).add(bond.getPeriodicCommission());
        
        return new Flow(
            bond,
            period,
            "AMORTIZATION",
            saldo,              // initialBalance
            finalBalance,      // finalBalance
            cuota,             // basePayment
            interest,          // interest
            amortization,      // amortization
            insurance,         // insuranceAmount
            bond.getFixedInsurance(),  // fixedInsuranceAmount
            bond.getPeriodicCommission(), // commissionAmount
            totalPayment,      // totalPayment
            tep                // periodicRate
        );
    }

    /**
     * Calcula la cuota usando método francés
     */
    private BigDecimal calculateFrenchMethodPayment(BigDecimal capital, BigDecimal tep, int periods) {
        if (periods <= 0) return BigDecimal.ZERO;
        
        BigDecimal uno = BigDecimal.ONE;
        BigDecimal factor = uno.subtract(
            BigDecimal.valueOf(Math.pow(uno.add(tep).doubleValue(), -periods))
        ).setScale(SCALE, RoundingMode.HALF_UP);
        
        return capital.multiply(tep).divide(factor, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula Tasa Efectiva Periódica (TEP)
     */
    private BigDecimal calculateTEP(Bond bond) {
        int daysInPeriod = getDaysInPeriod(bond.getPaymentFrequency());
        
        BigDecimal n2 = BigDecimal.valueOf(daysInPeriod);
        BigDecimal n1 = BigDecimal.valueOf(DAYS_PER_YEAR);
        BigDecimal base = BigDecimal.ONE.add(bond.getAnnualRate());
        BigDecimal exponent = n2.divide(n1, SCALE, RoundingMode.HALF_UP);

        return BigDecimal.valueOf(Math.pow(base.doubleValue(), exponent.doubleValue()))
                .subtract(BigDecimal.ONE)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Obtiene días según frecuencia de pago
     */
    private int getDaysInPeriod(EPaymentFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> DAYS_PER_MONTH;
            case QUARTERLY -> 90;
            case SEMI_ANNUALLY -> 180;
            case ANNUALLY -> DAYS_PER_YEAR;
        };
    }

    /**
     * Calcula seguro desgravamen
     */
    private BigDecimal calculateInsurance(Bond bond, BigDecimal saldo) {
        return saldo.multiply(bond.getInsurancePercentage());
    }

    /**
     * Ajusta el saldo final para evitar decimales residuales
     */
    private void adjustFinalBalance(Bond bond) {
        if (bond.getFlows().isEmpty()) return;
        
        Flow lastFlow = bond.getFlows().get(bond.getFlows().size() - 1);
        BigDecimal finalBalance = lastFlow.getFinalBalance();
        
        if (finalBalance.abs().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            lastFlow.setFinalBalance(BigDecimal.ZERO);
            
            // Ajustar la última amortización para cuadrar
            if (lastFlow.isAmortizationPeriod()) {
                BigDecimal adjustedAmortization = lastFlow.getInitialBalance();
                lastFlow.setAmortization(adjustedAmortization);
                lastFlow.setBasePayment(lastFlow.getInterest().add(adjustedAmortization));
                lastFlow.calculateTotalPayment();
            }
        }
    }

    /**
     * Calcula todos los resultados financieros (VAN, TIR, TCEA, etc.)
     */
    private void calculateFinancialResults(Bond bond) {
        ResultsFlow results = bond.getResultsFlow();
        if (results == null) {
            results = new ResultsFlow();
            results.setBond(bond);
        }
        
        // Calcular totales
        results.calculateTotals();
        
        // Calcular VAN si hay tasa de descuento
        if (bond.getDiscountRate() != null && bond.getDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            results.calculateVAN(bond.getDiscountRate());
        }
        
        // Calcular TIR y TCEA
        results.calculateTIR();
        
        bond.setResultsFlow(results);
    }

    // Métodos eliminados porque no existen en la interfaz

    /**
     * Actualiza el bond desde un command completo
     */
    private void updateBondFromCommand(Bond bond, CreateBondCommand command) {
        bond.setName(command.name());
        bond.setCurrency(ECurrency.valueOf(command.currency()));
        bond.setLoanAmount(command.loanAmount());
        bond.setBbpAmount(command.bbpAmount());
        bond.setInitialFeePercentage(command.initialFeePercentage());
        bond.setInitialFeeAmount(command.initialFeeAmount());
        bond.setExchangeRate(command.exchangeRate());
        bond.setInterestType(EInterestType.valueOf(command.interestType()));
        bond.setAnnualRate(command.annualRate());
        bond.setCapitalizationPeriod(ECapitalizationPeriod.valueOf(command.capitalizationPeriod()));
        bond.setPaymentFrequency(EPaymentFrequency.valueOf(command.paymentFrequency()));
        bond.setTotalMonths(command.totalMonths());
        bond.setGracePeriodType(EGracePeriodType.valueOf(command.gracePeriodType()));
        bond.setGracePeriodMonths(command.gracePeriodMonths());
        bond.setInsurancePercentage(command.insurancePercentage());
        bond.setFixedInsurance(command.fixedInsurance());
        bond.setInitialCommission(command.initialCommission());
        bond.setPeriodicCommission(command.periodicCommission());
        bond.setFinalCommission(command.finalCommission());
        bond.setDisbursementDate(command.disbursementDate());
        bond.setDiscountRate(command.discountRate());
        
        bond.updateFinancedCapital();
    }

    /**
     * Actualiza el bond desde un resource parcial
     */
    private void updateBondFromResource(Bond bond, UpdateBondResource resource) {
        if (resource.getName() != null) bond.setName(resource.getName());
        if (resource.getCurrency() != null) bond.setCurrency(ECurrency.valueOf(resource.getCurrency()));
        if (resource.getLoanAmount() != null) bond.setLoanAmount(resource.getLoanAmount());
        if (resource.getBbpAmount() != null) bond.setBbpAmount(resource.getBbpAmount());
        if (resource.getInitialFeePercentage() != null) bond.setInitialFeePercentage(resource.getInitialFeePercentage());
        if (resource.getInitialFeeAmount() != null) bond.setInitialFeeAmount(resource.getInitialFeeAmount());
        if (resource.getExchangeRate() != null) bond.setExchangeRate(resource.getExchangeRate());
        if (resource.getInterestType() != null) bond.setInterestType(EInterestType.valueOf(resource.getInterestType()));
        if (resource.getAnnualRate() != null) bond.setAnnualRate(resource.getAnnualRate());
        if (resource.getCapitalizationPeriod() != null) bond.setCapitalizationPeriod(ECapitalizationPeriod.valueOf(resource.getCapitalizationPeriod()));
        if (resource.getPaymentFrequency() != null) bond.setPaymentFrequency(EPaymentFrequency.valueOf(resource.getPaymentFrequency()));
        if (resource.getTotalMonths() != null) bond.setTotalMonths(resource.getTotalMonths());
        if (resource.getGracePeriodType() != null) bond.setGracePeriodType(EGracePeriodType.valueOf(resource.getGracePeriodType()));
        if (resource.getGracePeriodMonths() != null) bond.setGracePeriodMonths(resource.getGracePeriodMonths());
        if (resource.getInsurancePercentage() != null) bond.setInsurancePercentage(resource.getInsurancePercentage());
        if (resource.getFixedInsurance() != null) bond.setFixedInsurance(resource.getFixedInsurance());
        if (resource.getInitialCommission() != null) bond.setInitialCommission(resource.getInitialCommission());
        if (resource.getPeriodicCommission() != null) bond.setPeriodicCommission(resource.getPeriodicCommission());
        if (resource.getFinalCommission() != null) bond.setFinalCommission(resource.getFinalCommission());
        if (resource.getDisbursementDate() != null) bond.setDisbursementDate(resource.getDisbursementDate());
        if (resource.getDiscountRate() != null) bond.setDiscountRate(resource.getDiscountRate());
        
        bond.updateFinancedCapital();
    }
}