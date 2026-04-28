import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IDriverProfileRepository } from '../../domain/repositories/driver-profile.repository.interface';
import { DriverProfile } from '../../domain/entities/driver-profile.entity';
import { DriverProfileOrmEntity } from './driver-profile.orm-entity';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';
import { DriverStatus } from '../../domain/value-objects/driver-status.value-object';

@Injectable()
export class TypeOrmDriverProfileRepository implements IDriverProfileRepository {
  constructor(
    @InjectRepository(DriverProfileOrmEntity)
    private readonly repo: Repository<DriverProfileOrmEntity>,
  ) {}

  async findById(id: string): Promise<DriverProfile | null> {
    const orm = await this.repo.findOneBy({ id });
    return orm ? this.toDomain(orm) : null;
  }

  async findByUserId(userId: string): Promise<DriverProfile | null> {
    const orm = await this.repo.findOneBy({ userId });
    return orm ? this.toDomain(orm) : null;
  }

  async findAvailableNear(point: GeoCoordinates, radiusKm: number): Promise<DriverProfile[]> {
    const all = await this.repo.findBy({ isAvailable: true });
    return all.map(o => this.toDomain(o));
  }

  async save(profile: DriverProfile): Promise<void> {
    const orm = new DriverProfileOrmEntity();
    orm.id = profile.id;
    orm.userId = profile.userId;
    orm.status = profile.currentStatus;
    orm.licenseNumber = profile.licenseNumber;
    orm.vtcLicense = profile.vtcLicense;
    orm.isAvailable = profile.isAvailable;
    await this.repo.save(orm);
  }

  private toDomain(orm: DriverProfileOrmEntity): DriverProfile {
    return DriverProfile.reconstitute({
      id: orm.id,
      userId: orm.userId,
      status: orm.status as DriverStatus,
      licenseNumber: orm.licenseNumber,
      vtcLicense: orm.vtcLicense,
      isAvailable: orm.isAvailable,
    });
  }
}
