package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleConnection;
import com.avaya.amsp.domain.ClusterConnection;
import com.avaya.amsp.masterdata.annotation.AuditLog;

public interface ClusterConnectionRepository extends JpaRepository<ClusterConnection, Long>, JpaSpecificationExecutor<ClusterConnection> {
	
	@Query(value = "SELECT c FROM CLUSTER_CONNECTION c WHERE c.cluster.id = :clusterId AND c.connection.active = 1")
	Page<ClusterConnection> findByClusterId(Long clusterId, Pageable pageable);
	
	@AuditLog(action = "Insert",entity = "ClusterConnection",functionality = "add Clusters To Connection")
	default List<ClusterConnection> saveClustersToConnection(List<ClusterConnection> clusterConnectionList){
		return saveAll(clusterConnectionList);
	}
	
	@Query(value = "SELECT cc FROM CLUSTER_CONNECTION cc WHERE cc.connection.id = :connectionId AND cc.cluster.id in :clusterIds")
	List<ClusterConnection> findAllByConnIdAndClustersIds(Long connectionId, List<Long> clusterIds);

}
