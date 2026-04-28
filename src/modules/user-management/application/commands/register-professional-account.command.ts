export class RegisterProfessionalAccountCommand {
  constructor(
    public readonly email: string,
    public readonly fullName: string,
    public readonly phoneNumber: string,
    public readonly vtcLicense: string,
    public readonly licenseNumber: string,
  ) {}
}
