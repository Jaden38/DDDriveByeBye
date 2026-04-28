package com.dddrivebye.usermanagement.domain.repository;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.usermanagement.domain.entity.User;
import com.dddrivebye.usermanagement.domain.valueobject.AccountType;
import com.dddrivebye.usermanagement.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    List<User> findAvailableDriversNear(GeoCoordinates point, double radiusKm, AccountType requiredType);
}
