package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.PoolOrderItem;

@Repository
public interface PoolOrderRepository extends JpaRepository<PoolOrderItem, Long> {

	List<PoolOrderItem> findByStatus(Integer statusCode, Sort sort);

	List<PoolOrderItem> findByIdAndStatus(Long id, Integer statusCode, Sort sort);

	//List<PoolOrderItem> findBySiteAndStatus(Long siteId, Integer statusCode, Sort sort);

	//List<PoolOrderItem> findByClusterItemAndStatus(Long clusterItemId, Integer statusCode, Sort sort);

	//List<PoolOrderItem> findByRegionAndStatus(Long regionId, Integer statusCode, Sort sort);

	//List<PoolOrderItem> findByPoolAndStatus(Long poolId, Integer statusCode, Sort sort);

	@Query("SELECT poi FROM PoolOrderItem poi WHERE poi.site.id = :siteId AND poi.status = :statusCode")
	List<PoolOrderItem> findBySiteIdAndStatus(Long siteId, Integer statusCode, Sort sort);
	
	@Query("SELECT poi FROM PoolOrderItem poi WHERE poi.clusterItem.id = :clusterId AND poi.status = :statusCode")
	List<PoolOrderItem> findByClusterIdAndStatus(Long clusterId, Integer statusCode, Sort sort);

	@Query("SELECT poi FROM PoolOrderItem poi WHERE poi.region.id = :regionId AND poi.status = :statusCode")
	List<PoolOrderItem> findByRegionIdAndStatus(Long regionId, Integer statusCode, Sort sort);

	@Query("SELECT poi FROM PoolOrderItem poi WHERE poi.pool.id = :poolId AND poi.status = :statusCode")
	List<PoolOrderItem> findByPoolIdAndStatus(Long poolId, Integer statusCode, Sort sort);
}
