package com.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ClienteMovilHomeDTO(
        Long clienteId,
        String nombre,
        BigDecimal saldoTotal,
        int cantidadCuentas,
        List<CuentaMovilResumenDTO> cuentas) {
}
