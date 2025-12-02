package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.ClusterI18NName;

@Repository
public interface ClusterI18NRepository extends JpaRepository<ClusterI18NName, ClusterI18NName.ClusterI18NNameId> {
	
	@Query("SELECT N FROM CLUSTER_I18N_NAME N WHERE N.clusterItem.id IN :clusterIds")
	List<ClusterI18NName> findByClusterIds(List<Long> clusterIds);
	
}
