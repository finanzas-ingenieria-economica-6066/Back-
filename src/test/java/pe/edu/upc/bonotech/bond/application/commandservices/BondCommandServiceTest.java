package pe.edu.upc.bonotech.bond.application.commandservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.upc.bonotech.bond.domain.model.aggregates.Bond;
import pe.edu.upc.bonotech.bond.domain.model.commands.CreateBondCommand;
import pe.edu.upc.bonotech.bond.infrastructure.persistence.jpa.repositories.BondRepository;
import pe.edu.upc.bonotech.shared.domain.services.CurrencyConversionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BondCommandServiceTest {

    @Mock
    private BondRepository bondRepository;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @InjectMocks
    private BondCommandService bondCommandService;

    private CreateBondCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new CreateBondCommand(
            1L, "Test Loan", "PEN", 
            new BigDecimal("100000"), new BigDecimal("20000"), 
            new BigDecimal("0.10"), new BigDecimal("10000"),
            new BigDecimal("3.85"), "EFFECTIVE", new BigDecimal("0.08"),
            "ANNUALLY", "MONTHLY", 240, "NONE", 0,
            new BigDecimal("0.0005"), new BigDecimal("25.00"),
            new BigDecimal("500.00"), new BigDecimal("10.00"), new BigDecimal("100.00"),
            LocalDate.now(), new BigDecimal("0.06")
        );
    }

    @Test
    void testHandleValidCommand() {
        // Given
        when(bondRepository.save(any(Bond.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Bond> result = bondCommandService.handle(validCommand);

        // Then
        assertTrue(result.isPresent());
        Bond bond = result.get();
        assertEquals("Test Loan", bond.getName());
        assertEquals(new BigDecimal("100000"), bond.getLoanAmount());
        assertFalse(bond.getFlows().isEmpty()); // Debería tener flujos generados
        assertNotNull(bond.getResultsFlow()); // Debería tener resultados calculados
        
        verify(bondRepository, times(2)).save(any(Bond.class));
    }

    @Test
    void testHandleInvalidCommand() {
        // Given - Comando inválido con monto cero
        CreateBondCommand invalidCommand = new CreateBondCommand(
            1L, "Invalid", "PEN", 
            BigDecimal.ZERO, BigDecimal.ZERO, 
            BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ONE, "EFFECTIVE", BigDecimal.ZERO,
            "ANNUALLY", "MONTHLY", 0, "NONE", 0,
            BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            LocalDate.now(), BigDecimal.ZERO
        );

        when(bondRepository.save(any(Bond.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            bondCommandService.handle(invalidCommand);
        });
    }
}