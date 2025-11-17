package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avaya.amsp.domain.ClusterItem;

public interface ClusterRepository extends JpaRepository<ClusterItem, Long> {

	public ClusterItem findByName(String name);

	@EntityGraph(attributePaths = {"country", "language", "articleCurrency", "accCurrencyId"})
	List<ClusterItem> findByActiveOrderByIdAsc(int active);
	
    @Query(value = "SELECT p.name FROM CLUSTER_ITEM p WHERE p.id = :id")
    String findNameById(Long id);
    
    @Query(value = "SELECT C FROM CLUSTER_ITEM C LEFT JOIN FETCH C.country LEFT JOIN FETCH C.language LEFT JOIN FETCH C.articleCurrency LEFT JOIN FETCH C.accCurrencyId where C.active=1 ORDER BY C.id ASC")
	public Page<ClusterItem> findAll(Pageable pageable);
    
    @Query(value = "SELECT C FROM CLUSTER_ITEM C LEFT JOIN FETCH C.country LEFT JOIN FETCH C.language LEFT JOIN FETCH C.articleCurrency LEFT JOIN FETCH C.accCurrencyId where C.active=1 and C.id in (:clusterIds) ORDER BY C.id ASC")
    List<ClusterItem> findByIds(@Param("clusterIds") List<Long> clusterIds);
    
	@Query("SELECT c FROM CLUSTER_ITEM c " + "LEFT JOIN FETCH c.country " + "LEFT JOIN FETCH c.language "
			+ "LEFT JOIN FETCH c.articleCurrency " + "LEFT JOIN FETCH c.accCurrencyId "
			+ "LEFT JOIN ARTICLE_CLUSTER ac ON ac.clusterItem.id = c.id AND ac.article.id = :articleId AND ac.active = 1 "
			+ "WHERE c.active = 1 AND ac.id IS NULL " + "ORDER BY c.id ASC")
	List<ClusterItem> findUnassignedClusters(@Param("articleId") Long articleId);

}
