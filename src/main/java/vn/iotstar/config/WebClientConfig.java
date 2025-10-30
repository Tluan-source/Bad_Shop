package vn.iotstar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient openAiClient(
            @Value("${OPENAI_API_KEY:}") String openAiKey,
            @Value("${OPENROUTER_API_KEY:}") String openRouterKey) {

        boolean useOpenRouter = openRouterKey != null && !openRouterKey.isBlank();
        String baseUrl = useOpenRouter ? "https://openrouter.ai/api/v1" : "https://api.openai.com/v1";
        String token = useOpenRouter ? openRouterKey : (openAiKey == null ? "" : openAiKey);

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (useOpenRouter) {
            builder.defaultHeader("HTTP-Referer", "http://localhost");
            builder.defaultHeader("X-Title", "Bad Shop Chat");
        }

        return builder.build();
    }
}




