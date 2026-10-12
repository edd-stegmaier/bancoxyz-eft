package com.duoc.bancoxyz.bff.clients;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.duoc.bancoxyz.bff.dtos.backend.TransaccionBackendDTO;

/** Cliente hacia /api/cuentas/{id}/transacciones y /api/transacciones de bancoxyzBackend. */
@Component
public class TransaccionBackendClient {

    private final RestClient restClient;

    public TransaccionBackendClient(@Qualifier("bancoxyzBackendRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<TransaccionBackendDTO> listarPorCuenta(Long cuentaId) {
        return restClient.get()
                .uri("/api/cuentas/{id}/transacciones", cuentaId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TransaccionBackendDTO>>() {
                });
    }
}
