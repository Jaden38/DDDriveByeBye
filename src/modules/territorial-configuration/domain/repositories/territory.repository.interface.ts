import { Territory } from '../entities/territory.entity';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';

export interface ITerritoryRepository {
  findById(id: string): Promise<Territory | null>;
  findCovering(point: GeoCoordinates): Promise<Territory[]>;
  findAll(): Promise<Territory[]>;
  save(territory: Territory): Promise<void>;
}

export const ITerritoryRepository = Symbol('ITerritoryRepository');
