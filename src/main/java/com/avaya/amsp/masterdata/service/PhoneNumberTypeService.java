package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.PbxPhoneNumberType;
import com.avaya.amsp.masterdata.dtos.PhoneNumberTypeDto;
import com.avaya.amsp.masterdata.repo.PhoneNumberTypeRepository;
import com.avaya.amsp.masterdata.service.iface.PhoneNumberTypeServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhoneNumberTypeService implements PhoneNumberTypeServiceIface {

	@Autowired
	PhoneNumberTypeRepository phoneNumberTypeRepo;

	@Override
	public List<PhoneNumberTypeDto> fetchPhoneNumberType() {
		log.info("fetching available phone number type");

		List<PhoneNumberTypeDto> phoneTypeList = new ArrayList<PhoneNumberTypeDto>();
		List<PbxPhoneNumberType> phoneNumberTypeList = phoneNumberTypeRepo.findAll();

		if (phoneNumberTypeList != null && !phoneNumberTypeList.isEmpty()) {

			phoneNumberTypeList.forEach(phoneType -> {
				PhoneNumberTypeDto pbxDto = new PhoneNumberTypeDto();
				pbxDto.setId(phoneType.getId());
				pbxDto.setName(phoneType.getName());
				phoneTypeList.add(pbxDto);

			});
		} else {
			log.info("no phone number type records found");
		}
		return phoneTypeList;

	}
}
