package pe.edu.upc.bonotech.bond.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FlowTest {

    @Test
    void testCreateAmortizationFlow() {
        // Given
        Bond bond = new Bond();
        BigDecimal initialBalance = new BigDecimal("100000");
        BigDecimal basePayment = new BigDecimal("1321.51");
        BigDecimal interest = new BigDecimal("833.33");
        BigDecimal amortization = new BigDecimal("488.18");
        BigDecimal insurance = new BigDecimal("50.00");
        BigDecimal fixedInsurance = new BigDecimal("25.00");
        BigDecimal commission = new BigDecimal("10.00");
        BigDecimal periodicRate = new BigDecimal("0.008333");

        // When
        Flow flow = new Flow(
            bond, 1, "AMORTIZATION", 
            initialBalance, initialBalance.subtract(amortization),
            basePayment, interest, amortization,
            insurance, fixedInsurance, commission,
            basePayment.add(insurance).add(fixedInsurance).add(commission),
            periodicRate
        );

        // Then
        assertNotNull(flow);
        assertEquals(1, flow.getPeriodNumber());
        assertEquals("AMORTIZATION", flow.getPeriodType());
        assertEquals(initialBalance, flow.getInitialBalance());
        assertEquals(interest, flow.getInterest());
        assertEquals(amortization, flow.getAmortization());
        assertTrue(flow.isAmortizationPeriod());
        assertFalse(flow.isGracePeriod());
    }

    @Test
    void testCalculateTotalPayment() {
        // Given
        Bond bond = new Bond();
        Flow flow = new Flow(
            bond, 1, "AMORTIZATION", 
            new BigDecimal("100000"), new BigDecimal("99511.82"),
            new BigDecimal("1321.51"), new BigDecimal("833.33"), new BigDecimal("488.18"),
            new BigDecimal("50.00"), new BigDecimal("25.00"), new BigDecimal("10.00"),
            null, new BigDecimal("0.008333")
        );

        // When
        flow.calculateTotalPayment();

        // Then
        BigDecimal expectedTotal = new BigDecimal("1321.51")
            .add(new BigDecimal("50.00"))
            .add(new BigDecimal("25.00"))
            .add(new BigDecimal("10.00"));
        assertEquals(expectedTotal, flow.getTotalPayment());
    }
}