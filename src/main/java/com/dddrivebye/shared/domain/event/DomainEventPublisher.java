package com.dddrivebye.shared.domain.event;

public interface DomainEventPublisher {

    void publish(BaseDomainEvent event);
}
