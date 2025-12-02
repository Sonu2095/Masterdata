package com.avaya.amsp.sams.util;

import com.avaya.amsp.sams.exceptions.ErrorResponse;
import com.avaya.amsp.sams.exceptions.SAMSValidationException;
import com.avaya.amsp.sams.exceptions.ValidationErrorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value( "${sams.api.key}" )
    private String apiKey;

    @Autowired
    ObjectMapper objectMapper;

    private static final String HEADER_NAME = "X-API-KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String requestKey = request.getHeader(HEADER_NAME);
        if ( requestKey == null ) {

            log.error("Missing API Key");

            ErrorResponse errorPayload = new ErrorResponse();
            errorPayload.setCode(400);
            errorPayload.setType("Bad request");
            errorPayload.setMessage(new ErrorResponse.Message(ValidationErrorCodes.MISSING_API_KEY.getCode(), ValidationErrorCodes.MISSING_API_KEY.getMessage()));

            // Set headers and status
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Write JSON body
            objectMapper.writeValue(response.getWriter(), errorPayload);

            return;

        } else if ( !apiKey.equals(requestKey) ) {

            log.error("Wrong API Key");

            ErrorResponse errorPayload = new ErrorResponse();
            errorPayload.setCode(400);
            errorPayload.setType("Bad request");
            errorPayload.setMessage(new ErrorResponse.Message(ValidationErrorCodes.WRONG_API_KEY.getCode(), ValidationErrorCodes.WRONG_API_KEY.getMessage()));

            // Set headers and status
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Write JSON body
            objectMapper.writeValue(response.getWriter(), errorPayload);

            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken("samsUser", // principal name (can be anything)
                null, List.of(new SimpleGrantedAuthority("SAMS_BRIDGE_API_USER")) // optional role
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

}
