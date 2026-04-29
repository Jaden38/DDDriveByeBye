package com.dddrivebye.matching.api;

import com.dddrivebye.matching.api.dto.MatchDto;
import com.dddrivebye.matching.api.dto.RideOfferSearchResultDto;
import com.dddrivebye.matching.application.command.AcceptProposalCommand;
import com.dddrivebye.matching.application.command.CancelMatchCommand;
import com.dddrivebye.matching.application.command.DeclineProposalCommand;
import com.dddrivebye.matching.application.command.DissolveGroupingCommand;
import com.dddrivebye.matching.application.command.EvaluateGroupingCommand;
import com.dddrivebye.matching.application.command.ExpireProposalCommand;
import com.dddrivebye.matching.application.command.RunImmediateMatchingCommand;
import com.dddrivebye.matching.application.command.RunScheduledMatchingCommand;
import com.dddrivebye.matching.application.handler.GroupingCommandHandler;
import com.dddrivebye.matching.application.handler.MatchingCommandHandler;
import com.dddrivebye.matching.application.handler.MatchingQueryHandler;
import com.dddrivebye.matching.application.query.GetMatchForRideQuery;
import com.dddrivebye.matching.application.query.SearchRideOffersQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the matching bounded context.
 * Other modules MUST depend on this class only — never on internal handlers,
 * domain types, or persistence adapters.
 */
@Component
public class MatchingFacade {

    private final MatchingCommandHandler commandHandler;
    private final MatchingQueryHandler queryHandler;
    private final GroupingCommandHandler groupingHandler;

    public MatchingFacade(MatchingCommandHandler commandHandler,
                          MatchingQueryHandler queryHandler,
                          GroupingCommandHandler groupingHandler) {
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
        this.groupingHandler = groupingHandler;
    }

    public UUID runImmediateMatching(RunImmediateMatchingCommand command) {
        return commandHandler.handle(command);
    }

    public UUID runScheduledMatching(RunScheduledMatchingCommand command) {
        return commandHandler.handle(command);
    }

    public void acceptProposal(UUID rideId, UUID driverId) {
        commandHandler.handle(new AcceptProposalCommand(rideId, driverId));
    }

    public void declineProposal(UUID rideId, UUID driverId) {
        commandHandler.handle(new DeclineProposalCommand(rideId, driverId));
    }

    public void expireProposal(UUID rideId) {
        commandHandler.handle(new ExpireProposalCommand(rideId));
    }

    public void cancelMatch(UUID rideId) {
        commandHandler.handle(new CancelMatchCommand(rideId));
    }

    public Optional<UUID> evaluateGrouping(EvaluateGroupingCommand command) {
        return groupingHandler.handle(command);
    }

    public void dissolveGroupingForRideRequest(UUID rideRequestId) {
        groupingHandler.handle(new DissolveGroupingCommand(rideRequestId));
    }

    public Optional<MatchDto> getMatchForRide(UUID rideId) {
        return queryHandler.handle(new GetMatchForRideQuery(rideId));
    }

    public List<RideOfferSearchResultDto> searchRideOffers(SearchRideOffersQuery query) {
        return queryHandler.handle(query);
    }
}
