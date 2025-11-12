package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avaya.amsp.domain.ArticleConfig;

public interface ArticleConfigRepository extends JpaRepository<ArticleConfig, Long> {

	@Query(value = "select C from article_config C  where C.forMaster=1 ORDER BY C.id ASC")
	public List<ArticleConfig> findAllMasterAttribute();

	@Query(value = "select C from article_config C  where C.forSlave=1 ORDER BY C.id ASC")
	public List<ArticleConfig> findAllSlaveAttribute();

}
