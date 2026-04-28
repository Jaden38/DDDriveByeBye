import { randomUUID } from 'crypto';

export class TerritoryId {
  private constructor(public readonly value: string) {}
  static generate(): TerritoryId { return new TerritoryId(randomUUID()); }
  static of(value: string): TerritoryId { return new TerritoryId(value); }
}
