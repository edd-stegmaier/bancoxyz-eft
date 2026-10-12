package com.duoc.bancoxyz.bff.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP hacia bancoxyzBackend.
 * Timeouts explicitos: si el backend no responde a tiempo, el BFF falla rapido.
 */
@Configuration
public class BackendServicesConfig {

    @Bean
    public ClientHttpRequestFactory backendRequestFactory(
            @Value("${backend.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${backend.read-timeout-ms:3000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }

    @Bean
    @Qualifier("bancoxyzBackendRestClient")
    public RestClient bancoxyzBackendRestClient(
            @Value("${bancoxyz-backend.base-url}") String baseUrl,
            ClientHttpRequestFactory backendRequestFactory) {
        return RestClient.builder().baseUrl(baseUrl).requestFactory(backendRequestFactory).build();
    }

    /**
     * Executor dedicado para paralelizar llamadas independientes
     * (cuenta + transacciones, cliente + cuentas).
     */
    @Bean
    public ExecutorService backendExecutor() {
        return Executors.newFixedThreadPool(6);
    }
}
