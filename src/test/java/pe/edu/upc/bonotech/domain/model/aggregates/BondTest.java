package pe.edu.upc.bonotech.bond.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import pe.edu.upc.bonotech.bond.domain.model.commands.CreateBondCommand;
import pe.edu.upc.bonotech.bond.domain.model.valueobjects.*;

import static org.junit.jupiter.api.Assertions.*;

class BondTest {

    @Test
    void testCreateBondWithValidData() {
        // Given
        CreateBondCommand command = new CreateBondCommand(
            1L, "Test Loan", "PEN", 
            new BigDecimal("100000"), new BigDecimal("20000"), 
            new BigDecimal("0.10"), new BigDecimal("10000"),
            new BigDecimal("3.85"), "EFFECTIVE", new BigDecimal("0.08"),
            "ANNUALLY", "MONTHLY", 240, "NONE", 0,
            new BigDecimal("0.0005"), new BigDecimal("25.00"),
            new BigDecimal("500.00"), new BigDecimal("10.00"), new BigDecimal("100.00"),
            LocalDate.now(), new BigDecimal("0.06")
        );

        // When
        Bond bond = new Bond(command);

        // Then
        assertNotNull(bond);
        assertEquals("Test Loan", bond.getName());
        assertEquals(ECurrency.PEN, bond.getCurrency());
        assertEquals(new BigDecimal("100000"), bond.getLoanAmount());
        assertEquals(new BigDecimal("20000"), bond.getBbpAmount());
        assertEquals(new BigDecimal("70000"), bond.getFinancedCapital()); // 100000 - 20000 - 10000
        assertEquals(EInterestType.EFFECTIVE, bond.getInterestType());
        assertTrue(bond.isValidForCalculation());
    }

    @Test
    void testCalculateFinancedCapital() {
        // Given
        CreateBondCommand command = new CreateBondCommand(
            1L, "Test", "PEN", 
            new BigDecimal("150000"), new BigDecimal("25000"), 
            new BigDecimal("0.15"), new BigDecimal("22500"),
            new BigDecimal("3.85"), "EFFECTIVE", new BigDecimal("0.09"),
            "ANNUALLY", "MONTHLY", 180, "NONE", 0,
            new BigDecimal("0.0005"), new BigDecimal("30.00"),
            new BigDecimal("600.00"), new BigDecimal("15.00"), new BigDecimal("150.00"),
            LocalDate.now(), new BigDecimal("0.07")
        );

        // When
        Bond bond = new Bond(command);

        // Then
        BigDecimal expectedCapital = new BigDecimal("150000")
            .subtract(new BigDecimal("25000"))
            .subtract(new BigDecimal("22500"));
        assertEquals(expectedCapital, bond.getFinancedCapital());
    }

    @Test
    void testInvalidBondForCalculation() {
        // Given - Monto del crédito cero
        CreateBondCommand command = new CreateBondCommand(
            1L, "Invalid", "PEN", 
            BigDecimal.ZERO, BigDecimal.ZERO, 
            BigDecimal.ZERO, BigDecimal.ZERO,
            new BigDecimal("3.85"), "EFFECTIVE", new BigDecimal("0.08"),
            "ANNUALLY", "MONTHLY", 0, "NONE", 0,
            BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            LocalDate.now(), BigDecimal.ZERO
        );

        // When
        Bond bond = new Bond(command);

        // Then
        assertFalse(bond.isValidForCalculation());
    }
}