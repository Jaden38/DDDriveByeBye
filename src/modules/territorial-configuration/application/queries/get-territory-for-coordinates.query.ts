export class GetTerritoryForCoordinatesQuery {
  constructor(
    public readonly latitude: number,
    public readonly longitude: number,
  ) {}
}
