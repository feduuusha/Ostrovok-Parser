package ru.itis.parser.client;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.util.Map;

public class CitiesRestClientResource implements QuarkusTestResourceLifecycleManager {

    static WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.6.0")
            .withMappingFromResource(CitiesRestClientTest.class,"mocks-config.json");


    @Override
    public Map<String, String> start() {
        wiremockServer.start();
        return Map.of("quarkus.rest-client.cities-rest-client.url", wiremockServer.getBaseUrl(),
                "cities-rest-client.locale", "ru");
    }

    @Override
    public void stop() {
        wiremockServer.stop();
    }
}
