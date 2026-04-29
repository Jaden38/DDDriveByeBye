package com.dddrivebye.matching.application.port;

import com.dddrivebye.matching.domain.valueobject.RideOption;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Port describing what the matching module needs to read from the
 * ride-management bounded context for the Ride Offer search flow.
 * Until ride-management is implemented (Step 3), a stub adapter
 * returns an empty catalogue.
 */
public interface RideOfferCatalogPort {

    List<PublishedRideOffer> findOffersMatching(GeoCoordinates origin,
                                                GeoCoordinates destination,
                                                LocalDate date);

    record PublishedRideOffer(
            UUID rideOfferId,
            UUID driverId,
            GeoCoordinates origin,
            GeoCoordinates destination,
            java.time.LocalDateTime departureAt,
            int seatsAvailable,
            String status,
            Set<RideOption> supportedOptions) {
    }
}
