package com.avaya.amsp.masterdata.service;

import com.avaya.amsp.domain.Subscribers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
public class SubscriberSpecification {

    // Main method to build the specification based on filters
    public Specification<Subscribers> applyFilters(Map<String, String> filters) {
        Specification<Subscribers> spec = Specification.where(null);

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (StringUtils.hasText(value)) {
                switch (key) {
                    case "idRegion":
                    case "idCluster":
                    case "idSite":
                    case "idPbx":
                        spec = spec.and(integerEquals(key, value));
                        break;
                    case "areaCode":
                    case "extension":
                    case "e164":
                    case "name":
                    case "firstName":
                    case "ntUsername":
                    case "ntDomain":
                    case "costCenter":
                    case "email":
                    case "fax":
                    case "mobile":
                    case "pager":
                    case "department":
                    case "compasStatus":
                    case "cicatStatus":
                    case "accountType":
                    case "roomOffice":
                    case "typeOfUse":
                    case "remark":
                    case "connectionType":
                    case "bcsBunch":
                    case "msnMaster":
                    case "fromUser":
                    case "whenPinAvailable":
                    case "currentState":
                        spec = spec.and(likeContains(key, value));
                        break;
                    case "automaticSync":
                    case "dataRecordBlocked":
                    case "amfkExpansion":
                        spec = spec.and(booleanEquals(key, value));
                        break;
                    default:
                        log.warn("Unknown filter key: {}", key); // Log unknown keys for debugging
                        break;
                }
            }
        }

        return spec;
    }

    // Method for handling case-insensitive 'like' search for string fields
    private Specification<Subscribers> likeContains(String field, String value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    // Method to handle equality check for Boolean fields
    private Specification<Subscribers> booleanEquals(String field, String value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(field), Boolean.parseBoolean(value));
    }

    // Method to handle equality check for Integer fields (idRegion, idCluster, etc.)
    private Specification<Subscribers> integerEquals(String field, String value) {
        try {
            Integer intValue = Integer.parseInt(value);
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(field), intValue);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for field {}: {}", field, value); // Log invalid values
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(field)); // Ignore invalid integer filters
        }
    }
}
