package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.Pool;
import com.avaya.amsp.masterdata.annotation.AuditLog;

public interface ArticlePoolRepository extends JpaRepository<ArticlePool, Long>{
	
	ArticlePool findByArticleAndPool(ArticleCluster articleCluster, Pool pool);
	
	@AuditLog(action = "Insert",entity = "ArticlePool",functionality = "Add Article to Pool")
	default List<ArticlePool> addArticlesToPool(List<ArticlePool> listSitePools){
		return saveAll(listSitePools);
	}
	
	@Query(value = "SELECT ap FROM ARTICLE_POOL ap WHERE ap.pool.id = :poolId AND ap.article.id in :articleIds")
	List<ArticlePool> findAllByPoolIdAndArticleIds(Long poolId, List<Long> articleIds);

}


