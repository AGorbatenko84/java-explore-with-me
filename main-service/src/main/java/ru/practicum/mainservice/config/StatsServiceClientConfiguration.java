package ru.practicum.mainservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.statsservice.client.StatsServiceClient;

@Configuration
@RequiredArgsConstructor
public class StatsServiceClientConfiguration {
    @Value("${stats-service-server.url}")
    private String serverUrl;
    private final RestTemplateBuilder builder;

    @Bean
    public StatsServiceClient statsServiceClient() {
        return new StatsServiceClient(serverUrl, builder);
    }
}
