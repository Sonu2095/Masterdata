package com.avaya.amsp.masterdata.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.avaya.amsp.domain.ArticleCluster;
import com.avaya.amsp.domain.ArticleConnection;
import com.avaya.amsp.domain.ArticlePool;
import com.avaya.amsp.domain.CalendarClusterMapping;
import com.avaya.amsp.domain.CalendarWorkWeek;
import com.avaya.amsp.domain.ClusterConnection;
import com.avaya.amsp.domain.ClusterRegion;
import com.avaya.amsp.domain.PartListSubArticle;
import com.avaya.amsp.domain.SitePool;
import com.avaya.amsp.masterdata.annotation.AuditLog;
import com.avaya.amsp.masterdata.dtos.ArticlePropertyAttributeDto;
import com.avaya.amsp.masterdata.dtos.ArticleToConnectionReqDto;
import com.avaya.amsp.masterdata.dtos.CalendarDto;
import com.avaya.amsp.masterdata.dtos.CalendarHolidaysDto;
import com.avaya.amsp.masterdata.dtos.CalendarWorkWeekDto;
import com.avaya.amsp.masterdata.dtos.ClustersToConnectionDto;
import com.avaya.amsp.masterdata.dtos.PoolToArticleDto;
import com.avaya.amsp.masterdata.dtos.RegionToClusterDto;
import com.avaya.amsp.masterdata.dtos.SiteToPoolDto;
import com.avaya.amsp.masterdata.dtos.SubArticleDetailDto;
import com.avaya.amsp.masterdata.repo.ArticleClusterRepository;
import com.avaya.amsp.masterdata.repo.ArticleConnectionRepository;
import com.avaya.amsp.masterdata.repo.ArticlePoolRepository;
import com.avaya.amsp.masterdata.repo.ArticleRepository;
import com.avaya.amsp.masterdata.repo.CalendarClusterMappingRepository;
import com.avaya.amsp.masterdata.repo.CalendarHolidaysRepository;
import com.avaya.amsp.masterdata.repo.CalendarRepository;
import com.avaya.amsp.masterdata.repo.CalendarWorkWeekRepository;
import com.avaya.amsp.masterdata.repo.ClusterConnectionRepository;
import com.avaya.amsp.masterdata.repo.ClusterRegionRepository;
import com.avaya.amsp.masterdata.repo.ClusterRepository;
import com.avaya.amsp.masterdata.repo.ConnectionRepository;
import com.avaya.amsp.masterdata.repo.PartListSubArticleRepository;
import com.avaya.amsp.masterdata.repo.PoolRepository;
import com.avaya.amsp.masterdata.repo.RegionRepository;
import com.avaya.amsp.masterdata.repo.ShippingAddressRepository;
import com.avaya.amsp.masterdata.repo.SitePoolRepository;
import com.avaya.amsp.masterdata.repo.SiteRepository;
import com.avaya.amsp.masterdata.repo.TemplateConfigurationRepository;
import com.avaya.amsp.security.user.AMSPUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kumar32 This class will act as Aspect AOP
 * containing logics for Auditing purpose for pre&post changes on apis
 * 
 */
@Aspect
@Slf4j
@Component
public class AuditLoggingAspect {
	
	@Autowired
    private ObjectMapper objectMapper; // for JSON logging
    
    @Autowired
    private ArticleRepository articleRepo;
    
    @Autowired
	private ClusterRepository clusterRepo;
    
    @Autowired
	private ArticleClusterRepository articleClusterRepo;
    
    @Autowired
	private SiteRepository siteRepo;
    
    @Autowired
	private SitePoolRepository sitePoolRepo;
    
    @Autowired
	private RegionRepository regionRepo;
    
    @Autowired
	private ClusterRegionRepository clusterRegionRepo;
    
    @Autowired
	private PoolRepository poolRepo;
    
    @Autowired
	private ArticlePoolRepository articlePoolRepo;
    
    @Autowired
	ConnectionRepository connectionRepo;
    
    @Autowired
	ArticleConnectionRepository articleConnectionRepo;
    
    @Autowired
	ClusterConnectionRepository clusterConnRepo;
    
    @Autowired
	PartListSubArticleRepository partListRepo;
    
    @Autowired
	private ShippingAddressRepository shippingRepo;
    
    @Autowired
    private TemplateConfigurationRepository templateRepository;
    
    @Autowired
	private CalendarRepository calendarRepository;
    
    @Autowired
	private CalendarClusterMappingRepository calendarClusterRepo;
    
    @Autowired
    private CalendarWorkWeekRepository workWeekRepository;
    
    @Autowired
	private CalendarHolidaysRepository holidaysRepository;
    
    /**
	 * Purpose of this method is act as an advise Aop @Around and used as an 
	 * @AuditLog annotation on apis where we can Audit pre&post changes 
	 * @Around Advice will execute two time 1.Method will invoked 2.After its execution
	 */
    @Around("@annotation(auditLog)")
    public Object handleAuditLog(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
    	
    	String username = getUsername();
        String oldDataJson = null;
        String newDataJson = null;
        boolean success = false;
        Object id = null;
        List<ArticleCluster> oldArticleCluster = null;
        List<Long> IdsList = null;
        boolean deleteAssignedData = false;
        String deletedId = null;
        String deletedListIds = null;
        List<ArticleConnection> oldArticleConnectionList = null;
        List<ClusterConnection> oldClusterConnectionList = null;
        List<PartListSubArticle> oldPartListSubArticleList = null;
        
        try {
        	log.info("Auditing Started for functionality {} ",auditLog.functionality());
        	
        	Object[] args = joinPoint.getArgs();
        	Object dto = Arrays.stream(args).filter(a -> a != null && a.getClass().getSimpleName().endsWith("Dto")).findFirst().orElse(null);

            // For update/delete: fetch old entity from DB
			if ("UPDATE".equalsIgnoreCase(auditLog.action())) {
				id = extractIdFromArgs(args);
				if (id != null) {
					Object oldEntity = fetchEntityById(auditLog.entity(), id);
					oldDataJson = toJson(oldEntity);
				}
				if (Objects.isNull(dto)) {
					if (auditLog.functionality().equalsIgnoreCase("Update Master Data MasterArticle Eliminate Master Linkage")
							|| auditLog.functionality().equalsIgnoreCase("Update Master Data MasterArticle Set Master Linkage")) {
						Long articleId = (Long) args[0];
						List<Long> clusterIds = (List<Long>) args[1];
						oldArticleCluster = articleClusterRepo.findAllByArticleIdAndClusterIds(articleId, clusterIds);
						oldDataJson = toJson(oldArticleCluster);
					}
					else if (auditLog.functionality().equalsIgnoreCase("add Master Data MasterArticle Update Slave Attributes")) {
						List<ArticleCluster> listArticleClusters = (List<ArticleCluster>) args[0];
						Long articleId = listArticleClusters.get(0).getArticle().getId();
						List<Long> clusterIds = listArticleClusters.stream().map(e -> e.getClusterItem().getId()).toList();								
						oldArticleCluster = articleClusterRepo.findAllByArticleIdAndClusterIds(articleId, clusterIds);
						oldDataJson = toJson(oldArticleCluster);
					}
					else if (auditLog.functionality().equalsIgnoreCase("update Articles Properties from connection")) {
						Long connectionId = (Long) args[0];
						List<ArticlePropertyAttributeDto> articlePorpertiesListDto = (List<ArticlePropertyAttributeDto>) args[1];
						List<Long> articleIds = articlePorpertiesListDto.stream().map(e -> e.getArticleId()).toList();								
						oldArticleConnectionList = articleConnectionRepo.findAllByConnIdAndArticleIds(connectionId, articleIds);
						oldDataJson = toJson(oldArticleConnectionList);
					}
					else if (auditLog.functionality().equalsIgnoreCase("Remove Article PartList MasterPart Status")||
							auditLog.functionality().equalsIgnoreCase("Add Article PartList MasterPart Status")) {
						id=args[0];
						IdsList = (List<Long>) args[1];							
						oldArticleCluster = articleClusterRepo.findAllByArticleIdAndClusterIds((Long)id, IdsList);
						oldDataJson = toJson(oldArticleCluster);
					}
					
				}else if(Objects.nonNull(dto) && auditLog.functionality().equalsIgnoreCase("Update Existing New Calender")) {
					CalendarDto calendarDto = (CalendarDto) dto;
					String calendarKey = calendarDto.getCalendarKey();
					Object oldEntity = fetchEntityById(auditLog.entity(), calendarKey);
					oldDataJson = toJson(oldEntity);
					
				}
				else if(Objects.nonNull(dto) && auditLog.functionality().equalsIgnoreCase("Update Workweek to Calendar")) {
					CalendarWorkWeekDto calendarWorkWeekDto =  (CalendarWorkWeekDto) args[0];
					String calendarKey = calendarWorkWeekDto.getCalendarKey();				
					List<CalendarWorkWeek> oldCalendarWorkWeek = workWeekRepository.findByCalendar_CalendarKey(calendarKey);					
					oldDataJson = toJson(oldCalendarWorkWeek);					
				}
				else if(Objects.nonNull(dto) && auditLog.functionality().equalsIgnoreCase("update existing CalendarHolidays")) {
					CalendarHolidaysDto calendarHolidaysDto = (CalendarHolidaysDto) args[1];
					id=calendarHolidaysDto.getIdHoliday();
					Object oldEntity = fetchEntityById(auditLog.entity(), id);
					oldDataJson = toJson(oldEntity);					
				}
			}
            
			else if("DELETE".equalsIgnoreCase(auditLog.action())){
            	if(auditLog.functionality().equalsIgnoreCase("delete Pool Assign to Site")) {
            		id=args[0];
            		SiteToPoolDto siteToPoolDto = (SiteToPoolDto) dto;
            		IdsList = siteToPoolDto.getPoolIds();
            		deletedId="SiteId";
            		deletedListIds="PoolIds";
            		List<SitePool> oldSitePoolRecords = sitePoolRepo.findAllBySiteIdAndPoolIds((Long)id,IdsList);
            		oldDataJson = toJson(oldSitePoolRecords);
            	}
            	else if(auditLog.functionality().equalsIgnoreCase("delete Clusters From Region")) {
            		id=args[0];
            		RegionToClusterDto regionToClusterDto = (RegionToClusterDto) dto;
            		IdsList = regionToClusterDto.getClusterIds();
            		deletedId="RegionId";
            		deletedListIds="ClustersId";
            		List<ClusterRegion> oldClusterRegionList = clusterRegionRepo.findAllByRegionIdAndClusterIds((Long)id,IdsList);
            		oldDataJson = toJson(oldClusterRegionList);
            	}else if(auditLog.functionality().equalsIgnoreCase("delete Articles From Pool")) {
            		id=args[0];
            		PoolToArticleDto poolToArticleDtoDto = (PoolToArticleDto) dto;
            		IdsList = poolToArticleDtoDto.getArticleIds();
            		deletedId="PoolId";
            		deletedListIds="ArticlesId";
            		List<ArticlePool> oldClusterRegionList = articlePoolRepo.findAllByPoolIdAndArticleIds((Long)id,IdsList);
            		oldDataJson = toJson(oldClusterRegionList);
            	}else if(auditLog.functionality().equalsIgnoreCase("delete Articles from Connection")) {
            		id=args[0];
            		ArticleToConnectionReqDto articleToConnectionReqDto = (ArticleToConnectionReqDto) dto;
            		IdsList = articleToConnectionReqDto.getArticleIds();
            		deletedId="connectionId";
            		deletedListIds="ArticlesId";
            		oldArticleConnectionList = articleConnectionRepo.findAllByConnIdAndArticleIds((Long)id,IdsList);
            		oldDataJson = toJson(oldArticleConnectionList);
            	}else if(auditLog.functionality().equalsIgnoreCase("delete Clusters From Connection")) {
            		id=args[0];
            		ClustersToConnectionDto clustersToConnectionDto = (ClustersToConnectionDto) dto;
            		IdsList = clustersToConnectionDto.getClusterIds();
            		deletedId="connectionId";
            		deletedListIds="ClustersId";
            		oldClusterConnectionList = clusterConnRepo.findAllByConnIdAndClustersIds((Long)id,IdsList);
            		oldDataJson = toJson(oldClusterConnectionList);
            	}else if (auditLog.functionality().equalsIgnoreCase("delete Sub Article to Article")) {
					id = (Long) args[0];
					SubArticleDetailDto subArticleDetails = (SubArticleDetailDto) args[1];
				    IdsList = subArticleDetails.getSubArticleIds();	
					deletedId="leadId";
            		deletedListIds="subArticleIds";
					oldPartListSubArticleList = partListRepo.findAllByarticleIdAndSubArticleIds((Long)id, IdsList);
					oldDataJson = toJson(oldPartListSubArticleList);
				}else if(auditLog.functionality().equalsIgnoreCase("delete Master PartList Lead Article")) {
					id=args[0];
					oldPartListSubArticleList=partListRepo.findByArticle((Long) id);
					oldDataJson = toJson(oldPartListSubArticleList);
				}else if (auditLog.functionality().equalsIgnoreCase("delete Clusters from Calender")) {
					id = args[0];
					IdsList = (List<Long>) args[1];
					deletedId="calenderId";
            		deletedListIds="clustersIds";
            		List<CalendarClusterMapping> oldCalendarClusterMapping = calendarClusterRepo.findByCalendarIdAndClusterIds(id.toString(),IdsList);
					oldDataJson = toJson(oldCalendarClusterMapping);
				}else if (auditLog.functionality().equalsIgnoreCase("Update Workweek to Calendar")) {
					List<CalendarWorkWeek> listCalendarWorkWeek =  (List<CalendarWorkWeek>) args[0];
					String calendarKey = listCalendarWorkWeek.get(0).getCalendar().getCalendarKey();				
					List<CalendarWorkWeek> oldCalendarWorkWeek = workWeekRepository.findByCalendar_CalendarKey(calendarKey);					
					oldDataJson = toJson(oldCalendarWorkWeek);
				}
            	
            	else {
            		id = args[0];
                	Object oldEntity = fetchEntityById(auditLog.entity(), id);
                    oldDataJson = toJson(oldEntity);
            	}           	           	
            }
            
            //Above code will execute Before & at time of invoking of execution of Methods on which Audit operation annotation @auditLog has applied           
            Object result = joinPoint.proceed(); // method call
            success = true;

            //Below code will execute after successfully execution of Methods on which Audit operation annotation @auditLog has applied
            if ("INSERT".equalsIgnoreCase(auditLog.action())) {
                if (Objects.nonNull(dto)) {
                    newDataJson = toJson(dto);
                }
                if(Objects.isNull(dto)) {
                	newDataJson=toJson(args[0]);
                }
            }
            else if ("UPDATE".equalsIgnoreCase(auditLog.action())) {
                if (Objects.nonNull(dto) && !auditLog.functionality().equalsIgnoreCase("Pool Assign to Site")) {
                    newDataJson = toJson(dto);
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("Update Master Data MasterArticle Eliminate Master Linkage")) {
                	List<Long> articleClusterIds = oldArticleCluster.stream().map(e->e.getId()).toList();
                	newDataJson = "ArticleCluster Table column masterArticle = 0 has changed for given ArticleId :"+ articleClusterIds;
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("Update Master Data MasterArticle Set Master Linkage")) {
                	List<Long> articleClusterIds = oldArticleCluster.stream().map(e->e.getId()).toList();
                	newDataJson = "ArticleCluster Table column masterArticle = 1 has changed for given ArticleId :"+ articleClusterIds;
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("add Master Data MasterArticle Update Slave Attributes")) {
                	newDataJson = toJson(args[0]);
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("update existing Template")) {
                	newDataJson = toJson(args[1]);
                }
                else if(Objects.nonNull(dto) && auditLog.functionality().equalsIgnoreCase("Pool Assign to Site")) {
                	newDataJson = toJson(args[0]);
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("update Articles Properties from connection")) {
                	newDataJson = toJson(args[2]);
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("Remove Article PartList MasterPart Status")) {
                	newDataJson = String.format("Record with this articleId : %s and clusterIds : %s MasterPart Status has been set false",id,IdsList);
                }
                else if(Objects.isNull(dto) && auditLog.functionality().equalsIgnoreCase("Add Article PartList MasterPart Status")) {
                	newDataJson = String.format("Record with this articleId : %s and clusterIds : %s MasterPart Status has been set true",id,IdsList);
                }
            }
            else if("Delete".equalsIgnoreCase(auditLog.action())){
				deleteAssignedData = Stream
						.of("delete Pool Assign to Site", "delete Clusters From Region", "delete Articles From Pool","delete Articles from Connection",
								"delete Clusters From Connection","delete Sub Article to Article","delete Clusters from Calender")
						.anyMatch(s -> s.equalsIgnoreCase(auditLog.functionality()));
				
				if (!deleteAssignedData) {
	            	newDataJson = String.format("Record with this id %s has been marked deleted",id);
	            }
	            else if (deleteAssignedData) {
	            	newDataJson = String.format("Record with this %s : %s and %s : %s has been marked deleted",deletedId,id,deletedListIds,IdsList);	            	
	            }
			}
            
                                    
            logAudit(auditLog, username, success, oldDataJson, newDataJson, null);
            log.info("Auditing completed for functionality {} ",auditLog.functionality());
            return result;        	
        }
        
        catch (Exception ex) {
        	logAudit(auditLog, username, false, oldDataJson, newDataJson, ex);
            throw ex;
		}    	
    }
    
    /**
	 * Purpose of this method is to retrive userName from current SecurityContext
	 */
    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();       
        if(auth!=null && auth.isAuthenticated()) {
        	Object principal = auth.getPrincipal();
        	if(principal instanceof AMSPUser user) {
        		return user.getUsername();
        	}
        }
        return "UNKNOWN_USER";
    }
    
    /**
	 * Purpose of this method is to extract primary id value from received object
	 */
    private Object extractIdFromArgs(Object[] args) {
        for (Object arg : args) {
            try {
                if (arg == null) continue;
                if (arg instanceof Number) continue;
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    return getIdMethod.invoke(arg);
                } catch (NoSuchMethodException e) {
                	return null;
                }
            } catch (Exception e) {
                // log exception if needed
                return null;
            }
        }
        return null;
    }

    /**
	 * Purpose of this method is to fetch data by given entityName & id
	 */
    
    private Object fetchEntityById(String entityName, Object id) {
        return switch (entityName) {
            case "Article" -> articleRepo.findById((Long) id).orElse(null);
            case "ClusterItem" -> clusterRepo.findById((Long) id).orElse(null);
            case "ArticleCluster" -> articleClusterRepo.findById((Long) id).orElse(null);
            case "Site" -> siteRepo.findById((Long) id).orElse(null);
            case "Region" -> regionRepo.findById((Long) id).orElse(null);
            case "Pool" -> poolRepo.findById((Long) id).orElse(null);
            case "Connection" -> connectionRepo.findById((Long) id).orElse(null);
            case "Shipping" -> shippingRepo.findById((Long) id).orElse(null);
            case "TemplateConfiguration" -> templateRepository.findById((Long) id).orElse(null);
            case "Calendar" -> calendarRepository.findById(id.toString()).orElse(null);
            case "CalendarHolidays" -> holidaysRepository.findById((Long) id).orElse(null);
            default -> null;
        };
    }    

    /**
	 * Purpose of this method is to convert object data into json string
	 */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
        	log.error("On Audit Activity,Converting Domain obj to json {}",e.getMessage());
            return "Error converting to JSON";
        }
    }

    /**
	 * Purpose of this method is to log response into auditFile using logger Audit
	 */
    private void logAudit(AuditLog auditLog, String user, boolean success, String oldData, String newData, Exception ex) {
        Logger auditLogger = LoggerFactory.getLogger("AUDIT");
        if (ex != null) {
            auditLogger.error("Entity={} | Action={} | Functionality={} | User={} | Status=FAIL | Exception={} | Old={} | New={}",
                    auditLog.entity(), auditLog.action(),auditLog.functionality(), user, ex.getMessage(), oldData, newData);
        } else {
            auditLogger.info("Entity={} | Action={} | Functionality={} | User={} | Status=SUCCESS | Old={} | New={}",
                    auditLog.entity(), auditLog.action(),auditLog.functionality(), user, oldData, newData);
        }
    }
  
}
