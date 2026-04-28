import { Entity, PrimaryColumn, Column } from 'typeorm';

@Entity({ schema: 'users', name: 'users' })
export class UserOrmEntity {
  @PrimaryColumn('uuid') id!: string;
  @Column({ unique: true }) email!: string;
  @Column() fullName!: string;
  @Column() phoneNumber!: string;
  @Column() accountType!: string;
  @Column({ default: false }) isRestricted!: boolean;
}
