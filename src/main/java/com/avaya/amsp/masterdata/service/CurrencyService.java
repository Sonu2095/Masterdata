package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Currency;
import com.avaya.amsp.masterdata.dtos.CurrencyDto;
import com.avaya.amsp.masterdata.repo.CurrencyRepository;
import com.avaya.amsp.masterdata.service.iface.CurrencyServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author yadav188 
 * This class work as the service for the currency entity
 *
 */
public class CurrencyService implements CurrencyServiceIface {

	@Autowired
	private CurrencyRepository currencyRepo;

	@Autowired
	ModelMapper mapper;

	/**
	 * This method is used for getting all the available currency in the system
	 */
	@Override
	public List<CurrencyDto> fetchAllCurrencies() {

		List<CurrencyDto> currencyDtoList = new ArrayList<CurrencyDto>();
		List<Currency> currencyDataList = currencyRepo.findAll();

		if (currencyDataList != null && !currencyDataList.isEmpty()) {
			currencyDataList.forEach(currency -> {

				CurrencyDto currencyDto = new CurrencyDto();
				currencyDto.setId(currency.getId());
				currencyDto.setCurrencyName(currency.getName());
				currencyDto.setCurrencyCharacter(currency.getSymbol());
				currencyDto.setCurrencyCode(currency.getCode());
				currencyDto.setExchangeRate(currency.getEuroRate());
				currencyDto.setLogCreatedBy(currency.getLogCreatedBy());
				currencyDto.setLogCreatedOn(currency.getLogCreatedOn());
				currencyDto.setLogUpdatedBy(currency.getLogUpdatedBy());
				currencyDto.setLogUpdatedOn(currency.getLogUpdatedOn());
				currencyDtoList.add(currencyDto);

			});
		} else {
			log.info("no currencies found");
		}
		return currencyDtoList;
	}

}
