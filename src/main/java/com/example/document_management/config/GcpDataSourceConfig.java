package com.example.document_management.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class GcpDataSourceConfig {

    @Value("${gcp.secrets.manager.secret.name}")
    private String secretName;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.instance-connection-name}")
    private String instanceConnectionName;

    @Value("${spring.datasource.iam-user}")
    private String iamUser;

    @Bean
    public DataSource dataSource() throws Exception {
        // Fetch secret from Google Secret Manager
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of("western-beanbag-432114-m2", secretName, "latest");

            AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                    .setName(secretVersionName.toString())
                    .build();

            String secretString = client.accessSecretVersion(request).getPayload().getData().toStringUtf8();

            // Parse the secret JSON string to extract the username and password
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> secretMap = objectMapper.readValue(secretString, new TypeReference<Map<String, String>>() {});
            String username = secretMap.get("username");
            String password = secretMap.get("password");

            // Configure HikariCP with IAM authentication and the retrieved credentials
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(dbUrl);

            // Set IAM authentication
            hikariConfig.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
            hikariConfig.addDataSourceProperty("cloudSqlInstance", instanceConnectionName);
            hikariConfig.addDataSourceProperty("enableIamAuth", "true");
            hikariConfig.addDataSourceProperty("user", username); // Use the username retrieved from Secret Manager
            hikariConfig.addDataSourceProperty("password", password); // Use the password retrieved from Secret Manager
            hikariConfig.addDataSourceProperty("sslmode", "disable");

            hikariConfig.setDriverClassName("org.postgresql.Driver");

            // Optional: Set HikariCP-specific settings if needed
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setIdleTimeout(600000);

            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            // Log the error and rethrow it if necessary
            System.err.println("Error occurred while creating DataSource: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create DataSource using Google Cloud Secret Manager", e);
        }
    }
}
