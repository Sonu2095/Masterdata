package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Country;
import com.avaya.amsp.masterdata.dtos.CountryDto;
import com.avaya.amsp.masterdata.repo.CountryRepository;
import com.avaya.amsp.masterdata.service.iface.CountryServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author yadav188 This class work as the service for the country entity
 *
 */
public class CountryService implements CountryServiceIface {

	@Autowired
	private CountryRepository countryRepo;

	/**
	 * This method is used for getting all the available countries in the system
	 */
	@Override
	public List<CountryDto> fetchAllCountries() {
		log.info("fetching available countries");
		List<CountryDto> countryDtoList = new ArrayList<CountryDto>();
		List<Country> countryDataList = countryRepo.findAll();

		if (countryDataList != null && !countryDataList.isEmpty()) {
			countryDataList.forEach(country -> {
				CountryDto countryDto = new CountryDto();
				countryDto.setId(country.getId());
				countryDto.setName(country.getName());
				countryDto.setTwo_letters(country.getTwoLetters());
				countryDto.setThree_letters(country.getThreeLetters());
				countryDto.setLogCreatedOn(country.getLogCreatedOn());
				countryDto.setLogCreatedBy(country.getLogCreatedBy());
				countryDto.setCountryCode(country.getCountry_Code());
				countryDtoList.add(countryDto);

			});
		} else {
			log.info("no countries found");
		}
		return countryDtoList;
	}
}
