package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;

import com.avaya.amsp.domain.PartListSubArticle;
import com.avaya.amsp.domain.PartlistClusterSubarticle;
import com.avaya.amsp.masterdata.annotation.AuditLog;

import jakarta.transaction.Transactional;

public interface PartListArticleClusterRepository extends JpaRepository<PartlistClusterSubarticle, Long> {

	@Modifying
	@Transactional
	@Query(value = "delete from partlist_cluster_subarticle C  where C.articleCluster.id=:leadId")
	public void deleteSubArticlesByLeadId(Long leadId);

	@Query(value = "select C from partlist_cluster_subarticle C  where C.articleCluster.id=:leadId")
	public List<PartlistClusterSubarticle> findByArticle(Long leadId);
	
	@AuditLog(action = "insert",entity = "PartlistClusterSubarticle",functionality = "Add Clusters to MasterPartList Lead Article")
	default List<PartlistClusterSubarticle> addAllPartlistArticleToCluster(List<PartlistClusterSubarticle> partlistClusterSubarticleList) {
		return saveAll(partlistClusterSubarticleList);
	}
	
}
