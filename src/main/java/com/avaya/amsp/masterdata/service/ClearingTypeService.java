package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.masterdata.dtos.ClearingTypeDto;
import com.avaya.amsp.masterdata.service.iface.ClearingTypeServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClearingTypeService implements ClearingTypeServiceIface {

	@Autowired
	ModelMapper mapper;

	@Override
	public List<ClearingTypeDto> fetchAllClearingType() {

		List<ClearingTypeDto> dtos = new ArrayList<ClearingTypeDto>();

		for (ArticleClearingTypeEnum status : ArticleClearingTypeEnum.values()) {
			int id = status.getValue(); // Custom value of the enum.
			String name = status.name();
			ClearingTypeDto dto = mapper.map(status, ClearingTypeDto.class);
			dto.setId(id);
			dto.setName(name);
			dtos.add(dto);
			log.info("id {} and name {}of articleclearingenum are ", id, name);
		}
		return dtos;
	}
}
