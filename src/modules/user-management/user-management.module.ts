import { Module } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { TypeOrmModule } from '@nestjs/typeorm';

import { UserOrmEntity } from './infrastructure/persistence/user.orm-entity';
import { DriverProfileOrmEntity } from './infrastructure/persistence/driver-profile.orm-entity';
import { TypeOrmUserRepository } from './infrastructure/persistence/user.repository';
import { TypeOrmDriverProfileRepository } from './infrastructure/persistence/driver-profile.repository';

import { IUserRepository } from './domain/repositories/user.repository.interface';
import { IDriverProfileRepository } from './domain/repositories/driver-profile.repository.interface';

import { RegisterIndividualAccountHandler } from './application/handlers/register-individual-account.handler';
import { RegisterProfessionalAccountHandler } from './application/handlers/register-professional-account.handler';
import { SetAvailabilityHandler } from './application/handlers/set-availability.handler';
import { GetDriverProfileHandler } from './application/handlers/get-driver-profile.handler';
import { GetAvailableDriversNearHandler } from './application/handlers/get-available-drivers-near.handler';

import { UserManagementInterface } from './interface/user-management.interface';

const handlers = [
  RegisterIndividualAccountHandler,
  RegisterProfessionalAccountHandler,
  SetAvailabilityHandler,
  GetDriverProfileHandler,
  GetAvailableDriversNearHandler,
];

@Module({
  imports: [
    CqrsModule,
    TypeOrmModule.forFeature([UserOrmEntity, DriverProfileOrmEntity]),
  ],
  providers: [
    ...handlers,
    { provide: IUserRepository, useClass: TypeOrmUserRepository },
    { provide: IDriverProfileRepository, useClass: TypeOrmDriverProfileRepository },
    UserManagementInterface,
  ],
  exports: [UserManagementInterface],
})
export class UserManagementModule {}
