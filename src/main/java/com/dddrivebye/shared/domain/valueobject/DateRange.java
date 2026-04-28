package com.dddrivebye.shared.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

public final class DateRange {

    private final Instant start;
    private final Instant end;

    private DateRange(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    public static DateRange of(Instant start, Instant end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }
        return new DateRange(start, end);
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    public boolean contains(Instant instant) {
        return !instant.isBefore(start) && !instant.isAfter(end);
    }

    public boolean overlaps(DateRange other) {
        return !this.end.isBefore(other.start) && !other.end.isBefore(this.start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateRange that)) return false;
        return start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }
}
