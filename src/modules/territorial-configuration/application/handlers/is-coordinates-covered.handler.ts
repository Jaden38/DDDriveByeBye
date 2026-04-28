import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { IsCoordinatesCoveredQuery } from '../queries/is-coordinates-covered.query';
import { ITerritoryRepository } from '../../domain/repositories/territory.repository.interface';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';

@QueryHandler(IsCoordinatesCoveredQuery)
export class IsCoordinatesCoveredHandler
  implements IQueryHandler<IsCoordinatesCoveredQuery> {

  constructor(
    @Inject(ITerritoryRepository) private readonly territories: ITerritoryRepository,
  ) {}

  async execute(query: IsCoordinatesCoveredQuery): Promise<boolean> {
    const point = GeoCoordinates.of(query.latitude, query.longitude);
    const matching = await this.territories.findCovering(point);
    return matching.length > 0;
  }
}
