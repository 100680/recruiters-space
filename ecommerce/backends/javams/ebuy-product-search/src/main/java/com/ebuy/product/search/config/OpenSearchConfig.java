package com.ebuy.product.search.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
public class OpenSearchConfig {

    private final OpenSearchProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    public OpenSearchConfig(OpenSearchProperties properties) {
        this.properties = properties;
    }

    @Bean
    public OpenSearchClient openSearchClient() {
        try {
            log.info("Initializing OpenSearch client with host: {}:{}, scheme: {}",
                    properties.getHost(), properties.getPort(), properties.getScheme());

            // Create credentials provider
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));

            // Create SSL context that trusts all certificates (for dev/test environments)
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();

            // Build the RestClient with proper configuration
            RestClient restClient = RestClient.builder(
                            new HttpHost(properties.getHost(), properties.getPort(), properties.getScheme()))
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        httpClientBuilder
                                .setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credentialsProvider);

                        // Add connection and socket timeouts
                        return httpClientBuilder;
                    })
                    .setRequestConfigCallback(requestConfigBuilder -> {
                        return requestConfigBuilder
                                .setConnectTimeout(5000)        // 5 seconds
                                .setSocketTimeout(60000);       // 60 seconds
                    })
                    .build();

            // Create the transport layer with custom ObjectMapper
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));

            // Create and return the OpenSearch client
            OpenSearchClient client = new OpenSearchClient(transport);

            log.info("OpenSearch client initialized successfully");
            return client;

        } catch (Exception e) {
            log.error("Failed to create OpenSearch client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create OpenSearch client", e);
        }
    }

    @Bean
    public RestClient restClient() {
        try {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();

            return RestClient.builder(
                            new HttpHost(properties.getHost(), properties.getPort(), properties.getScheme()))
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        return httpClientBuilder
                                .setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credentialsProvider);
                    })
                    .setRequestConfigCallback(requestConfigBuilder -> {
                        return requestConfigBuilder
                                .setConnectTimeout(5000)
                                .setSocketTimeout(60000);
                    })
                    .build();
        } catch (Exception e) {
            log.error("Failed to create RestClient: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create RestClient", e);
        }
    }
}