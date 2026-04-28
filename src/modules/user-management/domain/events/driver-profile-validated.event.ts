import { BaseDomainEvent } from '@shared/domain/events/base-domain-event';

export class DriverProfileValidatedEvent extends BaseDomainEvent {
  public readonly eventName = 'driver-profile.validated';
  constructor(
    public readonly driverProfileId: string,
    public readonly userId: string,
  ) { super(); }
}
