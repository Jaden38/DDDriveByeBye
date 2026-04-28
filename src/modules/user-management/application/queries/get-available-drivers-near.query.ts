export class GetAvailableDriversNearQuery {
  constructor(
    public readonly latitude: number,
    public readonly longitude: number,
    public readonly radiusKm: number,
  ) {}
}
