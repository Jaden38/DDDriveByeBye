import { Entity, PrimaryColumn, Column } from 'typeorm';

@Entity({ schema: 'territory', name: 'territories' })
export class TerritoryOrmEntity {
  @PrimaryColumn('uuid') id!: string;
  @Column() name!: string;
  @Column('float') centerLatitude!: number;
  @Column('float') centerLongitude!: number;
  @Column('float') radiusKm!: number;
  @Column({ default: true }) isActive!: boolean;
  @Column({ type: 'jsonb' }) rule!: object;
  @Column({ nullable: true }) parentTerritoryId?: string;
}
