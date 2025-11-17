package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Language;
import com.avaya.amsp.masterdata.dtos.LanguageDto;
import com.avaya.amsp.masterdata.repo.LanguageRepository;
import com.avaya.amsp.masterdata.service.iface.LanguageServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author yadav188 
 * This class is used as service implementation of language
 *
 */
public class LanguageService implements LanguageServiceIface {

	@Autowired
	LanguageRepository languageRepo;

	/**
	 * This method is for getting all the available languages in the system
	 */
	@Override
	public List<LanguageDto> fetchAllLanguages() {
		
		log.info("fetching available languages");
		List<LanguageDto> languageDtoList = new ArrayList<LanguageDto>();
		List<Language> languageList = languageRepo.findAll();

		if (languageList != null && !languageList.isEmpty()) {
			languageList.forEach(language -> {

				LanguageDto languageDto = new LanguageDto();
				languageDto.setLocale(language.getId());
				languageDto.setName(language.getName());
				languageDtoList.add(languageDto);

			});
		} else {
			log.info("no languages found");
		}

		return languageDtoList;
	}

}
