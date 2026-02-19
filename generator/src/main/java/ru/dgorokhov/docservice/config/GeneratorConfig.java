package ru.dgorokhov.docservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.generator")
public class GeneratorConfig {

    private String serviceUrl;
    private Integer documentsCount;
    private Integer batchSize;
    private Long submitIntervalMs;
    private Long approveIntervalMs;

}
