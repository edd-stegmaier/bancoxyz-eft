package com.duoc.bancoxyz.bff.services;

import java.util.List;

import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.TransaccionBackendDTO;

/** Resultado crudo de agregar cuenta + transacciones, antes de adaptarlo a un canal. */
public record CuentaAgregadaDTO(
        CuentaBackendDTO cuenta,
        List<TransaccionBackendDTO> transacciones) {
}
