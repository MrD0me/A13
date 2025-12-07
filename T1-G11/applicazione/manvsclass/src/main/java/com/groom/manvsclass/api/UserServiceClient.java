package com.groom.manvsclass.api;

import com.groom.manvsclass.model.dto.gamification.HintCreditBalanceDTO;
import com.groom.manvsclass.model.dto.gamification.HintCreditUpdateDTO;
import com.groom.manvsclass.security.JwtRequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${API_GATEWAY_ENDPOINT:api-gateway_controller}")
    private String apiGatewayHost;

    @Value("${API_GATEWAY_PORT:8090}")
    private int apiGatewayPort;

    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        userServiceUrl = String.format("http://%s:%d/userService", apiGatewayHost, apiGatewayPort);
    }

    public int spendHintCredits(long playerId, int credits) {
        HintCreditUpdateDTO payload = new HintCreditUpdateDTO(credits);
        HttpHeaders headers = new HttpHeaders();
        String jwtCookie = JwtRequestContext.getJwtToken();
        if (jwtCookie != null) {
            headers.add(HttpHeaders.COOKIE, jwtCookie);
        }

        try {
            ResponseEntity<HintCreditBalanceDTO> response = restTemplate.exchange(
                    userServiceUrl + "/players/" + playerId + "/progression/credits/spend",
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headers),
                    HintCreditBalanceDTO.class
            );
            HintCreditBalanceDTO body = response.getBody();
            return body != null ? body.getCredits() : 0;
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
            );
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Errore nel contattare il servizio utenti",
                    ex
            );
        }
    }
}
