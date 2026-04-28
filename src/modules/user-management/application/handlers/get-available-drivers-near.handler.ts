import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { GetAvailableDriversNearQuery } from '../queries/get-available-drivers-near.query';
import { IDriverProfileRepository } from '../../domain/repositories/driver-profile.repository.interface';
import { GeoCoordinates } from '@shared/domain/value-objects/geo-coordinates.value-object';
import { DriverProfile } from '../../domain/entities/driver-profile.entity';

@QueryHandler(GetAvailableDriversNearQuery)
export class GetAvailableDriversNearHandler
  implements IQueryHandler<GetAvailableDriversNearQuery> {

  constructor(
    @Inject(IDriverProfileRepository) private readonly driverProfiles: IDriverProfileRepository,
  ) {}

  async execute(query: GetAvailableDriversNearQuery): Promise<DriverProfile[]> {
    const point = GeoCoordinates.of(query.latitude, query.longitude);
    return this.driverProfiles.findAvailableNear(point, query.radiusKm);
  }
}
