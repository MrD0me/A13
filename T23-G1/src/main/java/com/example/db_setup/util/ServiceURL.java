package com.example.db_setup.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServiceURL {
    @Value("${API_GATEWAY_ENDPOINT:api-gateway_controller}")
    private String apiGatewayEndpoint;

    @Value("${API_GATEWAY_PORT:8090}")
    private int apiGatewayPort;

    private final String t4Prefix = "gamerepo";

    @PostConstruct
    public void init() {
        System.out.println("🔹 API Gateway Endpoint: " + apiGatewayEndpoint);
        System.out.println("🔹 API Gateway Port: " + apiGatewayPort);
    }

    public String getT4ServiceURL() {
        return String.format("%s:%d/%s", apiGatewayEndpoint, apiGatewayPort, t4Prefix);
    }

}
