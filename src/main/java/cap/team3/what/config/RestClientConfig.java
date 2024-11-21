package cap.team3.what.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(new BufferingClientHttpRequestFactory(
                        ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                                .withConnectTimeout(Duration.ofSeconds(60))  // 연결 타임아웃
                                .withReadTimeout(Duration.ofSeconds(120))    // 읽기 타임아웃
                        )));
    }
}