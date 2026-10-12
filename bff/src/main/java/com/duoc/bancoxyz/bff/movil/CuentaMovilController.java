package com.duoc.bancoxyz.bff.movil;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyz.bff.movil.dtos.ClienteMovilHomeDTO;
import com.duoc.bancoxyz.bff.movil.dtos.CuentaMovilDetalleDTO;
import com.duoc.bancoxyz.bff.movil.dtos.CuentaMovilResumenDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movil")
@RequiredArgsConstructor
public class CuentaMovilController {

    private final BffMovilService bffMovilService;

    @GetMapping("/cuentas")
    public List<CuentaMovilResumenDTO> listarCuentas() {
        return bffMovilService.listarCuentas();
    }

    @GetMapping("/cuentas/{cuentaId}")
    public CuentaMovilDetalleDTO obtenerDetalle(@PathVariable Long cuentaId) {
        return bffMovilService.obtenerDetalle(cuentaId);
    }

    @GetMapping("/clientes/{clienteId}")
    public ClienteMovilHomeDTO homeCliente(@PathVariable Long clienteId) {
        return bffMovilService.homeCliente(clienteId);
    }
}
