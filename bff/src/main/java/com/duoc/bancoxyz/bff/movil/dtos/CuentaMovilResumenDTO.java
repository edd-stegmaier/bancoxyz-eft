package com.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;

public record CuentaMovilResumenDTO(
        Long id,
        String numeroCuenta,
        String tipo,
        BigDecimal saldo) {
}
