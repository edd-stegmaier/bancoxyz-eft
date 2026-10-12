package com.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;

/** Agrupacion generica usada por el canal Web (cuentas por tipo o movimientos por tipo). */
public record AgrupacionTipoDTO(
        String tipo,
        int cantidad,
        BigDecimal montoTotal) {
}
