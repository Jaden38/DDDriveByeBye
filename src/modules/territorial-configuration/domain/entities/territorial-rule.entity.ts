export interface TerritorialRuleProps {
  perKmRate: number;
  perMinRate: number;
  pickupFee: number;
  maxSurgeCoefficient: number;
  cancellationFee: number;
  carpoolingEnabled: boolean;
  fixedFares: { name: string; amountEur: number }[];
  regulatoryConstraints: string[];
}

export class TerritorialRule {
  public readonly perKmRate: number;
  public readonly perMinRate: number;
  public readonly pickupFee: number;
  public readonly maxSurgeCoefficient: number;
  public readonly cancellationFee: number;
  public readonly carpoolingEnabled: boolean;
  public readonly fixedFares: { name: string; amountEur: number }[];
  public readonly regulatoryConstraints: string[];

  constructor(props: TerritorialRuleProps) {
    this.perKmRate = props.perKmRate;
    this.perMinRate = props.perMinRate;
    this.pickupFee = props.pickupFee;
    this.maxSurgeCoefficient = props.maxSurgeCoefficient;
    this.cancellationFee = props.cancellationFee;
    this.carpoolingEnabled = props.carpoolingEnabled;
    this.fixedFares = props.fixedFares;
    this.regulatoryConstraints = props.regulatoryConstraints;
  }

  getFixedFareFor(destinationName: string): number | null {
    const match = this.fixedFares.find(f =>
      destinationName.toLowerCase().includes(f.name.toLowerCase()),
    );
    return match?.amountEur ?? null;
  }

  hasRegulatoryConstraint(constraint: string): boolean {
    return this.regulatoryConstraints.includes(constraint);
  }
}
