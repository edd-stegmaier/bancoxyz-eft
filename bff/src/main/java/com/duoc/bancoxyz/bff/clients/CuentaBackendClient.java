package com.duoc.bancoxyz.bff.clients;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.exceptions.CuentaNoEncontradaException;

/** Cliente hacia /api/cuentas de bancoxyzBackend. */
@Component
public class CuentaBackendClient {

    private final RestClient restClient;

    public CuentaBackendClient(@Qualifier("bancoxyzBackendRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<CuentaBackendDTO> listarCuentas() {
        return restClient.get()
                .uri("/api/cuentas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaBackendDTO>>() {
                });
    }

    public CuentaBackendDTO obtenerCuenta(Long cuentaId) {
        try {
            return restClient.get()
                    .uri("/api/cuentas/{id}", cuentaId)
                    .retrieve()
                    .body(CuentaBackendDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(cuentaId);
        }
    }

    public CuentaBackendDTO obtenerPorNumero(String numeroCuenta) {
        try {
            return restClient.get()
                    .uri("/api/cuentas/numero/{numero}", numeroCuenta)
                    .retrieve()
                    .body(CuentaBackendDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(numeroCuenta);
        }
    }
}
