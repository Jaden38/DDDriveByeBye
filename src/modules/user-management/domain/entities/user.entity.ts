import { randomUUID } from 'crypto';
import { AccountType } from '../value-objects/account-type.value-object';
import { BaseDomainEvent } from '@shared/domain/events/base-domain-event';

export interface UserProps {
  id?: string;
  email: string;
  fullName: string;
  phoneNumber: string;
  accountType: AccountType;
  isRestricted: boolean;
}

export class User {
  public readonly id: string;
  public readonly email: string;
  public readonly fullName: string;
  public readonly phoneNumber: string;
  public readonly accountType: AccountType;
  private restricted: boolean;
  private domainEvents: BaseDomainEvent[] = [];

  private constructor(props: UserProps) {
    this.id = props.id ?? randomUUID();
    this.email = props.email;
    this.fullName = props.fullName;
    this.phoneNumber = props.phoneNumber;
    this.accountType = props.accountType;
    this.restricted = props.isRestricted;
  }

  static createIndividual(props: Omit<UserProps, 'id' | 'accountType' | 'isRestricted'>): User {
    return new User({ ...props, accountType: AccountType.Individual, isRestricted: false });
  }

  static createProfessional(props: Omit<UserProps, 'id' | 'accountType' | 'isRestricted'>): User {
    return new User({ ...props, accountType: AccountType.Professional, isRestricted: false });
  }

  static reconstitute(props: UserProps): User {
    return new User(props);
  }

  get isIndividual(): boolean { return this.accountType === AccountType.Individual; }
  get isProfessional(): boolean { return this.accountType === AccountType.Professional; }
  get isRestricted(): boolean { return this.restricted; }

  restrict(): void {
    this.restricted = true;
  }

  liftRestriction(): void {
    this.restricted = false;
  }

  canActAsPassenger(): boolean {
    return this.isIndividual && !this.restricted;
  }

  pullDomainEvents(): BaseDomainEvent[] {
    const events = [...this.domainEvents];
    this.domainEvents = [];
    return events;
  }
}
