import { BaseDomainEvent } from '@shared/domain/events/base-domain-event';

export class DriverAvailabilityChangedEvent extends BaseDomainEvent {
  public readonly eventName = 'driver.availability-changed';
  constructor(
    public readonly driverProfileId: string,
    public readonly userId: string,
    public readonly isAvailable: boolean,
  ) { super(); }
}
