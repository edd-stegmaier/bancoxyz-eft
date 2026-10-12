package com.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ClienteWebDetalleDTO(
        Long id,
        String nombre,
        Integer edad,
        String email,
        String rut,
        BigDecimal saldoConsolidado,
        int cantidadCuentas,
        List<AgrupacionTipoDTO> cuentasPorTipo,
        List<CuentaWebDetalleDTO> cuentas,
        ResumenMovimientosDTO resumenGlobal) {
}
