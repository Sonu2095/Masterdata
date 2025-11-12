package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.avaya.amsp.domain.ArticleI18NName;

@Repository
public interface ArticleI18NRepository extends JpaRepository<ArticleI18NName, ArticleI18NName.ArticleI18NNameId> {
	
	@Query("SELECT N FROM ARTICLE_I18N_NAME N WHERE N.article.id IN :articleIds")
	List<ArticleI18NName> findByArticleIds(List<Long> articleIds);
	
	@Query("SELECT N FROM ARTICLE_I18N_NAME N WHERE N.article.id = :articleId")
	List<ArticleI18NName> findByArticleId(Long articleId);

}
