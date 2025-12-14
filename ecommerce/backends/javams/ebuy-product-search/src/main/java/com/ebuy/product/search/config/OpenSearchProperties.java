package com.ebuy.product.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.opensearch")
public class OpenSearchProperties {

    private String host = "localhost";
    private int port = 9200;
    private String scheme = "http";
    private String username = "admin";
    private String password = "MyStr0ngP@ssw0rd!";

}