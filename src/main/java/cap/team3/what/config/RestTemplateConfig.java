package cap.team3.what.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    protected RestTemplate restTemplate() {
        return new RestTemplate();
    }
}