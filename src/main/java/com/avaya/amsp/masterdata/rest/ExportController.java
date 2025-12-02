package com.avaya.amsp.masterdata.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.amsp.masterdata.dtos.ArticleDto;
import com.avaya.amsp.masterdata.dtos.ArticleLabelDto;
import com.avaya.amsp.masterdata.dtos.ArticleToClusterDto;
import com.avaya.amsp.masterdata.dtos.CalendarDto;
import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import com.avaya.amsp.masterdata.dtos.CalendarWorkWeekDto;
import com.avaya.amsp.masterdata.dtos.ClusterDto;
import com.avaya.amsp.masterdata.dtos.ExportConnectionDto;
import com.avaya.amsp.masterdata.dtos.ExportPoolDto;
import com.avaya.amsp.masterdata.dtos.PbxClusterDto;
import com.avaya.amsp.masterdata.dtos.PbxSystemDto;
import com.avaya.amsp.masterdata.dtos.ShippingAddressDto;
import com.avaya.amsp.masterdata.dtos.SiteDto;
import com.avaya.amsp.masterdata.dtos.SubscriberDto;
import com.avaya.amsp.masterdata.dtos.UserDto;
import com.avaya.amsp.masterdata.service.ExportArticleLabelService;
import com.avaya.amsp.masterdata.service.ExportArticleService;
import com.avaya.amsp.masterdata.service.ExportCalendarDataService;
import com.avaya.amsp.masterdata.service.ExportCalendarHolidaysDataService;
import com.avaya.amsp.masterdata.service.ExportCalendarWorkweekDataService;
import com.avaya.amsp.masterdata.service.ExportClusterService;
import com.avaya.amsp.masterdata.service.ExportConnectionService;
import com.avaya.amsp.masterdata.service.ExportPbxClusterDataService;
import com.avaya.amsp.masterdata.service.ExportPbxSystemDataService;
import com.avaya.amsp.masterdata.service.ExportPoolService;
import com.avaya.amsp.masterdata.service.ExportShippingAssemblyAddrService;
import com.avaya.amsp.masterdata.service.ExportSiteService;
import com.avaya.amsp.masterdata.service.ExportSubscriberService;
import com.avaya.amsp.masterdata.service.ExportUserDataService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/v1")
public class ExportController {
	
	private static final String MEDIA_TYPE_EXCEL = "application/vnd.ms-excel";
	
	@Autowired
	private ExportArticleService exportArticleService;
	
	@Autowired
	private ExportClusterService exportClusterService;
	
	@Autowired
	private ExportSiteService exportSiteService;
	
	@Autowired
	private ExportPoolService exportPoolService;
	
	@Autowired
	private ExportConnectionService exportConnectionService;
	
	@Autowired
	private ExportShippingAssemblyAddrService exportShippingAddrService;
	
	@Autowired
	private ExportArticleLabelService exportArticleLabelService;
	
	@Autowired
	private ExportSubscriberService exportSubscriberDataService;
	 
	@Autowired 
	private ExportCalendarDataService exportCalendarDataService;

	@Autowired
	private ExportCalendarHolidaysDataService exportCalendarHolidaysDataService;
	  
	@Autowired
	private ExportCalendarWorkweekDataService exportCalendarWorkweekDataService;
	  
	/*
	 * @Autowired private ExportCalendarDaylightSavingsDataService
	 * exportCalendarDaylightSavingsDataService;
	 */
	  
	@Autowired
	private ExportPbxClusterDataService exportPbxClusterDataService;

	@Autowired
	private ExportPbxSystemDataService exportPbxSystemDataService;
	
	@Autowired
	private ExportUserDataService exportUserDataService;
	 
	
	private static final String ARTICLE_DATA_FILE_NAME = "ArticleData.xlsx";
	
	private static final String ARTICLE_DATA_BY_CLUSTER_FILE_NAME = "ArticleDataByCluster.xlsx";
	
	private static final String CLUSTER_DATA_FILE_NAME = "ClusterData.xlsx";
	
	private static final String SITE_DATA_FILE_NAME = "SiteData.xlsx";
	
	private static final String POOL_DATA_FILE_NAME = "PoolData.xlsx";
	
	private static final String CONNECTION_DATA_FILE_NAME = "ConnectionData.xlsx";
	
	private static final String SHIPPING_DATA_FILE_NAME = "ShippingAssemblyAddressData.xlsx";
	
	private static final String ARTICLE_LABEL_FILE_NAME = "ArticleLabelData.xlsx";
	
	private static final String SUBSCRIBER_FILE_NAME = "SubscriberData.xlsx";
	  
	private static final String CALENDAR_FILE_NAME = "CalendarData.xlsx";
	 
	private static final String CALENDAR_HOLIDAYS_FILE_NAME = "CalendarHolidaysData.xlsx";
	  
	private static final String CALENDAR_WORKWEEK_FILE_NAME = "CalendarWorkweekData.xlsx";
	  
	private static final String CALENDAR_DAYLIGHT_SAVING_FILE_NAME = "CalendarDaylightSavingData.xlsx";
	  
	 private static final String PBX_CLUSTER_FILE_NAME = "PbxClusterData.xlsx";
	 
	 private static final String PBX_SYSTEM_FILE_NAME = "PbxSystemData.xlsx";
	 
	 private static final String USER_DATA_FILE_NAME = "UserData.xlsx";
	 
	
	@GetMapping("/exports/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportMasterArticleData() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + ARTICLE_DATA_FILE_NAME).body(exportArticleService.exportArticleData());
	}
	
	@GetMapping("/data/articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getMasterArticleData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ArticleDto> articleList = exportArticleService.getArticleData(pageable);
        if (articleList == null || articleList.isEmpty()) {
        	articleList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(articleList);
	}
	
	@GetMapping("/exports/cluster-articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportMasterArticleDataByCluster(@RequestParam(name = "clusterName", required = true) String clusterName) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + ARTICLE_DATA_BY_CLUSTER_FILE_NAME).body(exportArticleService.exportArticleDataByCluster(clusterName));
	}
	
	@GetMapping("/data/cluster-articles")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getMasterArticleDataByCluster(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "clusterName", required = true) String clusterName) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ArticleToClusterDto> articleList = exportArticleService.getArticleDataByCluster(clusterName, pageable);
        if (articleList == null || articleList.isEmpty()) {
        	articleList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(articleList);
	}
	
	@GetMapping("/exports/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportMasterClusterData() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + CLUSTER_DATA_FILE_NAME).body(exportClusterService.exportClusterData());
	}
	
	@GetMapping("/data/clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getMasterClusterData(@RequestParam int page, 
	        @RequestParam int size,
	        @RequestParam(required = false) String[] sort) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ClusterDto> clusterList = exportClusterService.getClusterData(pageable);
        if (clusterList == null || clusterList.isEmpty()) {
        	clusterList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(clusterList);
	}
	
	@GetMapping("/exports/sites")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportMasterSiteData(@RequestParam(name = "clusterKey", required = true) Long clusterKey) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + SITE_DATA_FILE_NAME).body(exportSiteService.exportSiteData(clusterKey));
	}
	
	@GetMapping("/data/sites")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getMasterSiteData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "clusterKey", required = true) Long clusterId) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<SiteDto> siteList = exportSiteService.getSiteData(clusterId, pageable);
        if (siteList == null || siteList.isEmpty()) {
        	siteList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(siteList);
	}
	
	@GetMapping("/exports/pools")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportMasterPoolData(@RequestParam(name = "siteKey", required = true) Long siteKey) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + POOL_DATA_FILE_NAME).body(exportPoolService.exportPoolData(siteKey));
	}
	
	@GetMapping("/data/pools")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getMasterPoolData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "siteKey", required = true) Long siteKey) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ExportPoolDto> poolList = exportPoolService.getPoolData(siteKey, pageable);
        if (poolList == null || poolList.isEmpty()) {
        	poolList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(poolList);
	}
	
	@GetMapping("/exports/connections")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportMasterConnectionData(@RequestParam(name = "clusterKey", required = true) Long clusterKey) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + CONNECTION_DATA_FILE_NAME).body(exportConnectionService.exportConnectionData(clusterKey));
	}
	
	@GetMapping("/data/connections")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getMasterConnectionData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "clusterKey", required = true) Long clusterKey) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ExportConnectionDto> connectionList = exportConnectionService.getConnectionData(clusterKey, pageable);
        if (connectionList == null || connectionList.isEmpty()) {
        	connectionList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(connectionList);
	}
	
	@GetMapping("/exports/shipping-addresses")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportShippingAddressData(@RequestParam(name = "siteKey", required = true) Long siteKey) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + SHIPPING_DATA_FILE_NAME).body(exportShippingAddrService.exportShippingData(siteKey));
	}
	
	@GetMapping("/data/shipping-addresses")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getShippingAddressData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "siteKey", required = true) Long siteKey) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ShippingAddressDto> shippingAddressList = exportShippingAddrService.getShippingData(siteKey, pageable);
        if (shippingAddressList == null || shippingAddressList.isEmpty()) {
        	shippingAddressList = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(shippingAddressList);
	}
	
	@GetMapping("/exports/article-labels")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportArticleLabel() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL)).
				header("Content-Disposition", "attachment; filename=" + ARTICLE_LABEL_FILE_NAME).body(exportArticleLabelService.exportArticleLabel());
	}
	
	@GetMapping("/data/article-labels")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getArticleLabel(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<ArticleLabelDto> articles = exportArticleLabelService.getArticleLabel(pageable);
        if (articles == null || articles.isEmpty()) {
        	articles = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(articles);
	}
	
	@GetMapping("/exports/subscribers")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportSubscriberData(
			@RequestParam(name = "clusterKey", required = true) Long clusterKey) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + SUBSCRIBER_FILE_NAME)
				.body(exportSubscriberDataService.exportSubscriberData(clusterKey));
	}
	
	@GetMapping("/data/subscribers")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getSubscriberData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort, 
	        @RequestParam(name = "clusterKey", required = true) Long clusterKey) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<SubscriberDto> subscriberData = exportSubscriberDataService.getSubscriberData(clusterKey, pageable);
        if (subscriberData == null || subscriberData.isEmpty()) {
        	subscriberData = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(subscriberData);
	}
	
	@GetMapping("/exports/calendars")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportCalendarData() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + CALENDAR_FILE_NAME)
				.body(exportCalendarDataService.exportCalendarData());
	}
	
	@GetMapping("/data/calendars")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Page<CalendarDto>> getCalendarData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort) {
		
        Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<CalendarDto> calendarData = exportCalendarDataService.getAllCalendars(pageable);
        if (calendarData == null || calendarData.isEmpty()) {
        	calendarData = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(calendarData);
	}
	 
	@GetMapping("/exports/calendar-holidays")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportCalendarHolidayData() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + CALENDAR_HOLIDAYS_FILE_NAME)
				.body(exportCalendarHolidaysDataService.exportCalendarHolidayData());
	}
	
	@GetMapping("/data/calendar-holidays")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getCalendarHoliday(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<CalendarHolidaysDto> calendarHolidaysData = exportCalendarHolidaysDataService.getAllCalendarHolidayData(pageable);
        if (calendarHolidaysData == null || calendarHolidaysData.isEmpty()) {
        	calendarHolidaysData = Page.empty(pageable); // create an empty page with pagination info
        }
        
		return ResponseEntity.ok(calendarHolidaysData);
	}
	
	@GetMapping("/exports/calendar-workweek")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportCalendarWorkweek() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + CALENDAR_WORKWEEK_FILE_NAME)
				.body(exportCalendarWorkweekDataService.exportCalendarWorkweekData());
	}
	
	@GetMapping("/data/calendar-workweek")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getCalendarWorkweek(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<CalendarWorkWeekDto> calendarWorkweekData = exportCalendarWorkweekDataService.getAllCalendarWorkweekData(pageable);
        if (calendarWorkweekData == null || calendarWorkweekData.isEmpty()) {
        	calendarWorkweekData = Page.empty(pageable);
        }
		
        return ResponseEntity.ok(calendarWorkweekData);
	}
	  
	/*
	 * @GetMapping("/exports/calendar-daylight-savings")
	 * 
	 * @PreAuthorize(
	 * "hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	 * public ResponseEntity<Object> exportCalendarDaylightSaving() { return
	 * ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(
	 * MEDIA_TYPE_EXCEL)) .header("Content-Disposition", "attachment; filename=" +
	 * CALENDAR_DAYLIGHT_SAVING_FILE_NAME)
	 * .body(exportCalendarDaylightSavingsDataService.
	 * exportCalendarDaylightSavingData()); }
	 */
	
	/*
	 * @GetMapping("/data/calendar-daylight-savings")
	 * 
	 * @PreAuthorize(
	 * "hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	 * public ResponseEntity<Object> getCalendarDaylightSaving(@RequestParam int
	 * page,
	 * 
	 * @RequestParam int size,
	 * 
	 * @RequestParam(required = false) String[] sort) {
	 * 
	 * Sort sortObject = Sort.by(sort != null ? sort : new String[0]); Pageable
	 * pageable = PageRequest.of(page, size, sortObject);
	 * Page<CalendarDayLightSavingDto> calendarDayLightSavingData =
	 * exportCalendarDaylightSavingsDataService.getAllCalendarDaylightSaving(
	 * pageable); if (calendarDayLightSavingData == null ||
	 * calendarDayLightSavingData.isEmpty()) { calendarDayLightSavingData =
	 * Page.empty(pageable); }
	 * 
	 * return ResponseEntity.ok(calendarDayLightSavingData); }
	 */
	  
	@GetMapping("/exports/pbx-clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportPbxClusterData(@RequestParam(name = "clusterKey", required = true) Long clusterId) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + PBX_CLUSTER_FILE_NAME)
				.body(exportPbxClusterDataService.exportPbxClusterData(clusterId));
	}
	
	@GetMapping("/data/pbx-clusters")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getPbxClusterData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "clusterKey", required = true) Long clusterId) {

		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<PbxClusterDto> pbxClusterData = exportPbxClusterDataService.getPbxClusterData(clusterId, pageable);
        if (pbxClusterData == null || pbxClusterData.isEmpty()) {
        	pbxClusterData = Page.empty(pageable);
        }
		
        return ResponseEntity.ok(pbxClusterData);
	}
	 
	@GetMapping("/exports/pbx-systems")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportPbxSystemData(@RequestParam(name = "clusterKey", required = true) Long clusterId) {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + PBX_SYSTEM_FILE_NAME)
				.body(exportPbxSystemDataService.exportPbxSystemData(clusterId));
	}
	
	@GetMapping("/data/pbx-systems")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getPbxSystemData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort,
	        @RequestParam(name = "clusterKey", required = true) Long clusterId) {

		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<PbxSystemDto> pbxSystemData = exportPbxSystemDataService.getPbxSystemData(clusterId, pageable);
        if (pbxSystemData == null || pbxSystemData.isEmpty()) {
        	pbxSystemData = Page.empty(pageable);
        }
		
        return ResponseEntity.ok(pbxSystemData);
	}
	
	@GetMapping("/exports/users")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> exportUserData() {
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(MEDIA_TYPE_EXCEL))
				.header("Content-Disposition", "attachment; filename=" + USER_DATA_FILE_NAME)
				.body(exportUserDataService.exportUserData());
	}
	
	@GetMapping("/data/users")
	@PreAuthorize("hasAnyRole('CC_RESP','TK_P','AVAYA_ADMIN','AVAYA_HOTLINE','TK_SV')")
	public ResponseEntity<Object> getUserData(@RequestParam int page, 
	        @RequestParam int size, 
	        @RequestParam(required = false) String[] sort) {
		
		Sort sortObject = Sort.by(sort != null ? sort : new String[0]);
        Pageable pageable = PageRequest.of(page, size, sortObject);
        Page<UserDto> users = exportUserDataService.getAllUsers(pageable);
        if (users == null || users.isEmpty()) {
        	users = Page.empty(pageable);
        }
		
        return ResponseEntity.ok(users);
	}
}
