import { randomUUID } from 'crypto';

export abstract class BaseDomainEvent {
  public readonly eventId: string = randomUUID();
  public readonly occurredAt: Date = new Date();
  public abstract readonly eventName: string;
}
