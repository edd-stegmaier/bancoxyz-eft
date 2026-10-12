package com.duoc.bancoxyz.bff.cajero;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyz.bff.cajero.dtos.SaldoCajeroDTO;
import com.duoc.bancoxyz.bff.clients.CuentaBackendClient;
import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;

import lombok.RequiredArgsConstructor;

/**
 * Transformacion del canal Cajero: NO necesita movimientos ni datos del cliente,
 * llama solo a {@link CuentaBackendClient} y expone saldo + estado operativo.
 */
@Service
@RequiredArgsConstructor
public class BffCajeroService {

    private final CuentaBackendClient cuentaBackendClient;

    public SaldoCajeroDTO consultarSaldo(Long cuentaId) {
        return aSaldo(cuentaBackendClient.obtenerCuenta(cuentaId));
    }

    public SaldoCajeroDTO consultarSaldoPorNumero(String numeroCuenta) {
        return aSaldo(cuentaBackendClient.obtenerPorNumero(numeroCuenta));
    }

    private SaldoCajeroDTO aSaldo(CuentaBackendDTO cuenta) {
        boolean puedeOperar = Boolean.TRUE.equals(cuenta.activa());
        return new SaldoCajeroDTO(cuenta.numeroCuenta(), cuenta.saldo(), puedeOperar);
    }
}
