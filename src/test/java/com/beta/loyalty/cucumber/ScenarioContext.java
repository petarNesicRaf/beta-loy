package com.beta.loyalty.cucumber;

public final class ScenarioContext {

    private static final ThreadLocal<Exception> thrownException = new ThreadLocal<>();

    private ScenarioContext() {}

    public static void setException(Exception e) {
        thrownException.set(e);
    }

    public static Exception getException() {
        return thrownException.get();
    }

    public static void clear() {
        thrownException.remove();
    }
}