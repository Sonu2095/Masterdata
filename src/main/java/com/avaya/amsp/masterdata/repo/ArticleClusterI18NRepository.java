package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avaya.amsp.domain.ArticleClusterI18NName;

public interface ArticleClusterI18NRepository extends JpaRepository<ArticleClusterI18NName, ArticleClusterI18NName.ArticleI18NNameId>, JpaSpecificationExecutor<ArticleClusterI18NName>{

	@Query(value = "select A from ARTICLE_CLUSTER_I18N_NAME A, ARTICLE_CLUSTER  C where  C.clusterItem.id= :clusterId AND C.id=A.article.id")
	public List<ArticleClusterI18NName> findAllByCluster(Long clusterId);
	
	
	@Query("SELECT N FROM ARTICLE_CLUSTER_I18N_NAME N WHERE N.article.id = :articleId")
	List<ArticleClusterI18NName> findByArticleId(Long articleId);
	
	@Query("SELECT N FROM ARTICLE_CLUSTER_I18N_NAME N WHERE N.article.id = :articleId and N.language.id = :langId")
	List<ArticleClusterI18NName> findByArticleIdAndLanguageId(Long articleId, String langId);

	
	@Query("SELECT N FROM ARTICLE_CLUSTER_I18N_NAME N WHERE N.article.id = :articleId and N.language.id = :langId")
	public ArticleClusterI18NName findByArticleClusterIdAndLaguage(Long articleId, String langId);

	@Query("SELECT i18n FROM ARTICLE_CLUSTER_I18N_NAME i18n WHERE i18n.article.article.id IN :articleIds")
	List<ArticleClusterI18NName> findByArticleIds(@Param("articleIds") List<Long> articleIds);

	@Query("SELECT i18n FROM ARTICLE_CLUSTER_I18N_NAME i18n WHERE i18n.article.id IN :allClusterIds")
	public List<ArticleClusterI18NName> findByArticleClusterIds(List<Long> allClusterIds);
}
