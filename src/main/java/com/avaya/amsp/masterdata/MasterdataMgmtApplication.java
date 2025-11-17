package com.avaya.amsp.masterdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication( scanBasePackages = {"com.avaya.amsp"} )
@EnableJpaRepositories(
    basePackages = {
        "com.avaya.amsp.masterdata.repo",
        "com.avaya.amsp.auth.common.repo"
    }
)
@EntityScan(
    basePackages = {
        "com.avaya.amsp.domain",
        "com.avaya.amsp.auth.common.entity"
    }
)
@EnableMethodSecurity
@EnableCaching
@Slf4j
@Import(com.avaya.amsp.security.config.JasyptConfig.class)
public class MasterdataMgmtApplication {


    public static void main(String[] args) {
      	SpringApplication.run(MasterdataMgmtApplication.class, args);
    }

}
