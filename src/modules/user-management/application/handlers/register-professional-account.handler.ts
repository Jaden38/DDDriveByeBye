import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { RegisterProfessionalAccountCommand } from '../commands/register-professional-account.command';
import { IUserRepository } from '../../domain/repositories/user.repository.interface';
import { IDriverProfileRepository } from '../../domain/repositories/driver-profile.repository.interface';
import { User } from '../../domain/entities/user.entity';
import { DriverProfile } from '../../domain/entities/driver-profile.entity';

@CommandHandler(RegisterProfessionalAccountCommand)
export class RegisterProfessionalAccountHandler
  implements ICommandHandler<RegisterProfessionalAccountCommand> {

  constructor(
    @Inject(IUserRepository) private readonly users: IUserRepository,
    @Inject(IDriverProfileRepository) private readonly driverProfiles: IDriverProfileRepository,
  ) {}

  async execute(command: RegisterProfessionalAccountCommand): Promise<string> {
    const existing = await this.users.findByEmail(command.email);
    if (existing) throw new Error(`Email already registered: ${command.email}`);

    const user = User.createProfessional({
      email: command.email,
      fullName: command.fullName,
      phoneNumber: command.phoneNumber,
    });

    const driverProfile = DriverProfile.create({
      userId: user.id,
      licenseNumber: command.licenseNumber,
      vtcLicense: command.vtcLicense,
    });

    await this.users.save(user);
    await this.driverProfiles.save(driverProfile);
    return user.id;
  }
}
