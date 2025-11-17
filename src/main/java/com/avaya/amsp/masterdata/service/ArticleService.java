package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ArticleCategory;
import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticleI18NName;
import com.avaya.amsp.domain.ArticleWizard;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Language;
import com.avaya.amsp.domain.PartListSubArticle;
import com.avaya.amsp.domain.PartlistClusterSubarticle;
import com.avaya.amsp.domain.PartsPropertyEnum;
import com.avaya.amsp.domain.ServiceCode;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticleDto;
import com.avaya.amsp.masterdata.dtos.ArticleI18NameDTO;
import com.avaya.amsp.masterdata.dtos.ArticlePartListDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterSyncDto;
import com.avaya.amsp.masterdata.dtos.ClusterArticleDetailDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.SubArticleDetailDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticleI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.LanguageRepository;
import com.avaya.amsp.masterdata.repo.PartListArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.PartListSubArticleRepository;
import com.avaya.amsp.masterdata.service.iface.ArticleServiceIface;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ArticleService implements ArticleServiceIface {

	@Autowired
	private ArticleRepository articleRepo;

	@Autowired
	ClusterRepository clusterRepo;

	@Autowired
	ArticleClusterRepository artClustRepo;

	@Autowired
	PartListSubArticleRepository partListRepo;

	@Autowired
	ArticleClusterService articleClusterService;

	@Autowired
	PartListArticleClusterRepository partListArticleClusterRepo;
	
	@Autowired
	ArticleI18NRepository articleI18Repo;
	
	@Autowired
	ArticleClusterI18NRepository artClusterI18NRepo;
	
	@Autowired
	LanguageRepository langRepo;

	@Autowired
	ModelMapper mapper;

	private final String dupArticleError = "duplicate_artice";
	private final String articleSuccess = "article_success";

	double finalSaleMonthly = 0.0;
	double finalSaleOneTime = 0.0;

	double finalPurchaseMonthly = 0.0;
	double finalPurchaseOneTime = 0.0;
	
	private List<String> getRolesForArticle (String roles) {
		List<String> roleList = new ArrayList<>();
		if(roles==null || roles.isEmpty()) {
			return roleList;
		}
		String roleArr[] = roles.split(",");
		return Arrays.asList(roleArr);
	}

	@Override
	public List<ArticleDto> fetchAllArticles() {
		log.info("fetching articles from database");
		Date sdate = new Date();
		List<ArticleDto> dtos = new ArrayList<ArticleDto>();
		List<Article> articles = articleRepo.findAll();
		List<ArticleI18NName> articleNamesList = articleI18Repo.findAll();
		if (articles != null && !articles.isEmpty()) {
			Map<Long,List<ArticleI18NName>> articleNameMap = new HashMap<>();
			articleNamesList.forEach((articleName)->{
				List<ArticleI18NName> articleNameList = articleNameMap.get(articleName.getArticleId());
				if(articleNameList==null) {
					articleNameList = new ArrayList<>();
				}
				articleNameList.add(articleName);
				articleNameMap.put(articleName.getArticleId(), articleNameList);
			});
			articles.forEach(article -> {

				ArticleDto dto = mapper.map(article, ArticleDto.class);
				List<ArticleI18NName> articleNameList = articleNameMap.get(article.getId());
				if(articleNameList!=null)
				articleNameList.forEach((articleI18Name)->{
					if("en".equalsIgnoreCase(articleI18Name.getLanguageId())) {
						dto.setNameEnglish(articleI18Name.getTranslation());
					} else if("de".equalsIgnoreCase(articleI18Name.getLanguageId())) {
						dto.setNameGerman(articleI18Name.getTranslation());
					}
				});
				dto.setName(article.getName());
				dto.setRemark(article.getDescription());
				dto.setUser(article.getLogCreatedBy());
				dto.setLogCreatedOn(article.getLogCreatedOn());
				dto.setLogCreatedBy(article.getLogCreatedBy());
				dto.setLogUpdatedBy(article.getLogUpdatedBy());
				dto.setLogUpdatedOn(article.getLogUpdatedOn());
				dto.setPricePurchase_dollar(article.getPricePurchaseDollar());
				dto.setPricePurchase_euro(article.getPricePurchaseEuro());
				dto.setPriceSales_dollar(article.getPriceSalesDollar());
				dto.setPriceSales_euro(article.getPriceSalesEuro());
				
				if (article.getServiceCode() != null) {
					dto.setServiceCode(article.getServiceCode().getServiceCode());
				}
				dto.setArticleClearingType(article.getArticleClearingType());

				dto.setSlaDays(article.getSlaDays());
				dto.setSlaHrs(article.getSlaHours());
				dto.setSlaMin(article.getSlaMinutes());
				dto.setIsPart(article.getIsPart());
				dto.setProperty(article.getProperty());
				dto.setValueTransfer(article.getValueTransfer());
				if (article.getArticleWizardType() != null) {
					dto.setArticleWizardId(article.getArticleWizardType().getId());
				}
				dto.setAvailableForRoles(getRolesForArticle(article.getAvailableForRoles()));
				dtos.add(dto);
			});

		} else {
			log.info("No article records found...");
		}
		Date eDate = new Date();
		System.out.println("Start time: " + sdate + " , end Time: " + eDate);
		return dtos;
	}
	
	private String getAvailableForRoles(List<String> roleList) {
		if(roleList!=null&&!roleList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> itr =roleList.iterator();
			while(itr.hasNext()) {
				String roleEnum = itr.next();
				sb = sb.append(roleEnum).append(",");
			}
			String result = sb.toString();
			return result.substring(0, result.length()-1);
		}
		return null;
	}

	@Override
	@AuditLog(action = "INSERT",entity = "Article",functionality = "Insert Master Data MasterArticle add Article")
	public String persistArticle(ArticleDto dto) {
		log.info("Adding new Aticle to database");

		// if (fetchArticleByName(dto.getName()).isPresent()) {
		// try {
		// log.info("article already exists for given request {} ", dto.getName());
		// } catch (ResourceAlreadyExistsException e) {
		// log.error("The given article resource already exists");

		// }
		// return dupArticleError;
		// }
		Optional<Article> existingArticle = fetchArticleByName(dto.getName());
		Article articleData;
		if (existingArticle.isPresent()) {
			articleData = existingArticle.get();

			if (articleData.getActive() != 0) {
				log.info("Article with name {} already exists and is active.", dto.getName());
				throw new ResourceAlreadyExistsException(
						String.format("Article with name %s already exists and is active", dto.getName()));
			}
		} else {
			articleData = new Article();
		}

		ArticleCategory articleCategory = new ArticleCategory();
		ServiceCode serviceCode = new ServiceCode();
		ArticleWizard articleWizard = new ArticleWizard();
		
		articleData.setActive(1);
		articleData.setName(dto.getName());
		articleData.setDescription(dto.getRemark());
		articleData.setPricePurchaseDollar(dto.getPricePurchase_dollar());
		articleData.setPricePurchaseEuro(dto.getPricePurchase_euro());
		articleData.setPriceSalesDollar(dto.getPriceSales_dollar());
		articleData.setPriceSalesEuro(dto.getPriceSales_euro());
		articleData.setArticleClearingType(dto.getArticleClearingType());
		if (dto.getArticleClearingType() != null) {
			articleData.setArticleClearingType(dto.getArticleClearingType());
		}
		if (dto.getArticleCategoryId() == null || (dto.getArticleCategoryId() == 0)) {
			log.info("ArticleCategoryId is 0");
			articleData.setArticleCategory(null);
		} else {
			if (dto.getArticleCategoryId() != null) {
				articleCategory.setId(dto.getArticleCategoryId());
				articleData.setArticleCategory(articleCategory);
			}
		}
		
		if (dto.getServiceCode() == null) {
			log.info("ServiceCode is 0");
			articleData.setServiceCode(null);
		} else {
			if (dto.getServiceCode() != null) {
				serviceCode.setServiceCode(dto.getServiceCode());
				articleData.setServiceCode(serviceCode);
			}
		}
		
		if (dto.getArticleWizardId() == null || (dto.getArticleWizardId() == 0)) {
			log.info("WizardId is 0");
		} else {
			if (dto.getArticleWizardId() != null) {
				articleWizard.setId(dto.getArticleWizardId());
				articleData.setArticleWizardType(articleWizard);
			}
		}
		articleData.setAvailableForRoles(getAvailableForRoles(dto.getAvailableForRoles()));
		articleData.setQuantifier(dto.getQuantifier());
		articleData.setSingleArticle(dto.getSingleArticle());
		articleData.setMasterArticle(dto.getMasterArticle());
		articleData.setValueDefault(dto.getValueDefault());
		articleData.setValueReadOnly(dto.getValueReadOnly());
		articleData.setHardwareFromAvaya(dto.getHardwareFromAvaya());
		articleData.setSubjectToAuthorization(dto.getSubjectToAuthorization());
		articleData.setBilling(dto.getBilling());
		articleData.setIncidentArticle(dto.getIncidentArticle());
		articleData.setServusInterface(dto.getServusInterface());
		articleData.setHidden(dto.getHidden());
		articleData.setNonAvailable(dto.getNonAvailable());
		articleData.setShippingAddress(dto.getShippingAddress());
		articleData.setAssemblingAddress(dto.getAssemblingAddress());
		articleData.setPoolHandling(dto.getPoolHandling());
		articleData.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		articleData.setLogCreatedBy(dto.getUser());
		articleData.setLifeTime(dto.getLifeTime());
		articleData.setSapBosh(dto.getSapBosh());
		articleData.setSapAvaya(dto.getSapAvaya());
		articleData.setSlaDays(dto.getSlaDays());
		articleData.setSlaHours(dto.getSlaHrs());
		articleData.setSlaMinutes(dto.getSlaMin());
		articleData.setProperty(PartsPropertyEnum.NULL);
		
		articleData.setValueTransfer(false);
		articleData.setIsPart(0);
		Article article = articleRepo.save(articleData);
		if(dto.getNameEnglish()!=null&&!("".equalsIgnoreCase(dto.getNameEnglish()))) {
			Language engLang = langRepo.getReferenceById("en");
			ArticleI18NName articleNameEng = new ArticleI18NName();
			articleNameEng.setArticle(article);
			articleNameEng.setLanguage(engLang);
			articleNameEng.setTranslation(dto.getNameEnglish());
			articleNameEng.setLogCreatedBy(dto.getUser());
			articleNameEng.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
			articleI18Repo.save(articleNameEng);
		}
		if(dto.getNameGerman()!=null&&!("".equalsIgnoreCase(dto.getNameGerman()))) {
			Language deLang = langRepo.getReferenceById("de");
			ArticleI18NName articleNameDe = new ArticleI18NName();
			articleNameDe.setArticle(article);
			articleNameDe.setLanguage(deLang);
			articleNameDe.setTranslation(dto.getNameGerman());
			articleNameDe.setLogCreatedBy(dto.getUser());
			articleNameDe.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
			articleI18Repo.save(articleNameDe);
		}
		log.info("new article added with id {}", article.getId());
		return articleSuccess;
		}


	@AuditLog(action = "UPDATE",entity = "Article",functionality = "Update MasterData MasterArticle/MasterPartList Prop Update Article")
	@Override
	public void updateArticle(ArticleDto dto) {

		log.info("Updating Article for Id {}", dto.getId());
		ArticleCategory articleCategory = new ArticleCategory();
		ServiceCode serviceCode = new ServiceCode();
		ArticleWizard articleWizard = new ArticleWizard();

		Optional<Article> record = articleRepo.findById(dto.getId());
		record.ifPresentOrElse(value -> {
			// Check if Article with same key exists other than one being updated
			Optional<Article> existingArticle = fetchArticleByName(dto.getName());
			if (existingArticle.isPresent()) {
				Article article = existingArticle.get();
				if (article.getId() != dto.getId()) {
					log.info("Article with key {} is already exists ", dto.getName());
					throw new ResourceAlreadyExistsException(
							String.format("Article with key %s is already exists", dto.getName()));
				}
			}
			
			Article articleData = mapper.map(dto, Article.class);
			articleData.setDescription(dto.getRemark());
			articleData.setPricePurchaseDollar(dto.getPricePurchase_dollar());
			articleData.setPricePurchaseEuro(dto.getPricePurchase_euro());
			articleData.setPriceSalesDollar(dto.getPriceSales_dollar());
			articleData.setPriceSalesEuro(dto.getPriceSales_euro());
			articleData.setLogCreatedBy(value.getLogCreatedBy());
			articleData.setLogCreatedOn(value.getLogCreatedOn());
			articleData.setLogUpdatedBy(dto.getUser());
			articleData.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			articleData.setActive(1);
			articleData.setSingleArticle(dto.getSingleArticle());
			articleData.setMasterArticle(dto.getMasterArticle());
			articleData.setValueDefault(dto.getValueDefault());
			articleData.setValueReadOnly(dto.getValueReadOnly());
			articleData.setHardwareFromAvaya(dto.getHardwareFromAvaya());
			articleData.setSubjectToAuthorization(dto.getSubjectToAuthorization());
			articleData.setBilling(dto.getBilling());
			articleData.setIncidentArticle(dto.getIncidentArticle());
			articleData.setServusInterface(dto.getServusInterface());
			articleData.setHidden(dto.getHidden());
			articleData.setNonAvailable(dto.getNonAvailable());
			articleData.setShippingAddress(dto.getShippingAddress());
			articleData.setAssemblingAddress(dto.getAssemblingAddress());
			articleData.setPoolHandling(dto.getPoolHandling());
			articleData.setLifeTime(dto.getLifeTime());
			articleData.setSapBosh(dto.getSapBosh());
			articleData.setSapAvaya(dto.getSapAvaya());
			articleData.setSlaDays(dto.getSlaDays());
			articleData.setSlaHours(dto.getSlaHrs());
			articleData.setSlaMinutes(dto.getSlaMin());
			articleData.setProperty(dto.getProperty());
			articleData.setValueTransfer(dto.isValueTransfer());
			articleData.setAvailableForRoles(getAvailableForRoles(dto.getAvailableForRoles()));
			
			if (record.get().getIsPart() == 1) {
				articleData.setIsPart(1);
			}

			if (dto.getArticleClearingType() != null) {
				articleData.setArticleClearingType(dto.getArticleClearingType());
			}

			if (dto.getArticleCategoryId() == null || (dto.getArticleCategoryId() == 0)) {
				log.info("ArticleCategoryId is 0");
				articleData.setArticleCategory(null);
			} else {
				if (dto.getArticleCategoryId() != null) {
					articleCategory.setId(dto.getArticleCategoryId());
					articleData.setArticleCategory(articleCategory);
				}
			}

			if (dto.getServiceCode() == null) {
				log.info("ServiceNumberId is 0");
				articleData.setServiceCode(null);
			} else {
				if (dto.getServiceCode() != null) {
					serviceCode.setServiceCode(dto.getServiceCode());
					articleData.setServiceCode(serviceCode);
				}
			}
			if (dto.getArticleWizardId() == null || (dto.getArticleWizardId() == 0)) {
				log.info("WizardId is 0");
			} else {
				if (dto.getArticleWizardId() != null) {
					articleWizard.setId(dto.getArticleWizardId());
					articleData.setArticleWizardType(articleWizard);
				}
			}

			Article article = articleRepo.save(articleData);
			List<ArticleI18NName> articleNameSet = articleI18Repo.findByArticleId(dto.getId());
			if(articleNameSet!=null &&!articleNameSet.isEmpty()) {
				Map<String, ArticleI18NName> articleI18Map = new HashMap<>();
				articleNameSet.forEach((articleI18Name)->{
					articleI18Map.put(articleI18Name.getLanguageId(), articleI18Name);
				});
				if(dto.getNameEnglish()!=null&&!dto.getNameEnglish().isEmpty()) {
					if(articleI18Map.containsKey("en")) {
						ArticleI18NName articleNameEng = articleI18Map.get("en");
						articleNameEng.setTranslation(dto.getNameEnglish());
						articleNameEng.setLogUpdatedBy(dto.getUser());
						articleNameEng.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
						articleI18Repo.save(articleNameEng);
					} else {
						Language engLang = langRepo.getReferenceById("en");
						ArticleI18NName articleNameEng = new ArticleI18NName();
						articleNameEng.setArticle(article);
						articleNameEng.setLanguage(engLang);
						articleNameEng.setTranslation(dto.getNameEnglish());
						articleNameEng.setLogCreatedBy(dto.getUser());
						articleNameEng.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
						articleI18Repo.save(articleNameEng);
					}
				}
				
				if(dto.getNameGerman()!=null&&!dto.getNameGerman().isEmpty()) {
					if(articleI18Map.containsKey("de")) {
						ArticleI18NName articleNameDe = articleI18Map.get("de");
						articleNameDe.setTranslation(dto.getNameGerman());
						articleNameDe.setLogUpdatedBy(dto.getUser());
						articleNameDe.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
						articleI18Repo.save(articleNameDe);
					} else {
						Language gerLang = langRepo.getReferenceById("de");
						ArticleI18NName articleNameDe = new ArticleI18NName();
						articleNameDe.setArticle(article);
						articleNameDe.setLanguage(gerLang);
						articleNameDe.setTranslation(dto.getNameGerman());
						articleNameDe.setLogCreatedBy(dto.getUser());
						articleNameDe.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
						articleI18Repo.save(articleNameDe);
					}
				}
			} else {
				if(dto.getNameEnglish()!=null&&!("".equalsIgnoreCase(dto.getNameEnglish()))) {
					Language engLang = langRepo.getReferenceById("en");
					ArticleI18NName articleNameEng = new ArticleI18NName();
					articleNameEng.setArticle(article);
					articleNameEng.setLanguage(engLang);
					articleNameEng.setTranslation(dto.getNameEnglish());
					articleNameEng.setLogCreatedBy(dto.getUser());
					articleNameEng.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
					articleI18Repo.save(articleNameEng);
				}
				if(dto.getNameGerman()!=null&&!("".equalsIgnoreCase(dto.getNameGerman()))) {
					Language deLang = langRepo.getReferenceById("de");
					ArticleI18NName articleNameDe = new ArticleI18NName();
					articleNameDe.setArticle(article);
					articleNameDe.setLanguage(deLang);
					articleNameDe.setTranslation(dto.getNameGerman());
					articleNameDe.setLogCreatedBy(dto.getUser());
					articleNameDe.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
					articleI18Repo.save(articleNameDe);
				}
			}
			

		}, () -> {
			log.info("Article record not found");
			throw new ResourceNotFoundException(String.format("Article with Id %s not found ", dto.getId()));
		});
		

	}

	@AuditLog(action = "DELETE",entity = "Article",functionality = "Delete Master Data MasterArticle Delete Article")
	@Override
	public void removeArticle(Long articleId) {

		log.info("Removing article record with ID {}", articleId);
		Optional<Article> record = articleRepo.findById(articleId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			articleRepo.save(value);

			List<ArticleCluster> articleClusterData = artClustRepo.findByArticleId(articleId);

			articleClusterData.forEach((artclusData) -> {
				/*
				 * since master article is removed, so active status is ) and hence all the
				 * associated master article from article_cluster and article_connection will be
				 * also set to active status as 0
				 */
				artClustRepo.updateActiveStatus(articleId); //

			});

		}, () -> {
			log.info("Article record not found");
			throw new ResourceNotFoundException(String.format("Article with Id %s not found ", articleId));
		});
	}

	@Override
	public List<ClusterDto> fetchMasterMergeClusters(Long articleId) {

		log.info("fetching clusters for article {}", articleId);
		List<ClusterDto> dtos = new ArrayList<ClusterDto>();

		Optional<Article> record = articleRepo.findById(articleId);
		record.ifPresentOrElse(article -> {
			Set<ArticleCluster> articleCluster = article.getArticleCluster();
			articleCluster.stream().forEach(articleClusterData -> {
				ClusterItem clusterItem = articleClusterData.getClusterItem();

				if (articleClusterData.getMasterArticle() == 1 && articleClusterData.getActive() != 0) {
					if (clusterItem != null && clusterItem.getActive() != 0) {
						ClusterDto dto = mapper.map(clusterItem, ClusterDto.class);
						dto.setName(clusterItem.getName());
						dto.setRemark(clusterItem.getRemark());
						dtos.add(dto);
					}
				}

			});

		}, () -> {
			log.info("Article record not found");
			throw new ResourceNotFoundException(String.format("Article with Id %s not found ", articleId));
		});
		return dtos;
	}

	@Override
	public List<ClusterDto> fetchSlaveMergeClusters(Long articleId) {

		log.info("fetching clusters for article {}", articleId);
		List<ClusterDto> dtos = new ArrayList<ClusterDto>();

		Optional<Article> record = articleRepo.findById(articleId);
		record.ifPresentOrElse(article -> {
			Set<ArticleCluster> articleCluster = article.getArticleCluster();
			articleCluster.stream().forEach(articleClusterData -> {
				ClusterItem clusterItem = articleClusterData.getClusterItem();

				if (articleClusterData.getMasterArticle() != 1 && articleClusterData.getActive() != 0) {

					if (clusterItem.getActive() != 0) {
						ClusterDto dto = mapper.map(clusterItem, ClusterDto.class);
						dto.setName(clusterItem.getName());
						dto.setRemark(clusterItem.getRemark());
						dtos.add(dto);
					}
				}

			});

		}, () -> {
			log.info("Article record not found");
			throw new ResourceNotFoundException(String.format("Article with Id %s not found ", articleId));
		});
		return dtos;
	}

	@Override
	public Optional<Article> fetchArticleByName(String name) {

		log.info("Fetching article with name {}", name);
		Article articleName = articleRepo.findByName(name);
		return Optional.ofNullable(articleName);
	}

	@Override
	public void addClustersToArticle(Long articleId, ClusterArticleDetailDto clusters) {

		log.info("request received for adding clusters to article");

		Optional<Article> articles = articleRepo.findById(articleId);

		articles.ifPresentOrElse(articleRecord -> {

			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}

			List<ArticleCluster> articleClusterRecord = new ArrayList<ArticleCluster>();
			clusterRecords.forEach((clusterData) -> {
				ArticleCluster articleCluster = new ArticleCluster();
				List<ArticleCluster> articleClusterData = artClustRepo.findByArticleCluster(articleId,
						clusterData.getId());

				if (articleClusterData != null && !articleClusterData.isEmpty() && articleClusterData.size() == 1) {
					log.error("duplicate record not allowed for articleId :{}, name:{} and clusterId :{}, name :{}",
							articleRecord.getId(), articleRecord.getName(), clusterData.getId(), clusterData.getName());
				}

				else {
					articleCluster.setArticle(articleRecord);
					articleCluster.setClusterItem(clusterData);
					articleCluster.setMasterArticle(1);
					articleCluster.setActive(1);
					articleCluster.setName(articleRecord.getName());
					articleCluster.setDescription(articleRecord.getDescription());
					articleCluster.setLifetime(articleRecord.getLifeTime());
					articleCluster.setArticleCategory(articleRecord.getArticleCategory());

					articleCluster.setArticleClearingType(articleRecord.getArticleClearingType());
					articleCluster.setServiceCodeCluster(articleRecord.getServiceCode());
					articleCluster.setSingleArticle(articleRecord.getSingleArticle());
					articleCluster.setArticleWizardType(articleRecord.getArticleWizardType());

					articleCluster.setClearingAtNewConnection(articleRecord.getClearingAtNewConnection());
					articleCluster.setClearingAtDelete(articleRecord.getClearingAtDelete());
					articleCluster.setClearingAtChangeMove(articleRecord.getClearingAtChangeMove());

					articleCluster.setSlaDays(articleRecord.getSlaDays());
					articleCluster.setSlaHours(articleRecord.getSlaHours());
					articleCluster.setSlaMinutes(articleRecord.getSlaMinutes());

					articleCluster.setPricePurchaseDollar(articleRecord.getPricePurchaseDollar());
					articleCluster.setPricePurchaseEuro(articleRecord.getPricePurchaseEuro());
					articleCluster.setPriceSalesDollar(articleRecord.getPriceSalesDollar());
					articleCluster.setPriceSalesEuro(articleRecord.getPriceSalesEuro());
					articleCluster.setPriority(articleRecord.getPriority());
					articleCluster.setQuantifier(articleRecord.getQuantifier());
					articleCluster.setSapAvaya(articleRecord.getSapAvaya());
					articleCluster.setSapBosh(articleRecord.getSapBosh());
					articleCluster.setSubjectToAuthorization(articleRecord.getSubjectToAuthorization());
					articleCluster.setValueDefault(articleRecord.getValueDefault());
					articleCluster.setValueReadOnly(articleRecord.getValueReadOnly());

					articleCluster.setHardwareFromAvaya(articleRecord.getHardwareFromAvaya());
					articleCluster.setSubjectToAuthorization(articleRecord.getSubjectToAuthorization());
					articleCluster.setBilling(articleRecord.getBilling());
					articleCluster.setIncidentArticle(articleRecord.getIncidentArticle());
					articleCluster.setServusInterface(articleRecord.getServusInterface());
					articleCluster.setHidden(articleRecord.getHidden());
					articleCluster.setNonAvailable(articleRecord.getNonAvailable());
					articleCluster.setShippingAddress(articleRecord.getShippingAddress());
					articleCluster.setAssemblingAddress(articleRecord.getAssemblingAddress());
					articleCluster.setPoolHandling(articleRecord.getPoolHandling());

					articleCluster.setLogCreatedBy(articleRecord.getLogCreatedBy());
					articleCluster.setLogCreatedOn(articleRecord.getLogCreatedOn());
					articleCluster.setLogUpdatedBy(articleRecord.getLogUpdatedBy());
					articleCluster.setLogUpdatedOn(articleRecord.getLogUpdatedOn());

					articleCluster.setIsPart(0);
					articleCluster.setProperty(articleRecord.getProperty());
					articleCluster.setAvailableForRoles(articleRecord.getAvailableForRoles());

					articleClusterRecord.add(articleCluster);
				}

			});

			List<ArticleCluster> resultList= artClustRepo.saveAll(articleClusterRecord);
			List<ArticleClusterI18NName> artClusterNameList = new ArrayList<ArticleClusterI18NName>();
			resultList.forEach((cArticle)->{
				Set<ArticleI18NName> articleNameSet = cArticle.getArticle().getArticleI18NName();
				if(articleNameSet!=null&&!articleNameSet.isEmpty()) {
					articleNameSet.forEach((articleName)->{
						ArticleClusterI18NName artClusterName = new ArticleClusterI18NName();
						artClusterName.setArticle(cArticle);
						artClusterName.setLanguage(articleName.getLanguage());
						artClusterName.setTranslation(articleName.getTranslation());
						artClusterName.setLogCreatedBy(clusters.getUser());
						artClusterName.setLogUpdatedBy(clusters.getUser());
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
			throw new ResourceNotFoundException(String.format("article with cluster Id %s not found ", articleId));
		});

	}

	@Override
	public void updateMasterToSlave(Long articleId, ClusterArticleDetailDto clusters) {

		log.info("request received for updating master to slave");
		Optional<Article> articleData = articleRepo.findById(articleId);

		articleData.ifPresentOrElse(articleRecord -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}

			/*clusterRecords.forEach((clusterData) -> {
				ArticleCluster articleCluster = new ArticleCluster();
				List<ArticleCluster> articleClusterData = artClustRepo.findByArticleCluster(articleId,
						clusterData.getId());
				articleClusterData.add(articleCluster);
			});
*/
			artClustRepo.updateMasterToSlave(articleId, clusters.getClusterIds());

		}, () -> {
			log.info("article with Id {} not found ", articleId);
			throw new ResourceNotFoundException(String.format("article with Id %s not found ", articleId));
		});
	}

	@Override
	public void updateSlaveToMaster(Long articleId, ClusterArticleDetailDto clusters) {
		log.info("request received for updating slave to master");
		Optional<Article> articleData = articleRepo.findById(articleId);

		articleData.ifPresentOrElse(articleRecord -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}

		/*	clusterRecords.forEach((clusterData) -> {
				ArticleCluster articleCluster = new ArticleCluster();
				List<ArticleCluster> articleClusterData = artClustRepo.findByArticleCluster(articleId,
						clusterData.getId());
				articleClusterData.add(articleCluster);
				artClustRepo.updateSlaveToMaster(articleId, clusterData.getId());

			});*/

			artClustRepo.updateSlaveToMaster(articleId, clusters.getClusterIds());

		}, () -> {
			log.info("article with Id {} not found ",articleId);
			throw new ResourceNotFoundException(String.format("article with Id %s not found ", articleId));
		});

	}

	@Override
	public void addSubArticlesToArticle(Long articleId, SubArticleDetailDto subArticleDetails, String user) {

		log.info("request received for adding sub articles to lead article");
		Optional<Article> articles = articleRepo.findById(articleId);
		articles.ifPresentOrElse(masterArticleRecord -> {
			List<Article> subArticleRecords = articleRepo.findAllById(subArticleDetails.getSubArticleIds());
			log.info("no of rows available {}", subArticleRecords.size());

			if (subArticleRecords.size() != subArticleDetails.getSubArticleIds().size()) {
				throw new IllegalArgumentException("Some of the subarticles not found");
			}

			List<PartListSubArticle> partSubArticle = new ArrayList<PartListSubArticle>();
			subArticleRecords.forEach((subArticle) -> {
				PartListSubArticle partSubRecord = new PartListSubArticle();
				partSubRecord.setArticle(masterArticleRecord);
				partSubRecord.setSubArticles(subArticle);
				partSubArticle.add(partSubRecord);
			});

			partListRepo.addSubArticlesToArticleMasterPartList(partSubArticle);

			//mark main article as part article
			log.info("Marking main article as part article");
			masterArticleRecord.setIsPart(1);

		}, () -> {
			log.info("master article record not found");
			throw new ResourceNotFoundException(String.format("master article with Id %s not found ", articleId));
		});

	}

	@Override
	public List<ArticlePartListDto> fetchAllArticlesByParts(int partStatus) {

		log.info("fetching articles from database");

		List<ArticlePartListDto> dtos = new ArrayList<ArticlePartListDto>();
		List<Article> articles = articleRepo.findByPartsStatus(partStatus);
		List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
		List<ArticleClusterI18NName> articleNamesList = artClusterI18NRepo.findByArticleIds(articleIds);
		 
		if (articles != null && !articles.isEmpty()) {
			Map<Long, List<ArticleClusterI18NName>> articleNameMap = articleNamesList.stream()
					.collect(Collectors.groupingBy(i18n -> i18n.getArticle().getArticle().getId()));
			articles.forEach(article -> {

				ArticlePartListDto partDto = new ArticlePartListDto();
				if (partStatus == 1) {

					double[] monthlySaleInEuro = { 0.0 };
					double[] oneTimeSalesInEuro = { 0.0 };

					double[] monthlyPurchaseInEuro = { 0.0 };
					double[] oneTimePurchaseInEuro = { 0.0 };

					Set<PartListSubArticle> partsData = article.getPartListSubArticle();
					log.info("total sublist" + partsData.size());

					if (partsData != null) {
						partsData.forEach(partListRecord -> {
							ArticleDto articleDto = new ArticleDto();
							if (article.getArticleClearingType() != null) {
								if (article.getArticleClearingType().equals(ArticleClearingTypeEnum.MONTHLY)) {
									if (partListRecord.getSubArticles().getArticleClearingType()
											.equals(ArticleClearingTypeEnum.MONTHLY)) {

										if (partListRecord.getSubArticles().getPriceSalesEuro() != null) {
											monthlySaleInEuro[0] += partListRecord.getSubArticles().getPriceSalesEuro();
											finalSaleMonthly = monthlySaleInEuro[0] + article.getPriceSalesEuro();
											partDto.setMonthlySalesPrice(finalSaleMonthly);

										}

										if (partListRecord.getSubArticles().getPricePurchaseEuro() != null) {
											monthlyPurchaseInEuro[0] += partListRecord.getSubArticles()
													.getPricePurchaseEuro();
											finalPurchaseMonthly = monthlyPurchaseInEuro[0]
													+ article.getPricePurchaseEuro();
											partDto.setMonthlyPurchasePrice(finalPurchaseMonthly);

										}

									} else if (partListRecord.getSubArticles().getArticleClearingType()
											.equals(ArticleClearingTypeEnum.ONETIME)) {
										if (partListRecord.getSubArticles().getPriceSalesEuro() != null) {
											oneTimeSalesInEuro[0] += partListRecord.getSubArticles()
													.getPriceSalesEuro();
											finalSaleOneTime = oneTimeSalesInEuro[0];
											partDto.setOneTimeSalesPrice(finalSaleOneTime);

										}

										if (partListRecord.getSubArticles().getPricePurchaseEuro() != null) {
											oneTimePurchaseInEuro[0] += partListRecord.getSubArticles()
													.getPricePurchaseEuro();
											finalPurchaseOneTime = oneTimePurchaseInEuro[0];
											partDto.setOneTimePurchasePrice(finalPurchaseOneTime);

										}
									}

								} else {

									if (article.getArticleClearingType().equals(ArticleClearingTypeEnum.ONETIME)) {
										if (partListRecord.getSubArticles().getArticleClearingType()
												.equals(ArticleClearingTypeEnum.MONTHLY)) {
											if (partListRecord.getSubArticles().getPriceSalesEuro() != null) {
												monthlySaleInEuro[0] += partListRecord.getSubArticles()
														.getPriceSalesEuro();
												finalSaleMonthly = monthlySaleInEuro[0];
												partDto.setMonthlySalesPrice(finalSaleMonthly);

											}

											if (partListRecord.getSubArticles().getPricePurchaseEuro() != null) {
												monthlyPurchaseInEuro[0] += partListRecord.getSubArticles()
														.getPricePurchaseEuro();
												finalPurchaseMonthly = monthlyPurchaseInEuro[0];
												partDto.setMonthlyPurchasePrice(finalPurchaseMonthly);

											}

										} else {
											if (partListRecord.getSubArticles().getArticleClearingType()
													.equals(ArticleClearingTypeEnum.ONETIME)) {
												if (partListRecord.getSubArticles().getPriceSalesEuro() != null) {
													oneTimeSalesInEuro[0] += partListRecord.getSubArticles()
															.getPriceSalesEuro();
													finalSaleOneTime = oneTimeSalesInEuro[0]
															+ article.getPriceSalesEuro();
													partDto.setOneTimeSalesPrice(finalSaleOneTime);

												}

												if (partListRecord.getSubArticles().getPricePurchaseEuro() != null) {
													oneTimePurchaseInEuro[0] += partListRecord.getSubArticles()
															.getPricePurchaseEuro();
													finalPurchaseOneTime = oneTimePurchaseInEuro[0]
															+ article.getPricePurchaseEuro();

													// partDto.setOneTimeSalesPrice(finalPurchaseOneTime);

													partDto.setOneTimePurchasePrice(finalPurchaseOneTime);

												}
											}
										}

									}
								}

								if ((article.getArticleClearingType().equals(ArticleClearingTypeEnum.MONTHLY)
										&& partDto.getMonthlySalesPrice() == 0)) {
									partDto.setMonthlySalesPrice(article.getPriceSalesEuro());

								} else {
									if ((article.getArticleClearingType().equals(ArticleClearingTypeEnum.ONETIME)
											&& partDto.getOneTimeSalesPrice() == 0)) {
										partDto.setOneTimeSalesPrice(article.getPriceSalesEuro());
									}
								}

								if ((article.getArticleClearingType().equals(ArticleClearingTypeEnum.MONTHLY)
										&& partDto.getMonthlyPurchasePrice() == 0)) {
									partDto.setMonthlyPurchasePrice(article.getPricePurchaseEuro());

								} else {
									if ((article.getArticleClearingType().equals(ArticleClearingTypeEnum.ONETIME)
											&& partDto.getOneTimePurchasePrice() == 0)) {
										partDto.setOneTimePurchasePrice(article.getPricePurchaseEuro());
									}
								}

								articleDto.setId(article.getId());
								articleDto.setName(article.getName());
								List<ArticleClusterI18NName> articleNameList = articleNameMap.get(article.getId());

								if (articleNameList != null) {
									for (ArticleClusterI18NName nameEntry : articleNameList) {
										if ("en".equalsIgnoreCase(nameEntry.getLanguageId())) {
											articleDto.setNameEnglish(nameEntry.getTranslation());
										} else if ("de".equalsIgnoreCase(nameEntry.getLanguageId())) {
											articleDto.setNameGerman(nameEntry.getTranslation());
										}
									}
								}
								articleDto.setArticleClearingType(article.getArticleClearingType());
								articleDto.setRemark(article.getDescription());
								articleDto.setHidden(article.getHidden());

								if (article.getPricePurchaseDollar() != null) {
									articleDto.setPricePurchase_dollar(article.getPricePurchaseDollar());
								}
								if (article.getPricePurchaseEuro() != null) {
									articleDto.setPricePurchase_euro(article.getPricePurchaseEuro());
								}
								if (article.getPriceSalesDollar() != null) {
									articleDto.setPriceSales_dollar(article.getPriceSalesDollar());
								}
								if (article.getPriceSalesEuro() != null) {
									articleDto.setPriceSales_euro(article.getPriceSalesEuro());
								}
								articleDto.setValueReadOnly(article.getValueReadOnly());
								articleDto.setSingleArticle(article.getSingleArticle());
								articleDto.setMasterArticle(article.getMasterArticle());
								articleDto.setHardwareFromAvaya(article.getHardwareFromAvaya());
								articleDto.setSubjectToAuthorization(article.getSubjectToAuthorization());
								articleDto.setBilling(article.getBilling());
								articleDto.setIncidentArticle(article.getIncidentArticle());
								articleDto.setServusInterface(article.getServusInterface());
								articleDto.setNonAvailable(article.getNonAvailable());
								articleDto.setShippingAddress(article.getShippingAddress());
								articleDto.setAssemblingAddress(article.getAssemblingAddress());
								articleDto.setPoolHandling(article.getPoolHandling());
								articleDto.setClearingAtNewConnection(article.getClearingAtNewConnection());
								articleDto.setClearingAtChangeMove(article.getClearingAtChangeMove());
								articleDto.setClearingAtDelete(article.getClearingAtDelete());
								articleDto.setQuantifier(article.getQuantifier());
								if (article.getArticleCategory() != null) {
									articleDto.setArticleCategoryId(article.getArticleCategory().getId());
								}
								articleDto.setLifeTime(article.getLifeTime());
								
								if (article.getServiceCode() != null) {
									articleDto.setServiceCode(article.getServiceCode().getServiceCode());
								}
								articleDto.setSapAvaya(article.getSapAvaya());
								articleDto.setSapBosh(article.getSapBosh());
								articleDto.setSlaDays(article.getSlaDays());
								articleDto.setSlaHrs(article.getSlaHours());
								articleDto.setSlaMin(article.getSlaMinutes());
								articleDto.setValueTransfer(article.getValueTransfer());
								articleDto.setProperty(article.getProperty());

								articleDto.setIsPart(article.getIsPart());

								partDto.setArticles(articleDto);
								partDto.setSubArticleCount(partsData.size());
							} else {
								log.info("article clearing type is null, so can't proceed..");
							}

						});
						dtos.add(partDto);
					}
				} else {
					if (partStatus == 0) {
						ArticleDto articleDto = new ArticleDto();
						articleDto.setId(article.getId());
						articleDto.setName(article.getName());
						List<ArticleClusterI18NName> articleNameList = articleNameMap.get(article.getId());

						if (articleNameList != null) {
						    for (ArticleClusterI18NName nameEntry : articleNameList) {
						        if ("en".equalsIgnoreCase(nameEntry.getLanguageId())) {
						            articleDto.setNameEnglish(nameEntry.getTranslation());
						        } else if ("de".equalsIgnoreCase(nameEntry.getLanguageId())) {
						            articleDto.setNameGerman(nameEntry.getTranslation());
						        }
						    }
						}
						articleDto.setArticleClearingType(article.getArticleClearingType());
						articleDto.setRemark(article.getDescription());
						articleDto.setHidden(article.getHidden());

						if (article.getPricePurchaseDollar() != null) {
							articleDto.setPricePurchase_dollar(article.getPricePurchaseDollar());
						}
						if (article.getPricePurchaseEuro() != null) {
							articleDto.setPricePurchase_euro(article.getPricePurchaseEuro());
						}
						if (article.getPriceSalesDollar() != null) {
							articleDto.setPriceSales_dollar(article.getPriceSalesDollar());
						}
						if (article.getPriceSalesEuro() != null) {
							articleDto.setPriceSales_euro(article.getPriceSalesEuro());
						}

						articleDto.setValueReadOnly(article.getValueReadOnly());
						articleDto.setSingleArticle(article.getSingleArticle());
						articleDto.setMasterArticle(article.getMasterArticle());
						articleDto.setHardwareFromAvaya(article.getHardwareFromAvaya());
						articleDto.setSubjectToAuthorization(article.getSubjectToAuthorization());
						articleDto.setBilling(article.getBilling());
						articleDto.setIncidentArticle(article.getIncidentArticle());
						articleDto.setServusInterface(article.getServusInterface());
						articleDto.setNonAvailable(article.getNonAvailable());
						articleDto.setShippingAddress(article.getShippingAddress());
						articleDto.setAssemblingAddress(article.getAssemblingAddress());
						articleDto.setPoolHandling(article.getPoolHandling());
						articleDto.setClearingAtNewConnection(article.getClearingAtNewConnection());
						articleDto.setClearingAtChangeMove(article.getClearingAtChangeMove());
						articleDto.setClearingAtDelete(article.getClearingAtDelete());
						articleDto.setQuantifier(article.getQuantifier());
						if (article.getArticleCategory() != null) {
							articleDto.setArticleCategoryId(article.getArticleCategory().getId());
						}
						articleDto.setLifeTime(article.getLifeTime());
						
						if (article.getServiceCode() != null) {
							articleDto.setServiceCode(article.getServiceCode().getServiceCode());
						}
						articleDto.setSapAvaya(article.getSapAvaya());
						articleDto.setSapBosh(article.getSapBosh());
						articleDto.setSlaDays(article.getSlaDays());
						articleDto.setSlaHrs(article.getSlaHours());
						articleDto.setSlaMin(article.getSlaMinutes());
						articleDto.setValueTransfer(article.getValueTransfer());
						articleDto.setProperty(article.getProperty());
						articleDto.setIsPart(article.getIsPart());

						if (articleDto.getArticleClearingType().equals(ArticleClearingTypeEnum.MONTHLY)) {

							if (articleDto.getPriceSales_euro() != null) {
								partDto.setMonthlySalesPrice(articleDto.getPriceSales_euro());
							}
						} else {

							if (articleDto.getArticleClearingType().equals(ArticleClearingTypeEnum.ONETIME)) {

								if (articleDto.getPriceSales_euro() != null) {
									partDto.setOneTimeSalesPrice(articleDto.getPriceSales_euro());
								}
							}
						}

						if (articleDto.getArticleClearingType().equals(ArticleClearingTypeEnum.MONTHLY)) {

							if (articleDto.getPricePurchase_euro() != null) {
								partDto.setMonthlyPurchasePrice(articleDto.getPricePurchase_euro());
							}
						} else {

							if (articleDto.getArticleClearingType().equals(ArticleClearingTypeEnum.ONETIME)) {

								if (articleDto.getPricePurchase_euro() != null) {
									partDto.setOneTimePurchasePrice(articleDto.getPricePurchase_euro());
								}
							}
						}

						partDto.setArticles(articleDto);
						partDto.setSubArticleCount(0);
						dtos.add(partDto);
					}
				}
			});

		} else {
			log.info("No article records found...");
		}

		return dtos;
	}

	@Override
	public List<ArticleDto> fetchSubArticles(Long leadId) {

		log.info("fetching articles from database");
		List<ArticleDto> dtos = new ArrayList<ArticleDto>();
		Optional<Article> articles = articleRepo.findById(leadId);
		if (articles != null && !articles.isEmpty()) {
			articles.ifPresentOrElse(article -> {
				Set<PartListSubArticle> subArticleData = article.getPartListSubArticle();
				subArticleData.forEach(subListRecord -> {
					ArticleDto articleDto = new ArticleDto();
					subListRecord.getSubArticleId();
					articleDto.setId(subListRecord.getSubArticleId());
					articleDto.setName(subListRecord.getSubArticles().getName());
					articleDto.setRemark(subListRecord.getSubArticles().getDescription());
					articleDto.setArticleClearingType(subListRecord.getSubArticles().getArticleClearingType());
					articleDto.setProperty(subListRecord.getSubArticles().getProperty());
					articleDto.setValueTransfer(subListRecord.getSubArticles().getValueTransfer());
					articleDto.setIsPart(subListRecord.getSubArticles().getIsPart());
					articleDto.setSapAvaya(subListRecord.getSubArticles().getSapAvaya());
					articleDto.setSapBosh(subListRecord.getSubArticles().getSapBosh());
					articleDto.setAvailableForRoles(getRolesForArticle(article.getAvailableForRoles()));
					if (subListRecord.getSubArticles().getArticleCategory() != null) {
						articleDto.setArticleCategoryId(subListRecord.getSubArticles().getArticleCategory().getId());
					}

					if (subListRecord.getSubArticles().getServiceCode() != null) {
						articleDto.setServiceCode(subListRecord.getSubArticles().getServiceCode().getServiceCode());
					}

					articleDto.setClearingAtNewConnection(subListRecord.getSubArticles().getClearingAtNewConnection());
					articleDto.setPriceSales_euro(subListRecord.getSubArticles().getPriceSalesEuro());
					articleDto.setPriceSales_dollar(subListRecord.getSubArticles().getPriceSalesDollar());
					articleDto.setPricePurchase_dollar(subListRecord.getSubArticles().getPricePurchaseDollar());
					articleDto.setPricePurchase_euro(subListRecord.getSubArticles().getPricePurchaseEuro());
					articleDto.setSingleArticle(subListRecord.getSubArticles().getSingleArticle());
					articleDto.setMasterArticle(subListRecord.getSubArticles().getMasterArticle());
					articleDto.setValueDefault(subListRecord.getSubArticles().getValueDefault());
					articleDto.setValueReadOnly(subListRecord.getSubArticles().getValueReadOnly());
					articleDto.setLogCreatedBy(subListRecord.getSubArticles().getLogCreatedBy());
					articleDto.setLogCreatedOn(subListRecord.getSubArticles().getLogUpdatedOn());
					articleDto.setLogUpdatedBy(subListRecord.getSubArticles().getLogUpdatedBy());
					articleDto.setLogUpdatedOn(subListRecord.getSubArticles().getLogUpdatedOn());

					dtos.add(articleDto);
				});

			}, () -> {
				log.info("No article records found...");

			});
		}
		return dtos;
	}

	@AuditLog(action = "delete",entity = "PartListSubArticle",functionality = "delete Sub Article to Article")
	@Override
	public void deleteAssignSubArticles(Long leadId, SubArticleDetailDto subArticles, String user) {

		Optional<Article> articleRecords = articleRepo.findById(leadId);

		articleRecords.ifPresentOrElse(articleRecord -> {
			Set<PartListSubArticle> partListRecords = articleRecord.getPartListSubArticle();
			if (partListRecords != null && partListRecords.size() != 0) {
				List<PartListSubArticle> PartSubList = new ArrayList<PartListSubArticle>();
				subArticles.getSubArticleIds().forEach(subArticleId -> {
					PartListSubArticle partListArticle = new PartListSubArticle();
					partListArticle.setSubArticleId(subArticleId);
					partListArticle.setArticleId(leadId);

					PartListSubArticle partListRecord = partListRecords.stream()
							.filter(value -> value.equals(partListArticle)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(String.format(
									"sub article with Id %s not found for lead article id %s", subArticleId, leadId)));
					PartSubList.add(partListRecord);
				});
				log.info("Removing subarticles from lead article {} ", PartSubList);
				partListRepo.deleteAll(PartSubList);
				List<PartListSubArticle> listData = partListRepo.findByArticle(leadId);
				if (listData.size() == 0) {
					log.info("all associated subarticles deleted");
					articleRepo.updateIsPartStatus((short) 0, leadId);
				} else {
					log.info("some of the subarticles are remaining");
				}

			} else {
				throw new IllegalArgumentException(
						String.format("No subarticle is associated with lead article with Id %s", leadId));
			}

		}, () -> {
			log.info("master article with Id {} not found", leadId);
			throw new ResourceNotFoundException(String.format("lead article with Id %s not found", leadId));
		});
	}

	@AuditLog(action = "delete",entity = "PartListSubArticle",functionality = "delete Master PartList Lead Article")
	@Override
	public void deleteLeadArticles(Long leadId) {

		Optional<Article> articleRecords = articleRepo.findById(leadId);
		articleRecords.ifPresentOrElse(articleRecord -> {

			log.info("leadId status is updated as ispart false for {}", leadId);
			articleRepo.updateIsPartStatus(0, leadId);
			log.info("deleting associated subarticles for leadarticle {}", leadId);
			partListRepo.deleteSubArticlesByLeadId(leadId);
			log.info("sub articles deletion completed..");

		}, () -> {
			log.error("lead article with Id {} not found", leadId);
			throw new ResourceNotFoundException(String.format("lead article with Id %s not found", leadId));
		});

	}

	@Override
	public List<ClusterDto> fetchClusterDetailsByParts(Long articleId, int partStatus) {
		log.info("fetching cluster details for a lead article {}", articleId);
		List<ClusterDto> dtos = new ArrayList<ClusterDto>();
		Optional<Article> record = articleRepo.findById(articleId);
		record.ifPresentOrElse(article -> {
			Set<ArticleCluster> articleClusterRecord = article.getArticleCluster();

			articleClusterRecord.stream().forEach(articleCluster -> {

				if (partStatus == 0) {
					if (articleCluster.getIsPart() == 0 && !articleCluster.getMasterPartStatus()) {
						ClusterItem clusterData = articleCluster.getClusterItem();
						if (clusterData.getActive() != 0) {
							ClusterDto dto = mapper.map(clusterData, ClusterDto.class);
							dto.setId(articleCluster.getClusterItem().getId());
							dto.setName(articleCluster.getClusterItem().getName());
							dtos.add(dto);
						}
					}
				} else {

					if (partStatus == 1) {
						if (articleCluster.getIsPart() == 1 && articleCluster.getMasterPartStatus()) {
							ClusterItem clusterData = articleCluster.getClusterItem();
							if (clusterData.getActive() != 0) {
								ClusterDto dto = mapper.map(clusterData, ClusterDto.class);
								dto.setId(articleCluster.getClusterItem().getId());
								dto.setName(articleCluster.getClusterItem().getName());
								dtos.add(dto);
							}

						}
					}

				}
			});

		}, () -> {
			log.info("Article record not found");
			throw new ResourceNotFoundException(
					String.format("master Article with Id %s not found for masterPartlist->extras ", articleId));
		});
		return dtos;
	}

	@Override
	public void assignMasterStatus(Long articleId, ClusterArticleDetailDto clusters) {

		log.info("request received for updating master part status to 1");
		Optional<Article> articleData = articleRepo.findById(articleId);

		articleData.ifPresentOrElse(articleRecord -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of cluster rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}
			clusterRecords.forEach((clusterData) -> {
				ArticleCluster articleCluster = new ArticleCluster();
				List<ArticleCluster> articleClusterData = artClustRepo.findByArticleCluster(articleId,
						clusterData.getId());
				articleClusterData.add(articleCluster);
				if (!articleClusterData.get(0).getMasterPartStatus()) {
					artClustRepo.updateMasterPartStatus(1, articleId, clusterData.getId());
				} else {
					log.info("lead article{} is already have a master part status", articleId);
				}

			});

		}, () -> {
			log.info("cluster id not found");
			throw new ResourceNotFoundException(String.format("article with cluster Id %s not found ", articleId));
		});
	}

	@Override
	public void removePartListMasterStatus(Long articleId, ClusterArticleDetailDto clusters) {

		log.info("request received to remove master part status for articleID {}",articleId);

		Optional<Article> articleData = articleRepo.findById(articleId);

		articleData.ifPresentOrElse(articleRecord -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of cluster rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}

//			clusterRecords.forEach((clusterData) -> {
//				ArticleCluster articleCluster = new ArticleCluster();
//				List<ArticleCluster> articleClusterData = artClustRepo.findByArticleCluster(articleId,
//						clusterData.getId());
//				articleClusterData.add(articleCluster);
//				artClustRepo.removeArticleMasterPartStatus(articleId, clusterData.getId());
//
//			});
			
			artClustRepo.removeArticleMasterPartStatus(articleId, clusters.getClusterIds(), clusters.getUser(), new Timestamp(System.currentTimeMillis()));

		}, () -> {
			log.info("article with Id {} not found",articleId);
			throw new ResourceNotFoundException(String.format("article with Id %s not found ", articleId));
		});
	}

	@Override
	public List<ClusterDto> fetchClusterDetailsByPartMasterStatus(Long articleId, int masterStatus) {
		log.info("fetching cluster details for a lead article as per master Status true/false{}", articleId);
		List<ClusterDto> dtos = new ArrayList<ClusterDto>();
		Optional<Article> record = articleRepo.findById(articleId);
		record.ifPresentOrElse(article -> {
			Set<ArticleCluster> articleClusterRecord = article.getArticleCluster();
			articleClusterRecord.stream().forEach(articleCluster -> {

				if (masterStatus == 0) {
					if (articleCluster.getIsPart() == 1 && !articleCluster.getMasterPartStatus()) {
						ClusterItem clusterData = articleCluster.getClusterItem();
						if (clusterData.getActive() != 0) {
							ClusterDto dto = mapper.map(clusterData, ClusterDto.class);
							dto.setId(articleCluster.getClusterItem().getId());
							dto.setName(articleCluster.getClusterItem().getName());
							dtos.add(dto);
						}
					}
				} else {

					if (masterStatus == 1) {
						if (articleCluster.getIsPart() == 1 && articleCluster.getMasterPartStatus()) {
							ClusterItem clusterData = articleCluster.getClusterItem();
							if (clusterData.getActive() != 0) {
								ClusterDto dto = mapper.map(clusterData, ClusterDto.class);
								dto.setId(articleCluster.getClusterItem().getId());
								dto.setName(articleCluster.getClusterItem().getName());
								dtos.add(dto);
							}

						}
					}

				}
			});

		}, () -> {
			log.info("Article record not found");
			throw new ResourceNotFoundException(
					String.format("master Article with Id %s not found for masterPartlist->extras ", articleId));
		});
		return dtos;
	}

	// for adding clusters to a lead article from master merge

	@Override
	public void addClustersToLeadArticlePartlist(Long articleId, ClusterArticleDetailDto clusters) {

		log.info("request received for adding clusters to lead articles");
		Optional<Article> articles = articleRepo.findById(articleId);
		List<ArticleDto> listOfSubArticle = fetchSubArticles(articleId);
		log.info("subarticle size is {}", listOfSubArticle.size());
		SubArticleDetailDto subArtDetailDto = new SubArticleDetailDto();
		List<Long> existingList = new ArrayList<Long>();
		for (ArticleDto subArticleList : listOfSubArticle) {
			Article subArticles = new Article();
			ArticleCategory articleCategory = new ArticleCategory();
			ServiceCode serviceCode = new ServiceCode();
			if (subArticleList.getArticleCategoryId() != null) {
				articleCategory.setId(subArticleList.getArticleCategoryId());
			} else {
				articleCategory.setId(1);
			}
			
			if (subArticleList.getServiceCode() != null) {
				serviceCode.setServiceCode(subArticleList.getServiceCode());
			} else {
				serviceCode.setServiceCode("1");
			}
			subArticles.setId(subArticleList.getId()); // setting the article id in article_cluster table as id_article
			subArticles.setArticleCategory(articleCategory);
			subArticles.setServiceCode(serviceCode);
			AtomicReference<Long> leadArticleDataId = new AtomicReference<>(null); // Use AtomicReference for mutability
			articles.ifPresentOrElse(articleRecord -> {
				ArrayList<Long> actualSavedList = new ArrayList<Long>();
				List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
				log.info("no of rows available {}", clusterRecords.size());
				if (clusterRecords.size() != clusters.getClusterIds().size()) {
					throw new IllegalArgumentException("Some of the clusters not found");
				}
				List<ArticleCluster> articleClusterRecord = new ArrayList<ArticleCluster>();
				clusterRecords.forEach((clusterData) -> {
					List<ArticleCluster> articleClusterId = artClustRepo.findByArticleCluster(articleId,
							clusterData.getId());
					leadArticleDataId.set(articleClusterId.get(0).getId());
					ArticleCluster articleCluster = new ArticleCluster();
					List<ArticleCluster> articleClusterData = artClustRepo.findByArticleCluster(subArticleList.getId(),
							clusterData.getId());
					if (articleClusterData != null && !articleClusterData.isEmpty() && articleClusterData.size() == 1) {
						log.error(
								"article is already available in table, duplicate record not allowed for articleId :{}, name:{} and clusterId :{}, name :{}",
								articleClusterData.get(0).getId(), articleClusterData.get(0).getName(),
								clusterData.getId(), clusterData.getName());
						existingList.add(articleClusterData.get(0).getId());
						subArtDetailDto.setSubArticleIds(existingList);
						articleClusterService.addSubArticlesToArticle(leadArticleDataId.get(), subArtDetailDto,clusters.getUser());
						assignMasterStatus(articleId, clusters);
					} else {
						articleCluster.setArticle(subArticles);
						articleCluster.setClusterItem(clusterData);
						articleCluster.setMasterArticle(subArticleList.getMasterArticle());
						articleCluster.setActive(1);
						articleCluster.setName(subArticleList.getName());
						articleCluster.setDescription(subArticleList.getRemark());
						articleCluster.setLifetime(subArticleList.getLifeTime());
						articleCluster.setArticleCategory(articleCategory);
						articleCluster.setArticleClearingType(subArticleList.getArticleClearingType());
						articleCluster.setServiceCodeCluster(serviceCode);
						articleCluster.setSingleArticle(subArticleList.getSingleArticle());
						articleCluster.setClearingAtNewConnection(subArticleList.getClearingAtNewConnection());
						articleCluster.setClearingAtDelete(subArticleList.getClearingAtDelete());
						articleCluster.setClearingAtChangeMove(subArticleList.getClearingAtChangeMove());
						articleCluster.setSlaDays(subArticleList.getSlaDays());
						articleCluster.setSlaHours(subArticleList.getSlaHrs());
						articleCluster.setSlaMinutes(subArticleList.getSlaMin());
						articleCluster.setPricePurchaseDollar(subArticleList.getPricePurchase_dollar());
						articleCluster.setPricePurchaseEuro(subArticleList.getPricePurchase_euro());
						articleCluster.setPriceSalesDollar(subArticleList.getPriceSales_dollar());
						articleCluster.setPriceSalesEuro(subArticleList.getPriceSales_euro());
						articleCluster.setPriority(subArticleList.getPriority());
						articleCluster.setQuantifier(subArticleList.getQuantifier());
						articleCluster.setSapAvaya(subArticleList.getSapAvaya());
						articleCluster.setSapBosh(subArticleList.getSapBosh());
						articleCluster.setSubjectToAuthorization(subArticleList.getSubjectToAuthorization());
						articleCluster.setValueDefault(subArticleList.getValueDefault());
						articleCluster.setValueReadOnly(subArticleList.getValueReadOnly());
						articleCluster.setHardwareFromAvaya(subArticleList.getHardwareFromAvaya());
						articleCluster.setSubjectToAuthorization(subArticleList.getSubjectToAuthorization());
						articleCluster.setBilling(subArticleList.getBilling());
						articleCluster.setIncidentArticle(subArticleList.getIncidentArticle());
						articleCluster.setServusInterface(subArticleList.getServusInterface());
						articleCluster.setHidden(subArticleList.getHidden());
						articleCluster.setNonAvailable(subArticleList.getNonAvailable());
						articleCluster.setShippingAddress(subArticleList.getShippingAddress());
						articleCluster.setAssemblingAddress(subArticleList.getAssemblingAddress());
						articleCluster.setPoolHandling(subArticleList.getPoolHandling());
						articleCluster.setLogCreatedBy(subArticleList.getLogCreatedBy());
						articleCluster.setLogCreatedOn(subArticleList.getLogCreatedOn());
						articleCluster.setLogUpdatedBy(subArticleList.getLogUpdatedBy());
						articleCluster.setLogUpdatedOn(subArticleList.getLogUpdatedOn());
						articleCluster.setIsPart(1);
						articleCluster.setProperty(subArticleList.getProperty());
						articleCluster.setValueTransfer(subArticleList.isValueTransfer());
						articleClusterRecord.add(articleCluster);
					}
				});
				List<ArticleCluster> savedArticleClusters = artClustRepo.saveAll(articleClusterRecord);
				// SubArticleDetailDto subArtDetailDto = new SubArticleDetailDto();
				List<Long> savedIds = savedArticleClusters.stream().map(ArticleCluster::getId)
						.collect(Collectors.toList());
				actualSavedList.addAll(savedIds);
				subArtDetailDto.setSubArticleIds(actualSavedList);
				log.info("ArticleCluster records saved with IDs: {}", savedIds);
				// for adding subarticles to lead article cluster ids in partlist cluster subarticle 
				articleClusterService.addSubArticlesToArticle(leadArticleDataId.get(), subArtDetailDto,clusters.getUser());
				assignMasterStatus(articleId, clusters);
			}, () -> {
				log.info("cluster id not found");
				throw new ResourceNotFoundException(String.format("article with cluster Id %s not found ", articleId));
			});

		}
	}

	@Override
	public void addPartListArticlesToClusters(Long articleId, ClusterArticleDetailDto clusterIds) {

		//fetch lead article records
		Optional< Article > article = articleRepo.findById(articleId);

		//fetch article cluster records of lead article against all cluster and build map clusterId vs record
		//This is required to insert data in partList_cluster_subarticle
		List< ArticleCluster > leadArticleClusters = artClustRepo.fetchArticleClusterByClusterIds(articleId, clusterIds.getClusterIds());

		if(leadArticleClusters.size()!=clusterIds.getClusterIds().size()){
			log.info("main article is not associated with one of cluster.");
			throw new IllegalArgumentException("main article is not associated with one of cluster.");
		}

		Map< Long, ArticleCluster > clusterIdVsArticleClusterofLead = new HashMap<>();
		leadArticleClusters.forEach(articleCluster -> {
			clusterIdVsArticleClusterofLead.put(articleCluster.getClusterItem().getId(), articleCluster);
		});

		log.info("leadArticleClusters size {}", leadArticleClusters.size());

		article.ifPresentOrElse(articleRcd -> {

			List< ArticleDto > subArticles = fetchSubArticles(articleId);
			log.info("Total {} subArticles found", subArticles.size());
			if ( !subArticles.isEmpty() ) {

				/** For each subarticle
					1.Check article cluster record is available for given subarticleId and clusterId
				 	2.if not, Insert record in article_cluster and build record for partList_cluster_subarticle mapping.
				 	3.Add mapping in partList_cluster_subarticle
				 **/
				List<PartlistClusterSubarticle> partlistClusterSubarticleList = new ArrayList<PartlistClusterSubarticle>();
				subArticles.forEach(subArticle -> {

					List< ArticleCluster > articleClusterList = new ArrayList< ArticleCluster >();
					//List<PartlistClusterSubarticle> partlistClusterSubarticleList = new ArrayList<PartlistClusterSubarticle>();

					log.info("Fetching Article Cluster Info subArticle {}", subArticle);
					List< ArticleCluster > articleClusters = artClustRepo.fetchArticleClusterByClusterIds(subArticle.getId(), clusterIds.getClusterIds());
					log.info("{} articleCluster Record found for subArticle", articleClusters.size());

					Map< Long, ArticleCluster > clusterIdVsArticleCluster = new HashMap<>();
					articleClusters.forEach(ArticleCluster -> {
						clusterIdVsArticleCluster.put(ArticleCluster.getClusterItem().getId(), ArticleCluster);
					});

					clusterIds.getClusterIds().forEach(clusterId -> {
						//ArticleCluster record is already present
						if ( clusterIdVsArticleCluster.containsKey(clusterId) ) {

							log.info("Article Cluster record already exists for articleId {}-ClusterId {}", subArticle.getId(), clusterId);

							//create PartlistClusterSubarticle entity record to save mapping in partList_cluster_subarticle
							PartlistClusterSubarticle partSubRecord = new PartlistClusterSubarticle();
							partSubRecord.setArticleCluster(clusterIdVsArticleClusterofLead.get(clusterId));
							partSubRecord.setSubArticles(clusterIdVsArticleCluster.get(clusterId));

							partlistClusterSubarticleList.add(partSubRecord);

						} else {

							ArticleCluster articleCluster = new ArticleCluster(); // new article_cluster entity
							Article articleClusterArticle = new Article();//article cluster - article entity
							ClusterItem clusterItem = new ClusterItem();//article cluster -  cluster entity
							clusterItem.setId(clusterId);

							ArticleCategory articleCategory = new ArticleCategory();
							ServiceCode serviceCode = new ServiceCode();
							if ( subArticle.getArticleCategoryId() != null ) {
								articleCategory.setId(subArticle.getArticleCategoryId());
							} else {
								articleCategory.setId(1);
							}
							
							if (subArticle.getServiceCode() != null) {
								serviceCode.setServiceCode(subArticle.getServiceCode());
							} else {
								serviceCode.setServiceCode("1");
							}
							articleClusterArticle.setId(subArticle.getId());
							articleClusterArticle.setArticleCategory(articleCategory);
							articleClusterArticle.setServiceCode(serviceCode);
							articleCluster.setArticle(articleClusterArticle);
							articleCluster.setClusterItem(clusterItem);
							articleCluster.setMasterArticle(subArticle.getMasterArticle());
							articleCluster.setActive(1);
							articleCluster.setName(subArticle.getName());
							articleCluster.setDescription(subArticle.getRemark());
							articleCluster.setLifetime(subArticle.getLifeTime());
							articleCluster.setArticleCategory(articleCategory);
							articleCluster.setArticleClearingType(subArticle.getArticleClearingType());
							articleCluster.setServiceCodeCluster(serviceCode);
							articleCluster.setSingleArticle(subArticle.getSingleArticle());
							articleCluster.setClearingAtNewConnection(subArticle.getClearingAtNewConnection());
							articleCluster.setClearingAtDelete(subArticle.getClearingAtDelete());
							articleCluster.setClearingAtChangeMove(subArticle.getClearingAtChangeMove());
							articleCluster.setSlaDays(subArticle.getSlaDays());
							articleCluster.setSlaHours(subArticle.getSlaHrs());
							articleCluster.setSlaMinutes(subArticle.getSlaMin());
							articleCluster.setPricePurchaseDollar(subArticle.getPricePurchase_dollar());
							articleCluster.setPricePurchaseEuro(subArticle.getPricePurchase_euro());
							articleCluster.setPriceSalesDollar(subArticle.getPriceSales_dollar());
							articleCluster.setPriceSalesEuro(subArticle.getPriceSales_euro());
							articleCluster.setPriority(subArticle.getPriority());
							articleCluster.setQuantifier(subArticle.getQuantifier());
							articleCluster.setSapAvaya(subArticle.getSapAvaya());
							articleCluster.setSapBosh(subArticle.getSapBosh());
							articleCluster.setSubjectToAuthorization(subArticle.getSubjectToAuthorization());
							articleCluster.setValueDefault(subArticle.getValueDefault());
							articleCluster.setValueReadOnly(subArticle.getValueReadOnly());
							articleCluster.setHardwareFromAvaya(subArticle.getHardwareFromAvaya());
							articleCluster.setSubjectToAuthorization(subArticle.getSubjectToAuthorization());
							articleCluster.setBilling(subArticle.getBilling());
							articleCluster.setIncidentArticle(subArticle.getIncidentArticle());
							articleCluster.setServusInterface(subArticle.getServusInterface());
							articleCluster.setHidden(subArticle.getHidden());
							articleCluster.setNonAvailable(subArticle.getNonAvailable());
							articleCluster.setShippingAddress(subArticle.getShippingAddress());
							articleCluster.setAssemblingAddress(subArticle.getAssemblingAddress());
							articleCluster.setPoolHandling(subArticle.getPoolHandling());
							articleCluster.setLogCreatedBy(subArticle.getLogCreatedBy());
							articleCluster.setLogCreatedOn(subArticle.getLogCreatedOn());
							articleCluster.setLogUpdatedBy(subArticle.getLogUpdatedBy());
							articleCluster.setLogUpdatedOn(subArticle.getLogUpdatedOn());
							articleCluster.setIsPart(0);
							articleCluster.setProperty(subArticle.getProperty());
							articleCluster.setValueTransfer(subArticle.isValueTransfer());

							articleClusterList.add(articleCluster);
						}
					});

					log.info("Persisting total {} new ArticleCluster records for ArticleId {}", articleClusterList.size(),subArticle.getId());
					List<ArticleCluster> newArticleClusterRecords = artClustRepo.saveAll(articleClusterList);
					List<Long> ids = newArticleClusterRecords.stream().map(ArticleCluster::getId)
							.collect(Collectors.toList());

					//Build mapping of newly created article cluster records to persist in partList_cluster_subarticle
					newArticleClusterRecords.forEach(articleCluster->{
						PartlistClusterSubarticle partSubRecord = new PartlistClusterSubarticle();
						//article cluster record of main article for given cluster.
						partSubRecord.setArticleCluster(clusterIdVsArticleClusterofLead.get(articleCluster.getClusterItem().getId()));
						partSubRecord.setSubArticles(articleCluster);
						partlistClusterSubarticleList.add(partSubRecord);

					});
					
				});
				//persist mapping in partList_cluster_subarticle
				if(Objects.nonNull(partlistClusterSubarticleList) && !partlistClusterSubarticleList.isEmpty()) {
					partListArticleClusterRepo.addAllPartlistArticleToCluster(partlistClusterSubarticleList);
				}
											
				//update is_part and master part flag as 1 for lead article against all cluster.
				log.info("Updating master_flag_partList as 1 for main article for given clusters");
				artClustRepo.updateLeadArticlePartProperties(articleId,clusterIds.getClusterIds(),articleRcd.getProperty(),articleRcd.getValueTransfer());

			} else {
				log.info("No subArticles found for given article");
			}

		}, () -> {
			log.info("article with article Id {} not found", articleId);
			throw new ResourceNotFoundException(String.format("article with article Id {} not found ", articleId));
		});
	}



	@Override
	public void addPartListMasterStatus(Long articleId, ClusterArticleDetailDto clusters) {

		log.info("request received to add master part status for articleID {}",articleId);

		Optional<Article> articleData = articleRepo.findById(articleId);

		articleData.ifPresentOrElse(articleRecord -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of cluster rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}

			artClustRepo.addArticleMasterPartStatus(articleId, clusters.getClusterIds(), clusters.getUser(), new Timestamp(System.currentTimeMillis()));

		}, () -> {
			log.info("article with Id {} not found",articleId);
			throw new ResourceNotFoundException(String.format("article with Id %s not found ", articleId));
		});
	}


	@Override
	public void syncMasterArticlesToClusters(Long articleId, ArticleToClusterSyncDto payload, String user) {

		List< Integer > attributeIds = payload.getAttributeIds();
		Optional< Article > optionalArticle = articleRepo.findById(articleId);

		optionalArticle.ifPresentOrElse(article -> {

			if ( article.getActive() == 1 ) {
				List< ArticleCluster > articleClusters = artClustRepo.fetchArticleClusterByClusterIds(articleId, payload.getClusterIds());
				if ( !articleClusters.isEmpty() ) {
					List< ArticleCluster > updatedArticleClusters = articleClusters.stream().map(articleClustRcd -> {
						
						log.info("Updating article cluster records {}", articleClustRcd.getId());						
						attributeIds.stream().forEach(attr -> {		
							articleClustRcd.setLogUpdatedBy(user);
							Timestamp ts =  new Timestamp(System.currentTimeMillis());
							articleClustRcd.setLogUpdatedOn(ts);
							switch (attr) {
								case 1 -> articleClustRcd.setPricePurchaseDollar(article.getPricePurchaseDollar());
								case 2 -> articleClustRcd.setPriceSalesDollar(article.getPriceSalesDollar());
								case 3 -> articleClustRcd.setArticleClearingType(article.getArticleClearingType());
								 case 4 -> articleClustRcd.setAvailableForRoles(article.getAvailableForRoles()); //TODO need to check if this need to be sync
								case 5 -> articleClustRcd.setQuantifier(article.getQuantifier());
								case 6 -> articleClustRcd.setSingleArticle(article.getSingleArticle());
								case 7 ->
										articleClustRcd.setDescription(article.getDescription()); //TODO need to check if its remark field
								case 8 ->
										articleClustRcd.setClearingAtNewConnection(article.getClearingAtNewConnection());
								case 9 -> articleClustRcd.setLifeTime(article.getLifeTime());
								case 10 -> articleClustRcd.setSapAvaya(article.getSapAvaya());
								case 11 -> articleClustRcd.setSapBosh(article.getSapBosh());
								case 12 -> articleClustRcd.setArticleCategory(article.getArticleCategory());
								case 13 -> articleClustRcd.setServiceCodeCluster(article.getServiceCode());
								case 14 -> articleClustRcd.setArticleWizardType(article.getArticleWizardType());
								case 15 -> articleClustRcd.setPricePurchaseEuro(article.getPricePurchaseEuro());
								case 16 -> articleClustRcd.setPriceSalesEuro(article.getPriceSalesEuro());
								case 17 -> articleClustRcd.setClearingAtDelete(article.getClearingAtDelete());
								case 18 -> articleClustRcd.setClearingAtChangeMove(article.getClearingAtChangeMove());
								case 19 -> updateArticleName(articleClustRcd, "en");
								case 20 -> updateArticleName(articleClustRcd, "de");
								case 21 -> articleClustRcd.setServiceCodeCluster(article.getServiceCode());
								default -> log.info("invalid attribute id {}", attr);
							}
						});
						return articleClustRcd;
					}).toList();				
					artClustRepo.syncArticleClusterOnSlaveUpdate(updatedArticleClusters);
				} else {
					log.info("No article clusters found.");
				}
			} else {
				log.info("Article is in inActive state , skip processing");
			}
		}, () -> {
			log.info("No article found with Id{}", articleId);
		});
	}
	
	private void updateArticleName(ArticleCluster article, String lang) {
		Set<ArticleI18NName> articleNameSet = article.getArticle().getArticleI18NName();
		if(articleNameSet!=null&&!articleNameSet.isEmpty()) {
			updateArticleName(article, lang, articleNameSet);
		}
	}
	
	private void updateArticleName(ArticleCluster article, String langId, Set<ArticleI18NName> articleNameSet) {
		List<ArticleClusterI18NName> artClusterNameList = artClusterI18NRepo.findByArticleIdAndLanguageId(article.getId(),langId);
		if(artClusterNameList!=null && !artClusterNameList.isEmpty()) {
			ArticleClusterI18NName articleClusteri18Name = artClusterNameList.get(0);
			Iterator<ArticleI18NName> itr = articleNameSet.iterator();
			while(itr.hasNext()) {
				ArticleI18NName articleName = itr.next();
				if(langId.equalsIgnoreCase(articleName.getLanguageId())) {
					articleClusteri18Name.setTranslation(articleName.getTranslation());
					artClusterI18NRepo.save(articleClusteri18Name);
					break;
				}
			}
		} else {
			Iterator<ArticleI18NName> itr = articleNameSet.iterator();
			Optional<Language> lang = langRepo.findById(langId);
			if (lang.isPresent()) {
				Language language = lang.get();
				while (itr.hasNext()) {
					ArticleI18NName articleName = itr.next();
					if (langId.equalsIgnoreCase(articleName.getLanguageId())) {
						ArticleClusterI18NName articleClusteri18Name = new ArticleClusterI18NName();
						articleClusteri18Name.setArticle(article);
						articleClusteri18Name.setLanguage(language);
						articleClusteri18Name.setTranslation(articleName.getTranslation());
						artClusterI18NRepo.save(articleClusteri18Name);
						break;
					}
				}
			}
		}
	}

	public ArticleI18NameDTO fetchArticleLabel(Long articleId) {
		List<ArticleI18NName> articleNameList = articleI18Repo.findByArticleId(articleId);
		ArticleI18NameDTO dto = new ArticleI18NameDTO();
		dto.setArticleId(articleId);
		articleNameList.forEach((articleI18Name)->{
			if("en".equalsIgnoreCase(articleI18Name.getLanguageId())) {
				dto.setArticleNameEnglish(articleI18Name.getTranslation());
			} else if("de".equalsIgnoreCase(articleI18Name.getLanguageId())) {
				dto.setArticleNameGerman(articleI18Name.getTranslation());
			}
		});
		return dto;
	}

	@Override
	public List<ClusterDto> fetchUnassignedClusters(Long articleId) {
		List<ClusterItem> unassignedClusters = clusterRepo.findUnassignedClusters(articleId);
		if (unassignedClusters.isEmpty()) {
			log.info("No unassigned clusters found for articleId: {}", articleId);
			return Collections.emptyList();
		}
		return unassignedClusters.stream().map(cluster -> mapper.map(cluster, ClusterDto.class))
				.collect(Collectors.toList());
	}

}
