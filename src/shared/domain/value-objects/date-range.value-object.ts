export class DateRange {
  private constructor(
    public readonly start: Date,
    public readonly end: Date,
  ) {}

  static of(start: Date, end: Date): DateRange {
    if (end <= start) throw new Error('End date must be after start date');
    return new DateRange(start, end);
  }

  contains(date: Date): boolean {
    return date >= this.start && date <= this.end;
  }

  durationMinutes(): number {
    return (this.end.getTime() - this.start.getTime()) / 60000;
  }
}
