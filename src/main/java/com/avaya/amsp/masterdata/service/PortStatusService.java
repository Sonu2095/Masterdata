package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.PortStatusEnum;
import com.avaya.amsp.masterdata.dtos.PortStatusDto;
import com.avaya.amsp.masterdata.service.iface.PortStatusServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PortStatusService implements PortStatusServiceIface {
	
	@Autowired
	ModelMapper mapper;

	@Override
	public List<PortStatusDto> fetchAllPortStatus() {
		List<PortStatusDto> dtos = new ArrayList<PortStatusDto>();

		for (PortStatusEnum status : PortStatusEnum.values()) {
			int id = status.getValue(); // Custom value of the enum.
			String name = status.name();
			PortStatusDto dto = mapper.map(status, PortStatusDto.class);
			dto.setId(id);
			dto.setName(name);
			dtos.add(dto);
			log.info("id {} and name {}of port status are ", id, name);
		}
		return dtos;
	}

}
