package com.avaya.amsp.masterdata.service.iface;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.avaya.amsp.domain.Site;
import com.avaya.amsp.masterdata.dtos.ArticlePropertiesDto;
import com.avaya.amsp.masterdata.dtos.ContractFlDto;
import com.avaya.amsp.masterdata.dtos.PoolDto;
import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.dtos.SiteToPoolDto;

public interface SiteServiceIface {

	List<SiteDto> fetchAllSites();

	public void createSite(SiteDto siteDto);

	public void updateSite(SiteDto siteDto);

	public void deleteSite(Long siteId);

	public Optional<Site> fetchSiteByName(String name);

	List<SiteDto> fetchSitesByCluster(Long id);

	public void addAssignPools(Long siteId, SiteToPoolDto pools);

	public void deleteAssignPools(Long siteId, SiteToPoolDto pools);

	public List<ContractFlDto> getContractFlsForSite(Long siteId);

	Set<PoolDto> getPoolsForSite(Long siteId);

	public List<ShippingAddressDto> getShippingAddressForSite(Long siteId);

	boolean addContractFlsForSite(Long siteId, List<ContractFlDto> contractFlDto, String user);

	boolean deleteContractFlsForSite(Long siteId, List<Long> contractFls, String user);

	List<ArticlePropertiesDto> getArticlesForSitePool(String defaultLanguage, Long siteId, Long poolId);
}
