package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.DynamicUpdate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ArticleCategory;
import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticleConnection;
import com.avaya.amsp.domain.ArticleWizard;
import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.domain.Language;
import com.avaya.amsp.domain.PartlistClusterSubarticle;
import com.avaya.amsp.domain.ServiceCode;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticleClusterPartListDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.dtos.PortTypeDto;
import com.avaya.amsp.masterdata.dtos.SubArticleDetailDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.ConnectionRepository;
import com.avaya.amsp.masterdata.repo.LanguageRepository;
import com.avaya.amsp.masterdata.repo.PartListArticleClusterRepository;
import com.avaya.amsp.masterdata.service.iface.ArticleClusterServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@DynamicUpdate
public class ArticleClusterService implements ArticleClusterServiceIface {

	@Autowired
	private ArticleClusterRepository articleClusterRepo;

	@Autowired
	private ArticleRepository articleRepo;

	@Autowired
	ConnectionRepository connectionRepo;
	
	@Autowired
	private ArticleClusterI18NRepository artClusterI18Repo;

	@Autowired
	ClusterRepository clusterRepo;
	
	@Autowired
	LanguageRepository langRepo;

	@Autowired
	PartListArticleClusterRepository partListRepo;

	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	ArticleClusterI18NNameService articleClusterI18NNameService;
	
	double finalSaleMonthly = 0.0;
	double finalSaleOneTime = 0.0;

	double finalPurchaseMonthly = 0.0;
	double finalPurchaseOneTime = 0.0;

	@Override
	public List<ArticleToClusterDto> fetchAllArticles() {

		log.info("fetching cluster articles from database");
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();

		List<ArticleCluster> articles = articleClusterRepo.findAll();

		if (articles != null && !articles.isEmpty()) {

			articles.forEach(article -> {

				ArticleToClusterDto dto = mapper.map(article, ArticleToClusterDto.class);
				dto.setId(article.getId());
				dto.setName(article.getName());
				dto.setRemark(article.getDescription());
				dto.setLogCreatedOn(article.getLogCreatedOn());
				dto.setLogCreatedOn(article.getLogCreatedOn());
				dto.setLogCreatedBy(article.getLogCreatedBy());
				dto.setLogUpdatedBy(article.getLogUpdatedBy());
				dto.setLogUpdatedOn(article.getLogUpdatedOn());
				dto.setPricePurchase_dollar(article.getPricePurchaseDollar());
				dto.setPricePurchase_euro(article.getPricePurchaseEuro());
				dto.setPriceSales_dollar(article.getPriceSalesDollar());
				dto.setPriceSales_euro(article.getPriceSalesEuro());
				/*
				 * if (article.getArticleLaslNumber() != null) {
				 * dto.setLaslNumberId(article.getArticleLaslNumber().getId()); }
				 */
				if (article.getServiceCodeCluster() != null) {
					dto.setServiceCode(article.getServiceCodeCluster().getServiceCode());
				}
				dto.setSlaDays(article.getSlaDays());
				dto.setSlaHrs(article.getSlaHours());
				dto.setSlaMin(article.getSlaMinutes());
				dto.setArticleClearingType(article.getArticleClearingType());
				if (article.getArticleWizardType() != null) {
					dto.setArticleWizardId(article.getArticleWizardType().getId());
				}
				dtos.add(dto);
			});

		} else {
			log.info("No article records found...");
		}
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

	@AuditLog(action = "UPDATE",entity = "ArticleCluster",functionality = "Update MasterData Article/PartList UI ArticleCluster")
	@Override
	public void updateArticle(ArticleToClusterDto dto) {

		log.info("Updating Article cluster for Id {}", dto.getId());
		ArticleCategory articleCategory = new ArticleCategory();
		//ArticleLaslNumber articleLaslNumber = new ArticleLaslNumber();
		ServiceCode serviceCode = new ServiceCode();
		ArticleWizard articleWizard = new ArticleWizard();
		Optional<ArticleCluster> record = articleClusterRepo.findById(dto.getId());
		record.ifPresentOrElse(value -> {
			value.setDescription(dto.getRemark());
			value.setPricePurchaseDollar(dto.getPricePurchase_dollar());
			value.setPricePurchaseEuro(dto.getPricePurchase_euro());
			value.setPriceSalesDollar(dto.getPriceSales_dollar());
			value.setPriceSalesEuro(dto.getPriceSales_euro());
			value.setLogCreatedBy(value.getLogCreatedBy());
			value.setLogCreatedOn(value.getLogCreatedOn());
			value.setLogUpdatedBy(dto.getUser());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			value.setActive(1);
			value.setSingleArticle(dto.getSingleArticle());
			value.setMasterArticle(dto.getMasterArticle());
			value.setValueDefault(dto.getValueDefault());
			value.setValueReadOnly(dto.getValueReadOnly());
			value.setHardwareFromAvaya(dto.getHardwareFromAvaya());
			value.setSubjectToAuthorization(dto.getSubjectToAuthorization());
			value.setBilling(dto.getBilling());
			value.setIncidentArticle(dto.getIncidentArticle());
			value.setServusInterface(dto.getServusInterface());
			value.setHidden(dto.getHidden());
			value.setNonAvailable(dto.getNonAvailable());
			value.setShippingAddress(dto.getShippingAddress());
			value.setAssemblingAddress(dto.getAssemblingAddress());
			value.setPoolHandling(dto.getPoolHandling());
			value.setLifeTime(dto.getLifeTime());
			value.setSapBosh(dto.getSapBosh());
			value.setSapAvaya(dto.getSapAvaya());
			value.setSlaDays(dto.getSlaDays());
			value.setSlaHours(dto.getSlaHrs());
			value.setSlaMinutes(dto.getSlaMin());
			value.setProperty(dto.getProperty());
			value.setValueTransfer(dto.getValueTransfer());
			if (dto.getArticleClearingType() != null) {
				value.setArticleClearingType(dto.getArticleClearingType());
			}

			if (dto.getArticleCategoryId() == null || (dto.getArticleCategoryId() == 0)) {
				log.info("ArticleCategoryId is 0");
				value.setArticleCategory(null);
			} else {
				if (dto.getArticleCategoryId() != null) {
					articleCategory.setId(dto.getArticleCategoryId());
					value.setArticleCategory(articleCategory);
				}
			}

			/*
			 * if (dto.getLaslNumberId() == null || (dto.getLaslNumberId() == 0)) {
			 * log.info("LaslNumberId is 0"); value.setArticleLaslNumber(null); } else { if
			 * (dto.getLaslNumberId() != null) {
			 * articleLaslNumber.setId(dto.getLaslNumberId());
			 * value.setArticleLaslNumber(articleLaslNumber); } }
			 */
			
			if (dto.getServiceCode() == null) {
				log.info("ServiceNumberId is 0");
				value.setServiceCodeCluster(null);
			} else {
				if (dto.getServiceCode() != null) {
					serviceCode.setServiceCode(dto.getServiceCode());
					value.setServiceCodeCluster(serviceCode);
				}
			}

			if (dto.getArticleWizardId() == null || (dto.getArticleWizardId() == 0)) {
				log.info("Article wizard is null");
				value.setArticleWizardType(null);
			} else {
				if (dto.getArticleWizardId() != null) {
					articleWizard.setId(dto.getArticleWizardId());
					value.setArticleWizardType(articleWizard);
				}
			}
			
			if(dto.getAvailableForRoles()!=null&&!dto.getAvailableForRoles().isEmpty()) {
				value.setAvailableForRoles(getAvailableForRoles(dto.getAvailableForRoles()));
			}
			

			ArticleCluster result=  articleClusterRepo.save(value);

			List<ArticleClusterI18NName> articleNameSet = artClusterI18Repo.findByArticleId(result.getId());
			if(articleNameSet!=null &&!articleNameSet.isEmpty()) {
				Map<String, ArticleClusterI18NName> articleI18Map = new HashMap<>();
				articleNameSet.forEach((articleI18Name)->{
					articleI18Map.put(articleI18Name.getLanguageId(), articleI18Name);
				});
				if(dto.getNameEnglish()!=null&&!dto.getNameEnglish().isEmpty()) {
					if(articleI18Map.containsKey("en")) {
						ArticleClusterI18NName articleNameEng = articleI18Map.get("en");
						articleNameEng.setTranslation(dto.getNameEnglish());
						articleNameEng.setLogUpdatedBy(dto.getUser());
						articleNameEng.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
						artClusterI18Repo.save(articleNameEng);
					} else {
						Language engLang = langRepo.getReferenceById("en");
						ArticleClusterI18NName articleNameEng = new ArticleClusterI18NName();
						articleNameEng.setArticle(result);
						articleNameEng.setLanguage(engLang);
						articleNameEng.setTranslation(dto.getNameEnglish());
						articleNameEng.setLogCreatedBy(dto.getUser());
						articleNameEng.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
						artClusterI18Repo.save(articleNameEng);
					}
				}
				
				if(dto.getNameGerman()!=null&&!dto.getNameGerman().isEmpty()) {
					if(articleI18Map.containsKey("de")) {
						ArticleClusterI18NName articleNameDe = articleI18Map.get("de");
						articleNameDe.setTranslation(dto.getNameGerman());
						articleNameDe.setLogUpdatedBy(dto.getUser());
						articleNameDe.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
						artClusterI18Repo.save(articleNameDe);
					} else {
						Language gerLang = langRepo.getReferenceById("de");
						ArticleClusterI18NName articleNameDe = new ArticleClusterI18NName();
						articleNameDe.setArticle(result);
						articleNameDe.setLanguage(gerLang);
						articleNameDe.setTranslation(dto.getNameGerman());
						articleNameDe.setLogCreatedBy(dto.getUser());
						articleNameDe.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
						artClusterI18Repo.save(articleNameDe);
					}
				}
			} else {
				if(dto.getNameEnglish()!=null&&!("".equalsIgnoreCase(dto.getNameEnglish()))) {
					Language engLang = langRepo.getReferenceById("en");
					ArticleClusterI18NName articleNameEng = new ArticleClusterI18NName();
					articleNameEng.setArticle(result);
					articleNameEng.setLanguage(engLang);
					articleNameEng.setTranslation(dto.getNameEnglish());
					articleNameEng.setLogCreatedBy(dto.getUser());
					articleNameEng.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
					artClusterI18Repo.save(articleNameEng);
				}
				if(dto.getNameGerman()!=null&&!("".equalsIgnoreCase(dto.getNameGerman()))) {
					Language deLang = langRepo.getReferenceById("de");
					ArticleClusterI18NName articleNameDe = new ArticleClusterI18NName();
					articleNameDe.setArticle(result);
					articleNameDe.setLanguage(deLang);
					articleNameDe.setTranslation(dto.getNameGerman());
					articleNameDe.setLogCreatedBy(dto.getUser());
					articleNameDe.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
					artClusterI18Repo.save(articleNameDe);
				}
			}
		}, () -> {
			log.info("Article cluster record not found");
			throw new ResourceNotFoundException(String.format("Article cluster with Id %s not found ", dto.getId()));
		});

	}

	@AuditLog(action = "DELETE",entity = "ArticleCluster",functionality = "Delete MasterData Article/PartList UI ArticleCluster")
	@Override
	public void removeArticle(Long articleId, String user) {

		log.info("Removing article cluster record with ID {}", articleId);

		Optional<ArticleCluster> record = articleClusterRepo.findById(articleId);
		record.ifPresentOrElse(value -> {
			if (value.getIsPart() == 0) { // check whether an article is a partlist or normal article
				value.setActive(0);
				articleClusterRepo.save(value);
			} else {
				log.info("leadId master status is updated as false for {}", articleId);
				articleClusterRepo.removeMasterStatus(articleId, user, new Timestamp(System.currentTimeMillis()));
				log.info("deleting associated subarticles for leadarticle in Cluster partlist {}", articleId);
				partListRepo.deleteSubArticlesByLeadId(articleId);
				log.info("Cluster parlist sub articles deletion completed for leadId{}", articleId);

			}
		}, () -> {
			log.info("Article cluster record not found");
			throw new ResourceNotFoundException(String.format("Article cluster with Id %s not found ", articleId));
		});
	}

	@Override
	public List<ConnectionDto> fetchConnectionsByArticle(Long articleId) {

		log.info("fetching connections for article {}", articleId);

		List<ConnectionDto> dtos = new ArrayList<ConnectionDto>();

		Optional<Article> record = articleRepo.findById(articleId);
		record.ifPresentOrElse(article -> {
			Set<ArticleConnection> articleConnection = article.getArticleConnection();

			articleConnection.stream().forEach(articleConn -> {
				Connection connection = articleConn.getConnection();

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
			log.info("Article record not found");
			throw new ResourceNotFoundException(String.format("Article with Id %s not found ", articleId));
		});
		return dtos;
	}

	@Override
	public List<ArticleToClusterDto> fetchRecordByArticleCluster(Long articleId, Long clusterId) {
		log.info("fetching article cluster from database with  master article id and cluster id");
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();

		List<ArticleCluster> articles = articleClusterRepo.findByArticleCluster(articleId, clusterId);
		if (articles != null && !articles.isEmpty()) {

			articles.forEach(article -> {

				ArticleToClusterDto dto = mapper.map(article, ArticleToClusterDto.class);
				dto.setId(article.getId());
				dto.setName(article.getName());
				dto.setRemark(article.getDescription());
				dto.setLogCreatedOn(article.getLogCreatedOn());
				dto.setLogCreatedOn(article.getLogCreatedOn());
				dto.setLogCreatedBy(article.getLogCreatedBy());
				dto.setLogUpdatedBy(article.getLogUpdatedBy());
				dto.setLogUpdatedOn(article.getLogUpdatedOn());
				dto.setPricePurchase_dollar(article.getPricePurchaseDollar());
				dto.setPricePurchase_euro(article.getPricePurchaseEuro());
				dto.setPriceSales_dollar(article.getPriceSalesDollar());
				dto.setPriceSales_euro(article.getPriceSalesEuro());
				
				if (article.getServiceCodeCluster() != null) {
					dto.setServiceCode(article.getServiceCodeCluster().getServiceCode());
				}
				dto.setSlaDays(article.getSlaDays());
				dto.setSlaHrs(article.getSlaHours());
				dto.setSlaMin(article.getSlaMinutes());
				dtos.add(dto);
			});

		} else {
			log.info("No article records found...");
		}
		return dtos;
	}

	// Changes for Article cluster Parts
	// for getting the list of master article against cluster in Cluster->Partlist
	// screen
	@Override
	public List<ArticleClusterPartListDto> fetchAllArticleClusterByParts(Long clusterId, int partStatus,AMSPUser user) {

		log.info("fetching article clusters from database for partstatus {}", partStatus);
		List<ArticleClusterPartListDto> dtos = new ArrayList<ArticleClusterPartListDto>();
		List<ArticleCluster> articles = articleClusterRepo.findArticleClusterByPartsStatus(partStatus, clusterId);
		
		List<Long> articleIds = articles.stream().map(ArticleCluster::getId).collect(Collectors.toList());
		List<ArticleClusterI18NName> articleNamesList = artClusterI18Repo.findByArticleClusterIds(articleIds);
		
		if (articles != null && !articles.isEmpty()) {
			
			Map<Long, List<ArticleClusterI18NName>> articleNameMap = articleNamesList.stream()
					.collect(Collectors.groupingBy(i18n -> i18n.getArticle().getId()));
			
			articles.forEach(article -> {
				// if (article.getMasterPartStatus()) {
				ArticleClusterPartListDto partDto = new ArticleClusterPartListDto();
				if (partStatus == 1) {
					double[] monthlySaleInEuro = { 0.0 };
					double[] oneTimeSalesInEuro = { 0.0 };
					double[] monthlyPurchaseInEuro = { 0.0 };
					double[] oneTimePurchaseInEuro = { 0.0 };

					Set<PartlistClusterSubarticle> partsData = article.getPartlistClusterSubarticle();
					log.info("total sublist size" + partsData.size());
					if (partsData.size() != 0) {
						partsData.forEach(partListRecord -> {
							ArticleToClusterDto articleDto = new ArticleToClusterDto();
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
							//	articleDto.setName(articleClusterI18NNameService.getTranslation(article.getId(),
								//		user.getDefaultLanguageId(), article.getName()));
								
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
								//end
								
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

								if (article.getServusInterface() != null) {
									articleDto.setServusInterface(article.getServusInterface());
								}
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
								
								if (article.getServiceCodeCluster() != null) {
									articleDto.setServiceCode(article.getServiceCodeCluster().getServiceCode());
								}
								articleDto.setSapAvaya(article.getSapAvaya());
								articleDto.setSapBosh(article.getSapBosh());
								articleDto.setSlaDays(article.getSlaDays());
								articleDto.setSlaHrs(article.getSlaHours());
								articleDto.setSlaMin(article.getSlaMinutes());
								articleDto.setValueTransfer(article.getValueTransfer());
								articleDto.setProperty(article.getProperty());
								articleDto.setIsMasterPartStatus(article.getMasterPartStatus());
								partDto.setArticles(articleDto);
								partDto.setSubArticleCount(partsData.size());
							} else {
								log.info("article clearing type is null, so can't proceed..");
							}

						});
						dtos.add(partDto);
					} else {

						if (article.getArticleClearingType().equals(ArticleClearingTypeEnum.MONTHLY)) {
							partDto.setMonthlySalesPrice(article.getPriceSalesEuro());
							partDto.setOneTimeSalesPrice(0);
							partDto.setSubArticleCount(partsData.size());
							partDto.setMonthlyPurchasePrice(article.getPricePurchaseEuro());
							partDto.setOneTimeSalesPrice(0);
							ArticleToClusterDto articleDto = new ArticleToClusterDto();
							articleDto.setId(article.getId());
							articleDto.setName(article.getName());
							//mycode
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
							//end
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
							if (article.getServusInterface() != null) {
								articleDto.setServusInterface(article.getServusInterface());
							}
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
							
							if (article.getServiceCodeCluster() != null) {
								articleDto.setServiceCode(article.getServiceCodeCluster().getServiceCode());
							}
							articleDto.setSapAvaya(article.getSapAvaya());
							articleDto.setSapBosh(article.getSapBosh());
							articleDto.setSlaDays(article.getSlaDays());
							articleDto.setSlaHrs(article.getSlaHours());
							articleDto.setSlaMin(article.getSlaMinutes());
							articleDto.setValueTransfer(article.getValueTransfer());
							articleDto.setProperty(article.getProperty());
							articleDto.setIsPart(article.getIsPart());
							articleDto.setIsMasterPartStatus(article.getMasterPartStatus());

							partDto.setArticles(articleDto);
							dtos.add(partDto);

						} else {
							if (article.getArticleClearingType().equals(ArticleClearingTypeEnum.ONETIME)) {
								partDto.setMonthlySalesPrice(0);
								partDto.setOneTimeSalesPrice(article.getPriceSalesEuro());
								partDto.setSubArticleCount(partsData.size());
								partDto.setMonthlyPurchasePrice(0);
								partDto.setOneTimeSalesPrice(article.getPricePurchaseEuro());

								ArticleToClusterDto articleDto = new ArticleToClusterDto();
								articleDto.setId(article.getId());
								articleDto.setName(article.getName());
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
								if (article.getServusInterface() != null) {
									articleDto.setServusInterface(article.getServusInterface());
								}
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
								
								if (article.getServiceCodeCluster() != null) {
									articleDto.setServiceCode(article.getServiceCodeCluster().getServiceCode());
								}
								articleDto.setSapAvaya(article.getSapAvaya());
								articleDto.setSapBosh(article.getSapBosh());
								articleDto.setSlaDays(article.getSlaDays());
								articleDto.setSlaHrs(article.getSlaHours());
								articleDto.setSlaMin(article.getSlaMinutes());
								articleDto.setValueTransfer(article.getValueTransfer());
								articleDto.setProperty(article.getProperty());
								articleDto.setIsPart(article.getIsPart());
								articleDto.setIsMasterPartStatus(article.getMasterPartStatus());
								partDto.setArticles(articleDto);
								dtos.add(partDto);
							}
						}
					}
				} else if (partStatus == 0) {
					ArticleToClusterDto articleDto = new ArticleToClusterDto();
					articleDto.setId(article.getId());
					articleDto.setName(article.getName());
					//mycode
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
					//end
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

					if (article.getServusInterface() != null) {
						articleDto.setServusInterface(article.getServusInterface());
					}
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
					
					if (article.getServiceCodeCluster() != null) {
						articleDto.setServiceCode(article.getServiceCodeCluster().getServiceCode());
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
				// }
			});

		} else {
			log.info("No article records found...");
		}
		return dtos;
	}

	@Override
	public void addSubArticlesToArticle(Long leadArticleId, SubArticleDetailDto subArticleDetails, String user) {

		log.info("request received for adding sub articles to lead article for cluster partlist");
		Optional<ArticleCluster> articles = articleClusterRepo.findById(leadArticleId);
		articles.ifPresentOrElse(articleClusterRecord -> {
			List<ArticleCluster> subArticleRecords = articleClusterRepo
					.findAllById(subArticleDetails.getSubArticleIds());
			log.info("no of rows available for subarticle for article cluster{}", subArticleRecords.size());

			if (subArticleRecords.size() != subArticleDetails.getSubArticleIds().size()) {
				throw new IllegalArgumentException("Some of the subarticles not found");
			}

			List<PartlistClusterSubarticle> partSubArticle = new ArrayList<PartlistClusterSubarticle>();
			subArticleRecords.forEach((subArticle) -> {
				PartlistClusterSubarticle partSubRecord = new PartlistClusterSubarticle();
				partSubRecord.setArticleCluster(articleClusterRecord);
				partSubRecord.setSubArticles(subArticle);
				// articleClusterRepo.updateIsPartStatus(1, articleClusterRecord.getId());
				partSubArticle.add(partSubRecord);
			});

			partListRepo.saveAll(partSubArticle);
			articleClusterRecord.setLogUpdatedBy(user);
			articleClusterRecord.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			articleClusterRepo.save(articleClusterRecord);
		}, () -> {
			log.info("article cluster record record not found");
			throw new ResourceNotFoundException(String.format(" article cluster with Id %s not found ", leadArticleId));
		});

	}

	@Override
	public void deleteClusterPartListLeadArticles(Long leadId, String user) {

		Optional<ArticleCluster> articleRecords = articleClusterRepo.findById(leadId);
		articleRecords.ifPresentOrElse(articleRecord -> {
			// if (!articleRecord.getMasterPartStatus()) {
			log.info("leadId master status is updated as false for {}", leadId);
			articleClusterRepo.removeMasterStatus(leadId, user, new Timestamp(System.currentTimeMillis()));
			log.info("deleting associated subarticles for leadarticle in Cluster partlist {}", leadId);
			partListRepo.deleteSubArticlesByLeadId(leadId);
			log.info("Cluster parlist sub articles deletion completed for leadid{}", leadId);
		},

				() -> {
					log.error("lead article with Id {} not found", leadId);
					throw new ResourceNotFoundException(String.format("lead article with Id %s not found", leadId));
				});
	}

	@Override
	public void deleteAssignSubArticles(Long leadId, SubArticleDetailDto subArticles, String user) {

		Optional<ArticleCluster> articleRecords = articleClusterRepo.findById(leadId);

		articleRecords.ifPresentOrElse(articleRecord -> {
			Set<PartlistClusterSubarticle> partListRecords = articleRecord.getPartlistClusterSubarticle();
			if (partListRecords != null && partListRecords.size() != 0) {
				List<PartlistClusterSubarticle> PartSubList = new ArrayList<PartlistClusterSubarticle>();
				subArticles.getSubArticleIds().forEach(subArticleId -> {
					PartlistClusterSubarticle partListArticle = new PartlistClusterSubarticle();

					partListArticle.setSubArticleId(subArticleId);
					partListArticle.setArticleClusterId(leadId);

					PartlistClusterSubarticle partListRecord = partListRecords.stream()
							.filter(value -> value.equals(partListArticle)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(String.format(
									"sub article with Id %s not found for lead article id for cluster partlist %s",
									subArticleId, leadId)));
					PartSubList.add(partListRecord);
				});
				log.info("Removing subarticles from lead article for cluster partlist{} ", PartSubList);
				partListRepo.deleteAll(PartSubList);
				List<PartlistClusterSubarticle> listData = partListRepo.findByArticle(leadId);
				if (listData.size() == 0) {
					log.info("all associated subarticles deleted in cluster partlist");
					articleClusterRepo.updateIsPartStatus((short) 0, leadId, user, new Timestamp(System.currentTimeMillis()));
				} else {
					articleRecord.setLogUpdatedBy(user);
					articleRecord.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
					articleClusterRepo.save(articleRecord);
					log.info("some of the subarticles are remaining for cluster article having lead id {}", leadId);
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

	@Override
	public List<ArticleToClusterDto> fetchClusterSubArticles(Long leadId) {
		log.info("fetching article cluster sublist from database");

		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();
		Optional<ArticleCluster> articles = articleClusterRepo.findById(leadId);
		List<Long> allClusterIds = new ArrayList<>();
		allClusterIds.add(articles.get().getId());
		for (PartlistClusterSubarticle partList : articles.get().getPartlistClusterArticle()) {
			if (partList.getSubArticles() != null) {
				allClusterIds.add(partList.getSubArticles().getId());
			}
		}
		List<ArticleClusterI18NName> i18nNames = artClusterI18Repo.findByArticleClusterIds(allClusterIds);
		
		
		if (articles != null && !articles.isEmpty()) {
			Map<Long, List<ArticleClusterI18NName>> articleNameMap = i18nNames.stream()
					.collect(Collectors.groupingBy(i18n -> i18n.getArticle().getId()));
			articles.ifPresentOrElse(article -> {
				Set<PartlistClusterSubarticle> subArticleData = article.getPartlistClusterArticle();
				subArticleData.forEach(subListRecord -> {
					ArticleToClusterDto articleDto = new ArticleToClusterDto();

					articleDto.setId(subListRecord.getSubArticles().getId());
					articleDto.setName(subListRecord.getSubArticles().getName());
					
					List<ArticleClusterI18NName> articleNameList = articleNameMap.get(subListRecord.getSubArticles().getId());
					if (articleNameList != null) {
						for (ArticleClusterI18NName nameEntry : articleNameList) {
							if ("en".equalsIgnoreCase(nameEntry.getLanguageId())) {
								articleDto.setNameEnglish(nameEntry.getTranslation());
							} else if ("de".equalsIgnoreCase(nameEntry.getLanguageId())) {
								articleDto.setNameGerman(nameEntry.getTranslation());
							}
						}
					}
					articleDto.setRemark(subListRecord.getSubArticles().getDescription());
					articleDto.setArticleClearingType(subListRecord.getSubArticles().getArticleClearingType());
					articleDto.setProperty(subListRecord.getSubArticles().getProperty());
					articleDto.setValueTransfer(subListRecord.getSubArticles().getValueTransfer());
					articleDto.setSapAvaya(subListRecord.getSubArticles().getSapAvaya());
					articleDto.setSapBosh(subListRecord.getSubArticles().getSapBosh());
					articleDto.setProperty(subListRecord.getSubArticles().getProperty());
					articleDto.setIsPart(subListRecord.getSubArticles().getIsPart());
					articleDto.setIsMasterPartStatus(subListRecord.getSubArticles().getMasterPartStatus());
					if (subListRecord.getSubArticles().getArticleCategory() != null) {
						articleDto.setArticleCategoryId(subListRecord.getSubArticles().getArticleCategory().getId());
					}
					articleDto.setClearingAtNewConnection(subListRecord.getSubArticles().getClearingAtNewConnection());
					articleDto.setPriceSales_euro(subListRecord.getSubArticles().getPriceSalesEuro());
					articleDto.setPriceSales_dollar(subListRecord.getSubArticles().getPriceSalesDollar());
					articleDto.setPricePurchase_dollar(subListRecord.getSubArticles().getPricePurchaseDollar());
					articleDto.setPricePurchase_euro(subListRecord.getSubArticles().getPricePurchaseEuro());
					articleDto.setSingleArticle(subListRecord.getSubArticles().getSingleArticle());
					articleDto.setMasterArticle(subListRecord.getSubArticles().getMasterArticle());

					dtos.add(articleDto);
				});

			}, () -> {
				log.info("No article records found...");

			});
		}
		return dtos;
	}

	@Override
	public void removeMasterPartStatus(Long articleId, String user) {

		log.info("request received to remove the master part list status for article id {}", articleId);
		Optional<ArticleCluster> articleData = articleClusterRepo.findById(articleId);
		articleData.ifPresentOrElse(articleRecord -> {
			articleClusterRepo.removeMasterStatus(articleId, user, new Timestamp(System.currentTimeMillis()));

		}, () -> {
			log.info("cluster id not found");
			throw new ResourceNotFoundException(String.format("article cluster with id %s not found ", articleId));
		});

	}
}
