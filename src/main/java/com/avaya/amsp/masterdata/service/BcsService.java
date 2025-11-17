package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.BcsBunch;
import com.avaya.amsp.masterdata.dtos.BcsDto;
import com.avaya.amsp.masterdata.repo.BcsRepository;
import com.avaya.amsp.masterdata.service.iface.BcsServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BcsService implements BcsServiceIface {

	@Autowired
	BcsRepository bcsRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<BcsDto> fetchAllBcsBunch() {

		List<BcsDto> dtos = new ArrayList<BcsDto>();
		List<BcsBunch> bcsType = bcsRepo.findAll();

		if (bcsType != null && !bcsType.isEmpty()) {
			bcsType.forEach(bcsTypes -> {
				BcsDto dto = mapper.map(bcsType, BcsDto.class);
				dto.setId(bcsTypes.getId());
				dto.setName(bcsTypes.getName());
				dtos.add(dto);
			});

		} else {
			log.info("No bcstype records found...");
		}
		return dtos;
	}

}
