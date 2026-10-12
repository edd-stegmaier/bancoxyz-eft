package com.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;

public record CuentaWebResumenDTO(
        Long id,
        String numeroCuenta,
        String tipoCuenta,
        BigDecimal saldo,
        Boolean activa,
        Long clienteId,
        String clienteNombre) {
}
