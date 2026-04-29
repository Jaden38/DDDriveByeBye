package com.dddrivebye.matching.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(schema = "matching", name = "grouping_members")
public class GroupingMemberJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grouping_id")
    private GroupingJpaEntity grouping;

    @Column(name = "ride_request_id", nullable = false)
    private UUID rideRequestId;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public GroupingJpaEntity getGrouping() { return grouping; }
    public void setGrouping(GroupingJpaEntity grouping) { this.grouping = grouping; }
    public UUID getRideRequestId() { return rideRequestId; }
    public void setRideRequestId(UUID rideRequestId) { this.rideRequestId = rideRequestId; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
}
