package com.dddrivebye.usermanagement.domain.exception;

import com.dddrivebye.usermanagement.domain.valueobject.UserId;

public class UserNotFoundException extends UserManagementException {

    public UserNotFoundException(UserId id) {
        super("User not found: " + id);
    }
}
