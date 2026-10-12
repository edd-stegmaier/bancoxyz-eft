package com.duoc.bancoxyz.bff.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyz.bff.clients.ClienteBackendClient;
import com.duoc.bancoxyz.bff.clients.TransaccionBackendClient;
import com.duoc.bancoxyz.bff.dtos.backend.ClienteBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.TransaccionBackendDTO;

import lombok.RequiredArgsConstructor;

/**
 * Agrega cliente + cuentas + transacciones de cada cuenta.
 * Combina GET /api/clientes/{id}, GET /api/clientes/{id}/cuentas
 * y GET /api/cuentas/{id}/transacciones por cada cuenta.
 */
@Service
@RequiredArgsConstructor
public class AgregadorClienteService {

    private final ClienteBackendClient clienteBackendClient;
    private final TransaccionBackendClient transaccionBackendClient;
    private final ExecutorService backendExecutor;

    public List<ClienteBackendDTO> listarClientes() {
        return clienteBackendClient.listarClientes();
    }

    public ClienteAgregadoDTO agregarDetalle(Long clienteId) {
        CompletableFuture<ClienteBackendDTO> clienteFuture =
                CompletableFuture.supplyAsync(() -> clienteBackendClient.obtenerCliente(clienteId), backendExecutor);
        CompletableFuture<List<CuentaBackendDTO>> cuentasFuture =
                CompletableFuture.supplyAsync(() -> clienteBackendClient.listarCuentasPorCliente(clienteId), backendExecutor);

        esperar(clienteFuture, cuentasFuture);

        ClienteBackendDTO cliente = clienteFuture.join();
        List<CuentaBackendDTO> cuentas = cuentasFuture.join();

        List<CompletableFuture<CuentaAgregadaDTO>> detalleCuentas = cuentas.stream()
                .map(cuenta -> CompletableFuture.supplyAsync(
                        () -> {
                            List<TransaccionBackendDTO> txs = transaccionBackendClient.listarPorCuenta(cuenta.id());
                            return new CuentaAgregadaDTO(cuenta, txs);
                        }, backendExecutor))
                .toList();

        esperar(detalleCuentas.toArray(CompletableFuture[]::new));

        return new ClienteAgregadoDTO(
                cliente,
                detalleCuentas.stream().map(CompletableFuture::join).toList());
    }

    private void esperar(CompletableFuture<?>... futures) {
        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeCause) {
                throw runtimeCause;
            }
            throw ex;
        }
    }
}
