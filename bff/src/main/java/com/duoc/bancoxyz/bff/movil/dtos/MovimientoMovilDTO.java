package com.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;

public record MovimientoMovilDTO(
        String fecha,
        String tipo,
        BigDecimal monto) {
}
