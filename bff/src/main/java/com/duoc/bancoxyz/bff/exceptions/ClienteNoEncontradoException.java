package com.duoc.bancoxyz.bff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(Long clienteId) {
        super("No existe el cliente con id " + clienteId);
    }
}
