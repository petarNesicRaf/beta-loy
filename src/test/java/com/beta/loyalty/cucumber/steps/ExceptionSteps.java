package com.beta.loyalty.cucumber.steps;

import com.beta.loyalty.cucumber.ScenarioContext;
import com.beta.loyalty.exception.UnauthorizedException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionSteps {

    @Before
    public void clearContext() {
        ScenarioContext.clear();
    }

    @Then("an UnauthorizedException is thrown")
    public void unauthorizedExceptionIsThrown() {
        assertThat(ScenarioContext.getException())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Then("an IllegalArgumentException is thrown with message {string}")
    public void illegalArgumentExceptionWithMessage(String message) {
        assertThat(ScenarioContext.getException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(message);
    }

    @Then("an IllegalStateException is thrown with message {string}")
    public void illegalStateExceptionWithMessage(String message) {
        assertThat(ScenarioContext.getException())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(message);
    }
}