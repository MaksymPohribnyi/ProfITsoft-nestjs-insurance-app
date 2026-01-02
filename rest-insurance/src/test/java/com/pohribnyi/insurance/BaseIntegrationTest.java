package com.pohribnyi.insurance;

import java.util.TimeZone;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        POSTGRE_SQL_CONTAINER.start();
    }

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
		registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
	}
}