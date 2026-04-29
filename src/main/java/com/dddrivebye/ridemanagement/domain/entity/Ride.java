package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.event.RideRequestedEvent;
import com.dddrivebye.ridemanagement.domain.valueobject.RideId;
import com.dddrivebye.shared.domain.event.BaseDomainEvent;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ride {
    private final RideId id;
    private final UserId passengerId;
    private UserId driverId;
    private RideState state;
    
    private final GeoCoordinates pickupPoint;
    private final GeoCoordinates destination;
    private Money price;
    private final int requestedSeats;
    
    private final List<BaseDomainEvent> domainEvents = new ArrayList<>();

    private Ride(RideId id, UserId passengerId, GeoCoordinates pickupPoint, GeoCoordinates destination, int requestedSeats, RideState state) {
        this.id = id;
        this.passengerId = passengerId;
        this.pickupPoint = pickupPoint;
        this.destination = destination;
        this.requestedSeats = requestedSeats;
        this.state = state;
    }

    public static Ride create(UserId passengerId, GeoCoordinates pickupPoint, GeoCoordinates destination, int requestedSeats) {
        Ride ride = new Ride(RideId.generate(), passengerId, pickupPoint, destination, requestedSeats, new RequestedState());
        ride.addEvent(new RideRequestedEvent(ride.id, passengerId, pickupPoint, destination, requestedSeats));
        return ride;
    }

    public static Ride reconstitute(RideId id, UserId passengerId, UserId driverId, 
                                     GeoCoordinates pickupPoint, GeoCoordinates destination, 
                                     Money price, int requestedSeats, RideState state) {
        Ride ride = new Ride(id, passengerId, pickupPoint, destination, requestedSeats, state);
        ride.driverId = driverId;
        ride.price = price;
        return ride;
    }

    public RideId id() { return id; }
    public UserId passengerId() { return passengerId; }
    public UserId driverId() { return driverId; }
    public RideState state() { return state; }
    public GeoCoordinates pickupPoint() { return pickupPoint; }
    public GeoCoordinates destination() { return destination; }
    public Money price() { return price; }
    public int requestedSeats() { return requestedSeats; }

    public void setPrice(Money price) {
        this.price = price;
    }

    void transitionTo(RideState newState) {
        this.state = newState;
    }

    void setDriver(UserId driverId) {
        this.driverId = driverId;
    }

    public void propose(UserId driverId) {
        state.propose(this, driverId);
    }

    public void accept() {
        state.accept(this);
    }

    public void pickUp() {
        state.pickUp(this);
    }

    public void start() {
        state.start(this);
    }

    public void arrive() {
        state.arrive(this);
    }

    public void finalizeRide() {
        state.finalizeRide(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void reportIncident() {
        state.reportIncident(this);
    }

    protected void addEvent(BaseDomainEvent event) {
        domainEvents.add(event);
    }

    public List<BaseDomainEvent> pullDomainEvents() {
        List<BaseDomainEvent> snapshot = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(snapshot);
    }
}
