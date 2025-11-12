package com.avaya.amsp.masterdata.dtos;

import com.avaya.amsp.domain.ArticleConnection;

public class ArticleConnectionMappingDto {
	private Long articleId;
	private ArticleConnection articleConn;

	public ArticleConnectionMappingDto(Long articleId, ArticleConnection articleConn) {
		this.articleId = articleId;
		this.articleConn = articleConn;
	}

	public Long getArticleId() {
		return articleId;
	}

	public ArticleConnection getArticleConn() {
		return articleConn;
	}
}
