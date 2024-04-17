package ru.lvmlabs.neuronum.baseconfigs.utils;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public abstract class RestClientWrap {
    protected static final RestClient restClient = RestClient
            .builder()
            .requestFactory(new JdkClientHttpRequestFactory() {{
                setReadTimeout(Duration.ofMinutes(5));
            }})
            .build();
}
