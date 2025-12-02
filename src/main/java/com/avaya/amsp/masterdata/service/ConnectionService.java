package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticleConnection;
import com.avaya.amsp.domain.ArticleI18NName;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.BcsBunch;
import com.avaya.amsp.domain.ClusterConnection;
import com.avaya.amsp.domain.ClusterConnection.ClusterConnectionId;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.domain.ConnectionPortType;
import com.avaya.amsp.domain.Currency;
import com.avaya.amsp.domain.PartlistClusterSubarticle;
import com.avaya.amsp.domain.PartsPropertyEnum;
import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.domain.PortType;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticleClusterDetailDto;
import com.avaya.amsp.masterdata.dtos.ArticleConnectionMappingDto;
import com.avaya.amsp.masterdata.dtos.ArticleDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesReqDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertyAttributeDto;
import com.avaya.amsp.masterdata.dtos.ArticleToConnectionReqDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ClustersToConnectionDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.dtos.PoolDto;
import com.avaya.amsp.masterdata.dtos.PortTypeDto;
import com.avaya.amsp.masterdata.dtos.PropertyDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticleConnectionRepository;
import com.avaya.amsp.masterdata.repo.ArticleI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.BcsRepository;
import com.avaya.amsp.masterdata.repo.ClusterConnectionRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.ConnectionPortTypeRepository;
import com.avaya.amsp.masterdata.repo.ConnectionRepository;
import com.avaya.amsp.masterdata.repo.PoolRepository;
import com.avaya.amsp.masterdata.repo.PortTypeRepository;
import com.avaya.amsp.masterdata.service.iface.ConnectionServiceIface;
import com.avaya.amsp.security.user.AMSPUser;
import com.google.gson.Gson;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ConnectionService implements ConnectionServiceIface {

	@Autowired
	ConnectionRepository connectionRepo;

	@Autowired
	PortTypeRepository portTypeRepo;

	@Autowired
	ConnectionPortTypeRepository connPortRepo;

	@Autowired
	ArticleRepository articleRepo;

	@Autowired
	ArticleClusterRepository articleClusterRepo;

	@Autowired
	ArticleConnectionRepository articleConnectionRepo;

	@Autowired
	ClusterRepository clusterRepo;

	@Autowired
	ClusterConnectionRepository clustConnRepo;

	@Autowired
	ModelMapper mapper;

	@Autowired
	ClusterConnectionRepository clusterConnRepo;

	@Autowired
	ClusterService clusterService;

	@Autowired
	SiteService siteService;

	@Autowired
	PoolRepository poolRepo;

	@Autowired
	BcsRepository bcsRepo;

	@Autowired
	ArticleClusterI18NNameService articleClusterI18NNameService;
	
	@Autowired
	ArticleClusterI18NRepository artClusterI18NRepo;
	
	@Autowired
	ArticleI18NRepository articleI18NRepository;

	@Override
	public List<ConnectionDto> fetchAllConnections() {

		log.info("fetching connections from database");
		List<ConnectionDto> dtos = new ArrayList<ConnectionDto>();
		List<Connection> connections = connectionRepo.findAll();
		if (connections != null && !connections.isEmpty()) {
			connections.forEach(connection -> {
				ConnectionDto dto = mapper.map(connection, ConnectionDto.class);
				if (connection.getConnectionPortType() != null) {
					dto.setPortTypes(new ArrayList<>());
					connection.getConnectionPortType().stream().forEach(connPort -> {
						dto.getPortTypes().add(mapper.map(connPort.getPortType(), PortTypeDto.class));
					});

				}
				dto.setUser(connection.getLogCreatedBy());
				dto.setCreatedOn(connection.getLogCreatedOn());
				dto.setUpdateddOn(connection.getLogUpdatedOn());
				dto.setLogCreatedBy(connection.getLogCreatedBy());
				dto.setLogUpdatedBy(connection.getLogUpdatedBy());

				dtos.add(dto);
			});
		} else {
			log.info("No Connection records found..");
		}
		return dtos;
	}

	@AuditLog(action = "insert", entity = "Connection", functionality = "Add New Connection")
	@Override
	@org.springframework.transaction.annotation.Transactional
	public void persistConnection(ConnectionDto dto) {

		log.info("Persisting connection to database");
		// if (fetchConnectionByName(dto.getName()).isPresent()) {
		// log.info("Connection with name {} is already exists ", dto.getName());
		// throw new ResourceAlreadyExistsException(
		// String.format("Connection with name %s is already exists", dto.getName()));
		// }
		Optional<Connection> existingConnectionOpt = fetchConnectionByName(dto.getName());
		Connection domain;
		if (existingConnectionOpt.isPresent()) {
			domain = existingConnectionOpt.get();
			if (domain.getActive() != 0) {
				log.info("Connection with name {} already exists and is active.", dto.getName());
				throw new ResourceAlreadyExistsException(
						String.format("Connection with name %s already exists and is active", dto.getName()));
			}
		} else {
			domain = new Connection();
		}
		// Connection domain = mapper.map(dto, Connection.class);
		// Connection domain = new Connection();
		domain.setActive(1);
		domain.setName(dto.getName());
		BcsBunch bcsBunch = bcsRepo.findById(dto.getIdBcsBunch().longValue()).get();
		domain.setBcsBunch(bcsBunch);
		domain.setDescription(dto.getDescription());
		domain.setPinApplication(dto.getPinApplication());
		domain.setLogCreatedBy(dto.getUser());
		domain.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		domain.setSfbTeamsConnectionFlag(dto.getSfbTeamsConnectionFlag());
		domain.setMigrationTosfbTeams(dto.getMigrationTosfbTeams());
		domain.setAutomationEnabled(dto.getAutomationEnabled());
		log.info("Checking given portType exists..");
		List<PortType> portTypes = portTypeRepo.findAllById(dto.getPortTypesIds());
		Connection record = connectionRepo.save(domain);
		log.info("persisted record with Id {}", record.getId());
		log.info("Inserting connection portType relationship");
		List<ConnectionPortType> newPortTypes = new ArrayList<ConnectionPortType>();
		portTypes.forEach(portType -> {
			ConnectionPortType connPort = new ConnectionPortType();
			connPort.setConnection(record);
			connPort.setPortType(portType);
			newPortTypes.add(connPort);
		});
		connPortRepo.saveAllConnectionPortTypes(newPortTypes);
	}

	@AuditLog(action = "update",entity = "Connection",functionality = "update Existing Connection")
	@Override
	public void updateConnection(ConnectionDto dto) {

		log.info("updating connection with Id {}", dto.getId());

		// Check if connection with same name exists other than one being updated
		Optional<Connection> existingConnection = fetchConnectionByName(dto.getName());
		Connection conn = null;
		if (existingConnection.isPresent()) {
			conn = existingConnection.get();
			if (conn.getId() != dto.getId()) {
				log.info("Connection with name {} is already exists ", dto.getName());
				throw new ResourceAlreadyExistsException(
						String.format("Connection with name %s is already exists", dto.getName()));
			}
		}

		log.info("Persisted cluster connection relationship");

		// Check if input portTypes do exist
		log.info("Checking given portType exists..");
		List<PortType> portTypes = portTypeRepo.findAllById(dto.getPortTypesIds());
		Optional<Connection> record = connectionRepo.findById(dto.getId());
		record.ifPresentOrElse(value -> {
			value.setName(dto.getName());
			value.setDescription(dto.getDescription());
			value.setPinApplication(dto.getPinApplication());
			value.setSfbTeamsConnectionFlag(dto.getSfbTeamsConnectionFlag());
			value.setMigrationTosfbTeams(dto.getMigrationTosfbTeams());
			value.setAutomationEnabled(dto.getAutomationEnabled());
			BcsBunch bunch = new BcsBunch();
			bunch.setId(dto.getIdBcsBunch());
			value.setBcsBunch(bunch);
			value.setLogUpdatedBy(dto.getUser());
			value.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			connectionRepo.save(value);
			log.info("Deleting existing connection portType relationship");
			Set<ConnectionPortType> existingPortTypes = value.getConnectionPortType();
			connPortRepo.deleteAll(existingPortTypes);
			log.info("Inserting connection portType relationship");
			List<ConnectionPortType> newPortTypes = new ArrayList<ConnectionPortType>();
			portTypes.forEach(portType -> {
				ConnectionPortType connPort = new ConnectionPortType();
				connPort.setConnection(value);
				connPort.setPortType(portType);
				newPortTypes.add(connPort);
			});
			connPortRepo.saveAll(newPortTypes);
		}, () -> {
			log.info("Connection record not found");
			throw new ResourceNotFoundException(String.format("Connection with Id %s not found ", dto.getId()));
		});

	}

	@AuditLog(action = "delete",entity = "Connection",functionality = "delete Existing Connection")
	@Override
	public void removeConnection(Long connectionId) {
		log.info("Removing Connection record with ID {}", connectionId);
		Optional<Connection> record = connectionRepo.findById(connectionId);
		record.ifPresentOrElse(value -> {
			value.setActive(0);
			connectionRepo.save(value);
		}, () -> {
			log.info("connectionId record not found");
			throw new ResourceNotFoundException(String.format("connectionId with Id %s not found ", connectionId));
		});
	}

	@Override
	public Optional<Connection> fetchConnectionByName(String name) {
		log.info("Fetching connection for name {}", name);
		Connection connection = connectionRepo.findByName(name);
		return Optional.ofNullable(connection);
	}

	@Override
	public void addArticles(Long connectionId, ArticleToConnectionReqDto articles) {

		log.info("processing add articles request");
		Optional<Connection> record = connectionRepo.findById(connectionId);
		record.ifPresentOrElse(conn -> {
			List<Article> articleRecords = articleRepo.findAllById(articles.getArticleIds());
			log.info("no of rows {}", articleRecords.size());

			if (articleRecords.size() != articles.getArticleIds().size()) {
				throw new IllegalArgumentException("Some of Articles not found");
			}

			List<ArticleConnection> articleConns = new ArrayList<ArticleConnection>();
			articleRecords.forEach((article) -> {
				ArticleConnection artConn = new ArticleConnection();
				artConn.setArticle(article);
				artConn.setConnection(conn);
				artConn.setQuantity(1);
				artConn.setSubscriberSwap(false);
				
				artConn.setAlwaysDelete(false);
				artConn.setAlwaysInsert(false);
				artConn.setAlwaysMove(false);
				artConn.setObligatory(false);
				artConn.setConnectionBcs(conn);
				artConn.setLogCreatedBy(articles.getUser());
				articleConns.add(artConn);
			});
			articleConnectionRepo.addArticlesToConn(articleConns);

			log.info("map articles to clusters");
			Set<ClusterConnection> clusters = conn.getClusterConnection();
			List<Long> clusterIds = clusters.stream().map(cluster -> cluster.getCluster().getId())
					.collect(Collectors.toList());

			if (!clusterIds.isEmpty()) {

				List<ArticleCluster> newArticleClusters = new ArrayList<ArticleCluster>();
				articleRecords.forEach(article -> {

					// Fetch existing article cluster records for given article and clusters
					List<ArticleCluster> articleClusters = articleClusterRepo
							.fetchArticleClusterByClusterIds(article.getId(), clusterIds);
					List<Long> existingClusterIds = articleClusters.stream().map(e -> e.getClusterItem().getId())
							.collect(Collectors.toList());
					List<Long> notMappedClusters = clusterIds.stream().filter(e -> !existingClusterIds.contains(e))
							.collect(Collectors.toList());

					// If there are certain clusters that need to be mapped against article.
					if (!notMappedClusters.isEmpty()) {
						notMappedClusters.stream().forEach(rcd -> {
							log.info("Cluster {} is not mapped against article {}", rcd, article.getId());

							ArticleCluster articleCluster = new ArticleCluster();
							ClusterItem clusterItem = new ClusterItem();// article cluster - cluster entity
							clusterItem.setId(rcd);

							articleCluster.setArticle(article);
							articleCluster.setClusterItem(clusterItem);
							articleCluster.setMasterArticle(0);
							articleCluster.setActive(1);
							articleCluster.setName(article.getName());
							articleCluster.setDescription(article.getDescription());
							articleCluster.setLifetime(1);
							articleCluster.setArticleCategory(article.getArticleCategory());
							articleCluster.setArticleClearingType(article.getArticleClearingType());
							articleCluster.setServiceCodeCluster(article.getServiceCode());
							articleCluster.setSingleArticle(false);

							articleCluster.setClearingAtNewConnection(article.getClearingAtNewConnection());
							articleCluster.setClearingAtDelete(article.getClearingAtDelete());
							articleCluster.setClearingAtChangeMove(article.getClearingAtChangeMove());

							articleCluster.setSlaDays(article.getSlaDays());
							articleCluster.setSlaHours(article.getSlaHours());
							articleCluster.setSlaMinutes(article.getSlaMinutes());

							articleCluster.setPricePurchaseDollar(article.getPricePurchaseDollar());
							articleCluster.setPricePurchaseEuro(article.getPricePurchaseEuro());
							articleCluster.setPriceSalesDollar(article.getPriceSalesDollar());
							articleCluster.setPriceSalesEuro(article.getPriceSalesEuro());
							articleCluster.setPriority(article.getPriority());
							articleCluster.setQuantifier(article.getQuantifier());
							articleCluster.setSapAvaya(article.getSapAvaya());
							articleCluster.setSapBosh(article.getSapBosh());
							articleCluster.setSubjectToAuthorization(article.getSubjectToAuthorization());
							articleCluster.setValueDefault(article.getValueDefault());
							articleCluster.setValueReadOnly(article.getValueReadOnly());

							articleCluster.setHardwareFromAvaya(article.getHardwareFromAvaya());
							articleCluster.setSubjectToAuthorization(article.getSubjectToAuthorization());
							articleCluster.setBilling(article.getBilling());
							articleCluster.setIncidentArticle(article.getIncidentArticle());
							articleCluster.setServusInterface(article.getServusInterface());
							articleCluster.setHidden(article.getHidden());
							articleCluster.setNonAvailable(article.getNonAvailable());
							articleCluster.setShippingAddress(article.getShippingAddress());
							articleCluster.setAssemblingAddress(article.getAssemblingAddress());
							articleCluster.setPoolHandling(article.getPoolHandling());

							articleCluster.setLogCreatedBy(article.getLogCreatedBy());
							articleCluster.setLogCreatedOn(article.getLogCreatedOn());
							articleCluster.setLogUpdatedBy(article.getLogUpdatedBy());
							articleCluster.setLogUpdatedOn(article.getLogUpdatedOn());

							articleCluster.setProperty(article.getProperty());
							articleCluster.setIsPart(0);
							articleCluster.setMasterPartStatus(false);
							articleCluster.setValueTransfer(article.getValueTransfer());

							newArticleClusters.add(articleCluster);

						});
					}

				});

				// persist list if records are there
				if (!newArticleClusters.isEmpty()) {
					log.info("Persisting {} article cluster records " , newArticleClusters.size());
					articleClusterRepo.saveArticlesToCluster(newArticleClusters);
				}
			} else {
				log.info("Currently no cluster is mapped against connection , skip article cluster mapping");
			}

		}, () -> {
			log.info("connectionId record not found");
			throw new ResourceNotFoundException(String.format("connectionId with Id %s not found ", connectionId));
		});

	}

	@AuditLog(action = "delete",entity = "ArticleConnection",functionality = "delete Articles from Connection")
	@Override
	public void removeArticles(Long connectionId, ArticleToConnectionReqDto articles) {
		Optional<Connection> conn = connectionRepo.findById(connectionId);
		conn.ifPresentOrElse(record -> {
			Set<ArticleConnection> articleConnections = record.getArticleConnection();
			if (articleConnections != null && articleConnections.size() != 0) {
				List<ArticleConnection> articleConns = new ArrayList<ArticleConnection>();
				articles.getArticleIds().forEach(articleId -> {
					ArticleConnection articleConn = new ArticleConnection();
					articleConn.setArticleId(articleId);
					articleConn.setConnectionId(connectionId);
					ArticleConnection artConnFromDb = articleConnections.stream()
							.filter(value -> value.equals(articleConn)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(String.format(
									"Article with Id %s not found for Connection Id %s", articleId, connectionId)));
					articleConns.add(artConnFromDb);
				});
				log.info("Removing articles from connection {} ", articleConns);
				articleConnectionRepo.deleteAllInBatch(articleConns);
			} else {
				throw new IllegalArgumentException(
						String.format("No articles is associated with connection with Id %s", connectionId));
			}

		}, () -> {
			log.info("Connection with Id {} not found", connectionId);
			throw new ResourceNotFoundException(String.format("Connection with Id %s not found", connectionId));
		});
	}

	@Override
	public List<ArticleDto> fetchArticles(Long connectionId) {

		log.info("fetching all articles for connection {}", connectionId);
		List<ArticleDto> dtos = new ArrayList<ArticleDto>();
		Optional<Connection> conn = connectionRepo.findById(connectionId);
		conn.ifPresentOrElse(record -> {
			Set<ArticleConnection> articleConnections = record.getArticleConnection();
			articleConnections.stream().forEach(articleConn -> {
				Article article = articleConn.getArticle();
				ArticleDto dto = mapper.map(article, ArticleDto.class);
				if (article.getActive() != 0) {
					dto.setUser(article.getLogCreatedBy());
					dto.setLogCreatedOn(article.getLogCreatedOn());
					dto.setPricePurchase_dollar(article.getPricePurchaseDollar());
					dto.setPricePurchase_euro(article.getPricePurchaseEuro());
					dto.setPriceSales_dollar(article.getPriceSalesDollar());
					dto.setPriceSales_euro(article.getPriceSalesEuro());
					dto.setRemark(article.getDescription());
					dto.setSlaDays(article.getSlaDays());
					dto.setSlaHrs(article.getSlaHours());
					dto.setSlaMin(article.getSlaMinutes());
					dtos.add(dto);
				}

			});

		}, () -> {
			log.info("Connection with Id {} not found", connectionId);
			throw new ResourceNotFoundException(String.format("Connection with Id %s not found", connectionId));
		});
		return dtos;
	}

	@Override
	public void updateArticles(Long connectionId, ArticleToConnectionReqDto articles) {

		log.info("processing update articles request");

		Optional<Connection> record = connectionRepo.findById(connectionId);
		record.ifPresentOrElse(conn -> {
			List<Article> articleRecords = articleRepo.findAllById(articles.getArticleIds());
			log.info("no of rows {}", articleRecords.size());
			if (articleRecords.size() != articles.getArticleIds().size()) {
				throw new IllegalArgumentException("Some of Articles not found");
			}
			log.info("Removing existing articles...");
			Set<ArticleConnection> articleConnections = conn.getArticleConnection();
			if (articleConnections != null && !articleConnections.isEmpty()) {
				articleConnectionRepo.deleteAll(articleConnections);
			}
			List<ArticleConnection> articleConns = new ArrayList<ArticleConnection>();
			articleRecords.forEach((article) -> {
				ArticleConnection artConn = new ArticleConnection();
				artConn.setArticle(article);
				artConn.setConnection(conn);
				artConn.setQuantity(1);
				artConn.setConnectionBcs(conn);
				artConn.setLogCreatedBy(articles.getUser());
				articleConns.add(artConn);
			});
			articleConnectionRepo.saveAll(articleConns);
		}, () -> {
			log.info("connectionId record not found");
			throw new ResourceNotFoundException(String.format("connectionId with Id %s not found ", connectionId));
		});

	}

	@Override
	public List<ArticlePropertyAttributeDto> fetchArticleProperties(Long connectionId) {

		log.info("fetching all articles properties for connection {}", connectionId);
		List<ArticlePropertyAttributeDto> dtos = new ArrayList<ArticlePropertyAttributeDto>();
		Optional<Connection> conn = connectionRepo.findById(connectionId);
		conn.ifPresentOrElse(record -> {
			Set<ArticleConnection> articleConnections = record.getArticleConnection();
			List<Long> articleIds = articleConnections.stream().map(ac -> ac.getArticle().getId()).distinct()
					.collect(Collectors.toList());
			List<ArticleI18NName> articleNamesList = articleI18NRepository.findByArticleIds(articleIds);
			Map<Long, List<ArticleI18NName>> articleNameMap = articleNamesList.stream()
					.collect(Collectors.groupingBy(i18n -> i18n.getArticle().getId()));
			articleConnections.stream().forEach(articleConn -> {
				ArticlePropertyAttributeDto dto = mapper.map(articleConn, ArticlePropertyAttributeDto.class);
				Article article = articleConn.getArticle();
				dto.setArticleId(article.getId());
				dto.setRemark(article.getDescription());
				dto.setName(article.getName());
				dto.setBcsId(articleConn.getConnectionBcs().getId());
				List<ArticleI18NName> articleNameList = articleNameMap.get(article.getId());

				if (articleNameList != null) {
					for (ArticleI18NName nameEntry : articleNameList) {
						if ("en".equalsIgnoreCase(nameEntry.getLanguageId())) {
							dto.setNameEnglish(nameEntry.getTranslation());
						} else if ("de".equalsIgnoreCase(nameEntry.getLanguageId())) {
							dto.setNameGerman(nameEntry.getTranslation());
						}
					}
				}
				dtos.add(dto);
			});

		}, () -> {
			log.info("Connection with Id {} not found", connectionId);
			throw new ResourceNotFoundException(String.format("Connection with Id %s not found", connectionId));
		});
		return dtos;
	}

	@Override
	public List<ArticlePropertiesDto> fetchArticlePropertiesInsert(Long connectionId, Long clusterId) {
		log.info("fetching article properties with connection id : {}, cluster Id:{}", connectionId, clusterId);
		Optional<ClusterItem> clusterRecord = this.clusterRepo.findById(clusterId);
		Currency articleCurrency = ((ClusterItem) clusterRecord.get()).getArticleCurrency();
		Optional<Connection> conn = this.connectionRepo.findById(connectionId);
		List<ArticlePropertiesDto> articleClusterDtoList = new ArrayList<ArticlePropertiesDto>();
		Set<ArticlePropertiesDto> articleClusterDtoList1 = new HashSet<ArticlePropertiesDto>();
		HashMap<Long, List<ArticleConnectionMappingDto>> mapSub = new HashMap<Long, List<ArticleConnectionMappingDto>>();
		Gson gson = new Gson();
		conn.ifPresentOrElse((connectionRecord) -> {

			Set<ArticleConnection> articleConnections = connectionRecord.getArticleConnection();
			articleConnections.stream().forEach((articleConn) -> {
				Article article = articleConn.getArticle();
				String jsonValue = null;
				List<ArticleCluster> articles = this.articleClusterRepo.findByArticleCluster(article.getId(),
						clusterId);
				Iterator<ArticleCluster> var10 = articles.iterator();
				while (true) {
					while (var10.hasNext()) {
						ArticleCluster articleClusterRecord = (ArticleCluster) var10.next();
						Set<PartlistClusterSubarticle> subData = articleClusterRecord.getPartlistClusterArticle();
						if (subData.isEmpty()) {
							log.info("No subarticle found" + articleClusterRecord.getId());
							ArticlePropertiesDto nonSubArticleDto = new ArticlePropertiesDto();
							nonSubArticleDto.setArticleClusterId(articleClusterRecord.getId());
							nonSubArticleDto.setName(articleClusterRecord.getName());
							nonSubArticleDto.setSingle(articleClusterRecord.getSingleArticle());
							nonSubArticleDto.setArticleClearingType(articleClusterRecord.getArticleClearingType());
							nonSubArticleDto.setAdditionalInfo(articleClusterRecord.getValueDefault());
							nonSubArticleDto.setReadOnly(articleClusterRecord.getValueReadOnly());
							nonSubArticleDto.setRemark(articleClusterRecord.getDescription());
							if (articleClusterRecord.getArticleWizardType() != null) {
								nonSubArticleDto
										.setArticleWizardType(articleClusterRecord.getArticleWizardType().getName());
							}

							if (articleCurrency.getId() == 1L) {
								nonSubArticleDto.setPuPrice(articleClusterRecord.getPricePurchaseEuro());
								nonSubArticleDto.setSaPrice(articleClusterRecord.getPriceSalesEuro());
								nonSubArticleDto.setCurrency("EURO");
							} else {
								nonSubArticleDto.setPuPrice(articleClusterRecord.getPricePurchaseDollar());
								nonSubArticleDto.setSaPrice(articleClusterRecord.getPriceSalesDollar());
								nonSubArticleDto.setCurrency("DOLLAR");
							}

							nonSubArticleDto.setObligatory(articleConn.getObligatory());
							nonSubArticleDto.setSubscriberSwap(articleConn.getSubscriberSwap());
							
							nonSubArticleDto.setAlwaysDelete(articleConn.getAlwaysDelete());
							nonSubArticleDto.setAlwaysInsert(articleConn.getAlwaysInsert());
							nonSubArticleDto.setAlwaysMove(articleConn.getAlwaysMove());
							nonSubArticleDto.setQuantity(articleConn.getQuantity());

							///
							nonSubArticleDto
									.setShippingReq(articleClusterRecord.getShippingAddress() == 1 ? true : false);
							nonSubArticleDto
									.setAssemblyReq(articleClusterRecord.getAssemblingAddress() == 1 ? true : false);
							///

							articleClusterDtoList.add(nonSubArticleDto);
							jsonValue = gson.toJson(articleClusterDtoList);
							log.info("json without partlist articles" + jsonValue.toString());
						} else {
							log.info("subarticle found {}", articleClusterRecord.getId());
							Iterator<PartlistClusterSubarticle> var13 = subData.iterator();

							while (var13.hasNext()) {
								PartlistClusterSubarticle subArticles = (PartlistClusterSubarticle) var13.next();
								ArticleConnectionMappingDto mapping = new ArticleConnectionMappingDto(
										articleClusterRecord.getId(), articleConn);
								if (mapSub.containsKey(subArticles.getSubArticles().getId())) {
									((List) mapSub.get(subArticles.getSubArticles().getId())).add(mapping);
								} else {
									List<ArticleConnectionMappingDto> list = new ArrayList<ArticleConnectionMappingDto>();
									list.add(mapping);
									mapSub.put(subArticles.getSubArticles().getId(), list);
								}
							}
						}
					}

					return;
				}
			});
			log.info("final created map is {}", mapSub.keySet());
			Set<Long> keys = mapSub.keySet();
			Iterator<Long> var10 = keys.iterator();
			while (var10.hasNext()) {
				Long key = (Long) var10.next();
				ArticlePropertiesDto subArticleDto = new ArticlePropertiesDto();
				PropertyDto property = new PropertyDto();
				List<ArticleConnectionMappingDto> listMappings = (List) mapSub.get(key);
				Iterator<ArticleConnectionMappingDto> var15 = listMappings.iterator();

				while (var15.hasNext()) {
					ArticleConnectionMappingDto mapping = (ArticleConnectionMappingDto) var15.next();
					ArticleConnection articleConnectionRecord = mapping.getArticleConn();
					log.info("listed key is {} and article connection is {}", key, articleConnectionRecord);
					Optional<ArticleCluster> dataList = this.articleClusterRepo.findById(key);
					subArticleDto.setArticleClusterId(key);
					subArticleDto.setName(((ArticleCluster) dataList.get()).getName());
					subArticleDto.setSingle(((ArticleCluster) dataList.get()).getSingleArticle());
					subArticleDto.setArticleClearingType(((ArticleCluster) dataList.get()).getArticleClearingType());
					subArticleDto.setAdditionalInfo(((ArticleCluster) dataList.get()).getValueDefault());
					subArticleDto.setReadOnly(((ArticleCluster) dataList.get()).getValueReadOnly());
					subArticleDto.setRemark(((ArticleCluster) dataList.get()).getDescription());
					subArticleDto.setSingle(((ArticleCluster) dataList.get()).getSingleArticle());
					subArticleDto.setObligatory(articleConnectionRecord.getObligatory());
					subArticleDto.setSubscriberSwap(articleConnectionRecord.getSubscriberSwap());
					
					subArticleDto.setAlwaysDelete(articleConnectionRecord.getAlwaysDelete());
					subArticleDto.setAlwaysInsert(articleConnectionRecord.getAlwaysInsert());
					subArticleDto.setAlwaysMove(articleConnectionRecord.getAlwaysMove());
					subArticleDto.setQuantity(articleConnectionRecord.getQuantity());
					if (((ArticleCluster) dataList.get()).getArticleWizardType() != null) {
						subArticleDto.setArticleWizardType(
								((ArticleCluster) dataList.get()).getArticleWizardType().getName());
					}

					if (articleCurrency.getId() == 1L) {
						subArticleDto.setPuPrice(((ArticleCluster) dataList.get()).getPricePurchaseEuro());
						subArticleDto.setSaPrice(((ArticleCluster) dataList.get()).getPriceSalesEuro());
						subArticleDto.setCurrency("EURO");
					} else {
						subArticleDto.setPuPrice(((ArticleCluster) dataList.get()).getPricePurchaseDollar());
						subArticleDto.setSaPrice(((ArticleCluster) dataList.get()).getPriceSalesDollar());
						subArticleDto.setCurrency("DOLLAR");
					}

					///
					subArticleDto
							.setShippingReq(((ArticleCluster) dataList.get()).getShippingAddress() == 1 ? true : false);
					subArticleDto.setAssemblyReq(
							((ArticleCluster) dataList.get()).getAssemblingAddress() == 1 ? true : false);
					///

					articleClusterDtoList1.clear();
					Optional<ArticleCluster> dataList1 = this.articleClusterRepo.findById(mapping.getArticleId());
					if (dataList1.isPresent()) {
						ArticlePropertiesDto articleClusterDto1 = new ArticlePropertiesDto();
						articleClusterDto1.setArticleClusterId(((ArticleCluster) dataList1.get()).getId());
						articleClusterDto1.setName(((ArticleCluster) dataList1.get()).getName());
						articleClusterDto1.setSingle(((ArticleCluster) dataList1.get()).getSingleArticle());
						articleClusterDto1
								.setArticleClearingType(((ArticleCluster) dataList1.get()).getArticleClearingType());
						articleClusterDto1.setAdditionalInfo(((ArticleCluster) dataList1.get()).getValueDefault());
						articleClusterDto1.setReadOnly(((ArticleCluster) dataList1.get()).getValueReadOnly());
						articleClusterDto1.setRemark(((ArticleCluster) dataList1.get()).getDescription());
						articleClusterDto1.setSingle(((ArticleCluster) dataList1.get()).getSingleArticle());

						///
						articleClusterDto1.setShippingReq(
								((ArticleCluster) dataList1.get()).getShippingAddress() == 1 ? true : false);
						articleClusterDto1.setAssemblyReq(
								((ArticleCluster) dataList1.get()).getAssemblingAddress() == 1 ? true : false);
						///
						articleClusterDto1.setObligatory(mapping.getArticleConn().getObligatory());
						articleClusterDto1.setSubscriberSwap(mapping.getArticleConn().getSubscriberSwap());
						
						articleClusterDto1.setAlwaysDelete(mapping.getArticleConn().getAlwaysDelete());
						articleClusterDto1.setAlwaysInsert(mapping.getArticleConn().getAlwaysInsert());
						articleClusterDto1.setAlwaysMove(mapping.getArticleConn().getAlwaysMove());
						articleClusterDto1.setQuantity(mapping.getArticleConn().getQuantity());
						articleClusterDto1.setQuantity(mapping.getArticleConn().getQuantity());
						if (((ArticleCluster) dataList1.get()).getArticleWizardType() != null) {
							articleClusterDto1.setArticleWizardType(
									((ArticleCluster) dataList1.get()).getArticleWizardType().getName());
						}

						if (articleCurrency.getId() == 1L) {
							articleClusterDto1.setPuPrice(((ArticleCluster) dataList1.get()).getPricePurchaseEuro());
							articleClusterDto1.setSaPrice(((ArticleCluster) dataList1.get()).getPriceSalesEuro());
							articleClusterDto1.setCurrency("EURO");
						} else {
							articleClusterDto1.setPuPrice(((ArticleCluster) dataList1.get()).getPricePurchaseDollar());
							articleClusterDto1.setSaPrice(((ArticleCluster) dataList1.get()).getPriceSalesDollar());
							articleClusterDto1.setCurrency("DOLLAR");
						}

						PartsPropertyEnum propertyEnum = ((ArticleCluster) dataList1.get()).getProperty();
						if (propertyEnum != null) {
							switch (propertyEnum.ordinal()) {
							case 1:
								property.setInsert(articleClusterDto1);
								break;
							case 2:
								property.setChange(articleClusterDto1);
								break;
							case 3:
								property.setDelete(articleClusterDto1);
								break;
							case 4:
								property.setNone(articleClusterDto1);
							}
						}
					}
				}
				subArticleDto.setPartProperties(property);

				articleClusterDtoList.add(subArticleDto);
				String jsonValue = gson.toJson(articleClusterDtoList);
				log.info("Complete json string for article and subarticle: " + jsonValue);
			}

		}, () -> {
			log.info("Connection with Id {} not found", connectionId);
			throw new ResourceNotFoundException(String.format("Connection with Id %s not found", connectionId));
		});
		return articleClusterDtoList;
	}

	@Override
	public void updateArticleProperties(Long connectionId, ArticlePropertiesReqDto articles) {

		log.info("updating all articles properties for connection {}", connectionId);
		List<ArticlePropertyAttributeDto> articlePorperties = articles.getArticleProperties();
		Optional<Connection> conn = connectionRepo.findById(connectionId);
		conn.ifPresentOrElse(record -> {
			Set<ArticleConnection> articleConnections = record.getArticleConnection();
			List<ArticleConnection> articleConns = new ArrayList<ArticleConnection>();
			articlePorperties.forEach(articleProp -> {
				ArticleConnection articleConn = articleConnections.stream()
						.filter(value -> value.getArticle().getId() == articleProp.getArticleId()).findFirst()
						.orElseThrow(() -> new IllegalArgumentException(
								String.format("Article with Id %s not found for Connection Id %s",
										articleProp.getArticleId(), connectionId)));
				articleConn.setObligatory(articleProp.getObligatory());
				articleConn.setAlwaysInsert(articleProp.getAlwaysInsert());
				articleConn.setAlwaysMove(articleProp.getAlwaysMove());
				articleConn.setAlwaysDelete(articleProp.getAlwaysDelete());
				articleConn.setSubscriberSwap(articleProp.getSubscriberSwap());
				
				articleConn.setQuantity(articleProp.getQuantity());
				Connection connect = new Connection();
				connect.setId(articleProp.getBcsId());
				articleConn.setConnectionBcs(connect);
				articleConns.add(articleConn);
			});
			articleConnectionRepo.updateArticleProperties(connectionId,articlePorperties,articleConns);

		}, () -> {
			log.info("Connection with Id {} not found", connectionId);
			throw new ResourceNotFoundException(String.format("Connection with Id %s not found", connectionId));
		});
	}

	
	@Override
	public List<ClusterDto> fetchClustersByConnection(long connectionId) {
		log.info("fetching clusters for connection {}", connectionId);
		List<ClusterDto> dtos = new ArrayList<ClusterDto>();
		Optional<Connection> record = connectionRepo.findById(connectionId);
		record.ifPresentOrElse(value -> {
			Set<ClusterConnection> clusterConnections = value.getClusterConnection();
			clusterConnections.stream().forEach(clusterConn -> {
				ClusterItem cluster = clusterConn.getCluster();
				if (cluster.getActive() != 0) {
					ClusterDto dto = mapper.map(cluster, ClusterDto.class);
					dto.setName(cluster.getName());
					dto.setRemark(cluster.getRemark());
					dtos.add(dto);
				}
			});

		}, () -> {
			log.info("connection record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", connectionId));
		});
		return dtos;
	}

	@Override
	public void addClustersToConnection(Long connectionId, ClustersToConnectionDto clusters) {

		log.info("request received for adding clusters to connection");
		Optional<Connection> connection = connectionRepo.findById(connectionId);
		connection.ifPresentOrElse(connections -> {
			List<ClusterItem> clusterRecords = clusterRepo.findAllById(clusters.getClusterIds());
			log.info("no of rows available {}", clusterRecords.size());

			if (clusterRecords.size() != clusters.getClusterIds().size()) {
				throw new IllegalArgumentException("Some of the clusters not found");
			}

			List<ClusterConnection> clusterConnection = new ArrayList<ClusterConnection>();

			clusterRecords.forEach((clusterData) -> {
				ClusterConnection clustConnReg = new ClusterConnection();

				clustConnReg.setCluster(clusterData);
				clustConnReg.setConnection(connections);
				clusterConnection.add(clustConnReg);
				clusterData.setLogUpdatedBy(clusters.getUser());
				clusterData.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			});
			clusterConnRepo.saveClustersToConnection(clusterConnection);
			
			// logic for saving the master article data in Article cluster with slave
			// article
			/*
			clusterRecords.forEach((clusterDetai1) -> {
				List<ArticleDto> artDto = fetchArticles(connectionId);
				artDto.forEach((myData) -> {
					ArticleClusterDetailDto masterArticleDto = new ArticleClusterDetailDto();
					List<Long> listData = new ArrayList<Long>();
					listData.add(myData.getId());
					masterArticleDto.setArticleIds(listData);
					masterArticleDto.setUser(clusters.getUser());

					clusterService.addArticlesToClusterOnConnMapping(clusterDetai1.getId(), masterArticleDto);

				});

			});
			clusterRepo.saveAll(clusterRecords);
			*/
		}, () -> {
			log.info("connection id not found");
			throw new ResourceNotFoundException(
					String.format("clusters with connection Id %s not found ", connectionId));
		});

	}

	@AuditLog(action = "delete",entity = "ClusterConnection",functionality = "delete Clusters From Connection")
	@Override
	public void deleteClustersFromConnection(Long connectionId, ClustersToConnectionDto clusters) {

		Optional<Connection> connectionRecord = connectionRepo.findById(connectionId);
		connectionRecord.ifPresentOrElse(connections -> {
			Set<ClusterConnection> clusterConnectionRecord = connections.getClusterConnection();
			if (clusterConnectionRecord != null && clusterConnectionRecord.size() != 0) {
				List<ClusterConnection> clusterConnectionList = new ArrayList<ClusterConnection>();
				List<ClusterItem> clusterItemList = new ArrayList<ClusterItem>();
				clusters.getClusterIds().forEach(clusterId -> {
					ClusterConnection clusterConnectionData = new ClusterConnection();
					ClusterItem clusterDetails = new ClusterItem();
					clusterDetails.setId(clusterId);
					clusterConnectionData.setCluster(clusterDetails);
					clusterConnectionData.setConnection(connections);
					ClusterConnectionId ClusterConnectionId = new ClusterConnectionId(connectionId, clusterId);
					clusterConnectionData.setId(ClusterConnectionId);

					ClusterConnection clusterFromdb = clusterConnectionRecord.stream()
							.filter(value -> value.getCluster().getId().equals(clusterConnectionData.getCluster().getId())).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(String.format(
									"Clusters with Id %s not found for connection Id %s", clusterId, connectionId)));
					ClusterItem cluster = clusterFromdb.getCluster();
					cluster.setLogUpdatedBy(clusters.getUser());
					cluster.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
					clusterItemList.add(cluster);
					clusterConnectionList.add(clusterFromdb);

				});

				log.info("Removing clusters from connections {} ", clusterConnectionList);
				clusterConnRepo.deleteAllInBatch(clusterConnectionList);
				clusterRepo.saveAll(clusterItemList);

			} else {
				throw new IllegalArgumentException(
						String.format("No cluster is associated with connection with Id %s", connectionId));
			}

		}, () -> {
			log.info("connection with Id {} not found", connectionId);
			throw new ResourceNotFoundException(String.format("connection with Id %s not found", connectionId));
		});
	}


	public List<ArticlePropertiesDto> getArticlesAsPoolViewForSite(Long siteId, Long connectionId, String languageId) {
		//Map<PoolDto, List<ArticlePropertiesDto>> articlesAsPoolView = new HashMap<PoolDto, List<ArticlePropertiesDto>>();
		List<ArticlePropertiesDto> articleListInPool = new ArrayList<ArticlePropertiesDto>();
		Set<PoolDto> listOfPools = siteService.getPoolsForSite(siteId);
		log.info("size listOfPools: " + listOfPools.size()); //5
		log.info("languageId: " + languageId);
		List<PoolDto> listPool = new ArrayList<>(listOfPools);
		Site site = siteService.getSiteById(siteId);
		
		//TODO: Populate site_i18n_name table first before using this code
		/*Set<SiteI18NName> siteKey1 = site.getSiteI18NName();
		for(SiteI18NName siteName : siteKey1) {
			if(siteName.getLanguageId().equals(languageId)) {
				siteKey = siteName.getTranslation();
			}
		}*/
		
		String siteKey = site.getName(); //TODO: i18n
		
		//ClusterKey
		ClusterItem clusterItem = site.getClusterItem();
		String clusterKey = clusterItem.getName(); //TODO: i18n
		
		/*Set<ClusterI18NName> clusterI18nNames = clusterItem.getClusterI18NName();
		for (ClusterI18NName clustName : clusterI18nNames) {
			if (clustName.getLanguageId().equals(languageId)) {
				clusterKey = clustName.getTranslation(); // Overriding with i18n translated name
			}
		}*/
		 
		
		Currency siteCurrency = clusterItem.getArticleCurrency();
		
		log.info("siteCurrency: " + siteCurrency);//DOLLAR
		Optional<Connection> connection = connectionRepo.findById(connectionId);
		log.info("connection: " + connection.get().getName());//murexconnection34
		Set<ArticleConnection> articleConnections = null;
		if(connection.isPresent() && connection.get().getArticleConnection() != null) {
			articleConnections = connection.get().getArticleConnection(); 
			log.info("articleConnections size: " + articleConnections.size());//7
		} else {
			log.info("Connection with Id {} not found", connectionId);
	        throw new ResourceNotFoundException(String.format("Connection with Id %s not found", connectionId));
		}
		//mycode
		List<Long> allArticleIds = new ArrayList<>();
		Map<Long, Pool> poolMapById = new HashMap<>();

		for (PoolDto eachPool : listOfPools) {
			Optional<Pool> poolDb = poolRepo.findById(eachPool.getId());
			if (poolDb.isPresent() && poolDb.get().getArticlePool() != null) {
				Pool pool = poolDb.get();
				poolMapById.put(pool.getId(), pool);
				for (ArticlePool articlePool : pool.getArticlePool()) {
					if (articlePool.getArticle() != null && articlePool.getArticle().getArticle() != null) {
						Long articleId = articlePool.getArticle().getArticle().getId();
						if (!allArticleIds.contains(articleId)) {
							allArticleIds.add(articleId);
						}
					}
				}
			}
		}
		List<ArticleClusterI18NName> i18nNames = artClusterI18NRepo.findByArticleIds(allArticleIds);
		Map<Long, List<ArticleClusterI18NName>> articleNameMap = i18nNames.stream()
				.collect(Collectors.groupingBy(name -> name.getArticle().getId()));
		
		//end
		if(listOfPools != null && !listOfPools.isEmpty()) {
			for(PoolDto eachPool : listOfPools) {
				Optional<Pool> poolDb = poolRepo.findById(eachPool.getId());
				log.info("Getting articles for pool {}", eachPool.getId()); //4
				
				if(poolDb.isPresent() && poolDb.get().getArticlePool() != null) {
					Set<ArticlePool> listOfArticlePool = poolDb.get().getArticlePool();
					log.info("listOfArticlePool size: " + listOfArticlePool.size());//222,278
					
					for(ArticlePool articlePoolRecord : listOfArticlePool) {
						log.info("For loop 1: articlePoolRecord " + articlePoolRecord);
						for(ArticleConnection articleConnectionRecord : articleConnections) {
							log.info("For loop 2: articleConnectionRecord " + articleConnectionRecord);
							if(articleConnectionRecord.getArticleId() == articlePoolRecord.getArticle().getArticle().getId()) {
								ArticleCluster articleClusterRecord = articlePoolRecord.getArticle();
								//Set<ArticleI18NName> articleI18nName = articleClusterRecord.getArticle().getArticleI18NName();
								//String articleKey = articleClusterRecord.getName();
								//for(ArticleI18NName articleName : articleI18nName) {
								//	if(articleName.getLanguageId().equals(languageId)) {
								//		articleKey = articleName.getTranslation(); //Overriding with i18n translated name
								//	}
							//	}
								//my code
								List<ArticleClusterI18NName> articleNameList = articleNameMap.get(articleClusterRecord.getId());
								//end
								
								ArticlePropertiesDto articleInPool = new ArticlePropertiesDto();
								articleInPool.setArticleClusterId(articleClusterRecord.getId());
			                    //articleInPool.setName(articleKey);
								articleInPool.setName(articleClusterRecord.getName());
								if (articleNameList != null) {
									for (ArticleClusterI18NName nameEntry : articleNameList) {
										if ("en".equalsIgnoreCase(nameEntry.getLanguageId())) {
											articleInPool.setNameEnglish(nameEntry.getTranslation());
										} else if ("de".equalsIgnoreCase(nameEntry.getLanguageId())) {
											articleInPool.setNameGerman(nameEntry.getTranslation());
										}
									}
								}
			                    articleInPool.setSingle(articleClusterRecord.getSingleArticle());
			                    articleInPool.setArticleClearingType(ArticleClearingTypeEnum.POOL);
			                    articleInPool.setAdditionalInfo(articleClusterRecord.getValueDefault());
			                    articleInPool.setReadOnly(articleClusterRecord.getValueReadOnly());
			                    articleInPool.setRemark(articleClusterRecord.getDescription());
			                    if (articleClusterRecord.getArticleWizardType() != null) {
			                       articleInPool.setArticleWizardType(articleClusterRecord.getArticleWizardType().getName());
			                    }

			                    if (siteCurrency.getId() == 1L) {
			                       articleInPool.setPuPrice(articleClusterRecord.getPricePurchaseEuro());
			                       articleInPool.setSaPrice(articleClusterRecord.getPriceSalesEuro());
			                       articleInPool.setCurrency("EURO");
			                    } else {
			                       articleInPool.setPuPrice(articleClusterRecord.getPricePurchaseDollar());
			                       articleInPool.setSaPrice(articleClusterRecord.getPriceSalesDollar());
			                       articleInPool.setCurrency("DOLLAR");
			                    }

			                    articleInPool.setObligatory(articleConnectionRecord.getObligatory());
			                    articleInPool.setSubscriberSwap(articleConnectionRecord.getSubscriberSwap());
			                    
			                    articleInPool.setAlwaysDelete(articleConnectionRecord.getAlwaysDelete());
			                    articleInPool.setAlwaysInsert(articleConnectionRecord.getAlwaysInsert());
			                    articleInPool.setAlwaysMove(articleConnectionRecord.getAlwaysMove());
			                    articleInPool.setQuantity(articleConnectionRecord.getQuantity());
			                    articleInPool.setReserved(articlePoolRecord.getReserved());
			                    articleInPool.setAvailable(articlePoolRecord.getAvailable());
			                    
			                    articleInPool.setPoolId(poolDb.get().getId());
			                    articleInPool.setPoolKey(poolDb.get().getName());
			                    articleInPool.setClusterKey(clusterKey);
			                    articleInPool.setSiteKey(siteKey);
			                    
			                    ///
			                    articleInPool.setShippingReq(articleClusterRecord.getShippingAddress() == 1 ? true : false);
			                    articleInPool.setAssemblyReq(articleClusterRecord.getAssemblingAddress() == 1 ? true : false);
			                    ///
			                    
			                    articleListInPool.add(articleInPool);
			                    break;
							}
						}
					}
				} else {
					log.info("pool record not found");
					throw new ResourceNotFoundException(String.format("Pool with Id %s not found ", eachPool.getId()));
				}
				/*
				 * if(!articleListInPool.isEmpty()) { articlesAsPoolView.put(eachPool,
				 * articleListInPool); }
				 */
			}
		} else {
			log.info("No active pools found for site {}", siteId);
			return null;
		}
		return articleListInPool;
	}
	
	@Override
	public List<ArticlePropertiesDto> getArticlesByConnectionId(Long connectionId, Long clusterId, AMSPUser user) {
		List<ArticlePropertiesDto> articles = new ArrayList<ArticlePropertiesDto>();
		Optional<Connection> conn = this.connectionRepo.findById(connectionId);
		List<Long> subArticleIds = Lists.newArrayList();

		conn.ifPresentOrElse(connection -> {

			Set<ArticleConnection> articleConnections = connection.getArticleConnection();

			Map<Long, ArticleConnection> articleIdVsArticleConnMap = new HashMap<>();
			articleConnections.forEach(articleConnection -> {
				articleIdVsArticleConnMap.put(articleConnection.getArticleId(), articleConnection);
			});

			List<Long> articleIds = articleConnections.stream().map(ArticleConnection::getArticleId)
					.collect(Collectors.toList());
			List<ArticleCluster> articleClusters = articleClusterRepo.fetchArticleClusters(articleIds, clusterId);
			log.info("Total ArticleClusterRecords {}", articleClusters.size());
			
			List<Long> allClusterIds = new ArrayList<>();
			for (ArticleCluster articleCluster : articleClusters) {
				allClusterIds.add(articleCluster.getId());
				for (PartlistClusterSubarticle partList : articleCluster.getPartlistClusterSubarticle()) {
					if (partList.getSubArticles() != null) {
						allClusterIds.add(partList.getSubArticles().getId());
					}
				}
			}

			List<ArticleClusterI18NName> i18nNames = artClusterI18NRepo.findByArticleClusterIds(allClusterIds);
			Map<Long, List<ArticleClusterI18NName>> articleNameMap = i18nNames.stream()
				.collect(Collectors.groupingBy(i18n -> i18n.getArticle().getId()));
			articleClusters.stream().forEach(articleCluster -> {

				Set<PartlistClusterSubarticle> partListArticleClusters = articleCluster.getPartlistClusterSubarticle();
				Currency articleCurrency = articleCluster.getClusterItem().getArticleCurrency();
				ArticleConnection articleConnection = articleIdVsArticleConnMap
						.get(articleCluster.getArticle().getId());
				//String translatedName = getTranslatedName.apply(articleCluster.getId());
				//if (translatedName != null) {
				//	articleCluster.setName(translatedName);
				//}
				// no subArticles for given article
				if (partListArticleClusters.isEmpty()) {

					ArticlePropertiesDto article = mapArticleClusterToArticle(articleCluster, articleCurrency,
							articleConnection,articleNameMap);
					articles.add(article);

				} else {

					// main article will be populated as part properties
					ArticlePropertiesDto mainArticle = mapArticleClusterToArticle(articleCluster, articleCurrency,
							articleConnection,articleNameMap);
					partListArticleClusters.forEach(partListArticleCluster -> {

						boolean subArticleAlreadyExists = false;
						PropertyDto property = new PropertyDto();
						ArticleCluster subArticleClusterRcd = partListArticleCluster.getSubArticles(); // get sub article	record
						
					//	String subTranslatedName = getTranslatedName.apply(subArticleClusterRcd.getId());
					//	if (subTranslatedName != null) {
					//		subArticleClusterRcd.setName(subTranslatedName);
					//	}
						
						ArticlePropertiesDto subArticle = mapArticleClusterToArticle(subArticleClusterRcd,
								articleCurrency, articleConnection,articleNameMap);

						for (ArticlePropertiesDto article : articles) {
							// SubArticle is already there so instead of creating/returning it as new record
							// use same
							if (article.getArticleClusterId() == subArticle.getArticleClusterId()) {
								if (article.getPartProperties() != null) {
									log.info("subarticle already exists {}-{}", subArticle.getArticleClusterId(),
											article.getArticleClusterId());
									property = article.getPartProperties();
									subArticleAlreadyExists = true;
									break;
								}
							}
						}
						;

						PartsPropertyEnum propertyEnum = articleCluster.getProperty();
						if (propertyEnum != null) {
							switch (propertyEnum.ordinal()) {
							case 1:
								property.setInsert(mainArticle);
								break;
							case 2:
								property.setChange(mainArticle);
								break;
							case 3:
								property.setDelete(mainArticle);
								break;
							case 4:
								property.setNone(mainArticle);
							}
						}

						subArticleIds.add(subArticle.getArticleClusterId());

						if (!subArticleAlreadyExists) {
							// set main article as part properties to sub article
							subArticle.setPartProperties(property);
							articles.add(subArticle);
						}

					});

				}

			});

		}, () -> {
			log.info("Connection with Id {} not available", connectionId);
		});

		// Remove individual sub articles records from list those are present in
		// partList
		List<ArticlePropertiesDto> filteredArticles = new ArrayList<>();
		if (!articles.isEmpty() && !subArticleIds.isEmpty()) {

			filteredArticles = articles.stream().filter(article -> {
				if (article.getPartProperties() == null && subArticleIds.contains(article.getArticleClusterId())) {
					log.info("Removing record {} since it is also present as partList ", article);
					return false;
				}
				return true;
			}).collect(Collectors.toList());

			return filteredArticles;
		}

		return articles;
	}

	private ArticlePropertiesDto mapArticleClusterToArticle(ArticleCluster articleCluster, Currency articleCurrency,
			ArticleConnection articleConnection, Map<Long, List<ArticleClusterI18NName>> articleNameMap) {

		ArticlePropertiesDto article = new ArticlePropertiesDto();
		article.setArticleClusterId(articleCluster.getId());
		article.setName(articleCluster.getName());
		List<ArticleClusterI18NName> articleNameList = articleNameMap.get(articleCluster.getId());

		if (articleNameList != null) {
			for (ArticleClusterI18NName nameEntry : articleNameList) {
				if ("en".equalsIgnoreCase(nameEntry.getLanguageId())) {
					article.setNameEnglish(nameEntry.getTranslation());
				} else if ("de".equalsIgnoreCase(nameEntry.getLanguageId())) {
					article.setNameGerman(nameEntry.getTranslation());
				}
			}
		}
		article.setSingle(articleCluster.getSingleArticle());
		article.setArticleClearingType(articleCluster.getArticleClearingType());
		article.setAdditionalInfo(articleCluster.getValueDefault());
		article.setReadOnly(articleCluster.getValueReadOnly());
		article.setRemark(articleCluster.getDescription());
		if (articleCluster.getArticleWizardType() != null) {
			article.setArticleWizardType(articleCluster.getArticleWizardType().getName());
		}

		if (articleCurrency.getId() == 1L) {
			article.setPuPrice(articleCluster.getPricePurchaseEuro());
			article.setSaPrice(articleCluster.getPriceSalesEuro());
			article.setCurrency("EURO");
		} else {
			article.setPuPrice(articleCluster.getPricePurchaseDollar());
			article.setSaPrice(articleCluster.getPriceSalesDollar());
			article.setCurrency("DOLLAR");
		}

		article.setObligatory(articleConnection.getObligatory());
		article.setSubscriberSwap(articleConnection.getSubscriberSwap());
		
		article.setAlwaysDelete(articleConnection.getAlwaysDelete());
		article.setAlwaysInsert(articleConnection.getAlwaysInsert());
		article.setAlwaysMove(articleConnection.getAlwaysMove());
		article.setQuantity(articleConnection.getQuantity());

		article.setShippingReq(articleCluster.getShippingAddress() == 1 ? true : false);
		article.setAssemblyReq(articleCluster.getAssemblingAddress() == 1 ? true : false);

		return article;
	}

}
