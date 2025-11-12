package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleConnection;
import com.avaya.amsp.domain.PartListSubArticle;
import com.avaya.amsp.domain.PartsObligatoryEnum;
import com.avaya.amsp.masterdata.annotation.AuditLog;

import jakarta.transaction.Transactional;

public interface PartListSubArticleRepository extends JpaRepository<PartListSubArticle, Long> {

	@Modifying
	@Transactional
	@Query("update PARTLIST_SUBARTICLE p SET p.obligatory = :obligatory WHERE p.article.id = :articleId AND p.subArticles.id = :subArticleId")
	public void updateObligatory(@Param("obligatory") PartsObligatoryEnum obligatory,
			@Param("articleId") Long articleId, @Param("subArticleId") long subArticleId);

	@Query(value = "select C from PARTLIST_SUBARTICLE C  where C.article.id=:articleId")
	public List<PartListSubArticle> findByArticle(Long articleId);

	@Modifying
	@Transactional
	@Query(value = "delete from PARTLIST_SUBARTICLE C  where C.article.id=:leadId")
	public void deleteSubArticlesByLeadId(Long leadId);
	
	@org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW,readOnly = true)
	@Query(value = "SELECT ps FROM PARTLIST_SUBARTICLE ps WHERE ps.article.id = :articleId AND ps.subArticles.id in :subArticleIds")
	List<PartListSubArticle> findAllByarticleIdAndSubArticleIds(Long articleId, List<Long> subArticleIds);
	
	@AuditLog(action = "insert",entity = "PartListSubArticle",functionality = "add Sub Article to Article")
	default List<PartListSubArticle> addSubArticlesToArticleMasterPartList(List<PartListSubArticle> partListSubArticleList){
		return saveAll(partListSubArticleList);
	}

}
