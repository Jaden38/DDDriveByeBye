package com.dddrivebye.territorialconfiguration.domain.valueobject;

import com.dddrivebye.shared.domain.valueobject.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public final class TerritorialRule {

    private final Money perKilometerRate;
    private final Money perMinuteRate;
    private final Money pickupFee;
    private final BigDecimal maxSurgeCoefficient;
    private final Money cancellationFee;
    private final boolean carpoolingGroupingEnabled;
    private final Money fixedFare;

    private TerritorialRule(Money perKilometerRate,
                            Money perMinuteRate,
                            Money pickupFee,
                            BigDecimal maxSurgeCoefficient,
                            Money cancellationFee,
                            boolean carpoolingGroupingEnabled,
                            Money fixedFare) {
        this.perKilometerRate = perKilometerRate;
        this.perMinuteRate = perMinuteRate;
        this.pickupFee = pickupFee;
        this.maxSurgeCoefficient = maxSurgeCoefficient;
        this.cancellationFee = cancellationFee;
        this.carpoolingGroupingEnabled = carpoolingGroupingEnabled;
        this.fixedFare = fixedFare;
    }

    public static TerritorialRule of(Money perKilometerRate,
                                     Money perMinuteRate,
                                     Money pickupFee,
                                     BigDecimal maxSurgeCoefficient,
                                     Money cancellationFee,
                                     boolean carpoolingGroupingEnabled,
                                     Money fixedFare) {
        Objects.requireNonNull(perKilometerRate, "perKilometerRate must not be null");
        Objects.requireNonNull(perMinuteRate, "perMinuteRate must not be null");
        Objects.requireNonNull(pickupFee, "pickupFee must not be null");
        Objects.requireNonNull(maxSurgeCoefficient, "maxSurgeCoefficient must not be null");
        Objects.requireNonNull(cancellationFee, "cancellationFee must not be null");
        if (maxSurgeCoefficient.signum() < 0) {
            throw new IllegalArgumentException("maxSurgeCoefficient must not be negative");
        }
        return new TerritorialRule(perKilometerRate, perMinuteRate, pickupFee,
                maxSurgeCoefficient, cancellationFee, carpoolingGroupingEnabled, fixedFare);
    }

    public Money perKilometerRate() {
        return perKilometerRate;
    }

    public Money perMinuteRate() {
        return perMinuteRate;
    }

    public Money pickupFee() {
        return pickupFee;
    }

    public BigDecimal maxSurgeCoefficient() {
        return maxSurgeCoefficient;
    }

    public Money cancellationFee() {
        return cancellationFee;
    }

    public boolean carpoolingGroupingEnabled() {
        return carpoolingGroupingEnabled;
    }

    public Optional<Money> fixedFare() {
        return Optional.ofNullable(fixedFare);
    }

    public TerritorialRule withPerKilometerRate(Money newRate) {
        return new TerritorialRule(newRate, perMinuteRate, pickupFee, maxSurgeCoefficient,
                cancellationFee, carpoolingGroupingEnabled, fixedFare);
    }

    public TerritorialRule withFixedFare(Money fare) {
        return new TerritorialRule(perKilometerRate, perMinuteRate, pickupFee, maxSurgeCoefficient,
                cancellationFee, carpoolingGroupingEnabled, fare);
    }
}
