package com.duoc.bancoxyz.bff.web;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.duoc.bancoxyz.bff.dtos.backend.ClienteBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.CuentaBackendDTO;
import com.duoc.bancoxyz.bff.dtos.backend.TransaccionBackendDTO;
import com.duoc.bancoxyz.bff.services.AgregadorClienteService;
import com.duoc.bancoxyz.bff.services.AgregadorCuentaService;
import com.duoc.bancoxyz.bff.services.ClienteAgregadoDTO;
import com.duoc.bancoxyz.bff.services.CuentaAgregadaDTO;
import com.duoc.bancoxyz.bff.web.dtos.AgrupacionTipoDTO;
import com.duoc.bancoxyz.bff.web.dtos.ClienteWebDetalleDTO;
import com.duoc.bancoxyz.bff.web.dtos.ClienteWebResumenDTO;
import com.duoc.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import com.duoc.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;
import com.duoc.bancoxyz.bff.web.dtos.MovimientoWebDTO;
import com.duoc.bancoxyz.bff.web.dtos.ResumenMovimientosDTO;

import lombok.RequiredArgsConstructor;

/**
 * Canal Web: vista completa para escritorio/backoffice.
 * Reutiliza los agregadores (llamadas paralelas al backend) y agrega
 * resumenes, agrupaciones por tipo de cuenta/movimiento y el historial
 * completo con descripcion. 
 */
@Service
@RequiredArgsConstructor
public class BffWebService {

    private final AgregadorCuentaService agregadorCuentaService;
    private final AgregadorClienteService agregadorClienteService;

    public List<CuentaWebResumenDTO> listarCuentas() {
        return agregadorCuentaService.listarCuentas().stream()
                .map(this::aResumen)
                .toList();
    }

    public CuentaWebDetalleDTO obtenerDetalle(Long cuentaId) {
        return aDetalle(agregadorCuentaService.agregarDetalle(cuentaId));
    }

    public List<ClienteWebResumenDTO> listarClientes() {
        return agregadorClienteService.listarClientes().stream()
                .map(c -> new ClienteWebResumenDTO(
                        c.id(), c.nombre(), c.edad(), c.email(), c.rut()))
                .toList();
    }

    public ClienteWebDetalleDTO obtenerDetalleCliente(Long clienteId) {
        ClienteAgregadoDTO agregado = agregadorClienteService.agregarDetalle(clienteId);
        ClienteBackendDTO cliente = agregado.cliente();

        List<CuentaWebDetalleDTO> cuentas = agregado.cuentas().stream()
                .map(this::aDetalle)
                .toList();

        BigDecimal saldoConsolidado = cuentas.stream()
                .map(CuentaWebDetalleDTO::saldo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransaccionBackendDTO> todas = agregado.cuentas().stream()
                .flatMap(c -> c.transacciones().stream())
                .toList();

        return new ClienteWebDetalleDTO(
                cliente.id(),
                cliente.nombre(),
                cliente.edad(),
                cliente.email(),
                cliente.rut(),
                saldoConsolidado,
                cuentas.size(),
                agruparCuentasPorTipo(agregado.cuentas().stream().map(CuentaAgregadaDTO::cuenta).toList()),
                cuentas,
                calcularResumen(todas));
    }

    private CuentaWebResumenDTO aResumen(CuentaBackendDTO cuenta) {
        return new CuentaWebResumenDTO(
                cuenta.id(),
                cuenta.numeroCuenta(),
                cuenta.tipoCuenta(),
                cuenta.saldo(),
                cuenta.activa(),
                cuenta.clienteId(),
                cuenta.clienteNombre());
    }

    private CuentaWebDetalleDTO aDetalle(CuentaAgregadaDTO agregado) {
        CuentaBackendDTO cuenta = agregado.cuenta();
        List<MovimientoWebDTO> movimientos = agregado.transacciones().stream()
                .map(m -> new MovimientoWebDTO(
                        m.id(),
                        m.fecha(),
                        m.tipo(),
                        m.monto(),
                        m.descripcion()))
                .toList();

        return new CuentaWebDetalleDTO(
                cuenta.id(),
                cuenta.numeroCuenta(),
                cuenta.tipoCuenta(),
                cuenta.saldo(),
                cuenta.activa(),
                cuenta.clienteId(),
                cuenta.clienteNombre(),
                movimientos,
                calcularResumen(agregado.transacciones()));
    }

    private ResumenMovimientosDTO calcularResumen(List<TransaccionBackendDTO> movimientos) {
        BigDecimal totalDepositos = sumarPorTipo(movimientos, "DEPOSITO");
        BigDecimal totalRetiros = sumarPorTipo(movimientos, "RETIRO");
        BigDecimal totalCompras = sumarPorTipo(movimientos, "COMPRA");
        BigDecimal totalPagos = sumarPorTipo(movimientos, "PAGO");
        BigDecimal saldoNeto = totalDepositos
                .subtract(totalRetiros)
                .subtract(totalCompras)
                .subtract(totalPagos);

        return new ResumenMovimientosDTO(
                totalDepositos,
                totalRetiros,
                totalCompras,
                totalPagos,
                saldoNeto,
                movimientos == null ? 0 : movimientos.size());
    }

    private BigDecimal sumarPorTipo(List<TransaccionBackendDTO> movimientos, String tipo) {
        if (movimientos == null || movimientos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return movimientos.stream()
                .filter(m -> tipo.equalsIgnoreCase(m.tipo()))
                .map(TransaccionBackendDTO::monto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<AgrupacionTipoDTO> agruparCuentasPorTipo(List<CuentaBackendDTO> cuentas) {
        Map<String, List<CuentaBackendDTO>> agrupadas = cuentas.stream()
                .collect(Collectors.groupingBy(c -> c.tipoCuenta() == null ? "DESCONOCIDO" : c.tipoCuenta()));

        return agrupadas.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AgrupacionTipoDTO(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .map(CuentaBackendDTO::saldo)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(AgrupacionTipoDTO::tipo))
                .toList();
    }
}
