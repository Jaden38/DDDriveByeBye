import { Module } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { TypeOrmModule } from '@nestjs/typeorm';

import { TerritoryOrmEntity } from './infrastructure/persistence/territory.orm-entity';
import { TypeOrmTerritoryRepository } from './infrastructure/persistence/territory.repository';
import { ITerritoryRepository } from './domain/repositories/territory.repository.interface';

import { GetRulesForTerritoryHandler } from './application/handlers/get-rules-for-territory.handler';
import { GetTerritoryForCoordinatesHandler } from './application/handlers/get-territory-for-coordinates.handler';
import { IsCoordinatesCoveredHandler } from './application/handlers/is-coordinates-covered.handler';

import { TerritorialConfigurationInterface } from './interface/territorial-configuration.interface';

const handlers = [
  GetRulesForTerritoryHandler,
  GetTerritoryForCoordinatesHandler,
  IsCoordinatesCoveredHandler,
];

@Module({
  imports: [
    CqrsModule,
    TypeOrmModule.forFeature([TerritoryOrmEntity]),
  ],
  providers: [
    ...handlers,
    { provide: ITerritoryRepository, useClass: TypeOrmTerritoryRepository },
    TerritorialConfigurationInterface,
  ],
  exports: [TerritorialConfigurationInterface],
})
export class TerritorialConfigurationModule {}
