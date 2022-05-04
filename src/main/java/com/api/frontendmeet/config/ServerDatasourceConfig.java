package com.api.frontendmeet.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class ServerDatasourceConfig implements DataSourceConfig {

	@Override
    public void setup() {
        System.out.println("Setting up datasource for server environment. ");
    }
}
