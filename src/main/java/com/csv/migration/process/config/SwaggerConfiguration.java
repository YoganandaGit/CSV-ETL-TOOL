package com.csv.migration.process.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Value("${migration.app.openapi.url}")
    private String devUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("URL for the data migration process");

        Contact contact = new Contact();
        contact.setEmail("migrationapp@csvapp.com");
        contact.setName("CSV Migration App");
        contact.setUrl("https://www.migrationapp.com");

        License mitLicense = new License().name("CSV Tool License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("CSV Import APIs")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to CSV ETL Process Import APIs.").termsOfService("https://www.csvapp.com/migrationapp/terms/")
                .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer));
    }
}
