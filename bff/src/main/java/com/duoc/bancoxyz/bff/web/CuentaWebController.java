package com.duoc.bancoxyz.bff.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bancoxyz.bff.web.dtos.ClienteWebDetalleDTO;
import com.duoc.bancoxyz.bff.web.dtos.ClienteWebResumenDTO;
import com.duoc.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import com.duoc.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;

import lombok.RequiredArgsConstructor;

/** Endpoints personalizados del canal Web (mismo deployable que Cajero y Movil). */
@RestController
@RequestMapping("/api/web")
@RequiredArgsConstructor
public class CuentaWebController {

    private final BffWebService bffWebService;

    @GetMapping("/cuentas")
    public List<CuentaWebResumenDTO> listarCuentas() {
        return bffWebService.listarCuentas();
    }

    @GetMapping("/cuentas/{cuentaId}")
    public CuentaWebDetalleDTO obtenerDetalle(@PathVariable Long cuentaId) {
        return bffWebService.obtenerDetalle(cuentaId);
    }

    @GetMapping("/clientes")
    public List<ClienteWebResumenDTO> listarClientes() {
        return bffWebService.listarClientes();
    }

    @GetMapping("/clientes/{clienteId}")
    public ClienteWebDetalleDTO obtenerDetalleCliente(@PathVariable Long clienteId) {
        return bffWebService.obtenerDetalleCliente(clienteId);
    }
}
