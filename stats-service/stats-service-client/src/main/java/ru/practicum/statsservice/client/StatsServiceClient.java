package ru.practicum.statsservice.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statsservice.dto.EndpointHitDto;

import java.util.List;
import java.util.Map;

public class StatsServiceClient extends BaseClient {

    public StatsServiceClient(String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public void create(EndpointHitDto dto) {
        post("/hit", dto);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {
        String uri = buildUris(uris);
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", uri,
                "unique", unique.toString()
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }

    public ResponseEntity<Object> getAllStatsByUris(List<String> uris) {
        String uri = buildUris(uris);
        Map<String, Object> parameters = Map.of(
                "uris", uri);
        return get("/stats/all?uris={uris}", parameters);
    }

    private String buildUris(List<String> uris) {
        StringBuilder sb = new StringBuilder();
        if (uris.size() >= 1) {
            sb.append(uris.get(0));
        }
        if (uris.size() > 1) {
            for (int i = 1; i < uris.size(); i++) {
                sb.append(",").append(uris.get(i));
            }
        }
        return sb.toString();
    }
}