package com.avaya.amsp.sams.rest;

import com.avaya.amsp.sams.dtos.*;
import com.avaya.amsp.sams.service.iface.SAMSServices;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "v1/methods" )
@Slf4j
public class SAMSController {

    @Autowired
    private SAMSServices samsServices;

    @PostMapping( "/AddOrder.php" )
    @PreAuthorize( "hasAnyRole('SAMS_BRIDGE_API_USER')" )
    public ResponseEntity< ? > addOrder(@Valid @RequestBody SAMSAddOrderRequestDTO payload) {

        log.info("SAMS Add order request received for {}", payload);
        String orderId = samsServices.openSamsOrder(payload);
        log.info("SAMS add order request completed...");

        return ResponseEntity.status(HttpStatus.OK).body(new SAMSOrderResponseDTO(orderId));
    }

    @PostMapping("/GetOrder.php")
    @PreAuthorize( "hasAnyRole('SAMS_BRIDGE_API_USER')" )
    public ResponseEntity< ? > getOrderDetails(@Valid @RequestBody SAMSGetOrderRequestDTO payload) {
        log.info("Fetching SAMS order details for {}",payload);
        SAMSGetOrderResponseDTO response = samsServices.fetchSamsOrder(payload.orderID());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping( "/DeleteOrder.php" )
    @PreAuthorize( "hasAnyRole('SAMS_BRIDGE_API_USER')" )
    public ResponseEntity<?> deleteOrder(@RequestBody SAMSDeleteOrderRequestDTO payload) {
        log.info("DeleteOrder received for {}", payload);

        SAMSDelMigrateOrderDto deleteOrderRequest = new SAMSDelMigrateOrderDto();
        deleteOrderRequest.setAreaCode(payload.callNumber().areaCode());
        deleteOrderRequest.setExtension(payload.callNumber().extension());
        deleteOrderRequest.setConnectionType(payload.connectionType().BCSType());

        String orderId =  samsServices.deleteSamsOrder(deleteOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new SAMSOrderResponseDTO(orderId));
    }

    @PostMapping( "/MigrationOrder.php" )
    @PreAuthorize( "hasAnyRole('SAMS_BRIDGE_API_USER')" )
    public ResponseEntity<?> migrateOrder(@RequestBody SAMSChangeOrderRequestDTO payload) {
        log.info("Change Order request received for {}", payload);

        SAMSDelMigrateOrderDto changeOrderRequest = new SAMSDelMigrateOrderDto();
        changeOrderRequest.setAreaCode(payload.callNumber().areaCode());
        changeOrderRequest.setExtension(payload.callNumber().extension());
        changeOrderRequest.setConnectionType(payload.connectionType().BCSType());

        String orderId = samsServices.migrateSamsOrder(changeOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new SAMSOrderResponseDTO(orderId));
    }

}
