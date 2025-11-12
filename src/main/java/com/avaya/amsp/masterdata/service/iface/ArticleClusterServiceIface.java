package com.avaya.amsp.masterdata.service.iface;

import java.util.List;

import com.avaya.amsp.masterdata.dtos.ArticleClusterPartListDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.dtos.SubArticleDetailDto;
import com.avaya.amsp.security.user.AMSPUser;

public interface ArticleClusterServiceIface {

	List<ArticleToClusterDto> fetchAllArticles();

	public void updateArticle(ArticleToClusterDto dto);

	public void removeArticle(Long articleId, String user);

	List<ConnectionDto> fetchConnectionsByArticle(Long articleId);

	List<ArticleToClusterDto> fetchRecordByArticleCluster(Long articleId, Long clusterId);

	// cluster Partlist changes

	List<ArticleClusterPartListDto> fetchAllArticleClusterByParts(Long clusterId, int partStatus,AMSPUser user);

	public void addSubArticlesToArticle(Long articleId, SubArticleDetailDto subArticleIds, String user);

	public void deleteClusterPartListLeadArticles(Long leadId, String user);

	public void deleteAssignSubArticles(Long leadId, SubArticleDetailDto subArticles, String user);

	List<ArticleToClusterDto> fetchClusterSubArticles(Long leadArticle);

	public void removeMasterPartStatus(Long articleId, String user);	

}