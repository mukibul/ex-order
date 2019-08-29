package com.example.order.config;

import com.example.order.domain.messaging.ProductEventSink;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;

/**
 * @author mukibul
 * @since 24/08/19
 */
@Configuration
@EnableBinding({ProductEventSink.class})
@IntegrationComponentScan(basePackages = "com.example")
public class IntegrationConfiguration {
}
