package pe.edu.upc.bonotech.bond.domain.model.valueobjects;

public enum EGracePeriodType {
    NONE,       // Sin gracia
    PARTIAL,    // Gracia parcial (solo se pagan intereses)
    TOTAL       // Gracia total (no se paga nada, intereses se capitalizan)
}