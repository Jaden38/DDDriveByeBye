package com.dddrivebye.usermanagement.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "users", name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "account_status", nullable = false)
    private String accountStatus;

    @Column(name = "driver_profile_status")
    private String driverProfileStatus;

    @Column(name = "drivers_license_number")
    private String driversLicenseNumber;

    @Column(name = "drivers_license_expiry")
    private LocalDate driversLicenseExpiry;

    @Column(name = "vtc_license_number")
    private String vtcLicenseNumber;

    @Column(name = "vtc_license_expiry")
    private LocalDate vtcLicenseExpiry;

    @Column(name = "insurance_company")
    private String insuranceCompany;

    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "vehicle_make")
    private String vehicleMake;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_year")
    private Integer vehicleYear;

    @Column(name = "vehicle_license_plate")
    private String vehicleLicensePlate;

    @Column(name = "availability_status")
    private String availabilityStatus;

    @Column(name = "activity_zone_label")
    private String activityZoneLabel;

    @Column(name = "activity_zone_lat")
    private Double activityZoneLat;

    @Column(name = "activity_zone_lon")
    private Double activityZoneLon;

    @Column(name = "activity_zone_radius_km")
    private Double activityZoneRadiusKm;

    @Column(name = "working_zone_label")
    private String workingZoneLabel;

    @Column(name = "working_zone_lat")
    private Double workingZoneLat;

    @Column(name = "working_zone_lon")
    private Double workingZoneLon;

    @Column(name = "working_zone_radius_km")
    private Double workingZoneRadiusKm;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VehicleJpaEntity> vehicles = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getDriverProfileStatus() { return driverProfileStatus; }
    public void setDriverProfileStatus(String driverProfileStatus) { this.driverProfileStatus = driverProfileStatus; }
    public String getDriversLicenseNumber() { return driversLicenseNumber; }
    public void setDriversLicenseNumber(String driversLicenseNumber) { this.driversLicenseNumber = driversLicenseNumber; }
    public LocalDate getDriversLicenseExpiry() { return driversLicenseExpiry; }
    public void setDriversLicenseExpiry(LocalDate driversLicenseExpiry) { this.driversLicenseExpiry = driversLicenseExpiry; }
    public String getVtcLicenseNumber() { return vtcLicenseNumber; }
    public void setVtcLicenseNumber(String vtcLicenseNumber) { this.vtcLicenseNumber = vtcLicenseNumber; }
    public LocalDate getVtcLicenseExpiry() { return vtcLicenseExpiry; }
    public void setVtcLicenseExpiry(LocalDate vtcLicenseExpiry) { this.vtcLicenseExpiry = vtcLicenseExpiry; }
    public String getInsuranceCompany() { return insuranceCompany; }
    public void setInsuranceCompany(String insuranceCompany) { this.insuranceCompany = insuranceCompany; }
    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }
    public LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    public void setInsuranceExpiry(LocalDate insuranceExpiry) { this.insuranceExpiry = insuranceExpiry; }
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }
    public String getVehicleLicensePlate() { return vehicleLicensePlate; }
    public void setVehicleLicensePlate(String vehicleLicensePlate) { this.vehicleLicensePlate = vehicleLicensePlate; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public String getActivityZoneLabel() { return activityZoneLabel; }
    public void setActivityZoneLabel(String activityZoneLabel) { this.activityZoneLabel = activityZoneLabel; }
    public Double getActivityZoneLat() { return activityZoneLat; }
    public void setActivityZoneLat(Double activityZoneLat) { this.activityZoneLat = activityZoneLat; }
    public Double getActivityZoneLon() { return activityZoneLon; }
    public void setActivityZoneLon(Double activityZoneLon) { this.activityZoneLon = activityZoneLon; }
    public Double getActivityZoneRadiusKm() { return activityZoneRadiusKm; }
    public void setActivityZoneRadiusKm(Double activityZoneRadiusKm) { this.activityZoneRadiusKm = activityZoneRadiusKm; }
    public String getWorkingZoneLabel() { return workingZoneLabel; }
    public void setWorkingZoneLabel(String workingZoneLabel) { this.workingZoneLabel = workingZoneLabel; }
    public Double getWorkingZoneLat() { return workingZoneLat; }
    public void setWorkingZoneLat(Double workingZoneLat) { this.workingZoneLat = workingZoneLat; }
    public Double getWorkingZoneLon() { return workingZoneLon; }
    public void setWorkingZoneLon(Double workingZoneLon) { this.workingZoneLon = workingZoneLon; }
    public Double getWorkingZoneRadiusKm() { return workingZoneRadiusKm; }
    public void setWorkingZoneRadiusKm(Double workingZoneRadiusKm) { this.workingZoneRadiusKm = workingZoneRadiusKm; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public List<VehicleJpaEntity> getVehicles() { return vehicles; }
    public void setVehicles(List<VehicleJpaEntity> vehicles) { this.vehicles = vehicles; }
}
