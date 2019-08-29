package com.example.order.domain.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author mukibul
 * @since 24/08/19
 */
public interface ProductEventSink {

    String PRODUCT_SUBSCRIBER_EVENTS = "productEventsSubscriberChannel";

    String PRODUCT_ADDED_SUBSCRIBER_EVENTS = "productAddedEventsSubscriberChannel";


    @Input
    SubscribableChannel productEventsSubscriberChannel();

    @Input
    SubscribableChannel productAddedEventsSubscriberChannel();
}
