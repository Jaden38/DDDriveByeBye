import { Injectable } from '@nestjs/common';
import { QueryBus, CommandBus } from '@nestjs/cqrs';
import { GetDriverProfileQuery } from '../application/queries/get-driver-profile.query';
import { GetAvailableDriversNearQuery } from '../application/queries/get-available-drivers-near.query';
import { SetAvailabilityCommand } from '../application/commands/set-availability.command';
import { DriverProfile } from '../domain/entities/driver-profile.entity';

@Injectable()
export class UserManagementInterface {
  constructor(
    private readonly queryBus: QueryBus,
    private readonly commandBus: CommandBus,
  ) {}

  async getDriverProfile(userId: string): Promise<DriverProfile | null> {
    return this.queryBus.execute(new GetDriverProfileQuery(userId));
  }

  async getAvailableDriversNear(
    latitude: number,
    longitude: number,
    radiusKm: number,
  ): Promise<DriverProfile[]> {
    return this.queryBus.execute(
      new GetAvailableDriversNearQuery(latitude, longitude, radiusKm),
    );
  }

  async setDriverAvailability(userId: string, isAvailable: boolean): Promise<void> {
    return this.commandBus.execute(new SetAvailabilityCommand(userId, isAvailable));
  }
}
