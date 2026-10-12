package com.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CuentaMovilDetalleDTO(
        Long id,
        String numeroCuenta,
        String tipo,
        BigDecimal saldo,
        List<MovimientoMovilDTO> ultimosMovimientos) {
}
