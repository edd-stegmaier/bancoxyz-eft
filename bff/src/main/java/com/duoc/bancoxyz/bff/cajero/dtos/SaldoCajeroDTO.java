package com.duoc.bancoxyz.bff.cajero.dtos;

import java.math.BigDecimal;

/** Respuesta minima para cajero automatico: saldo y si la cuenta puede operar. */
public record SaldoCajeroDTO(
        String numeroCuenta,
        BigDecimal saldo,
        boolean puedeOperar) {
}
