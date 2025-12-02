package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.domain.PartsPropertyEnum;

import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

	@Query(value = "select C from ARTICLE C  where C.active=1 ORDER BY C.id ASC")
	@QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
	public List<Article> findAll();

	public Article findByNameIgnoreCaseAndActive(String key, int active);

	public long countByIdInAndActive(List<Long> ids, int active);

	public Article findByName(String name);

	public List<Article> findByActive(Long active);

	@Query(value = "select C from ARTICLE C  where C.isPart=:partStatus AND C.active=1 ORDER BY C.id ASC")
	public List<Article> findByPartsStatus(int partStatus);

	@Modifying
	@Query(value = "update ARTICLE C set C.isPart=:isPart where C.id=:articleId")
	public void updateIsPartStatus(int isPart, long articleId);

}
