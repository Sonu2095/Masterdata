package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.LanguageDto;

public interface LanguageServiceIface {

	List<LanguageDto> fetchAllLanguages();

}
