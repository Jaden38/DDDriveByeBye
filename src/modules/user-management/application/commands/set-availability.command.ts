export class SetAvailabilityCommand {
  constructor(
    public readonly userId: string,
    public readonly isAvailable: boolean,
  ) {}
}
