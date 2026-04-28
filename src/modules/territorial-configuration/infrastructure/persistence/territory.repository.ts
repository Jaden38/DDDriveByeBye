import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ITerritoryRepository } from '../../domain/repositories/territory.repository.interface';
import { Territory } from '../../domain/entities/territory.entity';
import { TerritoryOrmEntity } from './territory.orm-entity';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';
import { TerritorialRule } from '../../domain/entities/territorial-rule.entity';

@Injectable()
export class TypeOrmTerritoryRepository implements ITerritoryRepository {
  constructor(
    @InjectRepository(TerritoryOrmEntity)
    private readonly repo: Repository<TerritoryOrmEntity>,
  ) {}

  async findById(id: string): Promise<Territory | null> {
    const orm = await this.repo.findOneBy({ id });
    return orm ? this.toDomain(orm) : null;
  }

  async findCovering(point: GeoCoordinates): Promise<Territory[]> {
    const all = await this.repo.findBy({ isActive: true });
    return all
      .map(o => this.toDomain(o))
      .filter(t => t.covers(point));
  }

  async findAll(): Promise<Territory[]> {
    const all = await this.repo.find();
    return all.map(o => this.toDomain(o));
  }

  async save(territory: Territory): Promise<void> {
    const orm = new TerritoryOrmEntity();
    orm.id = territory.id.value;
    orm.name = territory.name;
    orm.centerLatitude = (territory as any).center.latitude;
    orm.centerLongitude = (territory as any).center.longitude;
    orm.radiusKm = (territory as any).radiusKm;
    orm.isActive = territory.isActive;
    orm.rule = territory.rule as unknown as object;
    orm.parentTerritoryId = territory.parentTerritoryId;
    await this.repo.save(orm);
  }

  private toDomain(orm: TerritoryOrmEntity): Territory {
    return Territory.reconstitute({
      id: orm.id,
      name: orm.name,
      centerLatitude: orm.centerLatitude,
      centerLongitude: orm.centerLongitude,
      radiusKm: orm.radiusKm,
      isActive: orm.isActive,
      rule: new TerritorialRule(orm.rule as any),
      parentTerritoryId: orm.parentTerritoryId,
    });
  }
}
