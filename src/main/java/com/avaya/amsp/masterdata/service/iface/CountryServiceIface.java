package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.CountryDto;

public interface CountryServiceIface {

	List<CountryDto> fetchAllCountries();

}