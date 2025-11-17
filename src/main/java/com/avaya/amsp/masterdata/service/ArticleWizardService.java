package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleWizard;
import com.avaya.amsp.masterdata.dtos.ArticleWizardDto;
import com.avaya.amsp.masterdata.repo.ArticleWizardRepository;
import com.avaya.amsp.masterdata.service.iface.ArticleWizardServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ArticleWizardService implements ArticleWizardServiceIface {

	@Autowired
	ArticleWizardRepository articleWizardRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<ArticleWizardDto> fetchArticleWizard() {
		List<ArticleWizardDto> articleWizarddtos = new ArrayList<ArticleWizardDto>();

		List<ArticleWizard> wizardNumber = articleWizardRepo.findAll();

		if (wizardNumber != null && !wizardNumber.isEmpty()) {

			wizardNumber.forEach(wizard -> {
				ArticleWizardDto articleWizarddto = mapper.map(wizard, ArticleWizardDto.class);
				articleWizarddto.setId(wizard.getId());
				articleWizarddto.setName(wizard.getName());
				articleWizarddto.setRemark(wizard.getRemark());
				articleWizarddtos.add(articleWizarddto);

			});

		} else {
			log.info("No article wizard records found...");
		}
		return articleWizarddtos;
	}
}
