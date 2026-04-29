package com.dddrivebye.matching.domain.valueobject;

public enum RideKind {

    IMMEDIATE,
    SCHEDULED;

    public boolean allowsProfessionalDrivers() {
        return this == IMMEDIATE;
    }
}
