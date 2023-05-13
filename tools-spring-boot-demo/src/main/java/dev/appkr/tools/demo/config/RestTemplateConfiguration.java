package dev.appkr.tools.demo.config;

import static dev.appkr.tools.demo.config.Constants.LogKey.*;
import static dev.appkr.tools.demo.config.Constants.Profile.*;

import dev.appkr.tools.demo.support.JsonUtils;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class RestTemplateConfiguration {

  @Bean
  public RestTemplateBuilder restTemplateBuilder(Environment environment) {
    RestTemplateBuilder builder = new RestTemplateBuilder()
        .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .setConnectTimeout(Duration.ofSeconds(2L))
        .setReadTimeout(Duration.ofSeconds(5L));

    final boolean enableLogging = Stream.of(environment.getActiveProfiles())
        .anyMatch(p -> p.equals(LOCAL_PROFILE) || p.equals(DEV_PROFILE) || p.equals(TEST_PROFILE));
    if (enableLogging) {
      builder = builder.additionalInterceptors(new HttpOutboundLogger());
    }

    return builder;
  }

  static class HttpOutboundLogger implements ClientHttpRequestInterceptor {

    private Logger log = LoggerFactory.getLogger(HTTP_OUTBOUND_LOGGER);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
      final ClientHttpResponse response = execution.execute(request, body);

      final HashMap<String, Object> reqLog = new HashMap<>() ;
      final HashMap<String, Object> resLog = new HashMap<>() ;

      log.info("OUTBOUND Request\n{}", JsonUtils.convertObjectToPrettyString(reqLog));
      log.info("OUTBOUND Response\n{}", JsonUtils.convertObjectToPrettyString(resLog));

      return response;
    }
  }
}
