import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Inject } from '@nestjs/common';
import { RegisterIndividualAccountCommand } from '../commands/register-individual-account.command';
import { IUserRepository } from '../../domain/repositories/user.repository.interface';
import { User } from '../../domain/entities/user.entity';

@CommandHandler(RegisterIndividualAccountCommand)
export class RegisterIndividualAccountHandler
  implements ICommandHandler<RegisterIndividualAccountCommand> {

  constructor(
    @Inject(IUserRepository) private readonly users: IUserRepository,
  ) {}

  async execute(command: RegisterIndividualAccountCommand): Promise<string> {
    const existing = await this.users.findByEmail(command.email);
    if (existing) throw new Error(`Email already registered: ${command.email}`);

    const user = User.createIndividual({
      email: command.email,
      fullName: command.fullName,
      phoneNumber: command.phoneNumber,
    });

    await this.users.save(user);
    return user.id;
  }
}
