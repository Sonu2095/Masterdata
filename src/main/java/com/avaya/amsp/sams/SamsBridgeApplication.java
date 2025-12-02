/**
 * 
 */
package com.avaya.amsp.sams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EntityScan(basePackages = { "com.avaya.amsp.domain" })
@Import(com.avaya.amsp.security.config.JasyptConfig.class)
public class SamsBridgeApplication {



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(SamsBridgeApplication.class, args);
	}


}
