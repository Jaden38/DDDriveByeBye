package com.dddrivebye.usermanagement.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    @Query("""
           select u from UserJpaEntity u
           where u.availabilityStatus = 'AVAILABLE'
             and u.driverProfileStatus = 'ACTIVE'
             and (:accountType is null or u.accountType = :accountType)
           """)
    List<UserJpaEntity> findAvailableDrivers(String accountType);
}
