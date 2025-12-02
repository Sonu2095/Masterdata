package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.PoolOrderArticle;

@Repository
public interface PoolOrderArticleRepository extends JpaRepository<PoolOrderArticle, PoolOrderArticle.PoolOrderArticleId> {
	
	@Query("SELECT poa FROM PoolOrderArticle poa WHERE poa.poolOrderItem.id = :poolOrderId")
	List<PoolOrderArticle> findByOrderId(String poolOrderId);

}
