package com.dddrivebye.matching.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "matching", name = "matches")
public class MatchJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "ride_id", nullable = false, unique = true)
    private UUID rideId;

    @Column(name = "kind", nullable = false, length = 20)
    private String kind;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "proposed_driver_id")
    private UUID proposedDriverId;

    @Column(name = "proposal_expires_at")
    private Instant proposalExpiresAt;

    @Column(name = "accepted_driver_id")
    private UUID acceptedDriverId;

    @Column(name = "failure_reason")
    private String failureReason;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MatchExclusionJpaEntity> exclusions = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRideId() { return rideId; }
    public void setRideId(UUID rideId) { this.rideId = rideId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public UUID getProposedDriverId() { return proposedDriverId; }
    public void setProposedDriverId(UUID proposedDriverId) { this.proposedDriverId = proposedDriverId; }
    public Instant getProposalExpiresAt() { return proposalExpiresAt; }
    public void setProposalExpiresAt(Instant proposalExpiresAt) { this.proposalExpiresAt = proposalExpiresAt; }
    public UUID getAcceptedDriverId() { return acceptedDriverId; }
    public void setAcceptedDriverId(UUID acceptedDriverId) { this.acceptedDriverId = acceptedDriverId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public List<MatchExclusionJpaEntity> getExclusions() { return exclusions; }
    public void setExclusions(List<MatchExclusionJpaEntity> exclusions) { this.exclusions = exclusions; }
}
