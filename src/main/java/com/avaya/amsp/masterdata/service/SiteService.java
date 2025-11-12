package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.domain.Shipping;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.domain.SiteContractFL;
import com.avaya.amsp.domain.SitePool;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesDto;
import com.avaya.amsp.masterdata.dtos.ContractFlDto;
import com.avaya.amsp.masterdata.dtos.PoolDto;
import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.dtos.SiteToPoolDto;
import com.avaya.amsp.masterdata.exceptions.ResourceAlreadyExistsException;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.PoolRepository;
import com.avaya.amsp.masterdata.repo.ShippingAddressRepository;
import com.avaya.amsp.masterdata.repo.SiteContractFlRepository;
import com.avaya.amsp.masterdata.repo.SitePoolRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.service.iface.SiteServiceIface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 
 * @author yadav188 This class is work as a service for Site entity
 */
public class SiteService implements SiteServiceIface {

	@Autowired
	private SiteRepository siteRepo;

	@Autowired
	private PoolRepository poolRepo;

	@Autowired
	private SitePoolRepository sitePoolRepo;

	@Autowired
	private SiteContractFlRepository siteContractFlRepo;

	@Autowired
	private ShippingAddressRepository shippingAddressRepo;

	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	ArticleClusterI18NRepository articleClusterI18NRepository;

	/**
	 * Purpose of this method is to get all the available sites having active state
	 * as 1
	 */
	@Override
	public List<SiteDto> fetchAllSites() {

		log.info("fetching available sites");
		List<SiteDto> siteDtoList = new ArrayList<SiteDto>();
		List<Site> siteData = siteRepo.findByActive(1L);
		if (siteData != null && !siteData.isEmpty()) {
			siteData.forEach(site -> {
				SiteDto siteDto = new SiteDto();
				siteDto.setId(site.getId());
				siteDto.setName(site.getName());
				siteDto.setActive(site.getActive());
				siteDto.setRemark(site.getRemark());
				siteDto.setLogCreatedBy(site.getLogCreatedBy());
				siteDto.setLogCreatedOn(site.getLogCreatedOn());
				siteDto.setLogUpdatedBy(site.getLogUpdatedBy());
				siteDto.setLocationCode(site.getLocationCode());
				siteDto.setCity(site.getCity());
				siteDto.setStreet(site.getStreet());
				siteDto.setClusterId(site.getClusterItem().getId());
				siteDto.setClusterName(site.getClusterItem().getName());
				siteDto.setSipDomain(site.getSipDomain());
				siteDto.setRoutingPolicy(site.getRoutingPolicy());
				siteDto.setCmName(site.getCmName());
				siteDto.setNotes(site.getNotes());
				siteDto.setArs(site.getArs());
				siteDtoList.add(siteDto);
			});
		} else {
			log.info("no sites found");
		}
		return siteDtoList;
	}

	/**
	 * Purpose of this method is to create a new site
	 */
	@AuditLog(action = "Insert",entity = "Site",functionality = "Create New Site")
	@Override
	public void createSite(SiteDto request) {

		// if (fetchSiteByName(request.getName()).isPresent()) {
		// log.info("site already exists for given request {} ", request.getName());
		// throw new ResourceAlreadyExistsException(
		// String.format("site with type %s is already exists", request.getName()));
		// }

		Optional<Site> existingSite = fetchSiteByName(request.getName());
		Site siteData;
		if (existingSite.isPresent()) {
			siteData = existingSite.get();
			if (siteData.getActive() != 0) {

				log.info("Site with name {} already exists and is active.", request.getName());
				throw new ResourceAlreadyExistsException(
						String.format("Site with name %s already exists and is active", request.getName()));

			}
		} else {
			siteData = new Site();
		}

		// Site siteData = new Site();
		siteData.setActive(1);
		siteData.setName(request.getName());
		siteData.setLocationCode(request.getLocationCode());
		siteData.setCity(request.getCity());
		siteData.setStreet(request.getStreet());
		siteData.setRemark(request.getRemark());
		siteData.setLogCreatedBy(request.getLogCreatedBy());
		siteData.setLogCreatedOn(new Timestamp(System.currentTimeMillis()));
		siteData.setSipDomain(request.getSipDomain());
		siteData.setRoutingPolicy(request.getRoutingPolicy());
		siteData.setCmName(request.getCmName());
		siteData.setNotes(request.getNotes());
		siteData.setArs(request.getArs());
		ClusterItem cluster = new ClusterItem();
		cluster.setId(request.getClusterId());
		siteData.setClusterItem(cluster);
		Site savedSite = siteRepo.save(siteData);
		log.info("new site added with id {}", savedSite.getId());
	}

	/**
	 * Purpose of this method is to update the existing site
	 */
	@AuditLog(action = "Update",entity = "Site",functionality = "Update Site")
	@Override
	public void updateSite(SiteDto request) {

		log.info("updating site record with ID {}", request.getId());
		Optional<Site> record = siteRepo.findById(request.getId());
		record.ifPresentOrElse(site -> {
			site.setName(request.getName());
			site.setRemark(request.getRemark());
			site.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			site.setLogUpdatedBy(request.getLogUpdatedBy());
			site.setLocationCode(request.getLocationCode());
			site.setStreet(request.getStreet());
			site.setCity(request.getCity());
			site.setSipDomain(request.getSipDomain());
			site.setRoutingPolicy(request.getRoutingPolicy());
			site.setCmName(request.getCmName());
			site.setNotes(request.getNotes());
			site.setArs(request.getArs());
			ClusterItem cluster = new ClusterItem();
			cluster.setId(request.getClusterId());
			site.setClusterItem(cluster);
			Site updateSite = siteRepo.save(site);
			log.info("site updated succesfully having id as{}", updateSite.getId());
		}, () -> {
			log.info("site record not found");
			throw new ResourceNotFoundException(String.format("site with Id %s not found ", request.getId()));
		});

	}

	/**
	 * Purpose of this method is to delete the existing site
	 */
	@AuditLog(action = "delete",entity = "Site",functionality = "Delete Site")
	@Override
	public void deleteSite(Long siteId) {

		log.info("Removing Site record with ID {}", siteId);
		Optional<Site> record = siteRepo.findById(siteId);
		record.ifPresentOrElse(site -> {
			site.setActive(0);
			Site deleteSite = siteRepo.save(site);
			log.info("site deleted succesfully having id as {}", deleteSite.getId());

		}, () -> {
			log.info("Site record not found");
			throw new ResourceNotFoundException(String.format("Site with Id %s not found ", siteId));
		});

	}

	@Override
	public Optional<Site> fetchSiteByName(String name) {
		log.info("Fetching sites with name {}", name);
		Site siteName = siteRepo.findByName(name);
		return Optional.ofNullable(siteName);
	}

	/**
	 * Purpose of this method is to get all the available sites for a cluster key
	 */
	@Override
	public List<SiteDto> fetchSitesByCluster(Long id) {

		log.info("Fetching sites with cluster id {}", id);
		List<SiteDto> siteDtoList = new ArrayList<SiteDto>();
		List<Site> siteData = siteRepo.findByClusterItem(id);
		if (siteData != null && !siteData.isEmpty()) {
			siteData.forEach(site -> {
				SiteDto siteDto = new SiteDto();
				siteDto.setId(site.getId());
				siteDto.setName(site.getName());
				siteDto.setActive(site.getActive());
				siteDto.setRemark(site.getRemark());
				siteDto.setLogCreatedBy(site.getLogCreatedBy());
				siteDto.setLogCreatedOn(site.getLogCreatedOn());
				siteDto.setLogUpdatedBy(site.getLogUpdatedBy());
				siteDto.setLogUpdatedOn(site.getLogUpdatedOn());
				siteDto.setLocationCode(site.getLocationCode());
				siteDto.setCity(site.getCity());
				siteDto.setStreet(site.getStreet());
				siteDto.setSipDomain(site.getSipDomain());
				siteDto.setRoutingPolicy(site.getRoutingPolicy());
				siteDto.setCmName(site.getCmName());
				siteDto.setNotes(site.getNotes());
				siteDto.setArs(site.getArs());
				siteDto.setClusterId(site.getClusterItem().getId());
				siteDto.setClusterName(site.getClusterItem().getName());
				siteDtoList.add(siteDto);
			});
		} else {
			log.info("no sites found");
		}
		return siteDtoList;
	}

	/**
	 * Purpose of this method is to assign the selected pools to site
	 */

	@Override
	public void addAssignPools(Long siteId, SiteToPoolDto pools) {

		log.info("request for adding pools to site");

		Optional<Site> record = siteRepo.findById(siteId);
		record.ifPresentOrElse(sites -> {
			List<Pool> poolRecords = poolRepo.findAllById(pools.getPoolIds());
			log.info("no of rows {}", poolRecords.size());
			if (poolRecords.size() != pools.getPoolIds().size()) {
				throw new IllegalArgumentException("Some of pools not found");
			}

			List<SitePool> sitePoolRecords = new ArrayList<SitePool>();

			poolRecords.forEach((poolConn) -> {
				SitePool sitePool = new SitePool();
				sitePool.setPool(poolConn);
				sitePool.setSite(sites);
				sitePoolRecords.add(sitePool);
			});

			sitePoolRepo.addPoolsAssignToSite(sitePoolRecords);

		}, () -> {
			log.info("site not found");
			throw new ResourceNotFoundException(String.format("site with Id %s not found ", siteId));
		});

	}

	/**
	 * Purpose of this method is to deassociate/delete the selected pools from site
	 */

	@AuditLog(action = "delete",entity = "SitePool",functionality = "delete Pool Assign to Site")
	@Override
	public void deleteAssignPools(Long siteId, SiteToPoolDto pools) {

		Optional<Site> regionRecord = siteRepo.findById(siteId);
		regionRecord.ifPresentOrElse(record -> {
			Set<SitePool> sitePoolRecord = record.getSitePool();

			if (sitePoolRecord != null && sitePoolRecord.size() != 0) {
				List<SitePool> sitePoolList = new ArrayList<SitePool>();
				pools.getPoolIds().forEach(poolid -> {
					SitePool sitePool = new SitePool();
					sitePool.setPoolId(poolid);
					sitePool.setSiteId(siteId);
					SitePool poolFromdb = sitePoolRecord.stream().filter(value -> value.equals(sitePool)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(
									String.format("Pools with Id %s not found for Site id Id %s", poolid, siteId)));

					sitePoolList.add(poolFromdb);

				});

				log.info("Removing pools from site {} ", sitePoolList);
				sitePoolRepo.deleteAllInBatch(sitePoolList);

			} else {
				throw new IllegalArgumentException(String.format("No pool is associated with site with Id %s", siteId));
			}

		}, () -> {
			log.info("site with Id {} not found", siteId);
			throw new ResourceNotFoundException(String.format("site with Id %s not found", siteId));
		});

	}

	// Site and Pool Mapping
	public Set<PoolDto> getPoolsForSite(Long siteId) {
		log.info("fetching list of pools for site id {}", siteId);
		Set<PoolDto> poolDtoList = new HashSet<PoolDto>();
		Optional<Site> siteData = siteRepo.findById(siteId);
		siteData.ifPresentOrElse(sitepoolData -> {
			Set<SitePool> sitepoolDatadb = sitepoolData.getSitePool();
			sitepoolDatadb.stream().forEach(sitepoolvalue -> {
				Pool poolvalue = sitepoolvalue.getPool();
				if (poolvalue.getActive() != 0) {
					PoolDto dto = mapper.map(poolvalue, PoolDto.class);
					dto.setRemark(poolvalue.getDescription());
					dto.setSiteId(siteId);
					poolDtoList.add(dto);
				} else {
					log.info("there is no pools in active state for this site");
				}
			});

		}, () -> {
			log.info("site with Id {} not found", siteId);
			throw new ResourceNotFoundException(String.format("site with Id %s not found", siteId));
		});
		return poolDtoList;
	}

	public List<ContractFlDto> getContractFlsForSite(Long siteId) {
		log.debug("Getting ContractFLs for site : {}", siteId);
		Site site = new Site();
		site.setId(siteId);
		List<SiteContractFL> contractFLList = siteContractFlRepo.findBySite(site);
		log.debug("Got contractFLList as : {}", contractFLList);

		List<ContractFlDto> contractFlDtos = new ArrayList<>();
		for (SiteContractFL o : contractFLList) {
			contractFlDtos.add(mapper.map(o, ContractFlDto.class));
		}
		log.debug("Got contractFlDtos as : {}", contractFlDtos);
		return contractFlDtos;
	}

	public boolean addContractFlsForSite(Long siteId, List<ContractFlDto> contractFlDto, String user) {
		
		Optional<Site> record = siteRepo.findById(siteId);
		record.ifPresent(site->{
			List<SiteContractFL> contractFl = new ArrayList<>();// mapper.map(contractFlDto, List.class);
			for (ContractFlDto o : contractFlDto)
				contractFl.add(mapper.map(o, SiteContractFL.class));
			siteContractFlRepo.saveAll(contractFl);
			site.setLogUpdatedBy(user);
			site.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			siteRepo.save(site);
		});
//		log.info("Saved {}", saveAll);
		return true;
	}

	public boolean deleteContractFlsForSite(Long siteId, List<Long> contractFls, String user) {
		Optional<Site> record = siteRepo.findById(siteId);
		record.ifPresent(site -> {
			log.info("Deleting contractFLs {}", contractFls);
			siteContractFlRepo.deleteAllById(contractFls);
			site.setLogUpdatedBy(user);
			site.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
			siteRepo.save(site);
		});
		return true;
	}

	@Override
	public List<ShippingAddressDto> getShippingAddressForSite(Long siteId) {
		log.debug("Getting Shipping Address for site : {}", siteId);
		Site site = new Site();
		site.setId(siteId);
		List<Shipping> shippingAddresList = shippingAddressRepo.findBySite(site);
		log.debug("Got shippingAddressList as : {}", shippingAddresList);

		List<ShippingAddressDto> shippingAddressDto = new ArrayList<>();

		for (Shipping o : shippingAddresList) {
			if (o.getActive() != 0) {
				shippingAddressDto.add(mapper.map(o, ShippingAddressDto.class));
			}
		}
		log.debug("Got shippingAddresDto as : {}", shippingAddressDto);
		return shippingAddressDto;
	}
	
	/*
	 * public ClusterItem getClusterForSite(Long siteId) { Optional<Site> siteDb =
	 * siteRepo.findById(siteId); if(siteDb.get() != null) { return
	 * siteDb.get().getClusterItem(); } return null; }
	 */
	
	public Site getSiteById(Long siteId) {
		Optional<Site> siteDb = siteRepo.findById(siteId);
		if(siteDb.get() != null) {
			return siteDb.get();
		}
		return null;
	}

	@Override
	public List<ArticlePropertiesDto> getArticlesForSitePool(String langId,Long siteId, Long poolId) {
		List<ArticlePropertiesDto> listArticles = new ArrayList<>();
		Optional<Site> siteDb = siteRepo.findById(siteId);
		Optional<Pool> poolDb = poolRepo.findById(poolId);
		siteDb.ifPresentOrElse(site -> {	
			poolDb.ifPresentOrElse(pool -> {
				Set<ArticlePool> articlepoolData = pool.getArticlePool();
				articlepoolData.stream().forEach(articlePool -> {
					ArticleCluster articleCluster = articlePool.getArticle(); //id, id_article
					ArticlePropertiesDto dto = mapper.map(articleCluster, ArticlePropertiesDto.class); //articleClusterId
					//List<ArticleClusterI18NName> articleClusterI18NName=articleClusterI18NRepository.findByArticleIdAndLanguageId(articleCluster.getId(),langId);
					ArticleClusterI18NName articleClusterI18NName=articleClusterI18NRepository.findByArticleClusterIdAndLaguage(articleCluster.getId(),langId);
					if(articleClusterI18NName!=null) {
						dto.setName(articleClusterI18NName.getTranslation());
						}
					/*
					 * if(!articleClusterI18NName.isEmpty()) {
					 * dto.setName(articleClusterI18NName.get(0).getTranslation()); }
					 */
					dto.setArticleClusterId(articleCluster.getId()); //We have to set it explicitly to articleClusterId as the mapper above sets it to master article id
					dto.setPricePurchase_dollar(articleCluster.getPricePurchaseDollar());
					dto.setPricePurchase_euro(articleCluster.getPriceSalesEuro());
					dto.setPriceSales_dollar(articleCluster.getPriceSalesDollar());
					dto.setPriceSales_euro(articleCluster.getPriceSalesEuro());
					dto.setAvailable(articlePool.getAvailable());
					dto.setReserved(articlePool.getReserved());
					dto.setPoolId(poolId);
					dto.setPoolKey(pool.getName());
					dto.setSiteKey(site.getName());
					dto.setClusterKey(site.getClusterItem().getName());
					dto.setRemark(articleCluster.getDescription());
					
					/*
					 * if (site.getClusterItem().getAccCurrencyId().getId() == 1L) {
					 * dto.setPuPrice(articleCluster.getPricePurchaseEuro());
					 * dto.setSaPrice(articleCluster.getPriceSalesEuro()); dto.setCurrency("EURO");
					 * } else { dto.setPuPrice(articleCluster.getPricePurchaseDollar());
					 * dto.setSaPrice(articleCluster.getPriceSalesDollar());
					 * dto.setCurrency("DOLLAR"); }
					 */
					
					//We have to use article currency for order related transactions
					switch (site.getClusterItem().getArticleCurrency().getCode()) {
						case "EUR":
							dto.setPuPrice(articleCluster.getPricePurchaseEuro());
							dto.setSaPrice(articleCluster.getPriceSalesEuro());
							dto.setCurrency("EURO");
							break;
						case "USD":
							dto.setPuPrice(articleCluster.getPricePurchaseDollar());
							dto.setSaPrice(articleCluster.getPriceSalesDollar());
							dto.setCurrency("DOLLAR");
					}
					listArticles.add(dto);
				});
			}, () -> {
				log.info("Pool with id {} not found in DB", poolId);
				throw new ResourceNotFoundException(String.format("Pool with Id %s not found", poolId));
			});
		}, () -> {
			log.info("Site with id {} not found in DB", siteId);
			throw new ResourceNotFoundException(String.format("Site with Id %s not found", siteId));
		});
		
		return listArticles;
	}
}
