package com.avaya.amsp.sams.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.avaya.amsp.sams.dtos.ArticlePropertiesDto;
import com.avaya.amsp.sams.dtos.ConnectionDto;
import com.avaya.amsp.sams.dtos.PbxNumberRangeDto;
import com.avaya.amsp.sams.dtos.PbxWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MasterDataServicesClient {

    @Value("${services.endpoints.fetch-articles}")
    private String masterDataFetchArticlesEndpoint;

    @Value("${services.endpoints.fetch-pbx-by-area-code}")
    private String pbxByAreaCodeEndpoint;

    @Value("${services.endpoints.fetch-pbx-phone-number-range:''}")
    private String pbxNumberRangeEndpoint;
    
    @Value("${services.endpoints.fetch-connections-by-cluster-id}")
    private String connectionsByClusterIdEndpoint;
    
    @Autowired
    RestTemplate restTemplate;

    public List<ArticlePropertiesDto> fetchArticlesByConnectionId(Long connectionId, Long clusterId, String authToken) {

        String endpoint = String.format(masterDataFetchArticlesEndpoint, connectionId, clusterId);
        log.info("fetching articles from {}",endpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< List< ArticlePropertiesDto > > request = new HttpEntity<>(headers);
        ResponseEntity<List<ArticlePropertiesDto>> response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<ArticlePropertiesDto>>() {}
        );
        List<ArticlePropertiesDto> articles = response.getBody();
        return articles;
    }


    public PbxWrapper fetchPbxByAreaCode(String areaCode, String authToken) {

        String endpoint = String.format(pbxByAreaCodeEndpoint, areaCode);
        log.info("Fetching pbx info from {}",endpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< PbxWrapper > request = new HttpEntity<>(headers);
        ResponseEntity< PbxWrapper > response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference< PbxWrapper >() {}
        );
        log.info("Response received as {}",response.getStatusCode());
        return response.getBody();
    }

    public List<PbxNumberRangeDto> fetchPbxNumberRange(long pbxClusterId, String authToken) {

        String endpoint = String.format(pbxNumberRangeEndpoint, pbxClusterId);
        log.info("Fetching pbx number range from {}",endpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< List< PbxNumberRangeDto > > request = new HttpEntity<>(headers);
        ResponseEntity<List< PbxNumberRangeDto > > response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<PbxNumberRangeDto>>() {}
        );
        log.info("Response received as {}",response.getStatusCode());
        return response.getBody();
    }
    
    public List<ConnectionDto> fetchConnectionsByClusterId(long pbxClusterId, String authToken) {
    	String endpoint = String.format(connectionsByClusterIdEndpoint, pbxClusterId);
        log.info("Fetching connections by cluster id from {}",endpoint);

        // Set Basic Auth header
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity< List< ConnectionDto > > request = new HttpEntity<>(headers);
        ResponseEntity<List< ConnectionDto > > response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<ConnectionDto>>() {}
        );
        log.info("Response received as {}",response.getStatusCode());
        
        return response.getBody();
    }
}
