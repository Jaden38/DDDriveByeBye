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
@Table(schema = "matching", name = "match_exclusions")
public class MatchExclusionJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private MatchJpaEntity match;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public MatchJpaEntity getMatch() { return match; }
    public void setMatch(MatchJpaEntity match) { this.match = match; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
}
