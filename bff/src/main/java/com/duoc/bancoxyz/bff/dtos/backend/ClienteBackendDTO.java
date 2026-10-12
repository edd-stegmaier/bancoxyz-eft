package com.duoc.bancoxyz.bff.dtos.backend;

/** Espejo del contrato JSON de GET /api/clientes de bancoxyzBackend. */
public record ClienteBackendDTO(
        Long id,
        String nombre,
        Integer edad,
        String email,
        String rut) {
}
