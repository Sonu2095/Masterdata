package com.avaya.amsp.sams.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.avaya.amsp.sams.dtos.OrderItemDTO;
import com.avaya.amsp.sams.dtos.OrderWrapperdto;
import com.avaya.amsp.sams.dtos.SAMSDelMigrateOrderDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServicesClient {

    @Autowired
    RestTemplate orderServicesClient;

    @Value( "${services.endpoints.open-sams-order}" )
    private String samsOrderEndpoint;

    @Value( "${services.endpoints.fetch-sams-order}" )
    private String samsOrderStatusEndpoint;

    @Value( "${services.endpoints.delete-sams-order}" )
    private String samsDeleteOrderEndpoint;

    @Value( "${services.endpoints.change-sams-order}" )
    private String samsChangeOrderEndpoint;

    public String openSamsOrder(OrderWrapperdto payload, String authToken) {

        log.info("initiating SAMS order to {}", samsOrderEndpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< OrderWrapperdto > request = new HttpEntity<>(payload, headers);
        ResponseEntity< String > response = orderServicesClient.exchange(
                samsOrderEndpoint,
                HttpMethod.POST,
                request,
                String.class
        );
         log.info("Response received from order service {}", response.getStatusCode());
        return response.getBody();
    }


    public OrderItemDTO fetchSamsOrder(String orderId, String authToken) {

        String url = String.format(samsOrderStatusEndpoint, orderId);
        log.info("fetching sams order details from {}", url);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< Object > request = new HttpEntity<>(headers);
        ResponseEntity< OrderItemDTO > response = orderServicesClient.exchange(
                url,
                HttpMethod.GET,
                request,
                OrderItemDTO.class
        );

        log.info("Response received from order service {}", response.getStatusCode());
        return response.getBody();
    }

    public String deleteSamsOrder(SAMSDelMigrateOrderDto payload, String authToken) {

        log.info("initiating Delete SAMS order to {}", samsDeleteOrderEndpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<SAMSDelMigrateOrderDto> request = new HttpEntity<>(payload, headers);
        ResponseEntity< String > response = orderServicesClient.exchange(
                samsDeleteOrderEndpoint,
                HttpMethod.DELETE,
                request,
                String.class
        );
        log.info("Response received from order service {}", response.getStatusCode());
        return response.getBody();
    }


    public String changeSamsOrder(SAMSDelMigrateOrderDto payload, String authToken) {

        log.info("initiating Change/move SAMS order to {}", samsChangeOrderEndpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<SAMSDelMigrateOrderDto> request = new HttpEntity<>(payload, headers);
        ResponseEntity< String > response = orderServicesClient.exchange(
                samsChangeOrderEndpoint,
                HttpMethod.PUT,
                request,
                String.class
        );
        log.info("Response received from order service {}", response.getStatusCode());
        
        return response.getBody();
    }

}
