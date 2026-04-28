import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import configuration from './config/configuration';
import { DatabaseModule } from '@shared/infrastructure/database.module';
import { UserManagementModule } from '@modules/user-management/user-management.module';
import { TerritorialConfigurationModule } from '@modules/territorial-configuration/territorial-configuration.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true, load: [configuration] }),
    DatabaseModule,
    UserManagementModule,
    TerritorialConfigurationModule,
  ],
})
export class AppModule {}
