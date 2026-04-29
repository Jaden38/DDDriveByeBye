package com.dddrivebye.matching.infrastructure.adapters;

import com.dddrivebye.matching.application.port.RideOfferCatalogPort;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Placeholder adapter — to be replaced once the ride-management module exposes
 * its Ride Offer catalogue. Returns an empty result; consumers of the search
 * flow then receive the "fall back to ride request" outcome described in
 * features/matching.feature.
 */
@Component
@Profile("!ride-management-real")
public class StubRideOfferCatalogAdapter implements RideOfferCatalogPort {

    @Override
    public List<PublishedRideOffer> findOffersMatching(GeoCoordinates origin,
                                                      GeoCoordinates destination,
                                                      LocalDate date) {
        return List.of();
    }
}
