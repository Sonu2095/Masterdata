package com.avaya.amsp.masterdata.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.PartList;
import com.avaya.amsp.domain.PartListPropertyType;
import com.avaya.amsp.masterdata.dtos.PartListDto;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.PartListRepository;
import com.avaya.amsp.masterdata.service.iface.PartListServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author mreddys
 * Service for the partList entity
 *
 */
public class PartListService implements PartListServiceIface {

	@Autowired
	private PartListRepository partListRepo;
	
	@Autowired
	private ArticleRepository articleRepo;
	
	@Autowired
	private ClusterRepository clusterRepo;

	@Autowired
	private ModelMapper mapper;

	@Override
	public List<PartListDto> fetchAllPartList() {
		Iterable<PartList> partLists = partListRepo.findAll();
		ArrayList<PartListDto> partListsDto = new ArrayList<>();
		for(PartList partList : partLists) {
			log.debug("Fetched " + partList + " from DB");
			partListsDto.add(mapper.map(partList, PartListDto.class));
		}
		
		log.info("Got list : {}", partListsDto);
		return partListsDto;
	}



	@Override
	@org.springframework.transaction.annotation.Transactional
	public long savePartList(PartListDto partListDto) {
		PartList partList = new PartList();
		
		Article article = articleRepo.findById(partListDto.getArticle().getId()).get();
		partList.setArticle(article);
		
		ClusterItem cluster = clusterRepo.findById(partListDto.getCluster().getId()).get();
		partList.setCluster(cluster);
		
		partList.setIsMasterList(partListDto.getIsMasterList());
		partList.setLogCreatedBy(partListDto.getLogCreatedBy());
		partList.setLogCreatedOn(partListDto.getLogCreatedOn());
		partList.setMonthlyCost(partListDto.getMonthlyCost());
		partList.setOnetimeCost(partListDto.getOnetimeCost());
		partList.setSubArticleCount(partListDto.getSubArticleCount());
		partList.setPropertyType(partListDto.getPropertyType());
		partList.setWithValueTransfer(partListDto.getWithValueTransfer());
		
		PartList savedPartList = partListRepo.save(partList);
		return savedPartList.getId();
	}
}
