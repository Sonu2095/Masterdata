package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.PhoneNumberTypeDto;

public interface PhoneNumberTypeServiceIface {

	List<PhoneNumberTypeDto> fetchPhoneNumberType();

}
