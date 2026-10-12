package com.duoc.bancoxyz.bff.movil;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyz.bff.dtos.backend.ClienteBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.movil.dtos.ClienteMovilHomeDTO;
import com.duoc.bancoxyz.bff.movil.dtos.CuentaMovilDetalleDTO;
import com.duoc.bancoxyz.bff.movil.dtos.CuentaMovilResumenDTO;
import com.duoc.bancoxyz.bff.movil.dtos.MovimientoMovilDTO;
import com.duoc.bancoxyz.bff.services.AgregadorClienteService;
import com.duoc.bancoxyz.bff.services.AgregadorCuentaService;
import com.duoc.bancoxyz.bff.services.ClienteAgregadoDTO;
import com.duoc.bancoxyz.bff.services.CuentaAgregadaDTO;

import lombok.RequiredArgsConstructor;

/**
 * Canal Movil: mismos datos agregados que Web, recortados a lo esencial
 * para reducir el consumo de datos (ultimos 5 movimientos, sin descripcion).
 */
@Service
@RequiredArgsConstructor
public class BffMovilService {

    private static final int MAX_ULTIMOS_MOVIMIENTOS = 5;

    private final AgregadorCuentaService agregadorCuentaService;
    private final AgregadorClienteService agregadorClienteService;

    public List<CuentaMovilResumenDTO> listarCuentas() {
        return agregadorCuentaService.listarCuentas().stream()
                .filter(c -> Boolean.TRUE.equals(c.activa()))
                .map(this::aResumen)
                .toList();
    }

    public CuentaMovilDetalleDTO obtenerDetalle(Long cuentaId) {
        CuentaAgregadaDTO agregado = agregadorCuentaService.agregarDetalle(cuentaId);
        CuentaBackendDTO cuenta = agregado.cuenta();

        List<MovimientoMovilDTO> ultimos = agregado.transacciones().stream()
                .limit(MAX_ULTIMOS_MOVIMIENTOS)
                .map(m -> new MovimientoMovilDTO(
                        m.fecha() != null ? m.fecha().toString() : null,
                        m.tipo(),
                        m.monto()))
                .toList();

        return new CuentaMovilDetalleDTO(
                cuenta.id(),
                cuenta.numeroCuenta(),
                cuenta.tipoCuenta(),
                cuenta.saldo(),
                ultimos);
    }

    public ClienteMovilHomeDTO homeCliente(Long clienteId) {
        ClienteAgregadoDTO agregado = agregadorClienteService.agregarDetalle(clienteId);
        ClienteBackendDTO cliente = agregado.cliente();

        List<CuentaMovilResumenDTO> cuentas = agregado.cuentas().stream()
                .map(c -> aResumen(c.cuenta()))
                .toList();

        BigDecimal saldoTotal = cuentas.stream()
                .map(CuentaMovilResumenDTO::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ClienteMovilHomeDTO(
                cliente.id(),
                cliente.nombre(),
                saldoTotal,
                cuentas.size(),
                cuentas);
    }

    private CuentaMovilResumenDTO aResumen(CuentaBackendDTO cuenta) {
        return new CuentaMovilResumenDTO(
                cuenta.id(),
                cuenta.numeroCuenta(),
                cuenta.tipoCuenta(),
                cuenta.saldo());
    }
}
