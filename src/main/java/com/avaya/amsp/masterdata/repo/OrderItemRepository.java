package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

	@Query("SELECT o FROM OrderItem o WHERE o.pbxSystemId = :idPbxSystem AND o.orderType = 'ADD' AND o.orderStatus NOT IN (0,2000,5000,6000)")
	List<OrderItem> findByPbxSystemId(long idPbxSystem);

}
