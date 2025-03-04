package com.example.api_gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class JWTService {
    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    @Autowired
    public JWTService(RestTemplate restTemplate,
                      @Value("${T23_ENDPOINT}") String t23Endpoint,
                      @Value("${T23_PORT}") String t23Port) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = String.format("http://%s:%s/validateToken", t23Endpoint, t23Port);
    }

    public boolean verifyToken(String token) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("jwt", token);

        Boolean isAuthenticated = restTemplate.postForObject(authServiceUrl, formData, Boolean.class);
        return Boolean.TRUE.equals(isAuthenticated);
    }
}
