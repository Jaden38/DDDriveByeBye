import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { GetRulesForTerritoryQuery } from '../queries/get-rules-for-territory.query';
import { ITerritoryRepository } from '../../domain/repositories/territory.repository.interface';
import { TerritorialRule } from '../../domain/entities/territorial-rule.entity';

@QueryHandler(GetRulesForTerritoryQuery)
export class GetRulesForTerritoryHandler
  implements IQueryHandler<GetRulesForTerritoryQuery> {

  constructor(
    @Inject(ITerritoryRepository) private readonly territories: ITerritoryRepository,
  ) {}

  async execute(query: GetRulesForTerritoryQuery): Promise<TerritorialRule | null> {
    const territory = await this.territories.findById(query.territoryId);
    return territory?.rule ?? null;
  }
}
