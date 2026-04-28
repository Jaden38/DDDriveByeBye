import { randomUUID } from 'crypto';
import { DriverStatus } from '../value-objects/driver-status.value-object';
import { WorkingZone } from '../value-objects/working-zone.value-object';
import { ActivityZone } from '../value-objects/activity-zone.value-object';
import { VehicleProfile } from './vehicle-profile.entity';
import { BaseDomainEvent } from '@shared/domain/events/base-domain-event';
import { DriverProfileValidatedEvent } from '../events/driver-profile-validated.event';
import { DriverAvailabilityChangedEvent } from '../events/driver-availability-changed.event';

export interface DriverProfileProps {
  id?: string;
  userId: string;
  status: DriverStatus;
  licenseNumber: string;
  vtcLicense?: string;
  vehicleProfile?: VehicleProfile;
  workingZone?: WorkingZone;
  activityZone?: ActivityZone;
  isAvailable: boolean;
}

export class DriverProfile {
  public readonly id: string;
  public readonly userId: string;
  private status: DriverStatus;
  public readonly licenseNumber: string;
  public readonly vtcLicense?: string;
  private vehicleProfile?: VehicleProfile;
  private workingZone?: WorkingZone;
  private activityZone?: ActivityZone;
  private available: boolean;
  private domainEvents: BaseDomainEvent[] = [];

  private constructor(props: DriverProfileProps) {
    this.id = props.id ?? randomUUID();
    this.userId = props.userId;
    this.status = props.status;
    this.licenseNumber = props.licenseNumber;
    this.vtcLicense = props.vtcLicense;
    this.vehicleProfile = props.vehicleProfile;
    this.workingZone = props.workingZone;
    this.activityZone = props.activityZone;
    this.available = props.isAvailable;
  }

  static create(props: Omit<DriverProfileProps, 'id' | 'status' | 'isAvailable'>): DriverProfile {
    return new DriverProfile({ ...props, status: DriverStatus.PendingValidation, isAvailable: false });
  }

  static reconstitute(props: DriverProfileProps): DriverProfile {
    return new DriverProfile(props);
  }

  validate(): void {
    if (this.status !== DriverStatus.PendingValidation) {
      throw new Error('Only pending profiles can be validated');
    }
    this.status = DriverStatus.Active;
    this.domainEvents.push(new DriverProfileValidatedEvent(this.id, this.userId));
  }

  activate(workingZone?: WorkingZone): void {
    if (this.status !== DriverStatus.Active) {
      throw new Error('Driver must be active to go online');
    }
    if (this.workingZone === undefined && workingZone === undefined) {
      throw new Error('Professional drivers must define a Working Zone before activating');
    }
    if (workingZone) this.workingZone = workingZone;
    this.available = true;
    this.domainEvents.push(new DriverAvailabilityChangedEvent(this.id, this.userId, true));
  }

  deactivate(): void {
    this.available = false;
    this.domainEvents.push(new DriverAvailabilityChangedEvent(this.id, this.userId, false));
  }

  setVehicleProfile(profile: VehicleProfile): void {
    this.vehicleProfile = profile;
  }

  isWithinWorkingZone(point: import('@shared/domain/value-objects/geo-coordinates.value-object').GeoCoordinates): boolean {
    return this.workingZone?.contains(point) ?? true;
  }

  isInPreferredActivityZone(point: import('@shared/domain/value-objects/geo-coordinates.value-object').GeoCoordinates): boolean {
    return this.activityZone?.isPreferred(point) ?? false;
  }

  get isAvailable(): boolean { return this.available; }
  get currentStatus(): DriverStatus { return this.status; }
  get vehicle(): VehicleProfile | undefined { return this.vehicleProfile; }

  pullDomainEvents(): BaseDomainEvent[] {
    const events = [...this.domainEvents];
    this.domainEvents = [];
    return events;
  }
}
