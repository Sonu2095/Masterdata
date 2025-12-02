package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticleI18NName;
import com.avaya.amsp.domain.ClusterConnection;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.domain.Country;
import com.avaya.amsp.domain.Currency;
import com.avaya.amsp.domain.Language;
import com.avaya.amsp.domain.PbxCluster;
import com.avaya.amsp.domain.PbxNumberLock;
import com.avaya.amsp.domain.PbxSystem;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticleClusterDetailDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.dtos.MasterArticleDto;
import com.avaya.amsp.masterdata.dtos.PbxClusterDto;
import com.avaya.amsp.masterdata.dtos.PbxIdClusterDto;
import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.dtos.PortTypeDto;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberLockRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.service.iface.ClusterServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author yadav188 This class work as the service for the Cluster entity
 *
 */
public class ClusterService implements ClusterServiceIface {

	@Autowired
	private ClusterRepository clusterRepo;

	@Autowired
	private ArticleRepository articleRepo;

	@Autowired
	ArticleClusterRepository articleClustRepo;

	@Autowired
	ArticleClusterI18NRepository artClusterI18NRepo;

	@Autowired
	PbxClusterRepository pbxClusterRepo;

	@Autowired
	private PbxSystemRepository pbxSystemRepo;

	@Autowired
	private PbxNumberLockRepository pbxNumLockRepo;

	@Autowired
	ModelMapper mapper;

	/**
	 * Purpose of this method is to fetch all the available active clusters
	 */
	@Override
	public List<ClusterDto> fetchAllClusters() {
		log.info("fetching available clusters");

		List<ClusterDto> clusterDtoList = new ArrayList<ClusterDto>();
		List<ClusterItem> clusterDataList = clusterRepo.findByActiveOrderByIdAsc(1);

		if (clusterDataList != null && !clusterDataList.isEmpty()) {
			clusterDataList.forEach(cluster -> {

				ClusterDto clusterDto = new ClusterDto();
				clusterDto.setId(cluster.getId());
				clusterDto.setName(cluster.getName());
				clusterDto.setActive(cluster.getActive());
				clusterDto.setRemark(cluster.getRemark());
				clusterDto.setHotlineEmail(cluster.getHotlineEmail());
				clusterDto.setHotlinePhone(cluster.getHotlinePhone());
				clusterDto.setAccCurrencyId(cluster.getAccCurrencyId().getId());
				clusterDto.setCurrencyId(cluster.getArticleCurrency().getId());
				clusterDto.setCountryId(cluster.getCountry().getId());
				clusterDto.setLanguageId(cluster.getLanguage().getId());
				clusterDto.setTimeZoneId(cluster.getTimezoneId());
				clusterDto.setLogCreatedOn(cluster.getLogCreatedOn());
				clusterDto.setLogCreatedBy(cluster.getLogCreatedBy());
				clusterDto.setLogUpdatedBy(cluster.getLogUpdatedBy());
				clusterDto.setLogUpdatedOn(cluster.getLogUpdatedOn());
				clusterDtoList.add(clusterDto);

			});
		} else {
			log.info("no cluster found");
		}
		return clusterDtoList;
	}

	/**
	 * Purpose of this method is to create the cluster
	 */
	@AuditLog(action = "Insert",entity = "ClusterItem",functionality = "Create New Cluster")
	@Override
	@org.springframework.transaction.annotation.Transactional
	public void createCluster(ClusterDto clusterDto) {

		// if (fetchClusterByName(clusterDto.getName()).isPresent()) {
		// log.info("cluster already exists for given request {} ",
		// clusterDto.getName());
		// throw new ResourceAlreadyExistsException(
		// String.format("cluster with type %s is already exists",
		// clusterDto.getName()));
		// }
		Optional<ClusterItem> existingClusterItem = fetchClusterByName(clusterDto.getName());
		ClusterItem clusterItemData;
		if (existingClusterItem.isPresent()) {
			clusterItemData = existingClusterItem.get();

			if (clusterItemData.getActive() != 0) {
				log.info("Cluster with name {} already exists and is active.", clusterDto.getName());
				throw new ResourceAlreadyExistsException(
						String.format("Cluster with name %s already exists and is active", clusterDto.getName()));
			}
		} else {
			clusterItemData = new ClusterItem();
		}
		log.info("adding new cluster to database");

		// ClusterItem clusterItemData = mapper.map(clusterDto, ClusterItem.class);
		// ClusterItem clusterItemData = new ClusterItem();
		/*
		 * Integer idv = new Random().nextInt();
		 * clusterItemData.setId(Integer.toUnsignedLong(idv));
		 */
		clusterItemData.setActive(1);
		clusterItemData.setName(clusterDto.getName());
		clusterItemData.setLogCreatedBy(clusterDto.getLogCreatedBy());
		clusterItemData.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		clusterItemData.setRemark(clusterDto.getRemark());
		clusterItemData.setHotlineEmail(clusterDto.getHotlineEmail());
		clusterItemData.setHotlinePhone(clusterDto.getHotlinePhone());
		
		clusterItemData.setTimezoneId(clusterDto.getTimeZoneId());

		Language language = new Language();
		Country country = new Country();
		Currency currency = new Currency();
		Currency currency2 = new Currency();

		language.setId(clusterDto.getLanguageId());
		country.setId(clusterDto.getCountryId());
		currency.setId(clusterDto.getCurrencyId());
		currency2.setId(clusterDto.getAccCurrencyId());

		clusterItemData.setLanguage(language);
		clusterItemData.setAccCurrencyId(currency2);
		clusterItemData.setArticleCurrency(currency);
		clusterItemData.setCountry(country);
		clusterItemData.setTimezoneId(clusterDto.getTimeZoneId());
		clusterItemData.setLogCreatedBy(clusterDto.getLogCreatedBy());
		clusterItemData.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		ClusterItem clusterRecord = clusterRepo.saveAndFlush(clusterItemData);
		log.info("added cluster record having id {}", clusterRecord.getId());

	}

	/**
	 * Purpose of this method is to update the cluster
	 */

	@AuditLog(action = "Update",entity = "ClusterItem",functionality = "Update existing Cluster")
	@Override
	public void updateCluster(ClusterDto clusterDto) {

		log.info("updating cluster record with ID {}", clusterDto.getId());

		Optional<ClusterItem> clusterRecord = clusterRepo.findById(clusterDto.getId());
		clusterRecord.ifPresentOrElse(value -> {
			//before updating with new name 
			
            String newName = clusterDto.getName();
            if (newName != null && !newName.equals(value.getName())) {
                value.setName(newName);
            }
			
			value.setRemark(clusterDto.getRemark());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			value.setLogUpdatedBy(clusterDto.getLogUpdatedBy());
			value.setActive(1);
			value.setHotlineEmail(clusterDto.getHotlineEmail());
			value.setHotlinePhone(clusterDto.getHotlinePhone());

			Language language = new Language();
			Country country = new Country();
			Currency currency = new Currency();
			Currency currency2 = new Currency();

			language.setId(clusterDto.getLanguageId());
			country.setId(clusterDto.getCountryId());
			currency.setId(clusterDto.getCurrencyId());
			currency2.setId(clusterDto.getAccCurrencyId());

			value.setLanguage(language);
			value.setAccCurrencyId(currency2);
			value.setArticleCurrency(currency);
			value.setCountry(country);
			value.setTimezoneId(clusterDto.getTimeZoneId());

			ClusterItem updateCluster = clusterRepo.save(value);

			log.info("updated cluster record having id {}", updateCluster.getId());

			// clusterRepo.save(value);
		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterDto.getId()));
		});
	}

	/**
	 * Purpose of this method is to delete the cluster
	 */
	@AuditLog(action = "delete",entity = "ClusterItem",functionality = "delete existing Cluster")
	@Override
	public void deleteCluster(Long clusterId) {
		log.info("Removing cluster record with ID {}", clusterId);
		Optional<ClusterItem> record = clusterRepo.findById(clusterId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			clusterRepo.save(value);
		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterId));
		});

	}

	/**
	 * Purpose of this method is to fetch the clusters by cluster name
	 */
	@Override
	public Optional<ClusterItem> fetchClusterByName(String name) {
		log.info("Fetching clusters with name {}", name);
		ClusterItem clusterName = clusterRepo.findByName(name);
		return Optional.ofNullable(clusterName);
	}

	@Override
	public List<ConnectionDto> fetchConnectionsByCluster(long clusterId) {

		log.info("fetching connections for cluster {}", clusterId);
		List<ConnectionDto> dtos = new ArrayList<ConnectionDto>();

		Optional<ClusterItem> record = clusterRepo.findById(clusterId);
		record.ifPresentOrElse(value -> {

			Set<ClusterConnection> clusterConnections = value.getClusterConnection();
			clusterConnections.stream().forEach(clusterConn -> {
				Connection connection = clusterConn.getConnection();

				if (connection.getActive() != 0) {
					ConnectionDto dto = mapper.map(connection, ConnectionDto.class);
					if (connection.getConnectionPortType() != null) {
						dto.setPortTypes(new ArrayList<>());
						connection.getConnectionPortType().stream().forEach(connPort -> {
							dto.getPortTypes().add(mapper.map(connPort.getPortType(), PortTypeDto.class));
						});

					}
					dto.setUser(connection.getLogCreatedBy());
					dto.setCreatedOn(connection.getLogCreatedOn());
					dtos.add(dto);
				}

			});

		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterId));
		});
		return dtos;
	}
	
	private List<String> getRolesForArticle (String roles) {
		List<String> roleList = new ArrayList<>();
		if(roles==null || roles.isEmpty()) {
			return roleList;
		}
		String roleArr[] = roles.split(",");
		return Arrays.asList(roleArr);
	}

	@Override
	public List<ArticleToClusterDto> fetchArticlesByCluster(Long clusterId) {

		log.info("fetching articles for cluster {}", clusterId);
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();
		Optional<ClusterItem> record = clusterRepo.findById(clusterId);
		record.ifPresentOrElse(value -> {

			Set<ArticleCluster> clusterArticles = value.getArticleCluster();

			List<ArticleClusterI18NName> artClusterI18NSet = artClusterI18NRepo.findAllByCluster(clusterId);
			Map<Long, List<ArticleClusterI18NName>> articleNameMap = new HashMap<>();
			artClusterI18NSet.forEach((articleName) -> {
				List<ArticleClusterI18NName> articleNameList = articleNameMap.get(articleName.getArticleId());
				if (articleNameList == null) {
					articleNameList = new ArrayList<>();
				}
				articleNameList.add(articleName);
				articleNameMap.put(articleName.getArticleId(), articleNameList);
			});

			clusterArticles.stream().forEach(clusterArticle -> {

				if (clusterArticle.getActive() != 0) {
					ArticleToClusterDto dto = mapper.map(clusterArticle, ArticleToClusterDto.class);
					List<ArticleClusterI18NName> articleNameList = articleNameMap.get(clusterArticle.getId());
					if (articleNameList != null)
						articleNameList.forEach((articleI18Name) -> {
							if ("en".equalsIgnoreCase(articleI18Name.getLanguageId())) {
								dto.setNameEnglish(articleI18Name.getTranslation());
							} else if ("de".equalsIgnoreCase(articleI18Name.getLanguageId())) {
								dto.setNameGerman(articleI18Name.getTranslation());
							}
						});
					dto.setLogCreatedOn(clusterArticle.getLogCreatedOn());
					dto.setName(clusterArticle.getName());
					dto.setRemark(clusterArticle.getDescription());
					dto.setPricePurchase_dollar(clusterArticle.getPricePurchaseDollar());
					dto.setPricePurchase_euro(clusterArticle.getPricePurchaseEuro());
					dto.setPriceSales_dollar(clusterArticle.getPriceSalesDollar());
					dto.setPriceSales_euro(clusterArticle.getPriceSalesEuro());

					if (clusterArticle.getServiceCodeCluster() != null) {
						dto.setServiceCode(clusterArticle.getServiceCodeCluster().getServiceCode());
					}

					dto.setSlaDays(clusterArticle.getSlaDays());
					dto.setSlaHrs(clusterArticle.getSlaHours());
					dto.setSlaMin(clusterArticle.getSlaMinutes());
					dto.setMasterArticle(clusterArticle.getMasterArticle());
					dto.setSingleArticle(clusterArticle.getSingleArticle());
					dto.setIsMasterPartStatus(clusterArticle.getMasterPartStatus());
					// log.info("valuetransfer is" + clusterArticle.getValueTransfer());
					dto.setValueTransfer(clusterArticle.getValueTransfer());
					dto.setAvailableForRoles(getRolesForArticle(clusterArticle.getAvailableForRoles()));

					if (clusterArticle.getArticleWizardType() != null) {
						dto.setArticleWizardId(clusterArticle.getArticleWizardType().getId());
					}
					dtos.add(dto);
				}
			});

		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterId));
		});
		return dtos;
	}

	@Override
	public List<ArticleToClusterDto> fetchArticlesByClusterPoolEnabled(Long clusterId) {

		log.info("fetching articles for cluster {}", clusterId);
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();
		Optional<ClusterItem> record = clusterRepo.findById(clusterId);
		record.ifPresentOrElse(value -> {

			Set<ArticleCluster> clusterArticles = value.getArticleCluster();
			clusterArticles.stream().forEach(clusterArticle -> {

				if (clusterArticle.getActive() != 0 && clusterArticle.getPoolHandling() == 1) {
					ArticleToClusterDto dto = mapper.map(clusterArticle, ArticleToClusterDto.class);

					// dto.setArticleId(clusterArticle.getArticle().getId());
					// dto.setClusterId(clusterArticle.getClusterItem().getId());

					dto.setLogCreatedOn(clusterArticle.getLogCreatedOn());
					dto.setName(clusterArticle.getName());
					dto.setRemark(clusterArticle.getDescription());
					dto.setPricePurchase_dollar(clusterArticle.getPricePurchaseDollar());
					dto.setPricePurchase_euro(clusterArticle.getPricePurchaseEuro());
					dto.setPriceSales_dollar(clusterArticle.getPriceSalesDollar());
					dto.setPriceSales_euro(clusterArticle.getPriceSalesEuro());

					if (clusterArticle.getServiceCodeCluster() != null) {
						dto.setServiceCode(clusterArticle.getServiceCodeCluster().getServiceCode());
					}

					dto.setSlaDays(clusterArticle.getSlaDays());
					if (clusterArticle.getSlaHours() != null) {
						dto.setSlaHrs(clusterArticle.getSlaHours());
					}
					dto.setSlaMin(clusterArticle.getSlaMinutes());
					dtos.add(dto);
				}
			});

		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterId));
		});
		return dtos;
	}

	@Override
	public List<ArticleToClusterDto> fetchArticlesByClusterForPool(Long clusterId) {

		log.info("fetching articles by cluster id for pool {}", clusterId);
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();
		Optional<ClusterItem> record = clusterRepo.findById(clusterId);
		record.ifPresentOrElse(value -> {

			Set<ArticleCluster> clusterArticles = value.getArticleCluster();
			clusterArticles.stream().forEach(clusterArticle -> {

				if (clusterArticle.getActive() != 0 && clusterArticle.getPoolHandling() == 1
						&& clusterArticle.getArticleCategory() != null && clusterArticle.getArticleCategory().getId() == 2) {
					ArticleToClusterDto dto = mapper.map(clusterArticle, ArticleToClusterDto.class);

					// dto.setArticleId(clusterArticle.getArticle().getId());
					// dto.setClusterId(clusterArticle.getClusterItem().getId());

					dto.setLogCreatedOn(clusterArticle.getLogCreatedOn());
					dto.setName(clusterArticle.getName());
					dto.setRemark(clusterArticle.getDescription());
					dto.setPricePurchase_dollar(clusterArticle.getPricePurchaseDollar());
					dto.setPricePurchase_euro(clusterArticle.getPricePurchaseEuro());
					dto.setPriceSales_dollar(clusterArticle.getPriceSalesDollar());
					dto.setPriceSales_euro(clusterArticle.getPriceSalesEuro());

					if (clusterArticle.getServiceCodeCluster() != null) {
						dto.setServiceCode(clusterArticle.getServiceCodeCluster().getServiceCode());
					}

					dto.setSlaDays(clusterArticle.getSlaDays());
					if (clusterArticle.getSlaHours() != null) {
						dto.setSlaHrs(clusterArticle.getSlaHours());
					}
					dto.setSlaMin(clusterArticle.getSlaMinutes());
					dtos.add(dto);
				}
			});

		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterId));
		});
		return dtos;
	}
	
	private void addArticlesToCluster(Long clusterId, List<Long> articleIds, String user) {
		Optional<ClusterItem> cluster = clusterRepo.findById(clusterId);

		cluster.ifPresentOrElse(clusterRecord -> {

			List<Article> masterArticleRecords = articleRepo.findAllById(articleIds);
			log.info("no of rows available {}", masterArticleRecords.size());

			if (masterArticleRecords.size() != articleIds.size()) {
				throw new IllegalArgumentException("Some of the articles not found");
			}

			List<ArticleCluster> articleClusterRecord = new ArrayList<ArticleCluster>();

			masterArticleRecords.forEach((masterArticleRecord) -> {
				ArticleCluster articleCluster = new ArticleCluster();
				List<ArticleCluster> articleClusterData = articleClustRepo.findByClusterArticle(clusterId,
						masterArticleRecord.getId());

				if (articleClusterData != null && !articleClusterData.isEmpty() && articleClusterData.size() == 1) {
					log.error("duplicate record not allowed for articleId :{}, name:{} and clusterId :{}, name :{}",
							masterArticleRecord.getId(), masterArticleRecord.getName(), clusterRecord.getId(),
							clusterRecord.getName());
				} else {
					articleCluster.setArticle(masterArticleRecord);
					articleCluster.setClusterItem(clusterRecord);
					articleCluster.setMasterArticle(1);
					articleCluster.setActive(1);
					articleCluster.setName(masterArticleRecord.getName());
					articleCluster.setDescription(masterArticleRecord.getDescription());
					articleCluster.setLifetime(masterArticleRecord.getLifeTime());
					articleCluster.setArticleCategory(masterArticleRecord.getArticleCategory());
					articleCluster.setArticleClearingType(masterArticleRecord.getArticleClearingType());
					articleCluster.setServiceCodeCluster(masterArticleRecord.getServiceCode());
					articleCluster.setSingleArticle(masterArticleRecord.getSingleArticle());
					articleCluster.setArticleWizardType(masterArticleRecord.getArticleWizardType());

					articleCluster.setClearingAtNewConnection(masterArticleRecord.getClearingAtNewConnection());
					articleCluster.setClearingAtDelete(masterArticleRecord.getClearingAtDelete());
					articleCluster.setClearingAtChangeMove(masterArticleRecord.getClearingAtChangeMove());

					articleCluster.setSlaDays(masterArticleRecord.getSlaDays());
					articleCluster.setSlaHours(masterArticleRecord.getSlaHours());
					articleCluster.setSlaMinutes(masterArticleRecord.getSlaMinutes());

					articleCluster.setPricePurchaseDollar(masterArticleRecord.getPricePurchaseDollar());
					articleCluster.setPricePurchaseEuro(masterArticleRecord.getPricePurchaseEuro());
					articleCluster.setPriceSalesDollar(masterArticleRecord.getPriceSalesDollar());
					articleCluster.setPriceSalesEuro(masterArticleRecord.getPriceSalesEuro());
					articleCluster.setPriority(masterArticleRecord.getPriority());
					articleCluster.setQuantifier(masterArticleRecord.getQuantifier());
					articleCluster.setSapAvaya(masterArticleRecord.getSapAvaya());
					articleCluster.setSapBosh(masterArticleRecord.getSapBosh());
					articleCluster.setSubjectToAuthorization(masterArticleRecord.getSubjectToAuthorization());
					articleCluster.setValueDefault(masterArticleRecord.getValueDefault());
					articleCluster.setValueReadOnly(masterArticleRecord.getValueReadOnly());

					articleCluster.setHardwareFromAvaya(masterArticleRecord.getHardwareFromAvaya());
					articleCluster.setSubjectToAuthorization(masterArticleRecord.getSubjectToAuthorization());
					articleCluster.setBilling(masterArticleRecord.getBilling());
					articleCluster.setIncidentArticle(masterArticleRecord.getIncidentArticle());
					articleCluster.setServusInterface(masterArticleRecord.getServusInterface());
					articleCluster.setHidden(masterArticleRecord.getHidden());
					articleCluster.setNonAvailable(masterArticleRecord.getNonAvailable());
					articleCluster.setShippingAddress(masterArticleRecord.getShippingAddress());
					articleCluster.setAssemblingAddress(masterArticleRecord.getAssemblingAddress());
					articleCluster.setPoolHandling(masterArticleRecord.getPoolHandling());

					articleCluster.setLogCreatedBy(masterArticleRecord.getLogCreatedBy());
					articleCluster.setLogCreatedOn(masterArticleRecord.getLogCreatedOn());
					articleCluster.setLogUpdatedBy(masterArticleRecord.getLogUpdatedBy());
					articleCluster.setLogUpdatedOn(masterArticleRecord.getLogUpdatedOn());
					articleCluster.setProperty(masterArticleRecord.getProperty());
					// articleCluster.setIsPart(masterArticleRecord.getIsPart());

					articleCluster.setIsPart(0);
					articleCluster.setMasterPartStatus(false);
					articleCluster.setValueTransfer(masterArticleRecord.getValueTransfer());
					articleCluster.setArticleWizardType(masterArticleRecord.getArticleWizardType());
					articleCluster.setAvailableForRoles(masterArticleRecord.getAvailableForRoles());
					articleClusterRecord.add(articleCluster);
				}

			});

			List<ArticleCluster> resultList = articleClustRepo.saveAll(articleClusterRecord);
			List<ArticleClusterI18NName> artClusterNameList = new ArrayList<ArticleClusterI18NName>();
			resultList.forEach((cArticle)->{
				Set<ArticleI18NName> articleNameSet = cArticle.getArticle().getArticleI18NName();
				if(articleNameSet!=null&&!articleNameSet.isEmpty()) {
					articleNameSet.forEach((articleName)->{
						ArticleClusterI18NName artClusterName = new ArticleClusterI18NName();
						artClusterName.setArticle(cArticle);
						artClusterName.setLanguage(articleName.getLanguage());
						artClusterName.setTranslation(articleName.getTranslation());
						artClusterName.setLogCreatedBy(user);
						artClusterName.setLogUpdatedBy(user);
						Timestamp ts = new Timestamp(System.currentTimeMillis());
						artClusterName.setLogCreatedOn(ts);
						artClusterName.setLogUpdatedOn(ts);
						artClusterNameList.add(artClusterName);
					});
				}
			});
			artClusterI18NRepo.saveAll(artClusterNameList);

		}, () -> {
			log.info("cluster id not found");
			throw new ResourceNotFoundException(String.format("article with cluster Id %s not found ", clusterId));
		});
	}
	
	private void addArticlesToClusterOnConnMapping(Long clusterId, List<Long> articleIds, String user) {
		Optional<ClusterItem> cluster = clusterRepo.findById(clusterId);

		cluster.ifPresentOrElse(clusterRecord -> {

			List<Article> masterArticleRecords = articleRepo.findAllById(articleIds);
			log.info("no of rows available {}", masterArticleRecords.size());

			if (masterArticleRecords.size() != articleIds.size()) {
				throw new IllegalArgumentException("Some of the articles not found");
			}

			List<ArticleCluster> articleClusterRecord = new ArrayList<ArticleCluster>();

			masterArticleRecords.forEach((masterArticleRecord) -> {
				ArticleCluster articleCluster = new ArticleCluster();
				List<ArticleCluster> articleClusterData = articleClustRepo.findByClusterArticle(clusterId,
						masterArticleRecord.getId());

				if (articleClusterData != null && !articleClusterData.isEmpty() && articleClusterData.size() == 1) {
					log.error("duplicate record not allowed for articleId :{}, name:{} and clusterId :{}, name :{}",
							masterArticleRecord.getId(), masterArticleRecord.getName(), clusterRecord.getId(),
							clusterRecord.getName());
				} else {
					articleCluster.setArticle(masterArticleRecord);
					articleCluster.setClusterItem(clusterRecord);
					articleCluster.setMasterArticle(1);
					articleCluster.setActive(1);
					articleCluster.setName(masterArticleRecord.getName());
					articleCluster.setDescription(masterArticleRecord.getDescription());
					articleCluster.setLifetime(masterArticleRecord.getLifeTime());
					articleCluster.setArticleCategory(masterArticleRecord.getArticleCategory());
					articleCluster.setArticleClearingType(masterArticleRecord.getArticleClearingType());
					articleCluster.setServiceCodeCluster(masterArticleRecord.getServiceCode());
					articleCluster.setSingleArticle(masterArticleRecord.getSingleArticle());
					articleCluster.setArticleWizardType(masterArticleRecord.getArticleWizardType());

					articleCluster.setClearingAtNewConnection(masterArticleRecord.getClearingAtNewConnection());
					articleCluster.setClearingAtDelete(masterArticleRecord.getClearingAtDelete());
					articleCluster.setClearingAtChangeMove(masterArticleRecord.getClearingAtChangeMove());

					articleCluster.setSlaDays(masterArticleRecord.getSlaDays());
					articleCluster.setSlaHours(masterArticleRecord.getSlaHours());
					articleCluster.setSlaMinutes(masterArticleRecord.getSlaMinutes());

					articleCluster.setPricePurchaseDollar(masterArticleRecord.getPricePurchaseDollar());
					articleCluster.setPricePurchaseEuro(masterArticleRecord.getPricePurchaseEuro());
					articleCluster.setPriceSalesDollar(masterArticleRecord.getPriceSalesDollar());
					articleCluster.setPriceSalesEuro(masterArticleRecord.getPriceSalesEuro());
					articleCluster.setPriority(masterArticleRecord.getPriority());
					articleCluster.setQuantifier(masterArticleRecord.getQuantifier());
					articleCluster.setSapAvaya(masterArticleRecord.getSapAvaya());
					articleCluster.setSapBosh(masterArticleRecord.getSapBosh());
					articleCluster.setSubjectToAuthorization(masterArticleRecord.getSubjectToAuthorization());
					articleCluster.setValueDefault(masterArticleRecord.getValueDefault());
					articleCluster.setValueReadOnly(masterArticleRecord.getValueReadOnly());

					articleCluster.setHardwareFromAvaya(masterArticleRecord.getHardwareFromAvaya());
					articleCluster.setSubjectToAuthorization(masterArticleRecord.getSubjectToAuthorization());
					articleCluster.setBilling(masterArticleRecord.getBilling());
					articleCluster.setIncidentArticle(masterArticleRecord.getIncidentArticle());
					articleCluster.setServusInterface(masterArticleRecord.getServusInterface());
					articleCluster.setHidden(masterArticleRecord.getHidden());
					articleCluster.setNonAvailable(masterArticleRecord.getNonAvailable());
					articleCluster.setShippingAddress(masterArticleRecord.getShippingAddress());
					articleCluster.setAssemblingAddress(masterArticleRecord.getAssemblingAddress());
					articleCluster.setPoolHandling(masterArticleRecord.getPoolHandling());

					articleCluster.setLogCreatedBy(masterArticleRecord.getLogCreatedBy());
					articleCluster.setLogCreatedOn(masterArticleRecord.getLogCreatedOn());
					articleCluster.setLogUpdatedBy(masterArticleRecord.getLogUpdatedBy());
					articleCluster.setLogUpdatedOn(masterArticleRecord.getLogUpdatedOn());
					articleCluster.setProperty(masterArticleRecord.getProperty());
					// articleCluster.setIsPart(masterArticleRecord.getIsPart());

					articleCluster.setIsPart(0);
					articleCluster.setMasterPartStatus(false);
					articleCluster.setValueTransfer(masterArticleRecord.getValueTransfer());
					articleCluster.setArticleWizardType(masterArticleRecord.getArticleWizardType());
					articleCluster.setAvailableForRoles(masterArticleRecord.getAvailableForRoles());
					articleClusterRecord.add(articleCluster);
				}

			});

			List<ArticleCluster> resultList = articleClustRepo.saveAll(articleClusterRecord);
			List<ArticleClusterI18NName> artClusterNameList = new ArrayList<ArticleClusterI18NName>();
			resultList.forEach((cArticle)->{
				Set<ArticleI18NName> articleNameSet = cArticle.getArticle().getArticleI18NName();
				if(articleNameSet!=null&&!articleNameSet.isEmpty()) {
					articleNameSet.forEach((articleName)->{
						ArticleClusterI18NName artClusterName = new ArticleClusterI18NName();
						artClusterName.setArticle(cArticle);
						artClusterName.setLanguage(articleName.getLanguage());
						artClusterName.setTranslation(articleName.getTranslation());
						artClusterName.setLogCreatedBy(user);
						artClusterName.setLogUpdatedBy(user);
						Timestamp ts = new Timestamp(System.currentTimeMillis());
						artClusterName.setLogCreatedOn(ts);
						artClusterName.setLogUpdatedOn(ts);
						artClusterNameList.add(artClusterName);
					});
				}
			});
			artClusterI18NRepo.saveAll(artClusterNameList);

		}, () -> {
			log.info("cluster id not found");
			throw new ResourceNotFoundException(String.format("article with cluster Id %s not found ", clusterId));
		});
	}
	
	public void addArticlesToClusterOnConnMapping(Long clusterId, ArticleClusterDetailDto articles) {
		log.info("request received for adding Articles To Cluster On Connection Mapping");
		addArticlesToClusterOnConnMapping(clusterId, articles.getArticleIds(), articles.getUser());
	}

	@Override
	public void addArticlesToCluster(Long clusterId, ArticleClusterDetailDto articles) {
		log.info("request received for adding master articles to cluster");
		addArticlesToCluster(clusterId, articles.getArticleIds(), articles.getUser());
	}

	@Override
	public void addArticlesToClusterForConnection(Long clusterId, MasterArticleDto articles) {
		log.info("request received for adding master articles to cluster");
		addArticlesToCluster(clusterId, articles.getArticleIds(), null);
	}

	@Override
	public void deleteArticlesFromCluster(Long clusterId, ArticleClusterDetailDto articles) {

		log.info("request for deleting articles from cluster");
		Optional<ClusterItem> clusterRecord = clusterRepo.findById(clusterId);

		clusterRecord.ifPresentOrElse(article -> {

			Set<ArticleCluster> articleClusterRecord = article.getArticleCluster();

			if (articleClusterRecord != null && articleClusterRecord.size() != 0) {
				List<ArticleCluster> articleClusterList = new ArrayList<ArticleCluster>();
				articles.getArticleIds().forEach(clusterArticleConn -> {
					ArticleCluster articleCluster = new ArticleCluster();
					// articleCluster.setClusterId(clusterId);
					// articleCluster.setArticleId(clusterArticleConn);
					ArticleCluster clusterFromdb = articleClusterRecord.stream()
							.filter(value -> value.equals(articleCluster)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(
									String.format("articles not found for cluster Id %s", clusterId)));

					articleClusterList.add(clusterFromdb);

				});

				log.info("Removing articles from cluster {} ", articleClusterList);
				articleClustRepo.deleteAllInBatch(articleClusterList);

			} else {
				throw new IllegalArgumentException(
						String.format("No article is associated with cluster Id %s", clusterId));
			}

		}, () -> {
			log.info("cluster with Id {} not found", clusterId);
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found", clusterId));
		});

	}

	@Override
	public List<PbxClusterDto> fetchPbxByCluster(Long id) {
		log.debug("Fetching all pbxClusters by cluster Id {}", id);
		List<PbxCluster> pbxClusterList = pbxClusterRepo.findByClusterId(id);
		List<PbxClusterDto> pbxClusterDtos = new ArrayList<>();
		for (PbxCluster pbxCluster : pbxClusterList) {
			if (pbxCluster.getActive() != 0) {
				pbxClusterDtos.add(mapper.map(pbxCluster, PbxClusterDto.class));
			}
		}
		log.debug("Returning pbxClusterDtos by cluster as {}", pbxClusterDtos);
		return pbxClusterDtos;
	}

	@Override
	public List<PbxNumberLockDto> fetchAllNumberLockByCluser(Long idCluster) {

		log.info("fetching numberLock for cluster {}", idCluster);

		List<PbxNumberLockDto> dtos = new ArrayList<PbxNumberLockDto>();
		List<PbxNumberLock> record = pbxNumLockRepo.findByclusterItem_Id(idCluster);

		if (record != null && !record.isEmpty()) {
			record.forEach(value -> {
				PbxNumberLockDto pbxNumberLockDto = mapper.map(value, PbxNumberLockDto.class);
				if (value.getPbxcluster() != null) {
					pbxNumberLockDto.setIdPbxCluster(value.getPbxcluster().getId());
				} else {
					pbxNumberLockDto.setIdPbxCluster(null);
				}
				dtos.add(pbxNumberLockDto);
			});
		} else {
			log.info("no cluster found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", idCluster));
		}
		return dtos;
	}

	@Override
	public List<PbxSystemDto> fetchAllPbxSystemByCluster(Long idCluster) {

		log.info("fetching pbxSystems for pbx cluster {}", idCluster);

		List<PbxSystemDto> dtos = new ArrayList<PbxSystemDto>();
		List<PbxSystem> record = pbxSystemRepo.findByPbxCluster_ClusterItem_Id(idCluster);

		if (record != null && !record.isEmpty()) {
			record.forEach(value -> {

				PbxSystemDto dto = mapper.map(value, PbxSystemDto.class);
				if (value.getpbxSystemSite() != null) {
					dto.setSites(new ArrayList<>());
					value.getpbxSystemSite().stream().forEach(pbxSystemSite -> {
						dto.getSites().add(mapper.map(pbxSystemSite.getSite(), SiteDto.class));
					});
				}
				dtos.add(dto);

			});
		} else {
			log.info("no cluster found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", idCluster));
		}
		return dtos;
	}

	@Override
	public List<ArticleToClusterDto> fetchArticlesByClusterForPartList(Long clusterId) {
		log.info("fetching articles for cluster for partlist {}", clusterId);
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();
		Optional<ClusterItem> record = clusterRepo.findById(clusterId);
		record.ifPresentOrElse(value -> {

			Set<ArticleCluster> clusterArticles = value.getArticleCluster();
			clusterArticles.stream().forEach(clusterArticle -> {

				if (clusterArticle.getActive() != 0 && clusterArticle.getMasterPartStatus()) {
					ArticleToClusterDto dto = mapper.map(clusterArticle, ArticleToClusterDto.class);
					dto.setLogCreatedOn(clusterArticle.getLogCreatedOn());
					dto.setName(clusterArticle.getName());
					dto.setRemark(clusterArticle.getDescription());
					dto.setPricePurchase_dollar(clusterArticle.getPricePurchaseDollar());
					dto.setPricePurchase_euro(clusterArticle.getPricePurchaseEuro());
					dto.setPriceSales_dollar(clusterArticle.getPriceSalesDollar());
					dto.setPriceSales_euro(clusterArticle.getPriceSalesEuro());

					if (clusterArticle.getServiceCodeCluster() != null) {
						dto.setServiceCode(clusterArticle.getServiceCodeCluster().getServiceCode());
					}

					dto.setSlaDays(clusterArticle.getSlaDays());
					dto.setSlaHrs(clusterArticle.getSlaHours());
					dto.setSlaMin(clusterArticle.getSlaMinutes());
					dto.setMasterArticle(clusterArticle.getMasterArticle());
					dto.setSingleArticle(clusterArticle.getSingleArticle());
					dto.setIsMasterPartStatus(clusterArticle.getMasterPartStatus());
					dto.setValueTransfer(clusterArticle.getValueTransfer());

					if (clusterArticle.getArticleWizardType() != null) {
						dto.setArticleWizardId(clusterArticle.getArticleWizardType().getId());
					}
					dtos.add(dto);
				}
			});

		}, () -> {
			log.info("cluster record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", clusterId));
		});
		return dtos;
	}
	
	public List<PbxIdClusterDto> fetchAllPbxIdByCluster(Long clusterid){
		
        List<PbxCluster> clusters = pbxClusterRepo.findByClusterItemId(clusterid);
        return clusters.stream()
                .map(cluster -> {
                    PbxIdClusterDto dto = new PbxIdClusterDto();

                    // Ensure 'clusterItem' is not null
                    dto.setClusterId(cluster.getClusterItem() != null ? cluster.getClusterItem().getId() : null);
                    
                    // Ensure 'pbxId' is not null
                    dto.setName(cluster.getPbxId() != null ? cluster.getPbxId() : "Unknown");

                    return dto;
                })
                .filter(distinctByKey(dto -> dto.getClusterId() + "-" + dto.getName()))  // Ensuring uniqueness
                .collect(Collectors.toList());
	}
	
	public List<Map<String, String>> fetchAllAreaCodeByCluster(Long clusterid) {
	    List<PbxCluster> clusters = pbxClusterRepo.findByClusterItemId(clusterid);

	    return clusters.stream()
	            .map(cluster -> {
	                PbxIdClusterDto dto = new PbxIdClusterDto();

	                dto.setClusterId(cluster.getClusterItem() != null ? cluster.getClusterItem().getId() : null);
	                dto.setName(cluster.getAreacode() != null ? cluster.getAreacode() : "Unknown");
	                return dto;
	            })
	            .filter(dto -> dto.getName() != null && !dto.getName().trim().isEmpty())
	            .filter(distinctByKey(dto -> dto.getClusterId() + "-" + dto.getName()))
	            .map(dto -> {
	                Map<String, String> map = new HashMap<>();
	                map.put("name", dto.getName());
	        
	                return map;
	            })
	            .collect(Collectors.toList());
	}
	// Custom distinct by key function
	public static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
	    Map<Object, Boolean> seen = new HashMap<>();
	    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	@Override
	public List<ClusterDto> fetchClustersByClusterIds(List<Long> clusterIds) {
		List<ClusterDto> clusterDtoList = new ArrayList<ClusterDto>();
		List<ClusterItem> clusterDataList = clusterRepo.findByIds(clusterIds);

		if (clusterDataList != null && !clusterDataList.isEmpty()) {
			clusterDataList.forEach(cluster -> {

				ClusterDto clusterDto = new ClusterDto();
				clusterDto.setId(cluster.getId());
				clusterDto.setName(cluster.getName());
				clusterDto.setActive(cluster.getActive());
				clusterDto.setRemark(cluster.getRemark());
				clusterDto.setHotlineEmail(cluster.getHotlineEmail());
				clusterDto.setHotlinePhone(cluster.getHotlinePhone());
				clusterDto.setAccCurrencyId(cluster.getAccCurrencyId().getId());
				clusterDto.setCurrencyId(cluster.getArticleCurrency().getId());
				clusterDto.setCountryId(cluster.getCountry().getId());
				clusterDto.setLanguageId(cluster.getLanguage().getId());
				clusterDto.setTimeZoneId(cluster.getTimezoneId());
				clusterDto.setLogCreatedOn(cluster.getLogCreatedOn());
				clusterDto.setLogCreatedBy(cluster.getLogCreatedBy());
				clusterDto.setLogUpdatedBy(cluster.getLogUpdatedBy());
				clusterDto.setLogUpdatedOn(cluster.getLogUpdatedOn());
				clusterDtoList.add(clusterDto);

			});
		} else {
			log.info("no cluster found");
		}
		return clusterDtoList;	}
}
	
