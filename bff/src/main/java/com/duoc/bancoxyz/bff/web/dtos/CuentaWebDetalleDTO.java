package com.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CuentaWebDetalleDTO(
        Long id,
        String numeroCuenta,
        String tipoCuenta,
        BigDecimal saldo,
        Boolean activa,
        Long clienteId,
        String clienteNombre,
        List<MovimientoWebDTO> movimientos,
        ResumenMovimientosDTO resumen) {
}
