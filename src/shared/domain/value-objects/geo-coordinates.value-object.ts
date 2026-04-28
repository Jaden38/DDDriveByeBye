export class GeoCoordinates {
  private constructor(
    public readonly latitude: number,
    public readonly longitude: number,
  ) {}

  static of(latitude: number, longitude: number): GeoCoordinates {
    if (latitude < -90 || latitude > 90) throw new Error(`Invalid latitude: ${latitude}`);
    if (longitude < -180 || longitude > 180) throw new Error(`Invalid longitude: ${longitude}`);
    return new GeoCoordinates(latitude, longitude);
  }

  distanceKmTo(other: GeoCoordinates): number {
    const R = 6371;
    const dLat = this.toRad(other.latitude - this.latitude);
    const dLon = this.toRad(other.longitude - this.longitude);
    const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(this.toRad(this.latitude)) *
        Math.cos(this.toRad(other.latitude)) *
        Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  equals(other: GeoCoordinates): boolean {
    return this.latitude === other.latitude && this.longitude === other.longitude;
  }

  private toRad(deg: number): number {
    return (deg * Math.PI) / 180;
  }
}
