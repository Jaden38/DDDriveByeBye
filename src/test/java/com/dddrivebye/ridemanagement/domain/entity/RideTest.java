package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.event.RideFinalizedEvent;
import com.dddrivebye.ridemanagement.domain.event.RideRequestedEvent;
import com.dddrivebye.ridemanagement.domain.exception.InvalidRideOperationException;
import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideTest {

    private static final GeoCoordinates PICKUP = GeoCoordinates.of(48.8566, 2.3522);
    private static final GeoCoordinates DESTINATION = GeoCoordinates.of(48.8606, 2.3376);

    private static UserId passenger() {
        return UserId.of(UUID.randomUUID());
    }

    private static UserId driver() {
        return UserId.of(UUID.randomUUID());
    }

    private static Ride newRide() {
        return Ride.create(passenger(), PICKUP, DESTINATION, 2);
    }

    // ---- Construction & accessors ------------------------------------------

    @Test
    void shouldExposeAllConstructorInputsViaAccessors() {
        UserId pax = passenger();

        Ride ride = Ride.create(pax, PICKUP, DESTINATION, 3);

        assertThat(ride.id()).isNotNull();
        assertThat(ride.id().value()).isInstanceOf(UUID.class);
        assertThat(ride.passengerId()).isEqualTo(pax);
        assertThat(ride.driverId()).isNull();
        assertThat(ride.state()).isInstanceOf(RequestedState.class);
        assertThat(ride.pickupPoint()).isEqualTo(PICKUP);
        assertThat(ride.destination()).isEqualTo(DESTINATION);
        assertThat(ride.price()).isNull();
        assertThat(ride.requestedSeats()).isEqualTo(3);
    }

    @Test
    void shouldEmitRideRequestedEventOnCreate() {
        UserId pax = passenger();

        Ride ride = Ride.create(pax, PICKUP, DESTINATION, 2);

        List<BaseDomainEvent> events = ride.pullDomainEvents();
        assertThat(events).hasSize(1);
        RideRequestedEvent event = (RideRequestedEvent) events.get(0);
        assertThat(event.rideId()).isEqualTo(ride.id());
        assertThat(event.passengerId()).isEqualTo(pax);
        assertThat(event.pickupPoint()).isEqualTo(PICKUP);
        assertThat(event.destination()).isEqualTo(DESTINATION);
        assertThat(event.requestedSeats()).isEqualTo(2);
    }

    @Test
    void shouldClearEventsOnPull() {
        Ride ride = newRide();
        ride.pullDomainEvents();

        assertThat(ride.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldRehydrateFromPersistedFields() {
        RideId id = RideId.generate();
        UserId pax = passenger();
        UserId drv = driver();
        Money price = Money.of(new BigDecimal("12.50"), "EUR");

        Ride ride = Ride.reconstitute(id, pax, drv, PICKUP, DESTINATION, price, 4, new InProgressState());

        assertThat(ride.id()).isEqualTo(id);
        assertThat(ride.passengerId()).isEqualTo(pax);
        assertThat(ride.driverId()).isEqualTo(drv);
        assertThat(ride.pickupPoint()).isEqualTo(PICKUP);
        assertThat(ride.destination()).isEqualTo(DESTINATION);
        assertThat(ride.price()).isEqualTo(price);
        assertThat(ride.requestedSeats()).isEqualTo(4);
        assertThat(ride.state()).isInstanceOf(InProgressState.class);
        assertThat(ride.pullDomainEvents()).isEmpty();
    }

    @Test
    void shouldUpdatePrice() {
        Ride ride = newRide();
        Money price = Money.of(new BigDecimal("18.40"), "EUR");

        ride.setPrice(price);

        assertThat(ride.price()).isEqualTo(price);
    }

    // ---- Happy-path lifecycle through all transitions ----------------------

    @Test
    void shouldWalkTheFullHappyPathLifecycle() {
        Ride ride = newRide();
        UserId drv = driver();
        ride.pullDomainEvents();

        ride.propose(drv);
        assertThat(ride.state()).isInstanceOf(ProposedState.class);
        assertThat(ride.driverId()).isEqualTo(drv);

        ride.accept();
        assertThat(ride.state()).isInstanceOf(AcceptedState.class);

        ride.pickUp();
        assertThat(ride.state()).isInstanceOf(PickedUpState.class);

        ride.start();
        assertThat(ride.state()).isInstanceOf(InProgressState.class);

        ride.arrive();
        assertThat(ride.state()).isInstanceOf(ArrivedState.class);

        ride.finalizeRide();
        assertThat(ride.state()).isInstanceOf(FinalizedState.class);

        List<BaseDomainEvent> emitted = ride.pullDomainEvents();
        assertThat(emitted).hasSize(1);
        RideFinalizedEvent finalised = (RideFinalizedEvent) emitted.get(0);
        assertThat(finalised.rideId()).isEqualTo(ride.id());
    }

    // ---- Allowed-but-not-on-happy-path branches ----------------------------

    @Test
    void requestedStateAllowsCancel() {
        Ride ride = newRide();

        ride.cancel();

        assertThat(ride.state()).isInstanceOf(CancelledState.class);
    }

    @Test
    void proposedStateAllowsCancel() {
        Ride ride = newRide();
        ride.propose(driver());

        ride.cancel();

        assertThat(ride.state()).isInstanceOf(CancelledState.class);
    }

    @Test
    void proposedStateAllowsRepropositionToAnotherDriver() {
        Ride ride = newRide();
        UserId firstDriver = driver();
        UserId secondDriver = driver();
        ride.propose(firstDriver);

        ride.propose(secondDriver);

        assertThat(ride.state()).isInstanceOf(ProposedState.class);
        assertThat(ride.driverId()).isEqualTo(secondDriver);
    }

    @Test
    void acceptedStateAllowsCancel() {
        Ride ride = newRide();
        ride.propose(driver());
        ride.accept();

        ride.cancel();

        assertThat(ride.state()).isInstanceOf(CancelledState.class);
    }

    @Test
    void pickedUpStateAllowsIncidentReporting() {
        Ride ride = drivenTo(new PickedUpState());

        ride.reportIncident();

        assertThat(ride.state()).isInstanceOf(IncidentState.class);
    }

    @Test
    void inProgressStateAllowsIncidentReporting() {
        Ride ride = drivenTo(new InProgressState());

        ride.reportIncident();

        assertThat(ride.state()).isInstanceOf(IncidentState.class);
    }

    // ---- Forbidden transitions: drive a Ride into each "no further moves"
    // ---- state and verify every illegal action throws. This sweep covers
    // ---- all 8 default-thrower bodies in RideState across multiple states.

    @Test
    void finalizedStateForbidsEveryTransition() {
        Ride ride = drivenTo(new FinalizedState());

        assertEveryActionForbidden(ride, "FinalizedState");
    }

    @Test
    void cancelledStateForbidsEveryTransition() {
        Ride ride = drivenTo(new CancelledState());

        assertEveryActionForbidden(ride, "CancelledState");
    }

    @Test
    void incidentStateForbidsEveryTransition() {
        Ride ride = drivenTo(new IncidentState());

        assertEveryActionForbidden(ride, "IncidentState");
    }

    @Test
    void requestedStateForbidsAllNonProposeNonCancelActions() {
        Ride ride = drivenTo(new RequestedState());

        assertThatThrownBy(ride::accept).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::pickUp).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::start).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::arrive).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::finalizeRide).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::reportIncident).isInstanceOf(InvalidRideOperationException.class);
    }

    @Test
    void proposedStateForbidsActionsOutsideAcceptCancelPropose() {
        Ride ride = drivenTo(new ProposedState());

        assertThatThrownBy(ride::pickUp).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::start).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::arrive).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::finalizeRide).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::reportIncident).isInstanceOf(InvalidRideOperationException.class);
    }

    @Test
    void acceptedStateForbidsActionsOutsidePickUpCancel() {
        Ride ride = drivenTo(new AcceptedState());

        assertThatThrownBy(() -> ride.propose(driver())).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::accept).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::start).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::arrive).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::finalizeRide).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::reportIncident).isInstanceOf(InvalidRideOperationException.class);
    }

    @Test
    void pickedUpStateForbidsActionsOutsideStartIncident() {
        Ride ride = drivenTo(new PickedUpState());

        assertThatThrownBy(() -> ride.propose(driver())).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::accept).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::pickUp).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::arrive).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::finalizeRide).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::cancel).isInstanceOf(InvalidRideOperationException.class);
    }

    @Test
    void inProgressStateForbidsActionsOutsideArriveIncident() {
        Ride ride = drivenTo(new InProgressState());

        assertThatThrownBy(() -> ride.propose(driver())).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::accept).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::pickUp).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::start).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::finalizeRide).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::cancel).isInstanceOf(InvalidRideOperationException.class);
    }

    @Test
    void arrivedStateForbidsAllActionsExceptFinalize() {
        Ride ride = drivenTo(new ArrivedState());

        assertThatThrownBy(() -> ride.propose(driver())).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::accept).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::pickUp).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::start).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::arrive).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::cancel).isInstanceOf(InvalidRideOperationException.class);
        assertThatThrownBy(ride::reportIncident).isInstanceOf(InvalidRideOperationException.class);
    }

    // ---- Helpers ------------------------------------------------------------

    private static Ride drivenTo(RideState target) {
        Ride ride = Ride.reconstitute(
                RideId.generate(), passenger(), null, PICKUP, DESTINATION, null, 1, target);
        ride.pullDomainEvents();
        return ride;
    }

    private static void assertEveryActionForbidden(Ride ride, String stateName) {
        assertThatThrownBy(() -> ride.propose(driver()))
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::accept)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::pickUp)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::start)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::arrive)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::finalizeRide)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::cancel)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
        assertThatThrownBy(ride::reportIncident)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining(stateName);
    }
}
