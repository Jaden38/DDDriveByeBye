import { Injectable } from '@nestjs/common';
import { QueryBus } from '@nestjs/cqrs';
import { GetRulesForTerritoryQuery } from '../application/queries/get-rules-for-territory.query';
import { GetTerritoryForCoordinatesQuery } from '../application/queries/get-territory-for-coordinates.query';
import { IsCoordinatesCoveredQuery } from '../application/queries/is-coordinates-covered.query';
import { TerritorialRule } from '../domain/entities/territorial-rule.entity';
import { Territory } from '../domain/entities/territory.entity';

@Injectable()
export class TerritorialConfigurationInterface {
  constructor(private readonly queryBus: QueryBus) {}

  async getRulesForTerritory(territoryId: string): Promise<TerritorialRule | null> {
    return this.queryBus.execute(new GetRulesForTerritoryQuery(territoryId));
  }

  async getTerritoryForCoordinates(latitude: number, longitude: number): Promise<Territory | null> {
    return this.queryBus.execute(new GetTerritoryForCoordinatesQuery(latitude, longitude));
  }

  async isCoordinatesCovered(latitude: number, longitude: number): Promise<boolean> {
    return this.queryBus.execute(new IsCoordinatesCoveredQuery(latitude, longitude));
  }
}
