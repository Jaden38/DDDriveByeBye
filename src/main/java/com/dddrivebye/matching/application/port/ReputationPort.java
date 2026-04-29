package com.dddrivebye.matching.application.port;

import java.util.UUID;

/**
 * Port describing what the matching module needs from the reputation
 * bounded context. Until the reputation module is implemented (Step 5),
 * a stub adapter returns a neutral score and no restrictions.
 */
public interface ReputationPort {

    double reputationScore(UUID driverId);

    boolean hasActiveRestriction(UUID driverId);
}
