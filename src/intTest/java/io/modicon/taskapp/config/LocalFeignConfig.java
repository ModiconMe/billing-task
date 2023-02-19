package io.modicon.taskapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.modicon.taskapp.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

@RequiredArgsConstructor
@Configuration
public class LocalFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(Environment env) {
        return template -> {
            String token = AuthUtils.TokenHolder.token;
            if (token != null) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            if (template.headers().get(HttpHeaders.CONTENT_TYPE) == null) {
                template.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            }
        };
    }

    @Bean
    public Encoder encoder() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new JacksonEncoder(mapper);
    }

    @Bean
    public Decoder decoder() {
        return new JacksonDecoder(Collections.singletonList(new JavaTimeModule()));
    }

    @Bean
    public Client client() {
        return new Client.Default(null, null);
    }

    @Bean
    public Feign.Builder feignBuilder(Environment env) {
        return new Feign.Builder() {
            @Override
            public <T> T target(Target<T> target) {
                String port = System.getProperty("server.port");
                String name = target.name();
                String url = target.url().replace(name, "localhost:" + port);
                Target.HardCodedTarget<T> local = new Target.HardCodedTarget<>(target.type(), target.name(), url);
                return super.target(local);
            }
        };
    }

}
