import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';

export class ActivityZone {
  private constructor(
    public readonly name: string,
    public readonly center: GeoCoordinates,
    public readonly radiusKm: number,
  ) {}

  static of(name: string, center: GeoCoordinates, radiusKm: number): ActivityZone {
    if (radiusKm <= 0) throw new Error('Radius must be positive');
    return new ActivityZone(name, center, radiusKm);
  }

  isPreferred(point: GeoCoordinates): boolean {
    return this.center.distanceKmTo(point) <= this.radiusKm;
  }
}
