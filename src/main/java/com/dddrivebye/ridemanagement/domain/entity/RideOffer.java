package com.dddrivebye.ridemanagement.domain.entity;

import com.dddrivebye.ridemanagement.domain.valueobject.RideOfferId;
import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.shared.domain.valueobject.Money;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

import java.time.LocalDateTime;

public class RideOffer {
    private final RideOfferId id;
    private final UserId driverId;
    private final GeoCoordinates pickupPoint;
    private final GeoCoordinates destination;
    private final LocalDateTime departureTime;
    private final int totalSeats;
    private int availableSeats;
    private Money pricePerSeat;
    private boolean active;

    private RideOffer(RideOfferId id, UserId driverId, GeoCoordinates pickupPoint, GeoCoordinates destination, 
                      LocalDateTime departureTime, int totalSeats, int availableSeats, Money pricePerSeat, boolean active) {
        this.id = id;
        this.driverId = driverId;
        this.pickupPoint = pickupPoint;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.pricePerSeat = pricePerSeat;
        this.active = active;
    }

    public static RideOffer create(UserId driverId, GeoCoordinates pickupPoint, GeoCoordinates destination, 
                                   LocalDateTime departureTime, int totalSeats) {
        return new RideOffer(RideOfferId.generate(), driverId, pickupPoint, destination, departureTime, 
                             totalSeats, totalSeats, null, true);
    }

    public static RideOffer reconstitute(RideOfferId id, UserId driverId, GeoCoordinates pickupPoint, 
                                         GeoCoordinates destination, LocalDateTime departureTime, 
                                         int totalSeats, int availableSeats, Money pricePerSeat, boolean active) {
        return new RideOffer(id, driverId, pickupPoint, destination, departureTime, totalSeats, availableSeats, pricePerSeat, active);
    }

    public void setPricePerSeat(Money price) {
        this.pricePerSeat = price;
    }

    public void bookSeats(int count) {
        if (count > availableSeats) throw new IllegalArgumentException("Not enough seats available");
        this.availableSeats -= count;
    }

    public RideOfferId id() { return id; }
    public UserId driverId() { return driverId; }
    public GeoCoordinates pickupPoint() { return pickupPoint; }
    public GeoCoordinates destination() { return destination; }
    public LocalDateTime departureTime() { return departureTime; }
    public int totalSeats() { return totalSeats; }
    public int availableSeats() { return availableSeats; }
    public Money pricePerSeat() { return pricePerSeat; }
    public boolean isActive() { return active; }
}
