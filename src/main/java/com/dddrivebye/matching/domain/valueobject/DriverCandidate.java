package com.dddrivebye.matching.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class DriverCandidate {

    private final UUID driverId;
    private final String accountType;
    private final double distanceKm;
    private final double reputationScore;
    private final boolean withinActivityZone;

    private DriverCandidate(UUID driverId,
                            String accountType,
                            double distanceKm,
                            double reputationScore,
                            boolean withinActivityZone) {
        this.driverId = driverId;
        this.accountType = accountType;
        this.distanceKm = distanceKm;
        this.reputationScore = reputationScore;
        this.withinActivityZone = withinActivityZone;
    }

    public static DriverCandidate of(UUID driverId,
                                     String accountType,
                                     double distanceKm,
                                     double reputationScore,
                                     boolean withinActivityZone) {
        Objects.requireNonNull(driverId, "driverId");
        Objects.requireNonNull(accountType, "accountType");
        if (distanceKm < 0) {
            throw new IllegalArgumentException("distanceKm must be >= 0");
        }
        return new DriverCandidate(driverId, accountType, distanceKm, reputationScore, withinActivityZone);
    }

    public UUID driverId() {
        return driverId;
    }

    public String accountType() {
        return accountType;
    }

    public double distanceKm() {
        return distanceKm;
    }

    public double reputationScore() {
        return reputationScore;
    }

    public boolean withinActivityZone() {
        return withinActivityZone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriverCandidate that)) return false;
        return Double.compare(that.distanceKm, distanceKm) == 0
                && Double.compare(that.reputationScore, reputationScore) == 0
                && withinActivityZone == that.withinActivityZone
                && driverId.equals(that.driverId)
                && accountType.equals(that.accountType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId, accountType, distanceKm, reputationScore, withinActivityZone);
    }
}
