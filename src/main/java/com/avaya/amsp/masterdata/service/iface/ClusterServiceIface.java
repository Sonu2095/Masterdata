package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.ClusterItem;
import com.avaya.amsp.masterdata.dtos.ArticleClusterDetailDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.masterdata.dtos.MasterArticleDto;
import com.avaya.amsp.masterdata.dtos.PbxClusterDto;
import com.avaya.amsp.masterdata.dtos.PbxNumberLockDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;

public interface ClusterServiceIface {

	List<ClusterDto> fetchAllClusters();
	
	List<ClusterDto> fetchClustersByClusterIds(List<Long> clusterIds);

	public void createCluster(ClusterDto clusterDto);

	public void updateCluster(ClusterDto clusterDto);

	public void deleteCluster(Long clusterId);

	public Optional<ClusterItem> fetchClusterByName(String name);

	public List<ConnectionDto> fetchConnectionsByCluster(long clusterId);

	public List<ArticleToClusterDto> fetchArticlesByCluster(Long clusterId);
	
	public List<ArticleToClusterDto> fetchArticlesByClusterForPartList(Long clusterId);
	
	public List<ArticleToClusterDto> fetchArticlesByClusterPoolEnabled(Long clusterId);

	public void addArticlesToCluster(Long clusterId, ArticleClusterDetailDto articleIds);

	public void deleteArticlesFromCluster(Long clusterId, ArticleClusterDetailDto articles);

	List<PbxClusterDto> fetchPbxByCluster(Long id);

	List<PbxNumberLockDto> fetchAllNumberLockByCluser(Long idCluster);

	public List<PbxSystemDto> fetchAllPbxSystemByCluster(Long idCluster);

	public void addArticlesToClusterForConnection(Long clusterId, MasterArticleDto articleIds);

	List<ArticleToClusterDto> fetchArticlesByClusterForPool(Long clusterId);

}
