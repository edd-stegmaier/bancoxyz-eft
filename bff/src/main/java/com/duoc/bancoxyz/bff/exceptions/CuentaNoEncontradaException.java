package com.duoc.bancoxyz.bff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long cuentaId) {
        super("No existe la cuenta con id " + cuentaId);
    }

    public CuentaNoEncontradaException(String numeroCuenta) {
        super("No existe la cuenta con numero " + numeroCuenta);
    }
}
