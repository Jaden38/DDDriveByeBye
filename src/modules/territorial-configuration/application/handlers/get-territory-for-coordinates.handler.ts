import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { GetTerritoryForCoordinatesQuery } from '../queries/get-territory-for-coordinates.query';
import { ITerritoryRepository } from '../../domain/repositories/territory.repository.interface';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';
import { Territory } from '../../domain/entities/territory.entity';

@QueryHandler(GetTerritoryForCoordinatesQuery)
export class GetTerritoryForCoordinatesHandler
  implements IQueryHandler<GetTerritoryForCoordinatesQuery> {

  constructor(
    @Inject(ITerritoryRepository) private readonly territories: ITerritoryRepository,
  ) {}

  async execute(query: GetTerritoryForCoordinatesQuery): Promise<Territory | null> {
    const point = GeoCoordinates.of(query.latitude, query.longitude);
    const matching = await this.territories.findCovering(point);
    // Return most specific (smallest radius) territory first
    return matching.sort((a, b) => {
      const ar = (a as any).radiusKm as number;
      const br = (b as any).radiusKm as number;
      return ar - br;
    })[0] ?? null;
  }
}
