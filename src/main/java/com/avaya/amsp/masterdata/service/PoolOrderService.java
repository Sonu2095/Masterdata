package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.ArticlePoolInventoryHistory;
import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.domain.PoolOperationTypeEnum;
import com.avaya.amsp.domain.PoolOrderArticle;
import com.avaya.amsp.domain.PoolOrderFiles;
import com.avaya.amsp.domain.PoolOrderIdSequence;
import com.avaya.amsp.domain.PoolOrderItem;
import com.avaya.amsp.domain.Region;
import com.avaya.amsp.domain.Shipping;
import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.dtos.ListOfPoolOrderArticles;
import com.avaya.amsp.masterdata.dtos.PoolOrderArticleDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderFilesDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderRequestDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderResponseDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderStatusCode;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolInvHistoryRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.PoolOrderArticleRepository;
import com.avaya.amsp.masterdata.repo.PoolOrderFilesRepo;
import com.avaya.amsp.masterdata.repo.PoolOrderIdSequenceRepo;
import com.avaya.amsp.masterdata.repo.PoolOrderRepository;
import com.avaya.amsp.masterdata.repo.PoolRepository;
import com.avaya.amsp.masterdata.repo.RegionRepository;
import com.avaya.amsp.masterdata.repo.ShippingAddressRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.service.iface.PoolOrderEmailServiceIface;
import com.avaya.amsp.masterdata.service.iface.PoolOrderServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PoolOrderService implements PoolOrderServiceIface {

	@Autowired
	private PoolOrderRepository poolOrderRepo;

	@Autowired
	private PoolOrderArticleRepository poolOrderArticleRepo;

	@Autowired
	private ArticleClusterRepository articleClusterRepo;

	@Autowired
	private ClusterRepository clusterRepo;

	@Autowired
	private SiteRepository siteRepo;

	@Autowired
	private PoolRepository poolRepo;

	@Autowired
	private RegionRepository regionRepo;

	@Autowired
	private ArticlePoolInvHistoryRepository articlePoolInvHistRepo;

	@Autowired
	private ArticlePoolRepository articlePoolRepo;

	@Autowired
	private ShippingAddressRepository shippingRepo;

	@Autowired
	private PoolOrderEmailServiceIface poolOrderEmailService;

	@Autowired
	private PoolOrderFilesRepo poolOrderFilesRepo;

	@Autowired
	private PoolOrderIdSequenceRepo poolOrderIdSequenceRepo;

	@Autowired
	ArticleClusterI18NRepository articleClusterI18NRepository;

	@Autowired
	ArticleClusterI18NNameService articleClusterI18NNameService;

	private static final String ORDER_ID = "OID: ";

	@Transactional
	@Override
	public PoolOrderResponseDTO openPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user) {
		if (!poolOrderRequestDTO.getArticles().isEmpty() && poolOrderRequestDTO.getArticles().size() > 0) {
			PoolOrderResponseDTO responseDto = new PoolOrderResponseDTO();
			try {
				PoolOrderIdSequence sequence = poolOrderIdSequenceRepo.save(new PoolOrderIdSequence());
				// String poolOrderId = String.valueOf(sequenceID.getId());
				Long poolOrderId = sequence.getId();

				PoolOrderItem poolOrderItem = new PoolOrderItem();
				Region region = null;

				Pool pool = poolRepo.findById(poolOrderRequestDTO.getPoolId()).get();
				Site site = siteRepo.findById(poolOrderRequestDTO.getSiteId()).get();
				ClusterItem clusterItem = clusterRepo.findById(poolOrderRequestDTO.getClusterId()).get();

				// Region is not a mandatory field.
				if (poolOrderRequestDTO.getRegionId() != null) {
					region = regionRepo.findById(poolOrderRequestDTO.getRegionId()).get();
				}

				Hibernate.initialize(clusterItem.getArticleCurrency());
				Hibernate.initialize(clusterItem.getArticleCurrency().getCode());

				String userName = user.getUsername();

				poolOrderItem.setId(poolOrderId);
				poolOrderItem.setRegion(region);
				poolOrderItem.setClusterItem(clusterItem);
				poolOrderItem.setSite(site);
				poolOrderItem.setPool(pool);

				poolOrderItem.setContractCode(poolOrderRequestDTO.getContractCode());
				poolOrderItem.setWorkingUserId(userName);
				poolOrderItem.setPurchaserId(userName); // TODO: Different than the working user id?
				poolOrderItem.setPurchaseTs(LocalDateTime.now());
				poolOrderItem.setApproveTs(null);

				poolOrderItem.setStatus(PoolOrderStatusCode.OPENED.getPoolOrderStatusCodeValue());

				poolOrderItem.setSapRequestNumber(null);

				poolOrderItem.setOrderNotes(poolOrderRequestDTO.getOrderNotes());
				// poolOrderItem.setShippingAddress(shippingAddress);
				poolOrderItem.setPlannedShippingTs(null);
				poolOrderItem.setShippingTs(null);
				poolOrderItem.setShippingEmail(poolOrderRequestDTO.getShippingEmail()); // TODO: Will it be available
																						// while creating an order?
				poolOrderItem.setShippingNotes(null);

				List<PoolOrderArticle> articleList = new ArrayList<>();
				List<PoolOrderArticleDTO> articlesInOrder = poolOrderRequestDTO.getArticles();
				Map<ArticlePool, Long> articlePoolQntMap = new HashMap<>();

				for (PoolOrderArticleDTO articleObj : articlesInOrder) {
					ArticleCluster articleCluster = articleClusterRepo.findById(articleObj.getArticleClusterId()).get();
					log.info(articleCluster.toString());
					ArticlePool articlePool = articlePoolRepo.findByArticleAndPool(articleCluster, pool);
					Long quantity = articleObj.getQuantity();

					PoolOrderArticle poolOrderArticle = new PoolOrderArticle();
					poolOrderArticle.setClusterArticleId(articleCluster.getId());

					String articleClusterI18NName = articleClusterI18NNameService.getTranslation(articleCluster.getId(),
							user.getDefaultLanguageId(), articleCluster.getName());
					articleObj.setName(articleClusterI18NName);

					poolOrderArticle.setArticleCluster(articleCluster);
					poolOrderArticle.setPoolOrderItemId(poolOrderId);
					poolOrderArticle.setPoolOrderItem(poolOrderItem);
					poolOrderArticle.setQuantity(quantity);
					poolOrderArticle.setArticleAddInfo(null);
					poolOrderArticle.setArticleRemark(articleCluster.getDescription());
					articleList.add(poolOrderArticle);

					// This map is used later while auditing
					articlePoolQntMap.put(articlePool, quantity);
				}

				poolOrderItem.setPoolOrderArticles(articleList);
				poolOrderItem = poolOrderRepo.save(poolOrderItem);

				/*
				 * Record the current pool operation in ArticlePoolInventoryHistory table for
				 * auditing
				 */
				auditCurrentOperation(poolOrderItem.getId(), articlePoolQntMap, userName, pool,
						PoolOperationTypeEnum.ORDERING.name());

				responseDto.setOrderId(poolOrderItem.getId());
				responseDto.setArticles(poolOrderRequestDTO.getArticles());

				if (poolOrderItem.getRegion() != null) {
					responseDto.setRegionId(poolOrderItem.getRegion().getId());
					responseDto.setRegionKey(poolOrderItem.getRegion().getName());
				}
				responseDto.setClusterId(poolOrderItem.getClusterItem().getId());
				responseDto.setSiteId(poolOrderItem.getSite().getId());
				responseDto.setPoolId(poolOrderItem.getPool().getId());

				responseDto.setClusterKey(poolOrderItem.getClusterItem().getName());
				responseDto.setSiteKey(poolOrderItem.getSite().getName());
				responseDto.setPoolKey(poolOrderItem.getPool().getName());
				responseDto.setOrderNotes(poolOrderItem.getOrderNotes());
				responseDto.setPurchaserName(poolOrderItem.getPurchaserId());
				responseDto.setPurchaseTs(poolOrderItem.getPurchaseTs());
				responseDto.setWorkingUserName(poolOrderItem.getWorkingUserId());
				responseDto.setContractCode(poolOrderItem.getContractCode());

				poolOrderEmailService.sendOrderOpenConfirmationEmail(user, poolOrderItem, null);
			} catch (Exception e) {
				log.info("Exception caught: {}", e);
			}

			return responseDto;
		} else {
			log.info("No articles present in the order request");
			// throw new Exception("No articles present in the order request");
		}
		return null;
	}

	@Transactional
	@Override
	public PoolOrderResponseDTO submitPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user) {
		PoolOrderResponseDTO responseDto = new PoolOrderResponseDTO();
		poolOrderRepo.findById(poolOrderRequestDTO.getOrderId()).ifPresentOrElse(poItem -> {
			if (poItem.getStatus() == PoolOrderStatusCode.OPENED.getPoolOrderStatusCodeValue()
					&& poolOrderRequestDTO.getShippingAddressId() != null) {

				log.info("Submitting pool order {}", poItem.getId());

				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency());
				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency().getCode());

				Shipping shippingAddress = shippingRepo.findById(poolOrderRequestDTO.getShippingAddressId()).get();
				poItem.setStatus(PoolOrderStatusCode.WAITING_SAP.getPoolOrderStatusCodeValue());
				poItem.setShippingAddress(shippingAddress);
				poItem.setPlannedShippingTs(poolOrderRequestDTO.getPlannedShippingDate());
				poItem.setShippingNotes(poolOrderRequestDTO.getShippingNotes());
				poItem.setShippingEmail(poolOrderRequestDTO.getShippingEmail());
				poItem.setApproveTs(LocalDateTime.now());
				poItem.setOrderNotes(poolOrderRequestDTO.getOrderNotes());
				poItem = poolOrderRepo.save(poItem);

				responseDto.setOrderId(poItem.getId());
				// responseDto.setArticles(poItem.getArticles());

				if (poItem.getRegion() != null) {
					responseDto.setRegionId(poItem.getRegion().getId());
					responseDto.setRegionKey(poItem.getRegion().getName());
				}
				responseDto.setClusterId(poItem.getClusterItem().getId());
				responseDto.setSiteId(poItem.getSite().getId());
				responseDto.setPoolId(poItem.getPool().getId());

				responseDto.setClusterKey(poItem.getClusterItem().getName());
				responseDto.setSiteKey(poItem.getSite().getName());
				responseDto.setPoolKey(poItem.getPool().getName());
				responseDto.setOrderNotes(poItem.getOrderNotes());
				responseDto.setPurchaserName(poItem.getPurchaserId());
				responseDto.setPurchaseTs(poItem.getPurchaseTs());
				responseDto.setWorkingUserName(poItem.getWorkingUserId());
				responseDto.setShippingAddress(poItem.getShippingAddress().getShippingAddressString());
				responseDto.setShippingEmail(poItem.getShippingEmail());
				responseDto.setPlannedShippingDate(poItem.getPlannedShippingTs());
				responseDto.setShippingNotes(poItem.getShippingNotes());
				responseDto.setContractCode(poItem.getContractCode());

				auditCurrentOperation(poItem, PoolOperationTypeEnum.CLEARING.name());

				poolOrderEmailService.sendOrderApprovedConfirmationEmail(user, poItem, null);
			} else {
				log.info("Invalid operation");
			}
		}, () -> {
			log.info("No order found with id: {}", poolOrderRequestDTO.getOrderId());
		});
		return responseDto;
	}

	@Transactional
	@Override
	public PoolOrderResponseDTO processSapForPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user) {
		PoolOrderResponseDTO responseDto = new PoolOrderResponseDTO();
		poolOrderRepo.findById(poolOrderRequestDTO.getOrderId()).ifPresentOrElse(poItem -> {
			if (poItem.getStatus() == PoolOrderStatusCode.WAITING_SAP.getPoolOrderStatusCodeValue()
					&& poolOrderRequestDTO.getSapRequestNum() != null) {
				log.info("Processing SAP for pool order {}", poItem.getId());

				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency());
				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency().getCode());

				poItem.setStatus(PoolOrderStatusCode.WAITING_SHIPPING.getPoolOrderStatusCodeValue());
				poItem.setSapRequestNumber(poolOrderRequestDTO.getSapRequestNum());
				poItem.setPlannedShippingTs(poolOrderRequestDTO.getPlannedShippingDate());
				poItem.setOrderNotes(poolOrderRequestDTO.getOrderNotes());
				poItem.setSapTs(LocalDateTime.now());
				poItem = poolOrderRepo.save(poItem);

				responseDto.setOrderId(poItem.getId());
				// responseDto.setArticles(poItem.getArticles());

				if (poItem.getRegion() != null) {
					responseDto.setRegionId(poItem.getRegion().getId());
					responseDto.setRegionKey(poItem.getRegion().getName());
				}
				responseDto.setClusterId(poItem.getClusterItem().getId());
				responseDto.setSiteId(poItem.getSite().getId());
				responseDto.setPoolId(poItem.getPool().getId());

				responseDto.setClusterKey(poItem.getClusterItem().getName());
				responseDto.setSiteKey(poItem.getSite().getName());
				responseDto.setPoolKey(poItem.getPool().getName());
				responseDto.setOrderNotes(poItem.getOrderNotes());
				responseDto.setPurchaserName(poItem.getPurchaserId());
				responseDto.setPurchaseTs(poItem.getPurchaseTs());
				responseDto.setWorkingUserName(poItem.getWorkingUserId());
				responseDto.setShippingAddress(poItem.getShippingAddress().getShippingAddressString());
				responseDto.setShippingEmail(poItem.getShippingEmail());
				responseDto.setPlannedShippingDate(poItem.getPlannedShippingTs());
				responseDto.setShippingNotes(poItem.getShippingNotes());
				responseDto.setSapRequestNum(poItem.getSapRequestNumber());
				responseDto.setContractCode(poItem.getContractCode());

				auditCurrentOperation(poItem, PoolOperationTypeEnum.SAP_REQUEST.name());
				poolOrderEmailService.sendOrderDeliveryRequestEmail(user, poItem, null);
			} else {
				log.info("Invalid operation");
			}
		}, () -> {
			log.info("No order found with id: {}", poolOrderRequestDTO.getOrderId());
		});
		return responseDto;
	}

	@Transactional
	@Override
	public PoolOrderResponseDTO processShippingForPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user) {
		PoolOrderResponseDTO responseDto = new PoolOrderResponseDTO();
		poolOrderRepo.findById(poolOrderRequestDTO.getOrderId()).ifPresentOrElse(poItem -> {
			if (poItem.getStatus() == PoolOrderStatusCode.WAITING_SHIPPING.getPoolOrderStatusCodeValue()) {
				log.info("Processing shipping for pool order {}", poItem.getId());

				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency());
				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency().getCode());

				// Update the available quantity of all the articles in the master Article_Pool
				// in DB.
				for (PoolOrderArticle poolOrderArticle : poItem.getPoolOrderArticles()) {
					ArticlePool articlePool = articlePoolRepo.findByArticleAndPool(poolOrderArticle.getArticleCluster(),
							poItem.getPool());
					// Adding the ordered quantity to the already available quantity
					articlePool.setAvailable(articlePool.getAvailable() + poolOrderArticle.getQuantity());
					articlePool.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
					articlePool.setLogUpdatedBy(user.getUsername());
					articlePoolRepo.save(articlePool);
				}
				poItem.setStatus(PoolOrderStatusCode.CLOSED.getPoolOrderStatusCodeValue());
				if (poolOrderRequestDTO.getShippingDate() != null) {
					poItem.setShippingTs(poolOrderRequestDTO.getShippingDate());
				} else {
					poItem.setShippingTs(LocalDate.now());
				}

				poItem.setShippingNotes(poolOrderRequestDTO.getShippingNotes());
				poItem.setOrderNotes(poolOrderRequestDTO.getOrderNotes());
				poItem = poolOrderRepo.save(poItem);

				responseDto.setOrderId(poItem.getId());
				// responseDto.setArticles(poItem.getArticles());

				if (poItem.getRegion() != null) {
					responseDto.setRegionId(poItem.getRegion().getId());
					responseDto.setRegionKey(poItem.getRegion().getName());
				}
				responseDto.setClusterId(poItem.getClusterItem().getId());
				responseDto.setSiteId(poItem.getSite().getId());
				responseDto.setPoolId(poItem.getPool().getId());

				responseDto.setClusterKey(poItem.getClusterItem().getName());
				responseDto.setSiteKey(poItem.getSite().getName());
				responseDto.setPoolKey(poItem.getPool().getName());
				responseDto.setOrderNotes(poItem.getOrderNotes());
				responseDto.setPurchaserName(poItem.getPurchaserId());
				responseDto.setPurchaseTs(poItem.getPurchaseTs());
				responseDto.setWorkingUserName(poItem.getWorkingUserId());
				responseDto.setShippingAddress(poItem.getShippingAddress().getShippingAddressString());
				responseDto.setShippingEmail(poItem.getShippingEmail());
				responseDto.setPlannedShippingDate(poItem.getPlannedShippingTs());
				responseDto.setShippingNotes(poItem.getShippingNotes());
				responseDto.setSapRequestNum(poItem.getSapRequestNumber());
				responseDto.setShippingDate(poItem.getShippingTs());
				responseDto.setContractCode(poItem.getContractCode());

				auditCurrentOperation(poItem, PoolOperationTypeEnum.CHECK_IN.name());
				poolOrderEmailService.sendOrderClosedConfirmationEmail(user, poItem, null);
			} else {
				log.info("Invalid operation");
			}
		}, () -> {
			log.info("No order found with id: {}", poolOrderRequestDTO.getOrderId());
		});
		return responseDto;
	}

	@Transactional
	@Override
	public PoolOrderResponseDTO cancelPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user) {
		PoolOrderResponseDTO responseDto = new PoolOrderResponseDTO();
		poolOrderRepo.findById(poolOrderRequestDTO.getOrderId()).ifPresentOrElse(poItem -> {
			if (poItem.getStatus() != PoolOrderStatusCode.CLOSED.getPoolOrderStatusCodeValue()
					&& poItem.getStatus() != PoolOrderStatusCode.CANCELLED.getPoolOrderStatusCodeValue()) {
				log.info("Canceling pool order {}", poItem.getId());

				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency());
				Hibernate.initialize(poItem.getClusterItem().getArticleCurrency().getCode());

				responseDto.setOrderId(poItem.getId());
				// responseDto.setArticles(poItem.getArticles());
				responseDto.setClusterKey(poItem.getClusterItem().getName());
				responseDto.setSiteKey(poItem.getSite().getName());
				responseDto.setPoolKey(poItem.getPool().getName());
				responseDto.setOrderNotes(poItem.getOrderNotes());
				responseDto.setPurchaserName(poItem.getPurchaserId());
				responseDto.setPurchaseTs(poItem.getPurchaseTs());
				responseDto.setWorkingUserName(poItem.getWorkingUserId());
				if (poItem.getStatus() != PoolOrderStatusCode.OPENED.getPoolOrderStatusCodeValue()) {
					responseDto.setShippingAddress(poItem.getShippingAddress().getShippingAddressString());
				}

				responseDto.setShippingEmail(poItem.getShippingEmail());
				responseDto.setPlannedShippingDate(poItem.getPlannedShippingTs());
				responseDto.setShippingNotes(poItem.getShippingNotes());
				responseDto.setSapRequestNum(poItem.getSapRequestNumber());
				responseDto.setContractCode(poItem.getContractCode());

				poItem.setStatus(PoolOrderStatusCode.CANCELLED.getPoolOrderStatusCodeValue());
				// poItem.setCanceledTs(); TODO
				poItem = poolOrderRepo.save(poItem);

				auditCurrentOperation(poItem, PoolOperationTypeEnum.CANCEL.name());
				// TODO: Mail?
			} else {
				log.info("Invalid operation");
			}
		}, () -> {
			log.info("No order found with id: {}", poolOrderRequestDTO.getOrderId());
		});
		return responseDto;
	}

	@Transactional
	@Override
	public PoolOrderResponseDTO updatePoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user) {
		PoolOrderResponseDTO responseDto = new PoolOrderResponseDTO();
		poolOrderRepo.findById(poolOrderRequestDTO.getOrderId()).ifPresentOrElse(poItem -> {
			if (poItem.getStatus() == PoolOrderStatusCode.WAITING_SHIPPING.getPoolOrderStatusCodeValue()) {
				if (poolOrderRequestDTO.getSapRequestNum() != null
						&& !poolOrderRequestDTO.getSapRequestNum().isEmpty()) {
					poItem.setSapRequestNumber(poolOrderRequestDTO.getSapRequestNum());
				}
				if (poolOrderRequestDTO.getPlannedShippingDate() != null) {
					poItem.setPlannedShippingTs(poolOrderRequestDTO.getPlannedShippingDate());
				}
				if (poolOrderRequestDTO.getShippingNotes() != null
						&& !poolOrderRequestDTO.getShippingNotes().isEmpty()) {
					poItem.setShippingNotes(poolOrderRequestDTO.getShippingNotes());
				}
				if (poolOrderRequestDTO.getOrderNotes() != null && !poolOrderRequestDTO.getOrderNotes().isEmpty()) {
					poItem.setOrderNotes(poolOrderRequestDTO.getOrderNotes());
				}
				/*
				 * poItem.setSapRequestNumber(poolOrderRequestDTO.getSapRequestNum());
				 * poItem.setPlannedShippingTs(poolOrderRequestDTO.getPlannedShippingDate());//
				 * plannedShippingDate
				 * poItem.setShippingNotes(poolOrderRequestDTO.getShippingNotes());
				 * poItem.setOrderNotes(poolOrderRequestDTO.getOrderNotes());
				 */
				poolOrderRepo.save(poItem);

				responseDto.setOrderId(poItem.getId());
				// responseDto.setArticles(poItem.getArticles());
				responseDto.setClusterKey(poItem.getClusterItem().getName());
				responseDto.setSiteKey(poItem.getSite().getName());
				responseDto.setPoolKey(poItem.getPool().getName());
				responseDto.setOrderNotes(poItem.getOrderNotes());
				responseDto.setPurchaserName(poItem.getPurchaserId());
				responseDto.setPurchaseTs(poItem.getPurchaseTs());
				responseDto.setWorkingUserName(poItem.getWorkingUserId());
				responseDto.setShippingAddress(poItem.getShippingAddress().getShippingAddressString());
				responseDto.setShippingEmail(poItem.getShippingEmail());
				responseDto.setPlannedShippingDate(poItem.getPlannedShippingTs());
				responseDto.setShippingNotes(poItem.getShippingNotes());
				responseDto.setSapRequestNum(poItem.getSapRequestNumber());
			} else {
				log.info("Invalid operation");
			}
		}, () -> {
			log.info("No order found with id: {}", poolOrderRequestDTO.getOrderId());
		});
		return responseDto;
	}

	@Override
	public List<PoolOrderResponseDTO> getPoolOrdersFromStatus(PoolOrderStatusCode orderStatusCode) {
		List<PoolOrderResponseDTO> poolOrders = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "purchaseTs");
		List<PoolOrderItem> poItems = poolOrderRepo.findByStatus(orderStatusCode.getPoolOrderStatusCodeValue(), sort);
		for (PoolOrderItem eachItem : poItems) {
			PoolOrderResponseDTO poDto = new PoolOrderResponseDTO();
			poDto.setOrderId(eachItem.getId());

			if (eachItem.getRegion() != null) {
				poDto.setRegionId(eachItem.getRegion().getId());
				poDto.setRegionKey(eachItem.getRegion().getName());
			}
			poDto.setClusterId(eachItem.getClusterItem().getId());
			poDto.setSiteId(eachItem.getSite().getId());
			poDto.setPoolId(eachItem.getPool().getId());

			poDto.setClusterKey(eachItem.getClusterItem().getName());
			poDto.setPoolKey(eachItem.getPool().getName());
			poDto.setSiteKey(eachItem.getSite().getName());
			poDto.setPurchaserName(eachItem.getPurchaserId());
			poDto.setPurchaseTs(eachItem.getPurchaseTs());
			poDto.setWorkingUserName(eachItem.getWorkingUserId());
			poDto.setSapRequestNum(eachItem.getSapRequestNumber());
			poDto.setPlannedShippingDate(eachItem.getPlannedShippingTs());
			if (eachItem.getShippingAddress() != null) {
				// Till the order is submitted, we don't have shipping address info. Hence, the
				// check.
				poDto.setShippingAddress(eachItem.getShippingAddress().getShippingAddressString());
			}
			poDto.setShippingDate(eachItem.getShippingTs());
			poDto.setOrderNotes(eachItem.getOrderNotes());
			poDto.setShippingNotes(eachItem.getShippingNotes());
			poDto.setShippingEmail(eachItem.getShippingEmail());
			poDto.setContractCode(eachItem.getContractCode());
			poolOrders.add(poDto);
		}
		return poolOrders;
	}

	private void auditCurrentOperation(Long orderId, Map<ArticlePool, Long> articlePoolQntMap, String userName,
			Pool pool, String operation) {
		log.info("Auditing pool order {}", orderId);
		for (ArticlePool artPool : articlePoolQntMap.keySet()) {
			ArticlePoolInventoryHistory record = new ArticlePoolInventoryHistory();
			record.setArticle(artPool.getArticle());//
			record.setPool(pool);//
			record.setOperation(operation);//
			record.setAvailable(artPool.getAvailable());//
			record.setReserved(artPool.getReserved());//
			record.setDifference(articlePoolQntMap.get(artPool));//
			record.setReasonText(ORDER_ID + orderId);// order id
			record.setUserName(userName); //
			record.setUpdatedTS(new Timestamp(System.currentTimeMillis()));//
			record.setArticleName(artPool.getArticle().getArticle().getName());//
			articlePoolInvHistRepo.save(record);
		}
	}

	private void auditCurrentOperation(PoolOrderItem poItem, String operation) {
		log.info("Auditing pool order {}", poItem.getId());

		// We will search only with any one operation ORDERING here as it will tell us
		// the different number(not quantities) of articles in a given order
		articlePoolInvHistRepo
				.findByReasonTextAndOperation(ORDER_ID + poItem.getId(), PoolOperationTypeEnum.ORDERING.name())
				.ifPresentOrElse(listArticleHistory -> {
					for (ArticlePoolInventoryHistory invHistory : listArticleHistory) {
						// This will give us the actual current availability of an article in a pool
						ArticlePool articlePool = articlePoolRepo.findByArticleAndPool(invHistory.getArticle(),
								poItem.getPool());

						ArticlePoolInventoryHistory record = new ArticlePoolInventoryHistory();
						record.setArticle(invHistory.getArticle());//
						record.setPool(invHistory.getPool());//
						record.setOperation(operation);//
						record.setAvailable(articlePool.getAvailable());//
						record.setReserved(articlePool.getReserved());//
						record.setDifference(invHistory.getDifference());//
						record.setReasonText(ORDER_ID + poItem.getId());//
						record.setUserName(invHistory.getUserName()); //
						record.setUpdatedTS(new Timestamp(System.currentTimeMillis()));//
						record.setArticleName(invHistory.getArticle().getArticle().getName());//
						articlePoolInvHistRepo.save(record);
					}
				}, () -> {
					log.info("No order found for order id {}, operation {}", poItem.getId(),
							PoolOperationTypeEnum.ORDERING.name());
				});
	}

	@Override
	public List<PoolOrderResponseDTO> getPoolOrderItemsFromId(Long orderItemId,
			PoolOrderStatusCode poolOrderStatusCode) {
		List<PoolOrderResponseDTO> poolOrders = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "purchaseTs");
		List<PoolOrderItem> poItems = poolOrderRepo.findByIdAndStatus(orderItemId,
				poolOrderStatusCode.getPoolOrderStatusCodeValue(), sort);
		for (PoolOrderItem eachItem : poItems) {
			PoolOrderResponseDTO poDto = new PoolOrderResponseDTO();
			poDto.setOrderId(eachItem.getId());

			if (eachItem.getRegion() != null) {
				poDto.setRegionId(eachItem.getRegion().getId());
				poDto.setRegionKey(eachItem.getRegion().getName());
			}
			poDto.setClusterId(eachItem.getClusterItem().getId());
			poDto.setSiteId(eachItem.getSite().getId());
			poDto.setPoolId(eachItem.getPool().getId());

			poDto.setClusterKey(eachItem.getClusterItem().getName());
			poDto.setPoolKey(eachItem.getPool().getName());
			poDto.setSiteKey(eachItem.getSite().getName());
			poDto.setPurchaserName(eachItem.getPurchaserId());
			poDto.setPurchaseTs(eachItem.getPurchaseTs());
			poDto.setWorkingUserName(eachItem.getWorkingUserId());
			poDto.setSapRequestNum(eachItem.getSapRequestNumber());
			poDto.setPlannedShippingDate(eachItem.getPlannedShippingTs());
			if (eachItem.getShippingAddress() != null) {
				// Till the order is submitted, we don't have shipping address info. Hence, the
				// check.
				poDto.setShippingAddress(eachItem.getShippingAddress().getShippingAddressString());
			}
			poDto.setShippingDate(eachItem.getShippingTs());
			poDto.setOrderNotes(eachItem.getOrderNotes());
			poDto.setShippingNotes(eachItem.getShippingNotes());
			poDto.setShippingEmail(eachItem.getShippingEmail());
			poDto.setContractCode(eachItem.getContractCode());
			poolOrders.add(poDto);
		}
		return poolOrders;
	}

	@Override
	public List<PoolOrderResponseDTO> getPoolOrderItemsFromSite(Long siteId, PoolOrderStatusCode poolOrderStatusCode) {
		List<PoolOrderResponseDTO> poolOrders = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "purchaseTs");
		List<PoolOrderItem> poItems = poolOrderRepo.findBySiteIdAndStatus(siteId,
				poolOrderStatusCode.getPoolOrderStatusCodeValue(), sort);
		for (PoolOrderItem eachItem : poItems) {
			PoolOrderResponseDTO poDto = new PoolOrderResponseDTO();
			poDto.setOrderId(eachItem.getId());

			if (eachItem.getRegion() != null) {
				poDto.setRegionId(eachItem.getRegion().getId());
				poDto.setRegionKey(eachItem.getRegion().getName());
			}
			poDto.setClusterId(eachItem.getClusterItem().getId());
			poDto.setSiteId(eachItem.getSite().getId());
			poDto.setPoolId(eachItem.getPool().getId());

			poDto.setClusterKey(eachItem.getClusterItem().getName());
			poDto.setPoolKey(eachItem.getPool().getName());
			poDto.setSiteKey(eachItem.getSite().getName());
			poDto.setPurchaserName(eachItem.getPurchaserId());
			poDto.setPurchaseTs(eachItem.getPurchaseTs());
			poDto.setWorkingUserName(eachItem.getWorkingUserId());
			poDto.setSapRequestNum(eachItem.getSapRequestNumber());
			poDto.setPlannedShippingDate(eachItem.getPlannedShippingTs());
			if (eachItem.getShippingAddress() != null) {
				// Till the order is submitted, we don't have shipping address info. Hence, the
				// check.
				poDto.setShippingAddress(eachItem.getShippingAddress().getShippingAddressString());
			}
			poDto.setShippingDate(eachItem.getShippingTs());
			poDto.setOrderNotes(eachItem.getOrderNotes());
			poDto.setShippingNotes(eachItem.getShippingNotes());
			poDto.setShippingEmail(eachItem.getShippingEmail());
			poDto.setContractCode(eachItem.getContractCode());
			poolOrders.add(poDto);
		}
		return poolOrders;
	}

	@Override
	public List<PoolOrderResponseDTO> getPoolOrderItemsFromCluster(Long clusterId,
			PoolOrderStatusCode poolOrderStatusCode) {
		List<PoolOrderResponseDTO> poolOrders = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "purchaseTs");
		List<PoolOrderItem> poItems = poolOrderRepo.findByClusterIdAndStatus(clusterId,
				poolOrderStatusCode.getPoolOrderStatusCodeValue(), sort);
		for (PoolOrderItem eachItem : poItems) {
			PoolOrderResponseDTO poDto = new PoolOrderResponseDTO();
			poDto.setOrderId(eachItem.getId());

			if (eachItem.getRegion() != null) {
				poDto.setRegionId(eachItem.getRegion().getId());
				poDto.setRegionKey(eachItem.getRegion().getName());
			}
			poDto.setClusterId(eachItem.getClusterItem().getId());
			poDto.setSiteId(eachItem.getSite().getId());
			poDto.setPoolId(eachItem.getPool().getId());

			poDto.setClusterKey(eachItem.getClusterItem().getName());
			poDto.setPoolKey(eachItem.getPool().getName());
			poDto.setSiteKey(eachItem.getSite().getName());
			poDto.setPurchaserName(eachItem.getPurchaserId());
			poDto.setPurchaseTs(eachItem.getPurchaseTs());
			poDto.setWorkingUserName(eachItem.getWorkingUserId());
			poDto.setSapRequestNum(eachItem.getSapRequestNumber());
			poDto.setPlannedShippingDate(eachItem.getPlannedShippingTs());
			if (eachItem.getShippingAddress() != null) {
				// Till the order is submitted, we don't have shipping address info. Hence, the
				// check.
				poDto.setShippingAddress(eachItem.getShippingAddress().getShippingAddressString());
			}
			poDto.setShippingDate(eachItem.getShippingTs());
			poDto.setOrderNotes(eachItem.getOrderNotes());
			poDto.setShippingNotes(eachItem.getShippingNotes());
			poDto.setShippingEmail(eachItem.getShippingEmail());
			poDto.setContractCode(eachItem.getContractCode());
			poolOrders.add(poDto);
		}
		return poolOrders;
	}

	@Override
	public List<PoolOrderResponseDTO> getPoolOrderItemsFromRegion(Long regionId,
			PoolOrderStatusCode poolOrderStatusCode) {
		List<PoolOrderResponseDTO> poolOrders = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "purchaseTs");
		List<PoolOrderItem> poItems = poolOrderRepo.findByRegionIdAndStatus(regionId,
				poolOrderStatusCode.getPoolOrderStatusCodeValue(), sort);
		for (PoolOrderItem eachItem : poItems) {
			PoolOrderResponseDTO poDto = new PoolOrderResponseDTO();
			poDto.setOrderId(eachItem.getId());

			if (eachItem.getRegion() != null) {
				poDto.setRegionId(eachItem.getRegion().getId());
				poDto.setRegionKey(eachItem.getRegion().getName());
			}
			poDto.setClusterId(eachItem.getClusterItem().getId());
			poDto.setSiteId(eachItem.getSite().getId());
			poDto.setPoolId(eachItem.getPool().getId());

			poDto.setClusterKey(eachItem.getClusterItem().getName());
			poDto.setPoolKey(eachItem.getPool().getName());
			poDto.setSiteKey(eachItem.getSite().getName());
			poDto.setPurchaserName(eachItem.getPurchaserId());
			poDto.setPurchaseTs(eachItem.getPurchaseTs());
			poDto.setWorkingUserName(eachItem.getWorkingUserId());
			poDto.setSapRequestNum(eachItem.getSapRequestNumber());
			poDto.setPlannedShippingDate(eachItem.getPlannedShippingTs());
			if (eachItem.getShippingAddress() != null) {
				// Till the order is submitted, we don't have shipping address info. Hence, the
				// check.
				poDto.setShippingAddress(eachItem.getShippingAddress().getShippingAddressString());
			}
			poDto.setShippingDate(eachItem.getShippingTs());
			poDto.setOrderNotes(eachItem.getOrderNotes());
			poDto.setShippingNotes(eachItem.getShippingNotes());
			poDto.setShippingEmail(eachItem.getShippingEmail());
			poDto.setContractCode(eachItem.getContractCode());
			poolOrders.add(poDto);
		}
		return poolOrders;
	}

	@Override
	public List<PoolOrderResponseDTO> getPoolOrderItemsFromPool(Long poolId, PoolOrderStatusCode poolOrderStatusCode) {
		List<PoolOrderResponseDTO> poolOrders = new ArrayList<>();
		Sort sort = Sort.by(Sort.Direction.DESC, "purchaseTs");
		List<PoolOrderItem> poItems = poolOrderRepo.findByPoolIdAndStatus(poolId,
				poolOrderStatusCode.getPoolOrderStatusCodeValue(), sort);
		for (PoolOrderItem eachItem : poItems) {
			PoolOrderResponseDTO poDto = new PoolOrderResponseDTO();
			poDto.setOrderId(eachItem.getId());

			if (eachItem.getRegion() != null) {
				poDto.setRegionId(eachItem.getRegion().getId());
				poDto.setRegionKey(eachItem.getRegion().getName());
			}
			poDto.setClusterId(eachItem.getClusterItem().getId());
			poDto.setSiteId(eachItem.getSite().getId());
			poDto.setPoolId(eachItem.getPool().getId());

			poDto.setClusterKey(eachItem.getClusterItem().getName());
			poDto.setPoolKey(eachItem.getPool().getName());
			poDto.setSiteKey(eachItem.getSite().getName());
			poDto.setPurchaserName(eachItem.getPurchaserId());
			poDto.setPurchaseTs(eachItem.getPurchaseTs());
			poDto.setWorkingUserName(eachItem.getWorkingUserId());
			poDto.setSapRequestNum(eachItem.getSapRequestNumber());
			poDto.setPlannedShippingDate(eachItem.getPlannedShippingTs());
			if (eachItem.getShippingAddress() != null) {
				// Till the order is submitted, we don't have shipping address info. Hence, the
				// check.
				poDto.setShippingAddress(eachItem.getShippingAddress().getShippingAddressString());
			}
			poDto.setShippingDate(eachItem.getShippingTs());
			poDto.setOrderNotes(eachItem.getOrderNotes());
			poDto.setShippingNotes(eachItem.getShippingNotes());
			poDto.setShippingEmail(eachItem.getShippingEmail());
			poDto.setContractCode(eachItem.getContractCode());
			poolOrders.add(poDto);
		}
		return poolOrders;
	}

	@Override
	public ListOfPoolOrderArticles getArticlesForPoolOrder(String poolOrderId, AMSPUser user) {
		ListOfPoolOrderArticles articles = new ListOfPoolOrderArticles();
		List<PoolOrderArticleDTO> poolOrderArticles = new ArrayList<>();

		for (PoolOrderArticle orderArticle : poolOrderArticleRepo.findByOrderId(poolOrderId)) {
			PoolOrderArticleDTO dto = new PoolOrderArticleDTO();
			dto.setArticleClusterId(orderArticle.getClusterArticleId());
			dto.setName(orderArticle.getArticleCluster().getName());
			ArticleClusterI18NName articleClusterI18NName = articleClusterI18NRepository
					.findByArticleClusterIdAndLaguage(orderArticle.getClusterArticleId(), user.getDefaultLanguageId());
			if (articleClusterI18NName != null) {
				dto.setName(articleClusterI18NName.getTranslation());
			}
			dto.setQuantity(orderArticle.getQuantity());
			dto.setArticleAddInfo(orderArticle.getArticleAddInfo());
			dto.setArticleRemark(orderArticle.getArticleRemark());
			poolOrderArticles.add(dto);
		}
		articles.setListOfArticles(poolOrderArticles);
		return articles;
	}

	@Override
	public List<PoolOrderFilesDTO> getFilesForPoolOrder(String poolOrderId) {
		List<PoolOrderFiles> poolOrderFiles = poolOrderFilesRepo.findByOrderId(poolOrderId);
		List<PoolOrderFilesDTO> fileDetailsList = poolOrderFiles.stream().map(pof -> {
			PoolOrderFilesDTO fileDetails = new PoolOrderFilesDTO();
			fileDetails.setFileId(pof.getFileId());
			fileDetails.setFileName(pof.getFileName());
			fileDetails.setSizeInBytes(pof.getFileSize());
			fileDetails.setUploadTs(pof.getUploadTs());
			return fileDetails;
		}).collect(Collectors.toList());
		return fileDetailsList;
	}

	@Override
	public byte[] getFile(String fileId) {
		return poolOrderFilesRepo.findByFileId(fileId).getContent();
	}
}
