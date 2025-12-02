/**
 * 
 */
package com.avaya.amsp.masterdata.config;

import java.util.concurrent.TimeUnit;

import org.modelmapper.ModelMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * 
 */
@Configuration
public class ApplicationConfiguration {

	@Bean
	OpenAPI openAPI() {
		return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
				.components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
				.info(new Info().title("AMSP MasterData API")
						.description("APIs for extracting and updating master data").version("1.0")
						.contact(new Contact().name("Vaibhav S").email("vsawalkar@avaya.com"))
						.license(new License().name("© Avaya Inc.")));
	}

	private SecurityScheme createAPIKeyScheme() {
		return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
	}

	@Bean
	ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		return modelMapper;
	}

	/*
	 * @Bean ObjectMapper objectMapper() { ObjectMapper objectMapper = new
	 * ObjectMapper();
	 * objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
	 * false); objectMapper.registerModule(new JavaTimeModule()); return
	 * objectMapper; }
	 */

	@Bean
	ExpressionParser expressionParser() {
		return new SpelExpressionParser();
	}

	@Bean
	MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}
	/*
	 * @Bean CacheManager cacheManager() { CaffeineCacheManager cacheManager = new
	 * CaffeineCacheManager();
	 * cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(10,
	 * TimeUnit.MINUTES).maximumSize(100)); return cacheManager; }
	 */

	@Bean
	public CaffeineCacheManager cacheManager() {
		return new CaffeineCacheManager() {
			@Override
			protected Cache<Object, Object> createNativeCaffeineCache(String name) {
				if ("articleTranslations".equals(name)) {
					return Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(500).build();
				}
				return Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1000).build();
			}
		};
	}
}
