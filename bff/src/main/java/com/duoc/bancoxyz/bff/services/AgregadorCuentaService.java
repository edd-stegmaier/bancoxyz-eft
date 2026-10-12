package com.duoc.bancoxyz.bff.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyz.bff.clients.CuentaBackendClient;
import com.duoc.bancoxyz.bff.clients.TransaccionBackendClient;
import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.TransaccionBackendDTO;

import lombok.RequiredArgsConstructor;

/**
 * Punto donde el BFF integra cuenta y transacciones llamando en paralelo
 * a dos endpoints de bancoxyzBackend. Los canales Web y Movil reutilizan
 * este agregador y solo se diferencian en como transforman el resultado.
 */
@Service
@RequiredArgsConstructor
public class AgregadorCuentaService {

    private final CuentaBackendClient cuentaBackendClient;
    private final TransaccionBackendClient transaccionBackendClient;
    private final ExecutorService backendExecutor;

    public List<CuentaBackendDTO> listarCuentas() {
        return cuentaBackendClient.listarCuentas();
    }

    /**
     * /api/cuentas/{id} y /api/cuentas/{id}/transacciones no dependen entre si,
     * se consultan en paralelo: la latencia queda dada por la llamada mas lenta.
     */
    public CuentaAgregadaDTO agregarDetalle(Long cuentaId) {
        CompletableFuture<CuentaBackendDTO> cuentaFuture =
                CompletableFuture.supplyAsync(() -> cuentaBackendClient.obtenerCuenta(cuentaId), backendExecutor);
        CompletableFuture<List<TransaccionBackendDTO>> transaccionesFuture =
                CompletableFuture.supplyAsync(() -> transaccionBackendClient.listarPorCuenta(cuentaId), backendExecutor);

        esperar(cuentaFuture, transaccionesFuture);
        return new CuentaAgregadaDTO(cuentaFuture.join(), transaccionesFuture.join());
    }

    public CuentaBackendDTO obtenerPorNumero(String numeroCuenta) {
        return cuentaBackendClient.obtenerPorNumero(numeroCuenta);
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
