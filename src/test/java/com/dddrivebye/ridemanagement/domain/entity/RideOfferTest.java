package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.valueobject.RideOfferId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideOfferTest {

    private static final GeoCoordinates LYON = GeoCoordinates.of(45.76, 4.83);
    private static final GeoCoordinates PARIS = GeoCoordinates.of(48.85, 2.35);
    private static final LocalDateTime DEPARTURE = LocalDateTime.of(2026, 5, 10, 8, 0);

    @Test
    void shouldCreateOfferWithAllSeatsAvailableNoPrice() {
        UserId driver = UserId.of(UUID.randomUUID());

        RideOffer offer = RideOffer.create(driver, LYON, PARIS, DEPARTURE, 4);

        assertThat(offer.id()).isNotNull();
        assertThat(offer.driverId()).isEqualTo(driver);
        assertThat(offer.pickupPoint()).isEqualTo(LYON);
        assertThat(offer.destination()).isEqualTo(PARIS);
        assertThat(offer.departureTime()).isEqualTo(DEPARTURE);
        assertThat(offer.totalSeats()).isEqualTo(4);
        assertThat(offer.availableSeats()).isEqualTo(4);
        assertThat(offer.pricePerSeat()).isNull();
        assertThat(offer.isActive()).isTrue();
    }

    @Test
    void shouldRehydrateFromPersistedFields() {
        RideOfferId id = RideOfferId.generate();
        UserId driver = UserId.of(UUID.randomUUID());
        Money price = Money.of(new BigDecimal("25.00"), "EUR");

        RideOffer offer = RideOffer.reconstitute(id, driver, LYON, PARIS, DEPARTURE, 4, 2, price, false);

        assertThat(offer.id()).isEqualTo(id);
        assertThat(offer.driverId()).isEqualTo(driver);
        assertThat(offer.totalSeats()).isEqualTo(4);
        assertThat(offer.availableSeats()).isEqualTo(2);
        assertThat(offer.pricePerSeat()).isEqualTo(price);
        assertThat(offer.isActive()).isFalse();
    }

    @Test
    void shouldUpdatePricePerSeat() {
        RideOffer offer = newOffer();
        Money price = Money.of(new BigDecimal("12.40"), "EUR");

        offer.setPricePerSeat(price);

        assertThat(offer.pricePerSeat()).isEqualTo(price);
    }

    @Test
    void shouldDecrementAvailableSeatsOnBooking() {
        RideOffer offer = newOffer();

        offer.bookSeats(2);

        assertThat(offer.availableSeats()).isEqualTo(2);
        assertThat(offer.totalSeats()).isEqualTo(4);
    }

    @Test
    void shouldRejectBookingMoreSeatsThanAvailable() {
        RideOffer offer = newOffer();
        offer.bookSeats(3);

        assertThatThrownBy(() -> offer.bookSeats(2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough seats");
    }

    private static RideOffer newOffer() {
        return RideOffer.create(UserId.of(UUID.randomUUID()), LYON, PARIS, DEPARTURE, 4);
    }
}
