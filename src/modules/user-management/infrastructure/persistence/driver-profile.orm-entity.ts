import { Entity, PrimaryColumn, Column } from 'typeorm';

@Entity({ schema: 'users', name: 'driver_profiles' })
export class DriverProfileOrmEntity {
  @PrimaryColumn('uuid') id: string;
  @Column('uuid') userId: string;
  @Column() status: string;
  @Column() licenseNumber: string;
  @Column({ nullable: true }) vtcLicense?: string;
  @Column({ default: false }) isAvailable: boolean;
  @Column({ type: 'jsonb', nullable: true }) workingZone?: object;
  @Column({ type: 'jsonb', nullable: true }) activityZone?: object;
}
