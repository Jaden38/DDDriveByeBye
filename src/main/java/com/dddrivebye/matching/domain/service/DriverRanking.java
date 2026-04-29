package com.dddrivebye.matching.domain.service;

import com.dddrivebye.matching.domain.valueobject.DriverCandidate;

import java.util.Comparator;
import java.util.List;

/**
 * Pure ranking rules for the matching algorithm — encapsulated as a domain
 * service so it can be unit-tested without infrastructure.
 *
 * Ordering, highest priority first:
 *   1. Drivers within their Activity Zone come before drivers outside it
 *      (soft prioritisation — applies only when an Activity Zone is defined).
 *   2. Closer driver wins (proximity is primary among equally-zoned candidates,
 *      per features/matching.feature scenario "Immediate ride matched to both
 *      Individual and Professional drivers").
 *   3. Higher reputation score breaks distance ties.
 */
public final class DriverRanking {

    private DriverRanking() {
    }

    public static List<DriverCandidate> rank(List<DriverCandidate> candidates) {
        return candidates.stream()
                .sorted(comparator())
                .toList();
    }

    public static Comparator<DriverCandidate> comparator() {
        return Comparator
                .comparing(DriverCandidate::withinActivityZone, Comparator.reverseOrder())
                .thenComparingDouble(DriverCandidate::distanceKm)
                .thenComparing(Comparator.comparingDouble(DriverCandidate::reputationScore).reversed());
    }
}
