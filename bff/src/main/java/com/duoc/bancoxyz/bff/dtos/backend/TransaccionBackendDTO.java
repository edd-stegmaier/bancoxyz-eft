package com.duoc.bancoxyz.bff.dtos.backend;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Espejo del contrato JSON de GET /api/transacciones de bancoxyzBackend. */
public record TransaccionBackendDTO(
        Long id,
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        String descripcion,
        Long cuentaId,
        String numeroCuenta) {
}
