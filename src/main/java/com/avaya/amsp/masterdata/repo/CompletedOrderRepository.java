package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.CompletedOrders;
import com.avaya.amsp.domain.OrderItem;

public interface CompletedOrderRepository extends JpaRepository<CompletedOrders, String>, JpaSpecificationExecutor<CompletedOrders> {
	
	@Query("SELECT o FROM CompletedOrders o WHERE o.clusterId = :clusterId")
	Page<OrderItem> findByClusterId(Long clusterId, Pageable pageable);
	
	@Query("SELECT distinct o.id FROM CompletedOrders o WHERE o.clusterId = :clusterId")
	List<String> findOrderIdsByClusterId(Long clusterId);
	
	@Query("SELECT distinct o.purchasedById FROM CompletedOrders o WHERE o.clusterId = :clusterId")
	List<String> findPurchaserIdsByClusterId(Long clusterId);
}