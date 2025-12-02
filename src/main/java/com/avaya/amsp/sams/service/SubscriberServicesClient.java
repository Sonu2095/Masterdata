package com.avaya.amsp.sams.service;

import com.avaya.amsp.sams.dtos.SubscriberDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SubscriberServicesClient {

    @Autowired
    RestTemplate subscriberServicesClient;

    @Value( "${services.endpoints.fetch-subscriber}" )
    private String getSubscriberEndpoint;

    public SubscriberDto fetchSubscriber(String accountName, String domain, String authToken) {

        String url = String.format(getSubscriberEndpoint,accountName,domain);
        log.info("fetching subscriber info from {}", url);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< Object > request = new HttpEntity<>(headers);
        ResponseEntity< SubscriberDto > response = subscriberServicesClient.exchange(
                url,
                HttpMethod.GET,
                request,
                SubscriberDto.class
        );
        log.info("Response received from subscriber service {}", response.getStatusCode());
        return response.getBody();
    }

}
