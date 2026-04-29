package com.dddrivebye.matching.application.async;

import com.dddrivebye.matching.api.MatchingFacade;
import com.dddrivebye.matching.domain.event.MatchProposalSentEvent;
import com.dddrivebye.matching.domain.exception.MatchNotFoundException;
import com.dddrivebye.matching.domain.valueobject.MatchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProposalExpirySchedulerTest {

    @Mock private MatchingFacade matching;
    @Mock private TaskScheduler scheduler;

    private ProposalExpiryScheduler subject;

    @BeforeEach
    void setUp() {
        subject = new ProposalExpiryScheduler(matching, scheduler);
    }

    @Test
    void shouldScheduleExpiryAtProposalDeadline() {
        UUID rideId = UUID.randomUUID();
        Instant expiresAt = Instant.parse("2026-04-29T10:00:30Z");
        MatchProposalSentEvent event = new MatchProposalSentEvent(
                MatchId.generate(), rideId, UUID.randomUUID(), expiresAt);

        subject.on(event);

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(scheduler).schedule(any(Runnable.class), instantCaptor.capture());
        assertThat(instantCaptor.getValue()).isEqualTo(expiresAt);
    }

    @Test
    void scheduledTaskShouldInvokeFacadeExpire() {
        UUID rideId = UUID.randomUUID();
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        subject.on(new MatchProposalSentEvent(
                MatchId.generate(), rideId, UUID.randomUUID(),
                Instant.parse("2026-04-29T10:00:30Z")));

        verify(scheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        runnableCaptor.getValue().run();

        verify(matching).expireProposal(rideId);
    }

    @Test
    void shouldSwallowExpireFailures() {
        UUID rideId = UUID.randomUUID();
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        // Driver may have already accepted/declined by the time the timer fires.
        doThrow(new MatchNotFoundException("proposal already resolved"))
                .when(matching).expireProposal(rideId);

        subject.on(new MatchProposalSentEvent(
                MatchId.generate(), rideId, UUID.randomUUID(),
                Instant.parse("2026-04-29T10:00:30Z")));
        verify(scheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        // Must not propagate.
        runnableCaptor.getValue().run();
    }
}
