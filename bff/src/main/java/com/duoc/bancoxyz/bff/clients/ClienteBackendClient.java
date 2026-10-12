package com.duoc.bancoxyz.bff.clients;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.duoc.bancoxyz.bff.dtos.backend.ClienteBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.exceptions.ClienteNoEncontradoException;

/** Cliente hacia /api/clientes de bancoxyzBackend. */
@Component
public class ClienteBackendClient {

    private final RestClient restClient;

    public ClienteBackendClient(@Qualifier("bancoxyzBackendRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ClienteBackendDTO> listarClientes() {
        return restClient.get()
                .uri("/api/clientes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ClienteBackendDTO>>() {
                });
    }

    public ClienteBackendDTO obtenerCliente(Long clienteId) {
        try {
            return restClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .body(ClienteBackendDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClienteNoEncontradoException(clienteId);
        }
    }

    public List<CuentaBackendDTO> listarCuentasPorCliente(Long clienteId) {
        try {
            return restClient.get()
                    .uri("/api/clientes/{id}/cuentas", clienteId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CuentaBackendDTO>>() {
                    });
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClienteNoEncontradoException(clienteId);
        }
    }
}
