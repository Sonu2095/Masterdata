package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.CurrencyDto;

public interface CurrencyServiceIface {

	public List<CurrencyDto> fetchAllCurrencies();

}
