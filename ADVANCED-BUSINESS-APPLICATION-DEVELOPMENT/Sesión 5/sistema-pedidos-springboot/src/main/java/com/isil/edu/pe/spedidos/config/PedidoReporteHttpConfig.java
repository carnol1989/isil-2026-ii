package com.isil.edu.pe.spedidos.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PedidoReporteHttpConfig {

  @Bean("pedidoReporteHttpClient")
  public RestClient pedidoReporteHttpClient(
      @Value("${pedido-reportes.api.connect-timeout-ms:2000}") long connectTimeout,
      @Value("${pedido-reportes.api.read-timeout-ms:3000}") long readTimeout) {

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
    factory.setReadTimeout(Duration.ofMillis(readTimeout));

    return RestClient.builder().requestFactory(factory).build();
  }
}
