import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigService } from '@nestjs/config';
import { Client } from 'pg';

const SCHEMAS = [
  'users', 'territory', 'ride', 'matching', 'pricing',
  'reputation', 'payment', 'notification', 'geo',
];

async function createSchemasIfNotExist(databaseUrl: string): Promise<void> {
  console.log('[DatabaseModule] Creating schemas, url:', databaseUrl);
  const client = new Client({ connectionString: databaseUrl });
  try {
    await client.connect();
    for (const schema of SCHEMAS) {
      await client.query(`CREATE SCHEMA IF NOT EXISTS "${schema}"`);
      console.log(`[DatabaseModule] Schema "${schema}" ensured`);
    }
    const result = await client.query(
      `SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY($1)`,
      [SCHEMAS],
    );
    console.log('[DatabaseModule] Verified schemas in DB:', result.rows.map((r: { schema_name: string }) => r.schema_name));
  } catch (err) {
    console.error('[DatabaseModule] Schema creation failed:', err);
    throw err;
  } finally {
    await client.end();
  }
}

@Module({
  imports: [
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: async (config: ConfigService) => {
        const url = config.get<string>('database.url')!;
        await createSchemasIfNotExist(url);
        return {
          type: 'postgres',
          url,
          autoLoadEntities: true,
          synchronize: process.env.NODE_ENV !== 'production',
        };
      },
    }),
  ],
})
export class DatabaseModule {}
