export class RegisterIndividualAccountCommand {
  constructor(
    public readonly email: string,
    public readonly fullName: string,
    public readonly phoneNumber: string,
  ) {}
}
