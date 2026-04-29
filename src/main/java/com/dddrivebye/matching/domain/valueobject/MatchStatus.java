package com.dddrivebye.matching.domain.valueobject;

public enum MatchStatus {

    SEARCHING,
    PROPOSED,
    ACCEPTED,
    UNMATCHED,
    CANCELLED;

    public boolean isTerminal() {
        return this == ACCEPTED || this == UNMATCHED || this == CANCELLED;
    }
}
