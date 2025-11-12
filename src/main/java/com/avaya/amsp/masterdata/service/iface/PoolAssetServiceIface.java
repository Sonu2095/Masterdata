package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ArticlePoolInventoryHistoryDto;
import com.avaya.amsp.masterdata.dtos.PoolAssetDto;
import com.avaya.amsp.security.user.AMSPUser;

public interface PoolAssetServiceIface {

	void setAvailableArticlesInPool(PoolAssetDto poolAssetDto);

	void setReservedArticlesInPool(PoolAssetDto poolAssetDto);

	List<ArticlePoolInventoryHistoryDto> viewChangeHistoryForPool(String poolId, AMSPUser amspUser);
	
}
