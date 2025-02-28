package ru.itis.parser.config;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class WebClientConfig {

    @Produces
    @ApplicationScoped
    public WebClient webClient(@ConfigProperty(name = "http.client.connect-timeout") Duration duration,
                               @ConfigProperty(name = "http.client.user-agent") String userAgent) {
        WebClientOptions options = new WebClientOptions();
        options.setConnectTimeout((int) duration.toMillis());
        options.setUserAgent(userAgent);
        return WebClient.create(Vertx.vertx(), options);
    }
}
