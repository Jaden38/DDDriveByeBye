package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.exception.InvalidRideOperationException;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideTest {

    private final UserId passengerId = UserId.generate();
    private final UserId driverId = UserId.generate();
    private final GeoCoordinates pickup = GeoCoordinates.of(48.8566, 2.3522);
    private final GeoCoordinates destination = GeoCoordinates.of(45.7640, 4.8357);

    @Test
    void shouldFollowFullHappyPath() {
        // 1. Creation (Requested)
        Ride ride = Ride.create(passengerId, pickup, destination, 2);
        assertThat(ride.state()).isInstanceOf(RequestedState.class);

        // 2. Propose (Requested -> Proposed)
        ride.propose(driverId);
        assertThat(ride.state()).isInstanceOf(ProposedState.class);
        assertThat(ride.driverId()).isEqualTo(driverId);

        // 3. Accept (Proposed -> Accepted)
        ride.accept();
        assertThat(ride.state()).isInstanceOf(AcceptedState.class);

        // 4. Pick Up (Accepted -> PickedUp)
        ride.pickUp();
        assertThat(ride.state()).isInstanceOf(PickedUpState.class);

        // 5. Start (PickedUp -> InProgress)
        ride.start();
        assertThat(ride.state()).isInstanceOf(InProgressState.class);

        // 6. Arrive (InProgress -> Arrived)
        ride.arrive();
        assertThat(ride.state()).isInstanceOf(ArrivedState.class);

        // 7. Finalize (Arrived -> Finalized)
        ride.finalizeRide();
        assertThat(ride.state()).isInstanceOf(FinalizedState.class);
    }

    @Test
    void shouldNotAllowIllegalTransitions() {
        Ride ride = Ride.create(passengerId, pickup, destination, 1);

        // Cannot start a ride that hasn't been proposed or accepted
        assertThatThrownBy(ride::start)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining("Cannot start ride in state: RequestedState");

        // Cannot accept a ride that hasn't been proposed
        assertThatThrownBy(ride::accept)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining("Cannot accept ride in state: RequestedState");
        
        ride.propose(driverId);
        // Cannot pick up if not accepted
        assertThatThrownBy(ride::pickUp)
                .isInstanceOf(InvalidRideOperationException.class)
                .hasMessageContaining("Cannot pick up passenger in state: ProposedState");
    }

    @Test
    void shouldAllowCancellationBeforeAccepted() {
        Ride ride = Ride.create(passengerId, pickup, destination, 1);
        ride.cancel();
        assertThat(ride.state()).isInstanceOf(CancelledState.class);
        
        // Cannot propose a cancelled ride
        assertThatThrownBy(() -> ride.propose(driverId))
                .isInstanceOf(InvalidRideOperationException.class);
    }
}
