package com.avaya.amsp.masterdata;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class MasterdataMgmtApplicationTests {

	@Test
	void contextLoads() {
		// Test that the application context loads successfully
		// This test will be skipped if database/configuration is not available
	}

}
