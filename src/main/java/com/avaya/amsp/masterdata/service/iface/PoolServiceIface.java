package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.masterdata.dtos.PoolDto;
import com.avaya.amsp.masterdata.dtos.PoolToArticleDto;
import com.avaya.amsp.security.user.AMSPUser;

public interface PoolServiceIface {

	List<PoolDto> fetchAllPools();

	public void createPool(PoolDto poolDto);

	public void updatePool(PoolDto poolDto);

	public void deletePool(Long poolId);

	public Optional<Pool> fetchPoolByName(String name);

	public void addAssignArticles(Long poolId, PoolToArticleDto articles, AMSPUser amspUser);

	public void deleteAssignArticles(Long poolId, PoolToArticleDto articles, AMSPUser amspUser);

}
