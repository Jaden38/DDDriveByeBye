import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IUserRepository } from '../../domain/repositories/user.repository.interface';
import { User } from '../../domain/entities/user.entity';
import { UserOrmEntity } from './user.orm-entity';
import { AccountType } from '../../domain/value-objects/account-type.value-object';

@Injectable()
export class TypeOrmUserRepository implements IUserRepository {
  constructor(
    @InjectRepository(UserOrmEntity)
    private readonly repo: Repository<UserOrmEntity>,
  ) {}

  async findById(id: string): Promise<User | null> {
    const orm = await this.repo.findOneBy({ id });
    return orm ? this.toDomain(orm) : null;
  }

  async findByEmail(email: string): Promise<User | null> {
    const orm = await this.repo.findOneBy({ email });
    return orm ? this.toDomain(orm) : null;
  }

  async save(user: User): Promise<void> {
    await this.repo.save(this.toOrm(user));
  }

  private toDomain(orm: UserOrmEntity): User {
    return User.reconstitute({
      id: orm.id,
      email: orm.email,
      fullName: orm.fullName,
      phoneNumber: orm.phoneNumber,
      accountType: orm.accountType as AccountType,
      isRestricted: orm.isRestricted,
    });
  }

  private toOrm(user: User): UserOrmEntity {
    const orm = new UserOrmEntity();
    orm.id = user.id;
    orm.email = user.email;
    orm.fullName = user.fullName;
    orm.phoneNumber = user.phoneNumber;
    orm.accountType = user.accountType;
    orm.isRestricted = user.isRestricted;
    return orm;
  }
}
