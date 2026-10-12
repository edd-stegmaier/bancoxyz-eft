package com.duoc.bancoxyz.bff.dtos.backend;

import java.math.BigDecimal;

/** Espejo del contrato JSON de GET /api/cuentas de bancoxyzBackend. */
public record CuentaBackendDTO(
        Long id,
        String numeroCuenta,
        String tipoCuenta,
        BigDecimal saldo,
        Boolean activa,
        Long clienteId,
        String clienteNombre) {
}
