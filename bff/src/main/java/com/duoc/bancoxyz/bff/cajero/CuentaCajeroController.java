package com.duoc.bancoxyz.bff.cajero;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyz.bff.cajero.dtos.SaldoCajeroDTO;

import lombok.RequiredArgsConstructor;

/** Endpoint personalizado del canal Cajero Automatico. */
@RestController
@RequestMapping("/api/cajero/cuentas")
@RequiredArgsConstructor
public class CuentaCajeroController {

    private final BffCajeroService bffCajeroService;

    @GetMapping("/{cuentaId}/saldo")
    public SaldoCajeroDTO consultarSaldo(@PathVariable Long cuentaId) {
        return bffCajeroService.consultarSaldo(cuentaId);
    }

    @GetMapping("/numero/{numeroCuenta}/saldo")
    public SaldoCajeroDTO consultarSaldoPorNumero(@PathVariable String numeroCuenta) {
        return bffCajeroService.consultarSaldoPorNumero(numeroCuenta);
    }
}
