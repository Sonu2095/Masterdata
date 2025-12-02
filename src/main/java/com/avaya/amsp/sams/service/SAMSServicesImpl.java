package com.avaya.amsp.sams.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.avaya.amsp.domain.ActiveSubscribers;
import com.avaya.amsp.domain.Connection;
import com.avaya.amsp.sams.dtos.ArticlePropertiesDto;
import com.avaya.amsp.sams.dtos.ConnectionDto;
import com.avaya.amsp.sams.dtos.OrderArticleDTO;
import com.avaya.amsp.sams.dtos.OrderItemDTO;
import com.avaya.amsp.sams.dtos.OrderRequestDTO;
import com.avaya.amsp.sams.dtos.OrderType;
import com.avaya.amsp.sams.dtos.OrderWrapperdto;
import com.avaya.amsp.sams.dtos.PbxWrapper;
import com.avaya.amsp.sams.dtos.SAMSAddOrderRequestDTO;
import com.avaya.amsp.sams.dtos.SAMSDelMigrateOrderDto;
import com.avaya.amsp.sams.dtos.SAMSGetOrderResponseDTO;
import com.avaya.amsp.sams.dtos.SiteDto;
import com.avaya.amsp.sams.dtos.SubscriberDto;
import com.avaya.amsp.sams.exceptions.SAMSValidationException;
import com.avaya.amsp.sams.exceptions.ValidationErrorCodes;
import com.avaya.amsp.sams.repo.ActiveSubscribersRepo;
import com.avaya.amsp.sams.repo.ConnectionRepo;
import com.avaya.amsp.sams.service.iface.SAMSServices;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SAMSServicesImpl implements SAMSServices {

    @Autowired
    private ConnectionRepo connectionRepo;
    
    @Autowired
    private ActiveSubscribersRepo activeSubscribersRepo;

    @Autowired
    private TokenServices tokenServices;

    @Autowired
    OrderServicesClient orderServicesClient;

    @Autowired
    MasterDataServicesClient masterDataServicesClient;

    @Autowired
    SubscriberServicesClient subscriberServicesClient;

    @Override
    public String openSamsOrder(SAMSAddOrderRequestDTO payload) {

        AtomicReference< String > orderIdWrapper = new AtomicReference<>("");
        SubscriberDto subscriberInfo = null;
        PbxWrapper pbxWrapper = null;
        SiteDto site = new SiteDto();

        String authToken = tokenServices.getToken();

        try {
            pbxWrapper = masterDataServicesClient.fetchPbxByAreaCode(payload.connection().callNumber().areaCode(), authToken);

            log.info("validating PBX checks");

            if ( pbxWrapper.getPbxSystemCount() == 0 ) {
                throw new SAMSValidationException(ValidationErrorCodes.PBX_SYSTEM_MISSING);
            } else if ( pbxWrapper.getSites().size() == 0 ) {
                throw new SAMSValidationException(ValidationErrorCodes.AMFK_LOCATIN_MISSING);
            } else if ( pbxWrapper.getSites().size() > 1 ) {
                throw new SAMSValidationException(ValidationErrorCodes.AMFK_LOCATION_NOT_UNIQUE);
            } else {
                site.setId(pbxWrapper.getSites().get(0).getId());
                site.setName(pbxWrapper.getSites().get(0).getName());
                log.info("Site is mapped to {}", site);
            }
        } catch ( HttpClientErrorException.NotFound e ) {
            log.info("Pbx cluster record not found for area code {}", payload.connection().callNumber().areaCode());
            throw new SAMSValidationException(ValidationErrorCodes.AREA_CODE_NOT_FOUND);
        }

        long clusterId = pbxWrapper.getClusterId();
        log.info("Cluster is mapped to {}", clusterId);

        try {
            subscriberInfo = subscriberServicesClient.fetchSubscriber(payload.user().userID(), payload.user().domain(),authToken);
            subscriberInfo.setOLocationCode(site.getName());
            //log.info("Subscriber {}", subscriberInfo);
            if ( StringUtils.isEmpty(subscriberInfo.getCostCenter()) ) {
                log.info("Cost center is empty");
                throw new SAMSValidationException(ValidationErrorCodes.COST_CENTER_MISSING);
            }

        } catch ( HttpClientErrorException.NotFound e ) {
            throw new SAMSValidationException(ValidationErrorCodes.NAME_MISSING);
        }

        //fetch connection details
        Optional< Connection > connectionDetails = connectionRepo.findConnectionByNames(payload.connection().connectionType().BCSType().toLowerCase());

        if ( connectionDetails.isPresent() ) {
            Connection connection = connectionDetails.get();
            log.info("Fetched connection as {}", connection);

            //fetch articles for given connection and cluster.
            List< ArticlePropertiesDto > articles = masterDataServicesClient.fetchArticlesByConnectionId(connection.getId(), clusterId, authToken);
            log.info("Articles {}", articles);
            if ( !articles.isEmpty() ) {

                OrderRequestDTO orderRequest = new OrderRequestDTO();
                List< OrderArticleDTO > orderArticleDTOS = new ArrayList< OrderArticleDTO >();

                orderRequest.setQuantity(1);
                orderRequest.setClusterId(clusterId);
                orderRequest.setConnectionId(connection.getId());
                orderRequest.setSiteId(site.getId());
                orderRequest.setOrderType(OrderType.ADD);

                List<ArticlePropertiesDto> obligatoryArticles = articles.stream()
                        .filter(article -> article.getObligatory() || article.getAlwaysInsert())  // method reference
                        .collect(Collectors.toList());

                if(!obligatoryArticles.isEmpty()) {

                    obligatoryArticles.stream().forEach(article -> {
                        OrderArticleDTO orderArticleDTO = new OrderArticleDTO();
                        orderArticleDTO.setArticleClusterId(article.getArticleClusterId());
                        orderArticleDTO.setPoolId(article.getPoolId());
                        orderArticleDTO.setClusterId(clusterId);
                        orderArticleDTO.setQuantity(1);
                        orderArticleDTO.setArticleAddInfo(article.getAdditionalInfo());
                        orderArticleDTO.setArticleRemark("SAMS order"); // TODO need to check
                        orderArticleDTO.setShippingReq(article.getShippingReq());
                        orderArticleDTO.setAssemblyReq(article.getAssemblyReq());

                        orderArticleDTOS.add(orderArticleDTO);
                    });

                    Map< Integer, List< OrderArticleDTO > > orderArticlesMap = new HashMap< Integer, List< OrderArticleDTO > >();
                    orderArticlesMap.put(1, orderArticleDTOS);
                    orderRequest.setArticles(orderArticlesMap);
                    orderRequest.setAttachFilesInEmail(false);

                    orderRequest.setPhoneNumber(payload.connection().callNumber().extension()); //TODO this need to be checked
                    orderRequest.setAreaCode(payload.connection().callNumber().areaCode());

                    OrderWrapperdto samsRequest = new OrderWrapperdto();
                    samsRequest.setOrderRequestDTO(orderRequest);
                    samsRequest.setSubscriberDto(subscriberInfo);

                    try {
                        String orderId = orderServicesClient.openSamsOrder(samsRequest, authToken);
                        orderIdWrapper.set(orderId);
                    } catch ( HttpClientErrorException.BadRequest e ) {
                        log.info("Extension is already in use");
                        throw new SAMSValidationException(ValidationErrorCodes.EXT_ALREADY_IN_USE);
                    }
                }else{
                    log.info("No articles with always insert/obligatory found for given connection and cluster.");
                }
            } else {
                //TODO need to check what should be done if no articles found.
                log.info("No articles found for given connection and cluster.");
            }
        } else {
            //TODO need to check validation
            log.info("Connection not found..");
        }
        return orderIdWrapper.get();
    }

    @Override
    public SAMSGetOrderResponseDTO fetchSamsOrder(String orderItemId) {

        String authToken = tokenServices.getToken();
        log.info("Token fetching completed");
        try {
            OrderItemDTO orderItemDTO = orderServicesClient.fetchSamsOrder(orderItemId, authToken);
            return new SAMSGetOrderResponseDTO(
                    new SAMSGetOrderResponseDTO.OrderID(orderItemId),
                    new SAMSGetOrderResponseDTO.CallNumber(orderItemDTO.getAreaCode(), orderItemDTO.getExtension()),
                    orderItemDTO.getCreationDate(),
                    orderItemDTO.getLastUpdateDate(),
                    new SAMSGetOrderResponseDTO.OrderStatus(String.valueOf(orderItemDTO.getStatus())),
                    new SAMSGetOrderResponseDTO.OrderType(orderItemDTO.getType()),
                    orderItemDTO.isCompleted()
            );

        } catch ( HttpClientErrorException.NotFound e ) {
            throw new SAMSValidationException(ValidationErrorCodes.ORDER_NOT_FOUND);
        }
    }

    @Override
    public String deleteSamsOrder(SAMSDelMigrateOrderDto payload) {
        AtomicReference< String > orderIdWrapper = new AtomicReference<>("");
        PbxWrapper pbxWrapper = null;
        String authToken = tokenServices.getToken();
        try {
            pbxWrapper = masterDataServicesClient.fetchPbxByAreaCode(payload.getAreaCode(), authToken);

            //fetch connection details
            Optional< Connection > connectionDetails = connectionRepo.findConnectionByNames(payload.getConnectionType().toLowerCase());
            if(connectionDetails.isPresent()){
                payload.setConnectionId(connectionDetails.get().getId());
            }else{
                log.info("No connection details found for Connection {}",payload.getConnectionType());
                throw new SAMSValidationException(ValidationErrorCodes.DIFF_EXT);
            }

            //TODO initiate
            log.info("paylaod {}",payload);
            String orderId = orderServicesClient.deleteSamsOrder(payload, authToken);
            orderIdWrapper.set(orderId);

        }catch ( HttpClientErrorException.BadRequest e ) {

            String errorCode = e.getResponseBodyAsString();
            if ( ValidationErrorCodes.EXT_NOT_FOUND.name().equalsIgnoreCase(errorCode) ) {
                log.error("Extension was not found for the given area code.");
                throw new SAMSValidationException(ValidationErrorCodes.EXT_NOT_FOUND);
            } else {
                log.error("The found connection for the given area code and extension has a different connection type");
                throw new SAMSValidationException(ValidationErrorCodes.DIFF_EXT);
            }

        } catch ( HttpClientErrorException.NotFound e ) {
            log.info("Pbx cluster record not found for area code {}", payload.getAreaCode());
            throw new SAMSValidationException(ValidationErrorCodes.AREA_CODE_NOT_FOUND);
        }
        log.info("Completed SAMS delete order as {}",orderIdWrapper.get());
        return orderIdWrapper.get();
    }


    @Override
    public String migrateSamsOrder(SAMSDelMigrateOrderDto payload) {
        PbxWrapper pbxWrapper = null;
        String authToken = tokenServices.getToken();

        try{
            pbxWrapper = masterDataServicesClient.fetchPbxByAreaCode(payload.getAreaCode(), authToken);

        } catch ( HttpClientErrorException.BadRequest e ) {
            String errorCode = e.getResponseBodyAsString();
            if ( ValidationErrorCodes.EXT_NOT_FOUND.name().equalsIgnoreCase(errorCode) ) {
                log.error("Extension was not found for the given area code.");
                throw new SAMSValidationException(ValidationErrorCodes.EXT_NOT_FOUND);
            } else {
                log.error("The found connection for the given area code and extension has a different connection type");
                throw new SAMSValidationException(ValidationErrorCodes.DIFF_EXT);
            }

        } catch ( HttpClientErrorException.NotFound e ) {
            log.info("Pbx cluster record not found for area code {}", payload.getAreaCode());
            throw new SAMSValidationException(ValidationErrorCodes.AREA_CODE_NOT_FOUND);
        }

        long clusterId = pbxWrapper.getClusterId();
        log.info("Cluster is mapped to {}", clusterId);
        
        List<ConnectionDto> connectionDtos = masterDataServicesClient.fetchConnectionsByClusterId(clusterId, authToken);
        if( connectionDtos == null || connectionDtos.isEmpty() ) {
        	log.error("No connection types found for the cluster id {} for area code {}", clusterId, payload.getAreaCode());
        	throw new SAMSValidationException(ValidationErrorCodes.CONNECTION_TYPE_NOT_CONFIG);
        }
        
        Optional< List<ActiveSubscribers> > activeSubscribers = activeSubscribersRepo.findByAreaCodeAndExtension(payload.getAreaCode(), payload.getExtension());
        if( activeSubscribers.get().isEmpty() || !activeSubscribers.isPresent() ) {
        	log.error("Extension was not found for the given area code.");
            throw new SAMSValidationException(ValidationErrorCodes.EXT_NOT_FOUND);
        }
        
        Long connectionId = activeSubscribers.get().get(0).getConnectionId();
        
        Optional< Connection > connection = connectionRepo.findConnectionById(connectionId);
        if( connection.isEmpty() || !connection.isPresent() ) {
        	log.error("No connection types found for the cluster id {} for area code {}", clusterId, payload.getAreaCode());
        	throw new SAMSValidationException(ValidationErrorCodes.CONNECTION_TYPE_NOT_CONFIG);
        }
        
        //The existing connection type of the extension. 
        String currentConnectionType = connection.get().getName();
        
        if( currentConnectionType.equalsIgnoreCase(payload.getConnectionType()) ) {
        	log.error("Migration to same BCS type is not allowed. Current connection type {}, requested connection type {}", currentConnectionType, payload.getConnectionType());
        	throw new SAMSValidationException(ValidationErrorCodes.MGR_TO_SAME_BCS_TYPE_NOT_ALLOWED);
        }
        
        boolean migrationTosfbTeams = false;
        
        //Iterate over the connection types and validate if current connection is white listed for the change/move.
        for(ConnectionDto connectionDto : connectionDtos) {
        	if( connectionDto.getName().equalsIgnoreCase(currentConnectionType) && connectionDto.getMigrationTosfbTeams() == 1 ) {
        		migrationTosfbTeams = true;
        		break;
        	}
        }
        
        //The payload.connectiontype is not eligible for migration to Sfb/Teams. 
        if( !migrationTosfbTeams ) {
        	log.error("Migration to SfB/Teams is not supported for this original connection type {}", currentConnectionType);
        	throw new SAMSValidationException(ValidationErrorCodes.MGR_NOT_SUPPORTED);
        }
         
        
        //fetch connection details
        Optional< Connection > connectionDetails = connectionRepo.findConnectionByNames(payload.getConnectionType().toLowerCase());
        if(connectionDetails.isPresent()){
            payload.setConnectionId(connectionDetails.get().getId());
        }else{
            log.info("No connection details found for connection type {}",payload.getConnectionType());
            throw new SAMSValidationException(ValidationErrorCodes.WRONG_CONNECTION_TYPE);
        }

        log.info("paylaod {}",payload);
        //Initiate migration order
        return orderServicesClient.changeSamsOrder(payload,authToken);
    }


}