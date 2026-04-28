import { randomUUID } from 'crypto';

export interface VehicleProfileProps {
  id?: string;
  make: string;
  model: string;
  year: number;
  licensePlate: string;
  seats: number;
  fuelType: string;
  options: string[];
}

export class VehicleProfile {
  public readonly id: string;
  public readonly make: string;
  public readonly model: string;
  public readonly year: number;
  public readonly licensePlate: string;
  public readonly seats: number;
  public readonly fuelType: string;
  public readonly options: string[];

  private constructor(props: VehicleProfileProps) {
    this.id = props.id ?? randomUUID();
    this.make = props.make;
    this.model = props.model;
    this.year = props.year;
    this.licensePlate = props.licensePlate;
    this.seats = props.seats;
    this.fuelType = props.fuelType;
    this.options = props.options;
  }

  static create(props: Omit<VehicleProfileProps, 'id'>): VehicleProfile {
    return new VehicleProfile(props);
  }

  static reconstitute(props: VehicleProfileProps): VehicleProfile {
    return new VehicleProfile(props);
  }

  supportsOption(option: string): boolean {
    return this.options.includes(option);
  }
}
