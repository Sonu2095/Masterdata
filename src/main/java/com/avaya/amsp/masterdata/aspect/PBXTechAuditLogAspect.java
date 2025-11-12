package com.avaya.amsp.masterdata.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.avaya.amsp.masterdata.annotation.PBXTechAuditLog;
import com.avaya.amsp.masterdata.repo.DeviceTypeRepository;
import com.avaya.amsp.masterdata.repo.PbxClusterRepository;
import com.avaya.amsp.masterdata.repo.PbxComponentRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberLockRepository;
import com.avaya.amsp.masterdata.repo.PbxNumberRangeRepository;
import com.avaya.amsp.masterdata.repo.PbxPortRepository;
import com.avaya.amsp.masterdata.repo.PbxSpecialPortRepository;
import com.avaya.amsp.masterdata.repo.PbxSystemRepository;
import com.avaya.amsp.masterdata.repo.PortTypeRepository;
import com.avaya.amsp.masterdata.repo.ServiceTypeRepository;
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
public class PBXTechAuditLogAspect {
	
	@Autowired
    private ObjectMapper objectMapper; // for JSON logging
	
	@Autowired
	private PortTypeRepository portTypeRepo;
	
	@Autowired
	private ServiceTypeRepository serviceTypeRepo;
	
	@Autowired
	private DeviceTypeRepository deviceTypeRepo;
	
	@Autowired
	private PbxSpecialPortRepository pbxSpecialPortRepo;
	
	@Autowired
	private PbxNumberRangeRepository pbxNumberRangeRepo;
	
	@Autowired
	PbxNumberLockRepository pbxNumberLockRepo;
	
	@Autowired
	private PbxPortRepository pbxPortRepo;
	
	@Autowired
	private PbxClusterRepository pbxClusterRepo;
	
	@Autowired
	private PbxSystemRepository pbxSystemRepo;
	
	@Autowired
	private PbxComponentRepository pbxComponentRepo;
	
	@Around("@annotation(pbxAuditLog)")
    public Object handlePBXAuditLog(ProceedingJoinPoint joinPoint, PBXTechAuditLog pbxAuditLog) throws Throwable {
    	String username = getUsername();
        String oldDataJson = null;
        String newDataJson = null;
        boolean success = false;
        Object id = null;
        String action = null;
    	String functionality = null;
        
		try {
			log.info("Auditing Started for PBX Technologies functionality {} ",pbxAuditLog.functionality());
        	
        	Object[] args = joinPoint.getArgs();
        	Object dto = Arrays.stream(args).filter(a -> a != null && a.getClass().getSimpleName().endsWith("Dto")).findFirst().orElse(null);
        	action = pbxAuditLog.action();
        	functionality = pbxAuditLog.functionality();
        	
        	if ("UPDATE".equalsIgnoreCase(action)) {
				id = extractIdFromArgs(args);
				long dtoId=(long) id;
				if(dtoId == 0) {
					action="insert";
					id=null;
					if(functionality.equalsIgnoreCase("PBX update existing Service Type")) {
						functionality="PBX add New Service Type";
					}else if(functionality.equalsIgnoreCase("PBX update existing Device Type")) {
						functionality="PBX add New Device Type";
					}else if(functionality.equalsIgnoreCase("PBX update existing Phone Number Lock")) {
						functionality="PBX Add Phone Number Lock";
					}else if(functionality.equalsIgnoreCase("PBX update existing PbxCluster")) {
						functionality="PBX Add PbxCluster";
					}
				}
				
				if (id != null) {
					Object oldEntity = fetchEntityById(pbxAuditLog.entity(), id);
					oldDataJson = toJson(oldEntity);
				}
        	}
        	
        	else if("DELETE".equalsIgnoreCase(action)){
        		id=args[0];
        		if (id != null) {
					Object oldEntity = fetchEntityById(pbxAuditLog.entity(), id);
					oldDataJson = toJson(oldEntity);
				}
        	}
        	
			//Above code will execute Before & at time of invoking of execution of Methods on which Audit operation annotation @auditLog has applied           
            Object result = joinPoint.proceed(); // method call
            success = true;
            
          //Below code will execute after successfully execution of Methods on which Audit operation annotation @auditLog has applied
            if ("INSERT".equalsIgnoreCase(action)) {
                if (Objects.nonNull(dto)) {
                    newDataJson = toJson(dto);
                }
                if(Objects.isNull(dto)) {
                	newDataJson=toJson(args[0]);
                }
            }
            else if ("UPDATE".equalsIgnoreCase(action)) {
            	if (Objects.nonNull(dto)) {
                    newDataJson = toJson(dto);
                }
            	
            }
            else if("Delete".equalsIgnoreCase(action)){
            	newDataJson = String.format("Record with this id %s has been marked deleted",id);           	
            }
			
			logAudit(action,pbxAuditLog,functionality, username, success, oldDataJson, newDataJson, null);
            log.info("Auditing completed for PBX Technologies functionality {} ",pbxAuditLog.functionality());
            return result;
		}
		catch (Exception ex) {
        	logAudit(action,pbxAuditLog,functionality,username, false, oldDataJson, newDataJson, ex);
            throw ex;
		}
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
	 * Purpose of this method is to fetch data by given entityName & id
	 */
    
    private Object fetchEntityById(String entityName, Object id) {
        return switch (entityName) {
            case "PortType" -> portTypeRepo.findById((Long) id).orElse(null);
            case "ServiceType" -> serviceTypeRepo.findById((Long) id).orElse(null);
            case "DeviceType" -> deviceTypeRepo.findById((Long) id).orElse(null);
            case "PbxSpecialPort" -> pbxSpecialPortRepo.findById((Long) id).orElse(null);
            case "PbxNumberRange" -> pbxNumberRangeRepo.findById((Long) id).orElse(null);
            case "PbxNumberLock" -> pbxNumberLockRepo.findById((Long) id).orElse(null);
            case "PbxPort" -> pbxPortRepo.findById((Long) id).orElse(null);
            case "PbxCluster" -> pbxClusterRepo.findById((Long) id).orElse(null);
            case "PbxSystem" -> pbxSystemRepo.findById((Long) id).orElse(null);
            case "PbxComponent" -> pbxComponentRepo.findById((Long) id).orElse(null);
            default -> null;
        };
    }    

    /**
	 * Purpose of this method is to log response into auditFile using logger Audit
	 */
    private void logAudit(String action,PBXTechAuditLog pbxAuditLog,String functionality, String user, boolean success, String oldData, String newData, Exception ex) {
        Logger auditLogger = LoggerFactory.getLogger("AUDIT");
        if (ex != null) {
            auditLogger.error("Entity={} | Action={} | Functionality={} | User={} | Status=FAIL | Exception={} | Old={} | New={}",
            		pbxAuditLog.entity(), action,functionality, user, ex.getMessage(), oldData, newData);
        } else {
            auditLogger.info("Entity={} | Action={} | Functionality={} | User={} | Status=SUCCESS | Old={} | New={}",
            		pbxAuditLog.entity(), action,functionality, user, oldData, newData);
        }
    }

}
