import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';

export class WorkingZone {
  private constructor(
    public readonly name: string,
    public readonly center: GeoCoordinates,
    public readonly radiusKm: number,
  ) {}

  static of(name: string, center: GeoCoordinates, radiusKm: number): WorkingZone {
    if (radiusKm <= 0) throw new Error('Radius must be positive');
    return new WorkingZone(name, center, radiusKm);
  }

  contains(point: GeoCoordinates): boolean {
    return this.center.distanceKmTo(point) <= this.radiusKm;
  }
}
