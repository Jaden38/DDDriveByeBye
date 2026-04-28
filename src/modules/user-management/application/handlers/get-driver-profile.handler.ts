import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { GetDriverProfileQuery } from '../queries/get-driver-profile.query';
import { IDriverProfileRepository } from '../../domain/repositories/driver-profile.repository.interface';
import { DriverProfile } from '../../domain/entities/driver-profile.entity';

@QueryHandler(GetDriverProfileQuery)
export class GetDriverProfileHandler
  implements IQueryHandler<GetDriverProfileQuery> {

  constructor(
    @Inject(IDriverProfileRepository) private readonly driverProfiles: IDriverProfileRepository,
  ) {}

  async execute(query: GetDriverProfileQuery): Promise<DriverProfile | null> {
    return this.driverProfiles.findByUserId(query.userId);
  }
}
