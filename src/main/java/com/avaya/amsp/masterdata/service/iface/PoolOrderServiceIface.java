package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ListOfPoolOrderArticles;
import com.avaya.amsp.masterdata.dtos.PoolOrderFilesDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderRequestDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderResponseDTO;
import com.avaya.amsp.masterdata.dtos.PoolOrderStatusCode;
import com.avaya.amsp.security.user.AMSPUser;

public interface PoolOrderServiceIface {

	PoolOrderResponseDTO openPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user);
	
	PoolOrderResponseDTO submitPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user);
	
	PoolOrderResponseDTO processSapForPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user);
	
	PoolOrderResponseDTO processShippingForPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user);
	
	PoolOrderResponseDTO cancelPoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user);
	
	PoolOrderResponseDTO updatePoolOrder(PoolOrderRequestDTO poolOrderRequestDTO, AMSPUser user);
	
	List<PoolOrderResponseDTO> getPoolOrdersFromStatus(PoolOrderStatusCode orderStatusCode);

	List<PoolOrderResponseDTO> getPoolOrderItemsFromId(Long orderItemId, PoolOrderStatusCode poolOrderStatusCode);

	List<PoolOrderResponseDTO> getPoolOrderItemsFromSite(Long siteId, PoolOrderStatusCode poolOrderStatusCode);

	List<PoolOrderResponseDTO> getPoolOrderItemsFromCluster(Long clusterId, PoolOrderStatusCode poolOrderStatusCode);

	List<PoolOrderResponseDTO> getPoolOrderItemsFromRegion(Long regionId, PoolOrderStatusCode poolOrderStatusCode);

	List<PoolOrderResponseDTO> getPoolOrderItemsFromPool(Long poolId, PoolOrderStatusCode poolOrderStatusCode);

	ListOfPoolOrderArticles getArticlesForPoolOrder(String poolOrderId, AMSPUser amspUser);

	List<PoolOrderFilesDTO> getFilesForPoolOrder(String poolOrderId);

	byte[] getFile(String fileId);
	
	//void getFilesForPoolOrder(String poolOrderId);

	//PoolOrderResponseDTO getArticlesForPoolOrder(String poolOrderId); //Do we need it?

}
