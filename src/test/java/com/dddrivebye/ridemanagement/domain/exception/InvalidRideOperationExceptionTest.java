package com.dddrivebye.ridemanagement.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidRideOperationExceptionTest {

    @Test
    void shouldExposeMessage() {
        InvalidRideOperationException ex = new InvalidRideOperationException("bad transition");

        assertThat(ex).hasMessage("bad transition");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
