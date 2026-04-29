package com.dddrivebye.matching.infrastructure.persistence;

import com.dddrivebye.matching.domain.entity.Match;
import com.dddrivebye.matching.domain.repository.MatchRepository;
import com.dddrivebye.matching.domain.valueobject.MatchId;
import com.dddrivebye.matching.domain.valueobject.MatchStatus;
import com.dddrivebye.matching.domain.valueobject.ProposalWindow;
import com.dddrivebye.matching.domain.valueobject.RideKind;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaMatchRepository implements MatchRepository {

    private final SpringDataMatchRepository delegate;

    public JpaMatchRepository(SpringDataMatchRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Match match) {
        MatchJpaEntity entity = delegate.findById(match.id().value()).orElseGet(MatchJpaEntity::new);
        toJpa(match, entity);
        delegate.save(entity);
    }

    @Override
    public Optional<Match> findById(MatchId id) {
        return delegate.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Match> findByRideId(UUID rideId) {
        return delegate.findByRideId(rideId).map(this::toDomain);
    }

    private void toJpa(Match match, MatchJpaEntity entity) {
        entity.setId(match.id().value());
        entity.setRideId(match.rideId());
        entity.setKind(match.kind().name());
        entity.setStatus(match.status().name());
        entity.setMaxAttempts(match.maxAttempts());
        entity.setAttemptCount(match.attemptCount());
        entity.setProposedDriverId(match.proposal().map(ProposalWindow::driverId).orElse(null));
        entity.setProposalExpiresAt(match.proposal().map(ProposalWindow::expiresAt).orElse(null));
        entity.setAcceptedDriverId(match.acceptedDriverId().orElse(null));
        entity.setFailureReason(match.failureReason().orElse(null));

        entity.getExclusions().clear();
        for (UUID excluded : match.excludedDriverIds()) {
            MatchExclusionJpaEntity ex = new MatchExclusionJpaEntity();
            ex.setId(UUID.randomUUID());
            ex.setMatch(entity);
            ex.setDriverId(excluded);
            entity.getExclusions().add(ex);
        }
    }

    private Match toDomain(MatchJpaEntity entity) {
        Set<UUID> excluded = entity.getExclusions().stream()
                .map(MatchExclusionJpaEntity::getDriverId)
                .collect(Collectors.toCollection(HashSet::new));
        ProposalWindow proposal = null;
        if (entity.getProposedDriverId() != null && entity.getProposalExpiresAt() != null) {
            proposal = ProposalWindow.rehydrate(entity.getProposedDriverId(), entity.getProposalExpiresAt());
        }
        return Match.reconstitute(
                MatchId.of(entity.getId()),
                entity.getRideId(),
                RideKind.valueOf(entity.getKind()),
                entity.getMaxAttempts(),
                excluded,
                MatchStatus.valueOf(entity.getStatus()),
                proposal,
                entity.getAcceptedDriverId(),
                entity.getAttemptCount(),
                entity.getFailureReason());
    }
}
