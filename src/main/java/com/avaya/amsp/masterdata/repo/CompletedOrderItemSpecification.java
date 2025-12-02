package com.avaya.amsp.masterdata.repo;

import org.springframework.data.jpa.domain.Specification;

import com.avaya.amsp.domain.CompletedOrders;

public class CompletedOrderItemSpecification {
	
	public static Specification<CompletedOrders> hasClusterId(Long clusterId) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.equal(root
					.get("clusterId"), clusterId);
	}
	
	public static Specification<CompletedOrders> hasOrderId(String orderId) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.equal(root
					.get("orderId"), orderId);
	}
	
	public static Specification<CompletedOrders> hasPurchaserId(String purchaserId) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.equal(root
					.get("purchasedById"), purchaserId);
	}
	
	public static Specification<CompletedOrders> hasOrderType(String orderType) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.equal(root
					.get("orderType"), orderType);
	}
	
	public static Specification<CompletedOrders> hasConnectionId(String connectionId) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.equal(root
					.get("connectionId"), connectionId);
	}
	
	public static Specification<CompletedOrders> hasPhoneNum(String phoneNum) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.equal(root
					.get("phoneNumber"), phoneNum);
	}
}
