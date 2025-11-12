package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.ArticlePoolInventoryHistory;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.domain.PoolOperationTypeEnum;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.PoolDto;
import com.avaya.amsp.masterdata.dtos.PoolToArticleDto;
import com.avaya.amsp.masterdata.dtos.SiteToPoolDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolInvHistoryRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolRepository;
import com.avaya.amsp.masterdata.repo.PoolRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.service.iface.PoolServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PoolService implements PoolServiceIface {

	@Autowired
	private PoolRepository poolRepo;

	@Autowired
	private ArticleClusterRepository articleClusterRepo;

	@Autowired
	private ArticlePoolRepository articlePoolRepo;
	
	@Autowired
	private ArticlePoolInvHistoryRepository articlePoolInvHistRepo;

	@Autowired
	SiteService siteService;

	@Autowired
	SiteRepository siteRepo;

	@Autowired
	ModelMapper mapper;

	@Override
	public List<PoolDto> fetchAllPools() {
		log.info("fetching available pools");

		List<PoolDto> poolDtoList = new ArrayList<>();
		List<Pool> poolData = poolRepo.findByActive(1L);

		if (poolData != null && !poolData.isEmpty()) {
			poolData.forEach(pool -> {
				PoolDto dto = new PoolDto();

				dto.setId(pool.getId());
				dto.setName(pool.getName());
				dto.setContract(pool.getContract());
				dto.setRemark(pool.getDescription());
				dto.setResponsibleTitle(pool.getResponsibleTitle());
				dto.setResponsibleFirstName(pool.getResponsibleFirstName());
				dto.setResponsibleSurname(pool.getResponsibleSurname());
				dto.setResponsibleEmail(pool.getResponsibleEmail());
				dto.setLogCreatedBy(pool.getLogCreatedBy());
				dto.setLogCreatedOn(pool.getLogCreatedOn());
				dto.setLogUpdatedBy(pool.getLogUpdatedBy());
				poolDtoList.add(dto);
			});
		} else {
			log.info("no pools found");
		}
		return poolDtoList;
	}

	@AuditLog(action = "insert",entity = "Pool",functionality = "Add New Pool")
	@Override
	@org.springframework.transaction.annotation.Transactional
	public void createPool(PoolDto dto) {

		// if (fetchPoolByName(dto.getName()).isPresent()) {
		// log.info("pool already exists for given request {} ", dto.getName());
		// throw new ResourceAlreadyExistsException(
		// String.format("pool with type %s is already exists", dto.getName()));
		// }

		Optional<Pool> existingPool = fetchPoolByName(dto.getName());
		Pool entity;
		if (existingPool.isPresent()) {
			entity = existingPool.get();

			if (entity.getActive() != 0) {
				log.info("Pool with name {} already exists and is active.", dto.getName());
				throw new ResourceAlreadyExistsException(
						String.format("Pool with name %s already exists and is active", dto.getName()));
			}
		} else {
			entity = new Pool();
		}
		log.info("adding new pool to database");
		// Pool entity = mapper.map(dto, Pool.class);

		// Pool entity = new Pool();

		entity.setName(dto.getName());
		entity.setContract(dto.getContract());
		entity.setDescription(dto.getRemark());
		entity.setResponsibleTitle(dto.getResponsibleTitle());
		entity.setResponsibleFirstName(dto.getResponsibleFirstName());
		entity.setResponsibleSurname(dto.getResponsibleSurname());
		entity.setResponsibleEmail(dto.getResponsibleEmail());
		entity.setActive(1);
		entity.setLogCreatedBy(dto.getLogCreatedBy());
		entity.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		Optional<Site> siteData = siteRepo.findById(dto.getSiteId());

		siteData.ifPresentOrElse(record -> {
			if (dto.getSiteId() != null) {
				Pool poolRecord = poolRepo.save(entity);
				log.info("added pool record having id {}", poolRecord.getId());

				List<Long> poolList = new ArrayList<Long>();
				SiteToPoolDto siteToPoolDto = new SiteToPoolDto();
				poolList.add(poolRecord.getId());
				siteToPoolDto.setPoolIds(poolList);

				if (poolRecord.getId() != null) {
					siteService.addAssignPools(dto.getSiteId(), siteToPoolDto);
				} else {
					log.error("problem in creating pool entity");
				}
			}

		}, () -> {
			log.info("site with Id {} not found", dto.getSiteId());
			throw new ResourceNotFoundException(String.format("site with Id %s not found", dto.getSiteId()));

		});
	}

	@AuditLog(action = "Update",entity = "Pool",functionality = "Update Existing Pool")
	@Override
	public void updatePool(PoolDto dto) {
		log.info("updating pool record with ID {}", dto.getId());

		Optional<Pool> record = poolRepo.findById(dto.getId());
		record.ifPresentOrElse(pool -> {

			pool.setName(dto.getName());
			pool.setContract(dto.getContract());
			pool.setDescription(dto.getRemark());
			pool.setResponsibleTitle(dto.getResponsibleTitle());
			pool.setResponsibleFirstName(dto.getResponsibleFirstName());
			pool.setResponsibleSurname(dto.getResponsibleSurname());
			pool.setResponsibleEmail(dto.getResponsibleEmail());
			pool.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			pool.setLogUpdatedBy(dto.getLogUpdatedBy());
			pool.setActive(1);
			Pool poolRecord = poolRepo.save(pool);
			log.info("updated pool record having id {}", poolRecord.getId());

			List<Long> poolList = new ArrayList<Long>();
			SiteToPoolDto siteToPoolDto = new SiteToPoolDto();
			poolList.add(poolRecord.getId());
			siteToPoolDto.setPoolIds(poolList);
			siteService.addAssignPools(dto.getSiteId(), siteToPoolDto);
		}, () -> {
			log.info("pool record not found");
			throw new ResourceNotFoundException(String.format("pool with Id %s not found ", dto.getId()));
		});
	}

	@AuditLog(action = "delete",entity = "Pool",functionality = "delete Existing Pool")
	@Override
	public void deletePool(Long poolId) {
		log.info("Removing Pool record with ID {}", poolId);

		Optional<Pool> record = poolRepo.findById(poolId);
		record.ifPresentOrElse(pool -> {
			pool.setActive(0);

			Pool poolRecord = poolRepo.save(pool);
			log.info("deleted pool record having id {}", poolRecord.getId());

		}, () -> {
			log.info("Pool record not found");
			throw new ResourceNotFoundException(String.format("Pool with Id %s not found ", poolId));
		});

	}

	@Override
	public Optional<Pool> fetchPoolByName(String name) {
		log.info("Fetching sites with name {}", name);
		Pool poolName = poolRepo.findByName(name);

		return Optional.ofNullable(poolName);
	}

	@Override
	public void addAssignArticles(Long poolId, PoolToArticleDto articles, AMSPUser amspUser) {

		log.info("request for adding articles to pools");

		Optional<Pool> record = poolRepo.findById(poolId);
		record.ifPresentOrElse(pool -> {
			List<ArticleCluster> articleClusterRecords = articleClusterRepo.findAllById(articles.getArticleIds());

			log.info("no of rows {}", articleClusterRecords.size());
			if (articleClusterRecords.size() != articles.getArticleIds().size()) {
				throw new IllegalArgumentException("Some of articles not found");
			}

			List<ArticlePool> articlePoolRecords = new ArrayList<ArticlePool>();

			articleClusterRecords.forEach((articleClstr) -> {
				ArticlePool articlePool = new ArticlePool();

				articlePool.setArticle(articleClstr);
				articlePool.setPool(pool);

				articlePoolRecords.add(articlePool);
			});

			articlePoolRepo.addArticlesToPool(articlePoolRecords);
			
			auditCurrentOperation(articlePoolRecords, amspUser.getUsername(), PoolOperationTypeEnum.CREATE.name());

		}, () -> {
			log.info("pool not found");
			throw new ResourceNotFoundException(String.format("pool with Id %s not found ", poolId));
		});

	}

	@AuditLog(action = "delete",entity = "ArticlePool",functionality = "delete Articles From Pool")
	@Override
	public void deleteAssignArticles(Long poolId, PoolToArticleDto articles, AMSPUser amspUser) {

		Optional<Pool> poolRecord = poolRepo.findById(poolId);
		poolRecord.ifPresentOrElse(record -> {
			Set<ArticlePool> articlePoolRecord = record.getArticlePool();

			if (articlePoolRecord != null && articlePoolRecord.size() != 0) {
				List<ArticlePool> articlePoolList = new ArrayList<ArticlePool>();
				articles.getArticleIds().forEach(articleid -> {
					ArticlePool articlePool = new ArticlePool();
					articlePool.setArticleId(articleid);
					articlePool.setPoolId(poolId);

					ArticlePool articleFromdb = articlePoolRecord.stream().filter(value -> value.equals(articlePool))
							.findFirst().orElseThrow(() -> new IllegalArgumentException(String
									.format("article with Id %s not found for pool id Id %s", articleid, poolId)));

					articlePoolList.add(articleFromdb);

				});

				log.info("Removing articles from pools {} ", articlePoolList);
				articlePoolRepo.deleteAllInBatch(articlePoolList);
				
				auditCurrentOperation(articlePoolList, amspUser.getUsername(), PoolOperationTypeEnum.DROP.name());

			} else {
				throw new IllegalArgumentException(
						String.format("No article is associated with pool with Id %s", poolId));
			}

		}, () -> {
			log.info("pool with Id {} not found", poolId);
			throw new ResourceNotFoundException(String.format("pool with Id %s not found", poolId));
		});

	}

	public List<ArticleToClusterDto> getArticlesForPool(Long poolId) {
		Optional<Pool> pool = poolRepo.findById(poolId);
		List<ArticleToClusterDto> dtos = new ArrayList<ArticleToClusterDto>();
		pool.ifPresentOrElse(value -> {
			Set<ArticlePool> articlepoolData = value.getArticlePool();
			articlepoolData.stream().forEach(articlePool -> {
				ArticleCluster articleCluster = articlePool.getArticle();
				ClusterItem cluster = articleCluster.getClusterItem();
				String articleCurrency = cluster.getArticleCurrency() != null ? cluster.getArticleCurrency().getName() : "";
				String accCurrency = cluster.getAccCurrencyId() != null ? cluster.getAccCurrencyId().getName() : "";
				
				ArticleToClusterDto dto = mapper.map(articleCluster, ArticleToClusterDto.class);

				dto.setPricePurchase_dollar(articleCluster.getPricePurchaseDollar());
				dto.setPricePurchase_euro(articleCluster.getPriceSalesEuro());
				dto.setPriceSales_dollar(articleCluster.getPriceSalesDollar());
				dto.setPriceSales_euro(articleCluster.getPriceSalesEuro());
				dto.setAvailable(articlePool.getAvailable());
				dto.setReserved(articlePool.getReserved());
				dto.setAccCurrency(accCurrency);
				dto.setArticleCurrency(articleCurrency);
				dtos.add(dto);
			});
		}, () -> {
			log.info("pool record not found");
			throw new ResourceNotFoundException(String.format("cluster with Id %s not found ", poolId));
		});
		return dtos;

	}
	
	
	private void auditCurrentOperation(List<ArticlePool> articlePoolRecords, String userName, String operation) {
		log.info("Auditing article to pool un/assignment");
		for(ArticlePool artPool : articlePoolRecords) {
			ArticlePoolInventoryHistory record = new ArticlePoolInventoryHistory();
			record.setArticle(artPool.getArticle());
			record.setPool(artPool.getPool());
			record.setOperation(operation);
			record.setAvailable(0);
			record.setReserved(0);
			record.setDifference(0);
			record.setReasonText("Un/assigning article " + artPool.getArticle().getName() +  " from pool " + artPool.getPool().getName());
			record.setUserName(userName);
			record.setUpdatedTS(new Timestamp(System.currentTimeMillis()));
			record.setArticleName(artPool.getArticle().getArticle().getName());
			articlePoolInvHistRepo.save(record);
		}
	}

}
