package com.avaya.amsp.masterdata.repo;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.avaya.amsp.domain.ArticleCategory;
import com.avaya.amsp.domain.ArticleClearingTypeEnum;
import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleWizard;
import com.avaya.amsp.domain.PartsPropertyEnum;
import com.avaya.amsp.domain.ServiceCode;
import com.avaya.amsp.masterdata.annotation.AuditLog;

import jakarta.persistence.QueryHint;

public interface ArticleClusterRepository extends JpaRepository<ArticleCluster, Long>, JpaSpecificationExecutor<ArticleCluster> {

	@Query(value = "select C from ARTICLE_CLUSTER C  where C.active=1 ORDER BY C.id ASC")
	@QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
	public List<ArticleCluster> findAll();

	@AuditLog(action = "INSERT",entity = "ArticleCluster",functionality = "add Master Data MasterArticle add cluster to Article")
	default List<ArticleCluster> saveAllArticleCluster(List<ArticleCluster> articleClusterList){
		return saveAll(articleClusterList);
	}
	
	@AuditLog(action = "Insert",entity = "ArticleCluster",functionality = "add Cluster to Artilce on Connection Mapping")
	default List<ArticleCluster> saveArticlesToCluster(List<ArticleCluster> articleClusterList){
		return saveAll(articleClusterList);
	}
	
	@AuditLog(action = "Update",entity = "ArticleCluster",functionality = "add Master Data MasterArticle Update Slave Attributes")
	default List<ArticleCluster> syncArticleClusterOnSlaveUpdate(List<ArticleCluster> articleClusterList){
		return saveAll(articleClusterList);
	}
	
	@AuditLog(action = "INSERT",entity = "ArticleCluster",functionality = "Import Master Data MasterArticle")
	default List<ArticleCluster> importMultipleArticleToCluster(List<ArticleCluster> articleClusterList){
		return saveAll(articleClusterList);
	}

	public ArticleCluster findByNameIgnoreCaseAndActive(String key, int active);

	public ArticleCluster findByName(String name);

	public List<ArticleCluster> findByActive(Long active);

	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.clusterItem.id= :clusterId AND C.article.id=:articleId")
	public List<ArticleCluster> findByClusterArticle(Long clusterId, Long articleId);

	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.article.id=:articleId AND C.clusterItem.id= :clusterId AND C.active=1")
	public List<ArticleCluster> findByArticleCluster(Long articleId, Long clusterId);

	@AuditLog(action = "Update",entity = "ArticleCluster",functionality = "Update Master Data MasterArticle Eliminate Master Linkage")
	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.masterArticle=0 where C.article.id=:articleId AND C.clusterItem.id in :clusterIds")
	public void updateMasterToSlave(Long articleId, List<Long> clusterIds);
	
	@Transactional(propagation = Propagation.REQUIRES_NEW,readOnly = true)
	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.article.id=:articleId AND C.clusterItem.id in :clusterIds")
	public List<ArticleCluster> findAllByArticleIdAndClusterIds(Long articleId, List<Long> clusterIds);

	@AuditLog(action = "Update",entity = "ArticleCluster",functionality = "Update Master Data MasterArticle Set Master Linkage")
	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.masterArticle=1 where C.article.id=:articleId AND C.clusterItem.id in :clusterIds")
	public void updateSlaveToMaster(Long articleId, List<Long> clusterIds);

	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.article.id=:id and C.active=1")
	public List<ArticleCluster> findByArticleId(Long id);
	
	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.clusterItem.name=:clusterName and C.active=1")
	public List<ArticleCluster> findByClusterName(String clusterName);
	
	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.clusterItem.name=:clusterName and C.active=1")
	public Page<ArticleCluster> findByClusterName(String clusterName, Pageable pageable);

	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.active=0 where C.article.id=:articleId")
	public void updateActiveStatus(Long articleId);

	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.description=:description,C.pricePurchaseDollar=:pricePurchaseDollar,C.pricePurchaseEuro=:pricePurchaseEuro,C.priceSalesDollar=:priceSalesDollar,C.priceSalesEuro=:priceSalesEuro,C.valueDefault=:valueDefault,C.valueReadOnly=:valueReadOnly,C.singleArticle=:singleArticle,C.masterArticle=:masterArticle,C.hardwareFromAvaya=:hardwareFromAvaya,C.subjectToAuthorization=:subjectToAuthorization,C.billing=:billing,C.incidentArticle=:incidentArticle,C.servusInterface=:servusInterface,C.hidden=:hidden,C.nonAvailable=:nonAvailable,C.shippingAddress=:shippingAddress,C.assemblingAddress=:assemblingAddress,C.priority=:priority,C.poolHandling=:poolHandling,C.clearingAtNewConnection=:clearingAtNewConnection,C.clearingAtChangeMove=:clearingAtChangeMove,C.clearingAtDelete=:clearingAtDelete,C.lifeTime=:lifeTime,C.slaDays=:slaDays,C.slaHours=:slaHours,C.slaMinutes=:slaMinutes,C.sapAvaya=:sapAvaya,C.sapBosh=:sapBosh,C.quantifier=:quantifier,C.articleCategory=:articleCategory,C.articleClearingType=:articleClearingType,C.serviceCodeCluster=:serviceCode,C.logUpdatedBy=:logUpdatedBy, C.logUpdatedOn=:logUpdatedOn, C.property=:property,C.valueTransfer=:valueTransfer,C.articleWizardType=:articleWizardType where C.id=:id")
	public void updateArticleCluster(

			@Param("id") long id, @Param("description") String description,
			@Param("pricePurchaseDollar") Double pricePurchaseDollar,
			@Param("pricePurchaseEuro") Double pricePurchaseEuro, @Param("priceSalesDollar") Double priceSalesDollar,
			@Param("priceSalesEuro") Double priceSalesEuro, @Param("valueDefault") String valueDefault,
			@Param("valueReadOnly") Boolean valueReadOnly, @Param("singleArticle") Boolean singleArticle,
			@Param("masterArticle") Integer masterArticle, @Param("hardwareFromAvaya") Integer hardwareFromAvaya,
			@Param("subjectToAuthorization") Integer subjectToAuthorization, @Param("billing") Integer billing,
			@Param("incidentArticle") Integer incidentArticle, @Param("servusInterface") Integer servusInterface,
			@Param("hidden") Integer hidden, @Param("nonAvailable") Integer nonAvailable,
			@Param("shippingAddress") Integer shippingAddress, @Param("assemblingAddress") Integer assemblingAddress,
			@Param("priority") Integer priority, @Param("poolHandling") Integer poolHandling,
			@Param("clearingAtNewConnection") Integer clearingAtNewConnection,
			@Param("clearingAtChangeMove") Integer clearingAtChangeMove,
			@Param("clearingAtDelete") Integer clearingAtDelete, @Param("lifeTime") Integer lifeTime,
			@Param("slaDays") Integer slaDays, @Param("slaHours") Integer slaHours,
			@Param("slaMinutes") Integer slaMinutes, @Param("sapAvaya") String sapAvaya,
			@Param("sapBosh") String sapBosh, @Param("quantifier") Integer quantifier,
			@Param("articleCategory") ArticleCategory articleCategory,
			@Param("articleClearingType") ArticleClearingTypeEnum articleClearingType,
			@Param("serviceCode") ServiceCode serviceCode, @Param("logUpdatedBy") String logUpdatedBy,
			@Param("logUpdatedOn") Timestamp logUpdatedOn, @Param("property") PartsPropertyEnum property,
			@Param("valueTransfer") Boolean valueTransfer, @Param("articleWizardType") ArticleWizard articleWizardType);

	// for article cluster Partlist:

	@Query(value = "select C from ARTICLE_CLUSTER C  where C.isPart=:partStatus AND C.active=1 AND C.clusterItem.id =:clusterId ORDER BY C.id ASC")
	public List<ArticleCluster> findArticleClusterByPartsStatus(int partStatus, Long clusterId);

	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.isPart=:isPart, C.logUpdatedBy=:user, C.logUpdatedOn=:ts where C.id=:articleId")
	public void updateIsPartStatus(int isPart, long articleId, String user, Timestamp ts);

	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.isPart=:isPart,C.masterPartStatus=true  where C.article.id=:articleId AND C.clusterItem.id= :clusterId")
	public void updateMasterPartStatus(int isPart, Long articleId, Long clusterId);

	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.masterPartStatus=false, C.isPart=0, C.logUpdatedBy=:user, C.logUpdatedOn=:ts where C.id=:articleId")
	public void removeMasterStatus(Long articleId, String user, Timestamp ts);

	@AuditLog(action = "update",entity = "ArticleCluster",functionality = "Remove Article PartList MasterPart Status")
	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.masterPartStatus=false, C.logUpdatedBy=:user, C.logUpdatedOn=:ts  where C.article.id=:articleId AND C.clusterItem.id in :clusterIds")
	public void removeArticleMasterPartStatus(Long articleId, List<Long> clusterIds, String user, Timestamp ts);

	@AuditLog(action = "update",entity = "ArticleCluster",functionality = "Add Article PartList MasterPart Status")
	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.masterPartStatus=true, C.logUpdatedBy=:user, C.logUpdatedOn=:ts where C.article.id=:articleId AND C.clusterItem.id in :clusterIds")
	public void addArticleMasterPartStatus(Long articleId, List<Long> clusterIds , String user, Timestamp ts);

	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.article.id=:articleId AND C.clusterItem.id in :clusterIds AND C.active=1")
	public List<ArticleCluster> fetchArticleClusterByClusterIds(Long articleId,List<Long> clusterIds);

	@Modifying
	@Query(value = "update ARTICLE_CLUSTER C set C.isPart= 1,C.masterPartStatus=true,property= :property,C.valueTransfer=:valueTransfer  where C.article.id=:articleId AND C.clusterItem.id in :clusterIds")
	public void updateLeadArticlePartProperties(Long articleId, List<Long> clusterIds,PartsPropertyEnum property,boolean valueTransfer);

	@Query(value = "select C from ARTICLE_CLUSTER  C where  C.article.id in :articleIds AND C.clusterItem.id= :clusterId AND C.active=1")
	public List<ArticleCluster> fetchArticleClusters(List<Long> articleIds, Long clusterId);
}
