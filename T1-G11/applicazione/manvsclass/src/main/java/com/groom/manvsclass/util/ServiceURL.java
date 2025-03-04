package com.groom.manvsclass.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServiceURL {

    @Value("${services.t23.endpoint}")
    private String t23Endpoint;

    @Value("${services.t23.port}")
    private int t23Port;

    @Value("${services.t4.endpoint}")
    private String t4Endpoint;

    @Value("${services.t4.port}")
    private int t4Port;

    @Value("${services.t7.endpoint}")
    private String t7Endpoint;

    @Value("${services.t7.port}")
    private int t7Port;

    @Value("${services.t8.endpoint}")
    private String t8Endpoint;

    @Value("${services.t8.port}")
    private int t8Port;

    @PostConstruct
    public void init() {
        System.out.println("🔹 T23 Endpoint: " + t23Endpoint);
        System.out.println("🔹 T23 Port: " + t23Port);
        System.out.println("🔹 T8 Endpoint: " + t8Endpoint);
        System.out.println("🔹 T8 Port: " + t8Port);
    }

    public String getT23ServiceURL() {
        return String.format("%s:%d", t23Endpoint, t23Port);
    }

    public String getT4ServiceURL() {
        return String.format("%s:%d", t4Endpoint, t4Port);
    }

    public String getT7ServiceURL() {
        return String.format("%s:%d", t7Endpoint, t7Port);
    }

    public String getT8ServiceURL() {
        return String.format("%s:%d", t8Endpoint, t8Port);
    }
}
