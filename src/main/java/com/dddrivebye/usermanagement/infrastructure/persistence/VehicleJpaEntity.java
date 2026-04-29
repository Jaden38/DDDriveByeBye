package com.dddrivebye.usermanagement.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = "users", name = "vehicles")
public class VehicleJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "year_built", nullable = false)
    private int year;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(name = "seats", nullable = false)
    private int seats;

    @Column(name = "fuel_type", nullable = false)
    private String fuelType;

    @ElementCollection(fetch = FetchType.EAGER)
    @jakarta.persistence.CollectionTable(
            schema = "users",
            name = "vehicle_options",
            joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "option_name")
    private Set<String> options = new HashSet<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UserJpaEntity getUser() { return user; }
    public void setUser(UserJpaEntity user) { this.user = user; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public Set<String> getOptions() { return options; }
    public void setOptions(Set<String> options) { this.options = options; }
}
