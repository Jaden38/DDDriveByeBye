import { DriverProfile } from '../entities/driver-profile.entity';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';

export interface IDriverProfileRepository {
  findById(id: string): Promise<DriverProfile | null>;
  findByUserId(userId: string): Promise<DriverProfile | null>;
  findAvailableNear(point: GeoCoordinates, radiusKm: number): Promise<DriverProfile[]>;
  save(profile: DriverProfile): Promise<void>;
}

export const IDriverProfileRepository = Symbol('IDriverProfileRepository');
