package com.duoc.bancoxyz.bff.web.dtos;

public record ClienteWebResumenDTO(
        Long id,
        String nombre,
        Integer edad,
        String email,
        String rut) {
}
