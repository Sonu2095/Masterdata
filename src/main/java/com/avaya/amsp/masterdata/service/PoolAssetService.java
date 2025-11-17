package com.avaya.amsp.masterdata.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avaya.amsp.domain.ArticleClusterI18NName;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.ArticlePoolInventoryHistory;
import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.domain.PoolOperationTypeEnum;
import com.avaya.amsp.masterdata.dtos.ArticlePoolInventoryHistoryDto;
import com.avaya.amsp.masterdata.dtos.PoolAssetDto;
import com.avaya.amsp.masterdata.exceptions.ResourceNotFoundException;
import com.avaya.amsp.masterdata.repo.ArticleClusterI18NRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolInvHistoryRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolRepository;
import com.avaya.amsp.masterdata.repo.PoolRepository;
import com.avaya.amsp.masterdata.service.iface.PoolAssetServiceIface;
import com.avaya.amsp.security.user.AMSPUser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PoolAssetService implements PoolAssetServiceIface {
	
	@Autowired
	private PoolRepository poolRepo;
	
	@Autowired
	private ArticlePoolRepository articlePoolRepo;
	
	@Autowired
	private ArticlePoolInvHistoryRepository articlePoolInvHistRepo;
	
	@Autowired
	ArticleClusterI18NRepository articleClusterI18NRepository;
	
	@Override
	public void setAvailableArticlesInPool(PoolAssetDto poolAssetDto) {
		log.info("Updating available quantity for {} article in pool {}", poolAssetDto.getArticleId(), poolAssetDto.getPoolId());
		Optional<Pool> pool = poolRepo.findById(poolAssetDto.getPoolId());
		
		pool.ifPresentOrElse(value -> {
			Set<ArticlePool> articlePoolData = value.getArticlePool();
			
			articlePoolData.stream().filter(artPoolFilter -> artPoolFilter.getArticleId() == poolAssetDto.getArticleId())
				.forEach(artPool -> {
					log.info("Found article in pool");
					if(poolAssetDto.getOperation().equals(PoolOperationTypeEnum.CHECK_IN.name())) {
						artPool.setAvailable(artPool.getAvailable() + poolAssetDto.getQuantity());
					} else if(poolAssetDto.getOperation().equals(PoolOperationTypeEnum.CHECK_OUT.name())) {
						if((artPool.getAvailable() - poolAssetDto.getQuantity()) >= 0) {
							artPool.setAvailable(artPool.getAvailable() - poolAssetDto.getQuantity());
						} else {
							log.info("Quantity to be checked out not available");
							throw new IllegalArgumentException("Quantity to be checked out not available");
						}
					}
					artPool.setLogUpdatedBy(poolAssetDto.getUserName());
					artPool.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
					auditCurrentOperation(artPool, poolAssetDto, value);
					articlePoolRepo.save(artPool);
				});
		}, () -> {
			log.info("pool record not found");
			throw new ResourceNotFoundException(String.format("Pool with Id %s not found ", poolAssetDto.getPoolId()));
		});
	}

	@Override
	public void setReservedArticlesInPool(PoolAssetDto poolAssetDto) {
		log.info("Updating reserved quantity for {} article in pool {}", poolAssetDto.getArticleId(), poolAssetDto.getPoolId());
		Optional<Pool> pool = poolRepo.findById(poolAssetDto.getPoolId());
		
		pool.ifPresentOrElse(value -> {
			Set<ArticlePool> articlePoolData = value.getArticlePool();
			
			articlePoolData.stream().filter(artPoolFilter -> artPoolFilter.getArticleId() == poolAssetDto.getArticleId())
					.forEach(artPool -> {
						log.info("Found article in pool");
						artPool.setReserved(poolAssetDto.getQuantity());
						artPool.setLogUpdatedBy(poolAssetDto.getUserName());
						artPool.setLogUpdatedOn(new Timestamp(System.currentTimeMillis()));
						poolAssetDto.setOperation(PoolOperationTypeEnum.SET_RESERVED.name());
						poolAssetDto.setQuantity(0L); //we don't need difference to be set for set_reserved operation
						auditCurrentOperation(artPool, poolAssetDto, value);
						articlePoolRepo.save(artPool);
					});
		}, () -> {
			log.info("pool record not found");
			throw new ResourceNotFoundException(String.format("Pool with Id %s not found ", poolAssetDto.getPoolId()));
		});
	}

	@Override
	public List<ArticlePoolInventoryHistoryDto> viewChangeHistoryForPool(String poolId, AMSPUser amspUser) {
		log.info("Fetching change history for pool id {}", poolId);
		List<ArticlePoolInventoryHistory> history = articlePoolInvHistRepo.findByPoolId(poolId);
		List<ArticlePoolInventoryHistoryDto> historyDto = new ArrayList<>();
		history.stream().forEach(record -> {
			ArticlePoolInventoryHistoryDto dto = new ArticlePoolInventoryHistoryDto();
			dto.setId(record.getId());
			dto.setArticleName(record.getArticleName());
			ArticleClusterI18NName articleClusterI18NName=articleClusterI18NRepository.findByArticleClusterIdAndLaguage(record.getArticle().getId(),amspUser.getDefaultLanguageId());
			if(articleClusterI18NName!=null) {
				dto.setArticleName(articleClusterI18NName.getTranslation());
				}
			
			dto.setOperation(PoolOperationTypeEnum.valueOf(record.getOperation()));
			dto.setAvailable(record.getAvailable());
			dto.setDifference(record.getDifference());
			dto.setReserved(record.getReserved());
			dto.setReason(record.getReasonText());
			dto.setUser(record.getUserName());
			dto.setUpdatedTS(record.getUpdatedTS());
			historyDto.add(dto);
		}
		);
		return historyDto;
	}
	
	/* Record the current pool operation in ArticlePoolInventoryHistory table for auditing */
	private void auditCurrentOperation(ArticlePool artPool, PoolAssetDto poolAssetDto, Pool pool) {
		ArticlePoolInventoryHistory record = new ArticlePoolInventoryHistory();
		record.setArticle(artPool.getArticle());
		record.setPool(pool);
		record.setOperation(poolAssetDto.getOperation());
		record.setAvailable(artPool.getAvailable());
		record.setReserved(artPool.getReserved());
		record.setDifference(poolAssetDto.getQuantity());
		record.setReasonText(poolAssetDto.getReason());
		record.setUserName(poolAssetDto.getUserName());
		record.setUpdatedTS(new Timestamp(System.currentTimeMillis()));
		record.setArticleName(artPool.getArticle().getArticle().getName());
		articlePoolInvHistRepo.save(record);
	}
	
}
