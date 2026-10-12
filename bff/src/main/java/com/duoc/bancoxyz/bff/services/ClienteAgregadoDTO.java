package com.duoc.bancoxyz.bff.services;

import java.util.List;

import com.duoc.bancoxyz.bff.dtos.backend.ClienteBackendDTO;

/** Resultado crudo de agregar cliente + sus cuentas con movimientos. */
public record ClienteAgregadoDTO(
        ClienteBackendDTO cliente,
        List<CuentaAgregadaDTO> cuentas) {
}
