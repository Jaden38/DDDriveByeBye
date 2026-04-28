import { BaseDomainEvent } from '@shared/domain/events/base-domain-event';

export class AccountRestrictedEvent extends BaseDomainEvent {
  public readonly eventName = 'account.restricted';
  constructor(
    public readonly userId: string,
    public readonly reason: string,
  ) { super(); }
}
