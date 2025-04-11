package com.groom.manvsclass.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServiceURL {

    @Value("${API_GATEWAY_ENDPOINT:api-gateway_controller}")
    private String apiGatewayEndpoint;

    @Value("${API_GATEWAY_PORT:8090}")
    private int apiGatewayPort;

    private final String t23Prefix = "userService";
    private final String t4Prefix = "gamerepo";
    private final String t7Prefix = "compile/randoop";
    private final String t8Prefix = "compile/evosuite";

    @PostConstruct
    public void init() {
        System.out.println("🔹 API Gateway Endpoint: " + apiGatewayEndpoint);
        System.out.println("🔹 API Gateway Port: " + apiGatewayPort);
    }

    public String getT23ServiceURL() {
        return String.format("%s:%d/%s", apiGatewayEndpoint, apiGatewayPort, t23Prefix);
    }

    public String getT4ServiceURL() {
        return String.format("%s:%d/%s", apiGatewayEndpoint, apiGatewayPort, t4Prefix);
    }

    public String getT7ServiceURL() {
        return String.format("%s:%d/%s", apiGatewayEndpoint, apiGatewayPort, t7Prefix);
    }

    public String getT8ServiceURL() {
        return String.format("%s:%d/%s", apiGatewayEndpoint, apiGatewayPort, t8Prefix);
    }
}
