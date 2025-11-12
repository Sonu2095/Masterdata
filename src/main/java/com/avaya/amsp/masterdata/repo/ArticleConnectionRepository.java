package com.avaya.amsp.masterdata.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;

import com.avaya.amsp.domain.ArticleConnection;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticlePropertyAttributeDto;


public interface ArticleConnectionRepository extends JpaRepository<ArticleConnection, Long> {
	
	@AuditLog(action = "INSERT",entity = "ArticleConnection",functionality = "Save All Connections Port Type")
	default List<ArticleConnection> saveAllArticlesToConnection(List<ArticleConnection> articleConns){
		return saveAll(articleConns);
	}
	
	@org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW,readOnly = true)
	@Query(value = "SELECT ac FROM ARTICLE_CONNECTION ac WHERE ac.connection.id = :connectionId AND ac.article.id in :articleIds")
	List<ArticleConnection> findAllByConnIdAndArticleIds(Long connectionId, List<Long> articleIds);
	
	@AuditLog(action = "update",entity = "ArticleConnection",functionality = "update Articles Properties from connection")
	default List<ArticleConnection> updateArticleProperties(Long id,List<ArticlePropertyAttributeDto> articlePorpertiesDto,List<ArticleConnection> articleConns){
		return saveAll(articleConns);
	}
	
	@AuditLog(action = "INSERT",entity = "ArticleConnection",functionality = "Insert Articles to Connections")
	default List<ArticleConnection> addArticlesToConn(List<ArticleConnection> articleConns){
		return saveAll(articleConns);
	}
	
}
