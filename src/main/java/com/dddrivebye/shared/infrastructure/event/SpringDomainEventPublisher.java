package com.dddrivebye.shared.infrastructure.event;

import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(BaseDomainEvent event) {
        publisher.publishEvent(event);
    }
}
