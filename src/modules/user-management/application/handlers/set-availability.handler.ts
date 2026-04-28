import { CommandHandler, ICommandHandler, EventBus } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { SetAvailabilityCommand } from '../commands/set-availability.command';
import { IDriverProfileRepository } from '../../domain/repositories/driver-profile.repository.interface';

@CommandHandler(SetAvailabilityCommand)
export class SetAvailabilityHandler
  implements ICommandHandler<SetAvailabilityCommand> {

  constructor(
    @Inject(IDriverProfileRepository) private readonly driverProfiles: IDriverProfileRepository,
    private readonly eventBus: EventBus,
  ) {}

  async execute(command: SetAvailabilityCommand): Promise<void> {
    const profile = await this.driverProfiles.findByUserId(command.userId);
    if (!profile) throw new Error(`Driver profile not found for user: ${command.userId}`);

    if (command.isAvailable) {
      profile.activate();
    } else {
      profile.deactivate();
    }

    await this.driverProfiles.save(profile);
    profile.pullDomainEvents().forEach(e => this.eventBus.publish(e));
  }
}
