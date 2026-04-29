package com.dddrivebye.matching.infrastructure.persistence;

import com.dddrivebye.matching.domain.entity.Grouping;
import com.dddrivebye.matching.domain.repository.GroupingRepository;
import com.dddrivebye.matching.domain.valueobject.GroupingId;
import com.dddrivebye.matching.domain.valueobject.GroupingStatus;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaGroupingRepository implements GroupingRepository {

    private final SpringDataGroupingRepository delegate;

    public JpaGroupingRepository(SpringDataGroupingRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Grouping grouping) {
        GroupingJpaEntity entity = delegate.findById(grouping.id().value()).orElseGet(GroupingJpaEntity::new);
        toJpa(grouping, entity);
        delegate.save(entity);
    }

    @Override
    public Optional<Grouping> findById(GroupingId id) {
        return delegate.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Grouping> findActiveByRideRequestId(UUID rideRequestId) {
        return delegate.findActiveByRideRequestId(rideRequestId).map(this::toDomain);
    }

    private void toJpa(Grouping grouping, GroupingJpaEntity entity) {
        entity.setId(grouping.id().value());
        entity.setDriverId(grouping.driverId());
        entity.setStatus(grouping.status().name());

        entity.getMembers().clear();
        int ordinal = 0;
        for (UUID rideRequestId : grouping.rideRequestIds()) {
            GroupingMemberJpaEntity member = new GroupingMemberJpaEntity();
            member.setId(UUID.randomUUID());
            member.setGrouping(entity);
            member.setRideRequestId(rideRequestId);
            member.setOrdinal(ordinal++);
            entity.getMembers().add(member);
        }
    }

    private Grouping toDomain(GroupingJpaEntity entity) {
        Set<UUID> rideRequestIds = entity.getMembers().stream()
                .sorted(Comparator.comparingInt(GroupingMemberJpaEntity::getOrdinal))
                .map(GroupingMemberJpaEntity::getRideRequestId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Grouping.reconstitute(
                GroupingId.of(entity.getId()),
                entity.getDriverId(),
                rideRequestIds,
                GroupingStatus.valueOf(entity.getStatus()));
    }
}
