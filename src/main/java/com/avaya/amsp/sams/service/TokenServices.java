package com.avaya.amsp.sams.service;

import com.avaya.amsp.sams.dtos.ArticlePropertiesDto;
import com.avaya.amsp.sams.dtos.LoginResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TokenServices {

    @Autowired
    RestTemplate authServiceclient;

    @Value("${services.endpoints.auth-token}")
    private String authTokenEndpoint;

    @Value("${services.sams-account-username}")
    private String samsAccountUsername;

    @Value("${services.sams-account-pass}")
    private String samsAccountPass;


    public String getToken(){

        log.info("fetching token from {}",authTokenEndpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create body
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", samsAccountUsername);
        payload.put("password", samsAccountPass);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload,headers);
        ResponseEntity<LoginResponseDTO> response = authServiceclient.exchange(
                authTokenEndpoint,
                HttpMethod.POST,
                request,
                LoginResponseDTO.class
        );
        return response.getBody().getToken();
    }

}
