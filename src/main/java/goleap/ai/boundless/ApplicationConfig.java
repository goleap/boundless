package goleap.ai.boundless;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ApplicationConfig {
    public static final String YAML_MAPPER_ID = "yamlMapper";
    public static final String JSON_MAPPER_ID = "jsonMapper";

    @Bean
    @Qualifier(YAML_MAPPER_ID)
    public ObjectMapper yamlMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new Jdk8Module());
        // support Java 8 date time apis
        objectMapper.registerModule(new JavaTimeModule());
        // support Guava collections
        objectMapper.registerModule(new GuavaModule());
        return objectMapper;
    }

    @Bean
    @Qualifier(JSON_MAPPER_ID)
    @Primary
    public ObjectMapper jsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new Jdk8Module());
        // support Java 8 date time apis
        objectMapper.registerModule(new JavaTimeModule());
        // support Guava collections
        objectMapper.registerModule(new GuavaModule());
        return objectMapper;
    }

    @Bean
    public Executor taskExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(2 * processors);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("goleap-");
        executor.initialize();
        return executor;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(1000L);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        //Retry only 3 times
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
