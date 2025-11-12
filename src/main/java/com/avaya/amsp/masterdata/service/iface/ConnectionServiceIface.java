package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;

import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.masterdata.dtos.ArticleDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesReqDto;
import com.avaya.amsp.masterdata.dtos.ArticlePropertyAttributeDto;
import com.avaya.amsp.masterdata.dtos.ArticleToConnectionReqDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ClustersToConnectionDto;
import com.avaya.amsp.masterdata.dtos.ConnectionDto;
import com.avaya.amsp.security.user.AMSPUser;

public interface ConnectionServiceIface {

	List<ConnectionDto> fetchAllConnections();

	public void persistConnection(ConnectionDto dto);

	public void updateConnection(ConnectionDto dto);

	public void removeConnection(Long connectionId);

	public Optional<Connection> fetchConnectionByName(String name);

	public void addArticles(Long connectionId, ArticleToConnectionReqDto articles);

	public void removeArticles(Long connectionId, ArticleToConnectionReqDto articles);

	public List<ArticleDto> fetchArticles(Long connectionId);

	public void updateArticles(Long connectionId, ArticleToConnectionReqDto articles);

	public List<ArticlePropertyAttributeDto> fetchArticleProperties(Long connectionId);

	public List<ArticlePropertiesDto> fetchArticlePropertiesInsert(Long connectionId, Long clusterId);
	
	public void updateArticleProperties(Long connectionId, ArticlePropertiesReqDto articles);

	public List<ClusterDto> fetchClustersByConnection(long connectionId);

	public void addClustersToConnection(Long connectionId, ClustersToConnectionDto clusters);

	public void deleteClustersFromConnection(Long connectionId, ClustersToConnectionDto clusters);

	public List<ArticlePropertiesDto> getArticlesByConnectionId(Long connectionId,Long clusterId,AMSPUser user);

}
