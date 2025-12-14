package com.ebuy.product.search.service;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.cluster.HealthRequest;
import org.opensearch.client.opensearch.cluster.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class OpenSearchConnectionTestService implements CommandLineRunner {

    @Autowired
    private OpenSearchClient openSearchClient;

    @Override
    public void run(String... args) throws Exception {
        testConnection();
    }

    public void testConnection() {
        try {
            log.info("Testing OpenSearch connection...");

            // Test cluster health
            HealthRequest healthRequest = new HealthRequest.Builder().build();
            HealthResponse healthResponse = openSearchClient.cluster().health(healthRequest);

            log.info("OpenSearch cluster status: {}", healthResponse.status());
            log.info("OpenSearch cluster name: {}", healthResponse.clusterName());
            log.info("Number of nodes: {}", healthResponse.numberOfNodes());
            log.info("Number of data nodes: {}", healthResponse.numberOfDataNodes());

            log.info("OpenSearch connection successful!");

        } catch (OpenSearchException e) {
            log.error("OpenSearch error - Status: {}, Error: {}", e.status(), e.error().reason());
            log.error("Full error details: ", e);
        } catch (IOException e) {
            log.error("IO error connecting to OpenSearch: {}", e.getMessage());
            log.error("This usually indicates network connectivity issues or wrong host/port");
        } catch (Exception e) {
            log.error("Unexpected error connecting to OpenSearch: {}", e.getMessage(), e);
        }
    }
}