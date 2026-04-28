import { TerritoryId } from '../value-objects/territory-id.value-object';
import { TerritorialRule } from './territorial-rule.entity';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';

export interface TerritoryProps {
  id?: string;
  name: string;
  centerLatitude: number;
  centerLongitude: number;
  radiusKm: number;
  isActive: boolean;
  rule: TerritorialRule;
  parentTerritoryId?: string;
}

export class Territory {
  public readonly id: TerritoryId;
  public readonly name: string;
  private readonly center: GeoCoordinates;
  private readonly radiusKm: number;
  private active: boolean;
  public readonly rule: TerritorialRule;
  public readonly parentTerritoryId?: string;

  private constructor(props: TerritoryProps) {
    this.id = props.id ? TerritoryId.of(props.id) : TerritoryId.generate();
    this.name = props.name;
    this.center = GeoCoordinates.of(props.centerLatitude, props.centerLongitude);
    this.radiusKm = props.radiusKm;
    this.active = props.isActive;
    this.rule = props.rule;
    this.parentTerritoryId = props.parentTerritoryId;
  }

  static create(props: Omit<TerritoryProps, 'id' | 'isActive'>): Territory {
    return new Territory({ ...props, isActive: true });
  }

  static reconstitute(props: TerritoryProps): Territory {
    return new Territory(props);
  }

  covers(point: GeoCoordinates): boolean {
    return this.active && this.center.distanceKmTo(point) <= this.radiusKm;
  }

  deactivate(): void { this.active = false; }
  get isActive(): boolean { return this.active; }
}
