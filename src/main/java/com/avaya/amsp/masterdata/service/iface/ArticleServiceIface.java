package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.Article;
import com.avaya.amsp.masterdata.dtos.*;

public interface ArticleServiceIface {

	List<ArticleDto> fetchAllArticles();

	List<ArticlePartListDto> fetchAllArticlesByParts(int partStatus);

	public String persistArticle(ArticleDto dto);

	public void updateArticle(ArticleDto dto);

	public void removeArticle(Long articleId);

	List<ClusterDto> fetchMasterMergeClusters(Long articleId);

	List<ClusterDto> fetchSlaveMergeClusters(Long articleId);

	public Optional<Article> fetchArticleByName(String name);

	public void addClustersToArticle(Long articleId, ClusterArticleDetailDto clusterIds);

	public void updateMasterToSlave(Long articleId, ClusterArticleDetailDto clusterIds);

	public void updateSlaveToMaster(Long articleId, ClusterArticleDetailDto clusterIds);

	public void addSubArticlesToArticle(Long articleId, SubArticleDetailDto subArticleIds, String user);

	public void deleteAssignSubArticles(Long leadId, SubArticleDetailDto subArticles, String user);

	List<ArticleDto> fetchSubArticles(Long leadArticle);

	public void deleteLeadArticles(Long leadId);

	List<ClusterDto> fetchClusterDetailsByParts(Long articleId, int partStatus);

	List<ClusterDto> fetchClusterDetailsByPartMasterStatus(Long articleId, int masterStatus);

	// setting master partlist to cluster partlist record
	public void assignMasterStatus(Long articleId, ClusterArticleDetailDto clusterIds);

	public void removePartListMasterStatus(Long articleId, ClusterArticleDetailDto clusters);

	public void addPartListMasterStatus(Long articleId, ClusterArticleDetailDto clusters);

	// for adding clusters to a lead article partlist:
	public void addClustersToLeadArticlePartlist(Long articleId, ClusterArticleDetailDto clusterIds);

	public void addPartListArticlesToClusters(Long articleId, ClusterArticleDetailDto clusterIds);

	public void syncMasterArticlesToClusters(Long articleId,ArticleToClusterSyncDto payload, String user);

	public ArticleI18NameDTO fetchArticleLabel(Long articleId);

	List<ClusterDto> fetchUnassignedClusters(Long articleId);
	
}