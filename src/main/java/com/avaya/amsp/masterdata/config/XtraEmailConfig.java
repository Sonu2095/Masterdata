/**
 * 
 */
package com.avaya.amsp.masterdata.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * This is not a good way to do things. Have to ultimately remove this
 */
@Configuration
public class XtraEmailConfig {
	
	
	@Autowired
    private Environment environment;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttls;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private String starttlsRequired;

	@Bean
	TextEncryptor getTextEncryptor() {
		String password = "superDooperSecret";
		String salt = "5c0744940b5c369b"; // Securely generated and stored
		return Encryptors.text(password, salt);
	}
	
    @Bean
    JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);

        // Decrypt the password
        String encryptedPassword = environment.getProperty("spring.mail.password");
        String decryptedPassword = "random";
		try {
			decryptedPassword = getTextEncryptor().decrypt(encryptedPassword);
		} catch (Exception e) {
			// Do nothing
		}
        //decryptedPassword = getTextEncryptor().decrypt(encryptedPassword);
        mailSender.setPassword(decryptedPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.smtp.starttls.required", starttlsRequired);

        return mailSender;
    }

}
