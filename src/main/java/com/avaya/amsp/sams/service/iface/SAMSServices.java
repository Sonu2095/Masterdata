package com.avaya.amsp.sams.service.iface;

import com.avaya.amsp.sams.dtos.SAMSAddOrderRequestDTO;
import com.avaya.amsp.sams.dtos.SAMSDelMigrateOrderDto;
import com.avaya.amsp.sams.dtos.SAMSGetOrderResponseDTO;

public interface SAMSServices {
    public String openSamsOrder(SAMSAddOrderRequestDTO payload);

    SAMSGetOrderResponseDTO fetchSamsOrder(String orderItemId);

    public String deleteSamsOrder(SAMSDelMigrateOrderDto payload);

    public String migrateSamsOrder(SAMSDelMigrateOrderDto payload);

}
